/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.java5.performance;

import org.codehaus.backport175.reader.Annotation;
import org.codehaus.backport175.reader.Annotations;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import test.Target;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PerfTest extends TestCase {
    private static final Method method;
    private static int INVOCATIONS = 100000;

    static {
        try {
            method = Target.class.getDeclaredMethod("method", new Class[]{});
        } catch (Exception e) {
            throw new RuntimeException("member not found in class");
        }
    }

    public void setUp() {
        // warm up
        Annotations.getAnnotation("test.reader.TestAnnotations$VoidTyped", method);
    }

    public void testAccessAnnotation() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < INVOCATIONS; i++) {
            Annotation annotation = Annotations.getAnnotation("test.reader.TestAnnotations$VoidTyped", method
            );
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double) INVOCATIONS;
        System.out.println("NO CACHE : " + timePerInvocationNormalMethod);
    }

    public void testReflect() {
//        long startTime = System.currentTimeMillis();
//        for (int i = 0; i < INVOCATIONS; i++) {
//            java.lang.reader.Annotation reader = method.getAnnotation(Target.Test.class);
//        }
//        long time = System.currentTimeMillis() - startTime;
//        double timePerInvocationNormalMethod = time / (double) INVOCATIONS;
//        System.out.println("REFLECT : " + timePerInvocationNormalMethod);
    }

    //-- junit

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(PerfTest.class);
    }
}
