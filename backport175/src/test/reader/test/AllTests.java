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
import test.filtering.MemberFilteringTest;
import test.noduplicate.NoDuplicateTest;
import test.proxy.ProxyTest;
import test.reader.AnnotationReaderTest;
import test.primitives.PrimitiveTest;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AllTests extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("All tests");

        suite.addTestSuite(AnnotationReaderTest.class);
        suite.addTestSuite(NoDuplicateTest.class);
        suite.addTestSuite(ProxyTest.class);
        suite.addTestSuite(MemberFilteringTest.class);
        suite.addTestSuite(PrimitiveTest.class);

        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }


}
