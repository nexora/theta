package hu.bme.mit.theta.formalism.sts;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.core.expr.AndExpr;
import hu.bme.mit.theta.core.expr.Expr;
import hu.bme.mit.theta.core.expr.LitExpr;
import hu.bme.mit.theta.core.expr.impl.Exprs;
import hu.bme.mit.theta.core.model.Model;
import hu.bme.mit.theta.core.model.impl.Valuation;
import hu.bme.mit.theta.core.type.BoolType;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.utils.impl.ExprUtils;

/**
 * An immutable Symbolic Transition System (STS) implementation. An STS consists
 * of variables, an initial formula, a transition relation and a property. Use
 * the inner builder class for creating an instance.
 */
public final class STS {

	private final Collection<VarDecl<? extends Type>> vars;
	private final Collection<Expr<? extends BoolType>> init;
	private final Collection<Expr<? extends BoolType>> trans;
	private final Expr<? extends BoolType> prop;

	// Private constructor --> use the builder
	private STS(final Collection<VarDecl<? extends Type>> vars, final Collection<Expr<? extends BoolType>> init,
			final Collection<Expr<? extends BoolType>> trans, final Expr<? extends BoolType> prop) {
		this.vars = Collections.unmodifiableCollection(checkNotNull(vars));
		this.init = Collections.unmodifiableCollection(checkNotNull(init));
		this.trans = Collections.unmodifiableCollection(checkNotNull(trans));
		this.prop = checkNotNull(prop);
	}

	/**
	 * Gets the list of variables appearing in the formulas of the STS.
	 *
	 * @return
	 */
	public Collection<VarDecl<? extends Type>> getVars() {
		return vars;
	}

	/**
	 * Gets the initial formula.
	 *
	 * @return
	 */
	public Collection<Expr<? extends BoolType>> getInit() {
		return init;
	}

	/**
	 * Gets the transition relation.
	 *
	 * @return
	 */
	public Collection<Expr<? extends BoolType>> getTrans() {
		return trans;
	}

	/**
	 * Gets the property.
	 *
	 * @return
	 */
	public Expr<? extends BoolType> getProp() {
		return prop;
	}

	/**
	 * Creates a new builder instance.
	 *
	 * @return
	 */
	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("STS [" + System.lineSeparator());
		appendCollection(sb, "\tVars:  ", vars, System.lineSeparator());
		appendCollection(sb, "\tInit:  ", init, System.lineSeparator());
		appendCollection(sb, "\tTrans: ", trans, System.lineSeparator());
		sb.append("\tProp: ").append(prop).append(System.lineSeparator()).append("]");
		return sb.toString();
	}

	private void appendCollection(final StringBuilder sb, final String prefix, final Collection<?> collection,
			final String postfix) {
		sb.append(prefix);
		sb.append(String.join(", ", collection.stream().map(i -> i.toString()).collect(Collectors.toList())));
		sb.append(postfix);
	}

	/**
	 * Helper class for building the immutable STS. It splits AND expressions
	 * into conjuncts, eliminates duplicate expressions and collects the
	 * variables from the expressions automatically.
	 */
	public static class Builder {
		private final Collection<VarDecl<? extends Type>> vars;
		private final Collection<Expr<? extends BoolType>> init;
		private final Collection<Expr<? extends BoolType>> trans;
		private Expr<? extends BoolType> prop;
		private boolean built;

		private Builder() {
			vars = new HashSet<>();
			init = new HashSet<>();
			trans = new HashSet<>();
			prop = null;
			built = false;
		}

		/**
		 * Add an initial constraint. If the constraint is an AND expression it
		 * will be split into its conjuncts. Duplicate constraints are included
		 * only once.
		 */
		public Builder addInit(final Expr<? extends BoolType> expr) {
			checkNotNull(expr);
			checkState(!built);
			if (expr instanceof AndExpr)
				addInit(((AndExpr) expr).getOps());
			else
				init.add(checkNotNull(expr));
			return this;
		}

		/**
		 * Add initial constraints. If any constraint is an AND expression it
		 * will be split into its conjuncts. Duplicate constraints are included
		 * only once.
		 */
		public Builder addInit(final Iterable<? extends Expr<? extends BoolType>> exprs) {
			checkNotNull(exprs);
			checkState(!built);
			for (final Expr<? extends BoolType> expr : exprs)
				addInit(expr);
			return this;
		}

		/**
		 * Add an invariant constraint. If the constraint is an AND expression
		 * it will be split into its conjuncts. Duplicate constraints are
		 * included only once.
		 */
		public Builder addInvar(final Expr<? extends BoolType> expr) {
			checkNotNull(expr);
			checkState(!built);
			if (expr instanceof AndExpr) {
				addInvar(((AndExpr) expr).getOps());
			} else {
				addInit(expr);
				addTrans(expr);
				addTrans(Exprs.Prime(expr));
			}

			return this;
		}

		/**
		 * Add invariant constraints. If any constraint is an AND expression it
		 * will be split into its conjuncts. Duplicate constraints are included
		 * only once.
		 */
		public Builder addInvar(final Iterable<? extends Expr<? extends BoolType>> exprs) {
			checkNotNull(exprs);
			checkState(!built);
			for (final Expr<? extends BoolType> expr : exprs)
				addInvar(expr);
			return this;
		}

		/**
		 * Add a transition constraint. If the constraint is an AND expression
		 * it will be split into its conjuncts. Duplicate constraints are
		 * included only once.
		 */
		public Builder addTrans(final Expr<? extends BoolType> expr) {
			checkNotNull(expr);
			checkState(!built);
			if (expr instanceof AndExpr)
				addTrans(((AndExpr) expr).getOps());
			else
				trans.add(expr);
			return this;
		}

		/**
		 * Add transition constraints. If any constraint is an AND expression it
		 * will be split into its conjuncts. Duplicate constraints are included
		 * only once.
		 */
		public Builder addTrans(final Iterable<? extends Expr<? extends BoolType>> exprs) {
			checkNotNull(exprs);
			checkState(!built);
			for (final Expr<? extends BoolType> expr : exprs)
				addTrans(expr);
			return this;
		}

		/**
		 * Set the property.
		 */
		public Builder setProp(final Expr<? extends BoolType> expr) {
			checkNotNull(expr);
			checkState(!built);
			this.prop = expr;
			return this;
		}

		/**
		 * Build an STS. After building, this instance cannot be modified
		 * anymore, but building more STSs is possible.
		 */
		public STS build() {
			checkNotNull(prop);
			built = true;

			ExprUtils.collectVars(init, vars);
			ExprUtils.collectVars(trans, vars);
			ExprUtils.collectVars(prop, vars);

			return new STS(vars, init, trans, prop);
		}
	}

	// Deprecated functions for splitting CEGAR
	@Deprecated
	public Valuation getConcreteState(final Model model, final int i) {
		return getConcreteState(model, i, getVars());
	}

	@Deprecated
	public Valuation getConcreteState(final Model model, final int i,
			final Collection<VarDecl<? extends Type>> variables) {
		final Valuation.Builder builder = Valuation.builder();

		for (final VarDecl<? extends Type> varDecl : variables) {
			LitExpr<? extends Type> value = null;
			try {
				value = model.eval(varDecl.getConstDecl(i)).get();
			} catch (final Exception ex) {
				value = varDecl.getType().getAny();
			}
			builder.put(varDecl, value);
		}

		return builder.build();
	}

	@Deprecated
	public List<Valuation> extractTrace(final Model model, final int length) {
		return extractTrace(model, length, getVars());
	}

	@Deprecated
	public List<Valuation> extractTrace(final Model model, final int length,
			final Collection<VarDecl<? extends Type>> variables) {
		final List<Valuation> trace = new ArrayList<>(length);
		for (int i = 0; i < length; ++i)
			trace.add(getConcreteState(model, i, variables));
		return trace;
	}

}
