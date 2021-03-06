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
package hu.bme.mit.theta.core.stmt;

import static com.google.common.base.Preconditions.checkNotNull;

import hu.bme.mit.theta.core.decl.VarDecl;
import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.Type;
import hu.bme.mit.theta.core.type.booltype.BoolType;

public final class Stmts {

	private Stmts() {
	}

	public static AssumeStmt Assume(final Expr<BoolType> cond) {
		checkNotNull(cond);
		return new AssumeStmt(cond);
	}

	public static <T extends Type> AssignStmt<T> Assign(final VarDecl<T> lhs, final Expr<T> rhs) {
		checkNotNull(lhs);
		checkNotNull(rhs);
		return new AssignStmt<>(lhs, rhs);
	}

	public static <T extends Type> HavocStmt<T> Havoc(final VarDecl<T> varDecl) {
		checkNotNull(varDecl);
		return new HavocStmt<>(varDecl);
	}

}
