/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import test.validation.ValidationTest;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AllCompilerTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");

        suite.addTestSuite(ValidationTest.class);

        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }


}
