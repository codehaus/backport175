/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.performance;

import org.codehaus.backport175.reader.Annotation;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import test.reader.Target;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PerfTest {
    private static final Method method;
    private static final Constructor constructor;
    private static int INVOCATIONS = 100000;

    static {
        try {
            constructor = Target.class.getDeclaredConstructor(new Class[]{});
            method = Target.class.getDeclaredMethod("method", new Class[]{});
        } catch (Exception e) {
            throw new RuntimeException("member not found in class");
        }
    }

    public static void main(String[] args) {
        org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.reader.TestAnnotations$VoidTyped", method
        );
        org.codehaus.backport175.reader.Annotations.getAnnotation(
                "test.reader.TestAnnotations$VoidTyped", constructor
        );
        testNoCache();
        testCache();
//        testReflect();
    }

    private static void testNoCache() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < INVOCATIONS; i++) {
            Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                    "test.reader.TestAnnotations$VoidTyped", method
            );
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double) INVOCATIONS;
        System.out.println("NO CACHE : " + timePerInvocationNormalMethod);
    }

    private static void testCache() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < INVOCATIONS; i++) {
            Annotation annotation = org.codehaus.backport175.reader.Annotations.getAnnotation(
                    "test.reader.TestAnnotations$VoidTyped", constructor
            );
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double) INVOCATIONS;
        System.out.println("CACHE : " + timePerInvocationNormalMethod);
    }

    private static void testReflect() {
//        long startTime = System.currentTimeMillis();
//        for (int i = 0; i < INVOCATIONS; i++) {
//            java.lang.reader.Annotation reader = method.getAnnotation(Target.Test.class);
//        }
//        long time = System.currentTimeMillis() - startTime;
//        double timePerInvocationNormalMethod = time / (double) INVOCATIONS;
//        System.out.println("REFLECT : " + timePerInvocationNormalMethod);
    }
}
