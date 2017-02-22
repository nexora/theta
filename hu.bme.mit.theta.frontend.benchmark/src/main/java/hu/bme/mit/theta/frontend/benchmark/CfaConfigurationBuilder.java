package hu.bme.mit.theta.frontend.benchmark;

import hu.bme.mit.theta.analysis.Action;
import hu.bme.mit.theta.analysis.Analysis;
import hu.bme.mit.theta.analysis.Precision;
import hu.bme.mit.theta.analysis.State;
import hu.bme.mit.theta.analysis.algorithm.ArgBuilder;
import hu.bme.mit.theta.analysis.algorithm.SafetyChecker;
import hu.bme.mit.theta.analysis.algorithm.cegar.Abstractor;
import hu.bme.mit.theta.analysis.algorithm.cegar.CegarChecker;
import hu.bme.mit.theta.analysis.algorithm.cegar.ExplItpRefiner;
import hu.bme.mit.theta.analysis.algorithm.cegar.ExplVarsRefiner;
import hu.bme.mit.theta.analysis.algorithm.cegar.Refiner;
import hu.bme.mit.theta.analysis.algorithm.cegar.SimplePredItpRefiner;
import hu.bme.mit.theta.analysis.algorithm.cegar.SingleExprTraceRefiner;
import hu.bme.mit.theta.analysis.algorithm.cegar.WaitlistBasedAbstractor;
import hu.bme.mit.theta.analysis.cfa.CfaAction;
import hu.bme.mit.theta.analysis.cfa.CfaLts;
import hu.bme.mit.theta.analysis.expl.ExplAnalysis;
import hu.bme.mit.theta.analysis.expl.ExplPrecision;
import hu.bme.mit.theta.analysis.expl.ExplState;
import hu.bme.mit.theta.analysis.expr.ExprTraceCraigItpChecker;
import hu.bme.mit.theta.analysis.expr.ExprTraceSeqItpChecker;
import hu.bme.mit.theta.analysis.expr.ExprTraceUnsatCoreChecker;
import hu.bme.mit.theta.analysis.loc.ConstLocPrecRefiner;
import hu.bme.mit.theta.analysis.loc.ConstLocPrecision;
import hu.bme.mit.theta.analysis.loc.GenLocExplPrecItpRefiner;
import hu.bme.mit.theta.analysis.loc.GenLocExplPrecVarsRefiner;
import hu.bme.mit.theta.analysis.loc.GenLocSimplePredPrecItpRefiner;
import hu.bme.mit.theta.analysis.loc.GenericLocPrecision;
import hu.bme.mit.theta.analysis.loc.LocAction;
import hu.bme.mit.theta.analysis.loc.LocAnalysis;
import hu.bme.mit.theta.analysis.loc.LocPrecision;
import hu.bme.mit.theta.analysis.loc.LocState;
import hu.bme.mit.theta.analysis.pred.PredAnalysis;
import hu.bme.mit.theta.analysis.pred.PredState;
import hu.bme.mit.theta.analysis.pred.SimplePredPrecision;
import hu.bme.mit.theta.analysis.waitlist.PriorityWaitlist;
import hu.bme.mit.theta.common.logging.Logger;
import hu.bme.mit.theta.core.expr.impl.Exprs;
import hu.bme.mit.theta.formalism.cfa.CFA;
import hu.bme.mit.theta.formalism.cfa.CfaEdge;
import hu.bme.mit.theta.formalism.cfa.CfaLoc;
import hu.bme.mit.theta.solver.ItpSolver;
import hu.bme.mit.theta.solver.SolverFactory;

public class CfaConfigurationBuilder extends ConfigurationBuilder {

	public enum LocPrec {
		CONST, GEN
	};

	private LocPrec locPrec = LocPrec.CONST;

	public CfaConfigurationBuilder(final Domain domain, final Refinement refinement) {
		super(domain, refinement);
	}

	public CfaConfigurationBuilder logger(final Logger logger) {
		setLogger(logger);
		return this;
	}

	public CfaConfigurationBuilder solverFactory(final SolverFactory solverFactory) {
		setSolverFactory(solverFactory);
		return this;
	}

	public CfaConfigurationBuilder search(final Search search) {
		setSearch(search);
		return this;
	}

	public CfaConfigurationBuilder locPrec(final LocPrec locPrec) {
		this.locPrec = locPrec;
		return this;
	}

	public Configuration<? extends State, ? extends Action, ? extends Precision> build(final CFA cfa) {
		final ItpSolver solver = getSolverFactory().createItpSolver();
		final CfaLts lts = CfaLts.getInstance();

		if (getDomain() == Domain.EXPL) {
			final Analysis<LocState<ExplState, CfaLoc, CfaEdge>, LocAction<CfaLoc, CfaEdge>, LocPrecision<ExplPrecision, CfaLoc, CfaEdge>> analysis = LocAnalysis
					.create(cfa.getInitLoc(), ExplAnalysis.create(solver, Exprs.True()));
			final ArgBuilder<LocState<ExplState, CfaLoc, CfaEdge>, CfaAction, LocPrecision<ExplPrecision, CfaLoc, CfaEdge>> argBuilder = ArgBuilder
					.create(lts, analysis, s -> s.getLoc().equals(cfa.getErrorLoc()));
			final Abstractor<LocState<ExplState, CfaLoc, CfaEdge>, CfaAction, LocPrecision<ExplPrecision, CfaLoc, CfaEdge>> abstractor = WaitlistBasedAbstractor
					.create(argBuilder, LocState::getLoc, PriorityWaitlist.supplier(getSearch().comparator),
							getLogger());

			Refiner<LocState<ExplState, CfaLoc, CfaEdge>, CfaAction, LocPrecision<ExplPrecision, CfaLoc, CfaEdge>> refiner = null;

			switch (getRefinement()) {
			case CRAIG_ITP:
				if (locPrec == LocPrec.CONST) {
					refiner = SingleExprTraceRefiner.create(
							ExprTraceCraigItpChecker.create(Exprs.True(), Exprs.True(), solver),
							ConstLocPrecRefiner.create(new ExplItpRefiner<>()), getLogger());
				} else {
					refiner = SingleExprTraceRefiner.create(
							ExprTraceCraigItpChecker.create(Exprs.True(), Exprs.True(), solver),
							new GenLocExplPrecItpRefiner<>(), getLogger());
				}
				break;
			case SEQ_ITP:
				if (locPrec == LocPrec.CONST) {
					refiner = SingleExprTraceRefiner.create(
							ExprTraceSeqItpChecker.create(Exprs.True(), Exprs.True(), solver),
							ConstLocPrecRefiner.create(new ExplItpRefiner<>()), getLogger());
				} else {
					refiner = SingleExprTraceRefiner.create(
							ExprTraceSeqItpChecker.create(Exprs.True(), Exprs.True(), solver),
							new GenLocExplPrecItpRefiner<>(), getLogger());
				}
				break;
			case UNSAT_CORE:
				if (locPrec == LocPrec.CONST) {
					refiner = SingleExprTraceRefiner.create(
							ExprTraceUnsatCoreChecker.create(Exprs.True(), Exprs.True(), solver),
							ConstLocPrecRefiner.create(new ExplVarsRefiner<>()), getLogger());
				} else {
					refiner = SingleExprTraceRefiner.create(
							ExprTraceUnsatCoreChecker.create(Exprs.True(), Exprs.True(), solver),
							new GenLocExplPrecVarsRefiner<>(), getLogger());
				}
				break;
			default:
				throw new UnsupportedOperationException();
			}

			final SafetyChecker<LocState<ExplState, CfaLoc, CfaEdge>, CfaAction, LocPrecision<ExplPrecision, CfaLoc, CfaEdge>> checker = CegarChecker
					.create(abstractor, refiner, getLogger());

			LocPrecision<ExplPrecision, CfaLoc, CfaEdge> prec = null;
			switch (locPrec) {
			case CONST:
				prec = ConstLocPrecision.create(ExplPrecision.create());
				break;
			case GEN:
				prec = GenericLocPrecision.create(ExplPrecision.create());
				break;
			default:
				throw new UnsupportedOperationException();
			}

			return Configuration.create(checker, prec);

		} else if (getDomain() == Domain.PRED) {
			final Analysis<LocState<PredState, CfaLoc, CfaEdge>, CfaAction, LocPrecision<SimplePredPrecision, CfaLoc, CfaEdge>> analysis = LocAnalysis
					.create(cfa.getInitLoc(), PredAnalysis.create(solver, Exprs.True()));
			final ArgBuilder<LocState<PredState, CfaLoc, CfaEdge>, CfaAction, LocPrecision<SimplePredPrecision, CfaLoc, CfaEdge>> argBuilder = ArgBuilder
					.create(lts, analysis, s -> s.getLoc().equals(cfa.getErrorLoc()));
			final Abstractor<LocState<PredState, CfaLoc, CfaEdge>, CfaAction, LocPrecision<SimplePredPrecision, CfaLoc, CfaEdge>> abstractor = WaitlistBasedAbstractor
					.create(argBuilder, LocState::getLoc, PriorityWaitlist.supplier(getSearch().comparator),
							getLogger());

			Refiner<LocState<PredState, CfaLoc, CfaEdge>, CfaAction, LocPrecision<SimplePredPrecision, CfaLoc, CfaEdge>> refiner = null;

			switch (getRefinement()) {
			case CRAIG_ITP:
				if (locPrec == LocPrec.CONST) {
					refiner = SingleExprTraceRefiner.create(
							ExprTraceCraigItpChecker.create(Exprs.True(), Exprs.True(), solver),
							ConstLocPrecRefiner.create(new SimplePredItpRefiner<>()), getLogger());
					break;
				} else {
					refiner = SingleExprTraceRefiner.create(
							ExprTraceCraigItpChecker.create(Exprs.True(), Exprs.True(), solver),
							new GenLocSimplePredPrecItpRefiner<>(), getLogger());
				}
			case SEQ_ITP:
				if (locPrec == LocPrec.CONST) {
					refiner = SingleExprTraceRefiner.create(
							ExprTraceSeqItpChecker.create(Exprs.True(), Exprs.True(), solver),
							ConstLocPrecRefiner.create(new SimplePredItpRefiner<>()), getLogger());
				} else {
					refiner = SingleExprTraceRefiner.create(
							ExprTraceSeqItpChecker.create(Exprs.True(), Exprs.True(), solver),
							new GenLocSimplePredPrecItpRefiner<>(), getLogger());
				}
				break;
			default:
				throw new UnsupportedOperationException();
			}

			final SafetyChecker<LocState<PredState, CfaLoc, CfaEdge>, CfaAction, LocPrecision<SimplePredPrecision, CfaLoc, CfaEdge>> checker = CegarChecker
					.create(abstractor, refiner, getLogger());

			LocPrecision<SimplePredPrecision, CfaLoc, CfaEdge> prec = null;

			switch (locPrec) {
			case CONST:
				prec = ConstLocPrecision.create(SimplePredPrecision.create(solver));
				break;
			case GEN:
				prec = GenericLocPrecision.create(SimplePredPrecision.create(solver));
				break;
			default:
				throw new UnsupportedOperationException();
			}

			return Configuration.create(checker, prec);

		} else {
			throw new UnsupportedOperationException();
		}
	}
}