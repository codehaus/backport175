/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
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

    /**
     * @DefaultString "(hello) -              see the space here !"
     */
    int j1;

    /**
     * @DefaultString execution(hello)
     */
    int j2;

    /**
     * @test.TestAnnotations.DefaultInt(1)
     * @DefaultString ("set(* test.fieldsetbug.TargetClass.public*) && within(test.fieldsetbug.*)")
     */
    int j3;

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

    public void testDocletSyntaxWithNestedParentesis() throws Throwable {
        String fieldName = "j1";
        Field field = DocletSyntaxTest.class.getDeclaredField(fieldName);
        TestAnnotations.DefaultString anno =
                (TestAnnotations.DefaultString)Annotations.getAnnotation(TestAnnotations.DefaultString.class, field);
        if (anno == null) {
            fail("could not find annotation on field " + fieldName);
        }
        assertEquals("(hello) - see the space here !", anno.value());
    }

    public void testDocletSyntaxWithNestedParentesisAndSpace() throws Throwable {
        String fieldName = "j2";
        Field field = DocletSyntaxTest.class.getDeclaredField(fieldName);
        TestAnnotations.DefaultString anno =
                (TestAnnotations.DefaultString)Annotations.getAnnotation(TestAnnotations.DefaultString.class, field);
        if (anno == null) {
            fail("could not find annotation on field " + fieldName);
        }
        assertEquals("execution(hello)", anno.value());
    }

    public void testDocletSyntaxWithPointcutStyleThings() throws Throwable {
        String fieldName = "j3";
        Field field = DocletSyntaxTest.class.getDeclaredField(fieldName);
        TestAnnotations.DefaultString anno =
                (TestAnnotations.DefaultString)Annotations.getAnnotation(TestAnnotations.DefaultString.class, field);
        if (anno == null) {
            fail("could not find annotation on field " + fieldName);
        }
        assertEquals("set(* test.fieldsetbug.TargetClass.public*) && within(test.fieldsetbug.*)", anno.value());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DocletSyntaxTest.class);
    }

}
