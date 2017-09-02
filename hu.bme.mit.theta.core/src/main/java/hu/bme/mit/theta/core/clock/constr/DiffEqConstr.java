package hu.bme.mit.theta.core.clock.constr;

import static hu.bme.mit.theta.core.type.rattype.RatExprs.Eq;
import static hu.bme.mit.theta.core.type.rattype.RatExprs.Rat;
import static hu.bme.mit.theta.core.type.rattype.RatExprs.Sub;

import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.core.type.anytype.RefExpr;
import hu.bme.mit.theta.core.type.rattype.RatEqExpr;
import hu.bme.mit.theta.core.type.rattype.RatType;

public final class DiffEqConstr extends DiffConstr {

	private static final int HASH_SEED = 5261;

	private static final String OPERATOR_LABEL = "=";

	private volatile RatEqExpr expr = null;

	DiffEqConstr(final VarDecl<RatType> leftVar, final VarDecl<RatType> rightVar, final int bound) {
		super(leftVar, rightVar, bound);
	}

	@Override
	public RatEqExpr toExpr() {
		RatEqExpr result = expr;
		if (result == null) {
			final RefExpr<RatType> leftRef = getLeftVar().getRef();
			final RefExpr<RatType> rightRef = getRightVar().getRef();
			result = Eq(Sub(leftRef, rightRef), Rat(getBound(), 1));
			expr = result;
		}
		return result;
	}

	@Override
	public <P, R> R accept(final ClockConstrVisitor<? super P, ? extends R> visitor, final P param) {
		return visitor.visit(this, param);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof DiffEqConstr) {
			final DiffEqConstr that = (DiffEqConstr) obj;
			return this.getBound() == that.getBound() && this.getLeftVar().equals(that.getLeftVar())
					&& this.getRightVar().equals(that.getRightVar());
		} else {
			return false;
		}
	}

	@Override
	protected int getHashSeed() {
		return HASH_SEED;
	}

	@Override
	protected String getOperatorLabel() {
		return OPERATOR_LABEL;
	}

}