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



public interface IExprVisitor {

	void visit(Var i);
    void visit(FieldGet fieldGet);
	void visit(CallExpr callExpr);
	
	// ALEX Andy. New kinds of expression for visiting...
    void visit(StringConstExpr stringConstExpr);
    void visit(CastExpr castExpr);
    void visit(FieldGetOn fieldGetOn);

}
