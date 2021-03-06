/*
 *  Copyright 2017 Budapest University of Technology and Economics
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package hu.bme.mit.theta.analysis.pred;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import hu.bme.mit.theta.analysis.expr.refinement.ItpRefutation;
import hu.bme.mit.theta.analysis.expr.refinement.RefutationToPrec;
import hu.bme.mit.theta.analysis.pred.ExprSplitters.ExprSplitter;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.booltype.BoolType;
import hu.bme.mit.theta.core.utils.ExprUtils;
import hu.bme.mit.theta.solver.Solver;

public class ItpRefToSimplePredPrec implements RefutationToPrec<SimplePredPrec, ItpRefutation> {

	private final Solver solver;
	private final ExprSplitter exprSplitter;

	public ItpRefToSimplePredPrec(final Solver solver, final ExprSplitter exprSplitter) {
		this.solver = checkNotNull(solver);
		this.exprSplitter = checkNotNull(exprSplitter);
	}

	@Override
	public SimplePredPrec toPrec(final ItpRefutation refutation, final int index) {
		final Expr<BoolType> expr = refutation.get(index);
		final Collection<Expr<BoolType>> exprs = exprSplitter.apply(expr);
		final SimplePredPrec prec = SimplePredPrec.create(exprs, solver);
		return prec;
	}

	@Override
	public SimplePredPrec join(final SimplePredPrec prec1, final SimplePredPrec prec2) {
		checkNotNull(prec1);
		checkNotNull(prec2);
		return prec1.join(prec2);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName(); // TODO: splitting strategy should be
											// included
	}

	public static Function<Expr<BoolType>, Collection<Expr<BoolType>>> whole() {
		return Collections::singleton;
	}

	public static Function<Expr<BoolType>, Collection<Expr<BoolType>>> conjuncts() {
		return ExprUtils::getConjuncts;
	}

	public static Function<Expr<BoolType>, Collection<Expr<BoolType>>> atoms() {
		return ExprUtils::getAtoms;
	}

}
