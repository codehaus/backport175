/*******************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.reader;

import junit.framework.TestCase;

import java.lang.reflect.Field;

import org.codehaus.backport175.reader.Annotations;
import test.TestAnnotations;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DocletSyntaxTest extends TestCase {

    /**
     * @DefaultString hello"there
     */
    int i1;

    /**
     * @DefaultString "hello\"there"
     */
    int i2;

    /**
     * @DefaultString ("hello\"there")
     */
    int i3;

    public void testDocletSyntax() throws Throwable {
        for (int i =1; i <= 3; i++) {
            String fieldName = "i"+i;
            Field field = DocletSyntaxTest.class.getDeclaredField(fieldName);
            TestAnnotations.DefaultString anno =
                    (TestAnnotations.DefaultString)Annotations.getAnnotation(TestAnnotations.DefaultString.class, field);
            if (anno == null) {
                fail("could not find annotation on field " + fieldName);
            }
            assertEquals("hello\"there", anno.value());
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DocletSyntaxTest.class);
    }

}
