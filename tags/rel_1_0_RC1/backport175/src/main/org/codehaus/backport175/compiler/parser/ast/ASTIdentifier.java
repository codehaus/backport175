/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.parser.ast;

public class ASTIdentifier extends SimpleNode {
    private String m_value;

    public ASTIdentifier(int id) {
        super(id);
    }

    public ASTIdentifier(AnnotationParser p, int id) {
        super(p, id);
    }

    public Object jjtAccept(AnnotationParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public void setValue(String value) {
        m_value = value;
    }

    public String getValue() {
        return m_value;
    }
}