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
	
	/**
     * Visit a string constant
     * @param stringConstExpr
     */
    void visit(StringConstExpr stringConstExpr);

    /**
     * Visit a CHECKCAST instruction
     * @param castExpr
     */
    void visit(CastExpr castExpr);

    /**
     * Visit a field GET
     * @param fieldGetOn
     */
    void visit(FieldGetOn fieldGetOn);


}
