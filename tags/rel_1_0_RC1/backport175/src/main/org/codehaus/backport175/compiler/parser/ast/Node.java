/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.parser.ast;

public interface Node {
    /**
     * This method is called after the node has been made the current node. It indicates that child nodes can now be
     * added to it.
     */
    public void jjtOpen();

    /**
     * This method is called after all the child nodes have been added.
     */
    public void jjtClose();

    /**
     * This pair of methods are used to inform the node of its parent.
     */
    public void jjtSetParent(Node n);

    public Node jjtGetParent();

    /**
     * This method tells the node to add its argument to the node's list of children.
     */
    public void jjtAddChild(Node n, int i);

    /**
     * This method returns a child node. The children are numbered from zero, left to right.
     */
    public Node jjtGetChild(int i);

    /**
     * Return the number of children the node has.
     */
    public int jjtGetNumChildren();

    /**
     * Accept the visitor. *
     */
    public Object jjtAccept(AnnotationParserVisitor visitor, Object data);
}