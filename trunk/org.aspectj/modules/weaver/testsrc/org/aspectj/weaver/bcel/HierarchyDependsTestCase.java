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


package org.aspectj.weaver.bcel;

import junit.framework.TestCase;

import org.apache.bcel.classfile.JavaClass;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.patterns.*;


public class HierarchyDependsTestCase extends TestCase {

	/**
	 * Constructor for ParserTestCase.
	 * @param arg0
	 */
	public HierarchyDependsTestCase(String arg0) {
		super(arg0);
	}
	
	
	public void testHierarchyDepends() {
		BcelWorld world = new BcelWorld();
		TypePatternQuestions questions = new TypePatternQuestions();
		BcelObjectType runnableType = (BcelObjectType)world.resolve("java.lang.Runnable");
		BcelObjectType numberType = (BcelObjectType)world.resolve("java.lang.Number");
		BcelObjectType integerType = (BcelObjectType)world.resolve("java.lang.Integer");
		BcelObjectType stringType = (BcelObjectType)world.resolve("java.lang.String");
		
		
		TypePattern numberPattern = new ExactTypePattern(numberType, false);
		questions.askQuestion(numberPattern, integerType, TypePattern.STATIC);
		questions.askQuestion(numberPattern, integerType, TypePattern.DYNAMIC);
		assertNull(questions.anyChanges());
		
		JavaClass saveClass = integerType.getJavaClass();
		integerType.replaceJavaClass(stringType.getJavaClass());
		assertNotNull(questions.anyChanges());
		
		integerType.replaceJavaClass(saveClass);
		assertNull(questions.anyChanges());
		
		TypePattern runnablePattern = new ExactTypePattern(runnableType, false);
		questions.askQuestion(runnablePattern, stringType, TypePattern.DYNAMIC);
		assertNull(questions.toString(), questions.anyChanges());
		
		saveClass = stringType.getJavaClass();
		stringType.replaceJavaClass(numberType.getJavaClass());
		assertNotNull(questions.toString(), questions.anyChanges());
		
		stringType.replaceJavaClass(saveClass);
		assertNull(questions.toString(), questions.anyChanges());
	}
}
