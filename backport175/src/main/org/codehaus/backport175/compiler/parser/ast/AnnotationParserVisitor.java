/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/

package org.codehaus.backport175.compiler.parser.ast;

public interface AnnotationParserVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTRoot node, Object data);
  public Object visit(ASTAnnotation node, Object data);
  public Object visit(ASTKeyValuePair node, Object data);
  public Object visit(ASTIdentifier node, Object data);
  public Object visit(ASTBoolean node, Object data);
  public Object visit(ASTChar node, Object data);
  public Object visit(ASTString node, Object data);
  public Object visit(ASTArray node, Object data);
  public Object visit(ASTInteger node, Object data);
  public Object visit(ASTFloat node, Object data);
  public Object visit(ASTHex node, Object data);
  public Object visit(ASTOct node, Object data);
}
