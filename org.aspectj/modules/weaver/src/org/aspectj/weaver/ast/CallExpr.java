/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.ast;

import org.aspectj.weaver.*;
import org.aspectj.weaver.Member;

public class CallExpr extends Expr {
	// assert m.return value is boolean
	private final Member method;
	private final Expr[] args;
	private final ResolvedTypeX returnType; // yes, stored in method as well, but that one isn't resolved

	public CallExpr(Member m, Expr[] args, ResolvedTypeX returnType) {
		super();
		this.method = m;
		this.args = args;
		this.returnType = returnType;
	}

	public void accept(IExprVisitor v) {
		v.visit(this);
	}

	public Expr[] getArgs() {
		return args;
	}

	public Member getMethod() {
		return method;
	}

	public ResolvedTypeX getType() {
		return returnType;
	}

}
