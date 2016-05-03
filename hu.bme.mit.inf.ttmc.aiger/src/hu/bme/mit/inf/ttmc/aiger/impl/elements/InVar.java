package hu.bme.mit.inf.ttmc.aiger.impl.elements;

import java.util.List;

import hu.bme.mit.inf.ttmc.core.expr.Expr;
import hu.bme.mit.inf.ttmc.core.type.BoolType;
import hu.bme.mit.inf.ttmc.core.type.impl.Types;
import hu.bme.mit.inf.ttmc.formalism.common.decl.VarDecl;
import hu.bme.mit.inf.ttmc.formalism.common.decl.impl.Decls2;

public final class InVar extends HWElement {
	private final VarDecl<BoolType> varDecl;

	public InVar(final int nr, final String token) {
		this(nr, Integer.parseInt(token));
	}

	public InVar(final int nr, final int literal) {
		super(literal / 2);
		varDecl = Decls2.Var("I" + nr + "_l" + varId, Types.Bool());
	}

	@Override
	public Expr<? extends BoolType> getExpr(final List<HWElement> elements) {
		return varDecl.getRef();
	}

}