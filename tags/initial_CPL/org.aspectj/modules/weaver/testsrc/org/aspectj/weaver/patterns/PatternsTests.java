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


package org.aspectj.weaver.patterns;

import junit.framework.*;

public class PatternsTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(PatternsTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(AndOrNotTestCase.class); 
        suite.addTestSuite(BindingTestCase.class); 
        suite.addTestSuite(DeclareErrorOrWarningTestCase.class); 
        suite.addTestSuite(ModifiersPatternTestCase.class); 
        suite.addTestSuite(NamePatternParserTestCase.class); 
        suite.addTestSuite(NamePatternTestCase.class); 
        suite.addTestSuite(ParserTestCase.class); 
        suite.addTestSuite(SignaturePatternTestCase.class); 
        suite.addTestSuite(ThisOrTargetTestCase.class); 
        suite.addTestSuite(TypePatternListTestCase.class); 
        suite.addTestSuite(TypePatternTestCase.class); 
        suite.addTestSuite(WithinTestCase.class); 
        //$JUnit-END$
        return suite;
    }

    public PatternsTests(String name) { super(name); }

}  
