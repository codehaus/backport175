/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.ast;

import org.aspectj.weaver.TypeX;

public class Instanceof extends Test {
	Var var;
	TypeX type;

	public Instanceof(Var left, TypeX right) {
		super();
		this.var = left;
		this.type = right;
	}

	public void accept(ITestVisitor v) {
		v.visit(this);
	}
	
	public String toString() {
		return "(" + var + " instanceof " + type + ")";
	}

	public boolean equals(Object other) {
		if (other instanceof Instanceof) {
			Instanceof o = (Instanceof) other;
			return o.var.equals(var) && o.type.equals(type);
		} else {
			return false;
		}
	}

    public Var getVar() {
        return var;
    }

    public TypeX getType() {
        return type;
    }
}
