/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/

package org.codehaus.backport175.compiler.parser.ast;

public interface AnnotationParserTreeConstants
{
  public int JJTROOT = 0;
  public int JJTANNOTATION = 1;
  public int JJTVOID = 2;
  public int JJTKEYVALUEPAIR = 3;
  public int JJTIDENTIFIER = 4;
  public int JJTBOOLEAN = 5;
  public int JJTCHAR = 6;
  public int JJTSTRING = 7;
  public int JJTARRAY = 8;
  public int JJTINTEGER = 9;
  public int JJTFLOAT = 10;
  public int JJTHEX = 11;
  public int JJTOCT = 12;


  public String[] jjtNodeName = {
    "Root",
    "Annotation",
    "void",
    "KeyValuePair",
    "Identifier",
    "Boolean",
    "Char",
    "String",
    "Array",
    "Integer",
    "Float",
    "Hex",
    "Oct",
  };
}
