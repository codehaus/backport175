/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.parser.ast;

public class ASTAnnotation extends SimpleNode {
    public ASTAnnotation(int id) {
        super(id);
    }

    public ASTAnnotation(AnnotationParser p, int id) {
        super(p, id);
    }

    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(AnnotationParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}