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
import org.codehaus.backport175.reader.bytecode.AnnotationReader;

import test.java5.Target5;
import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class PerfTest extends TestCase {
    private static int INVOCATIONS = 100000;

    public void setUp() {
        // warm up
        final AnnotationReader reader = AnnotationReader.getReaderFor(test.reader.Target.METHOD.getDeclaringClass());
        reader.getAnnotation("test.TestAnnotations$VoidTyped", test.reader.Target.METHOD);
        Target5.METHOD.getAnnotation(Target5.Test.class);
    }

    public void testAccessAnnotation() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < INVOCATIONS; i++) {
            final AnnotationReader reader = AnnotationReader.getReaderFor(test.reader.Target.METHOD.getDeclaringClass());
            Annotation annotation = reader.getAnnotation("test.TestAnnotations$VoidTyped", test.reader.Target.METHOD);
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double) INVOCATIONS;
        System.out.println("backport175 API : " + timePerInvocationNormalMethod);
    }

    public void testReflect() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < INVOCATIONS; i++) {
            //FIXME: the Java 5 annotation is not exactly the same
            // use same Target and not TArget 5 when we can read annotationC ann from Java 5
            java.lang.annotation.Annotation annotation = Target5.METHOD.getAnnotation(Target5.Test.class);
        }
        long time = System.currentTimeMillis() - startTime;
        double timePerInvocationNormalMethod = time / (double) INVOCATIONS;
        System.out.println("Java 5 reflect : " + timePerInvocationNormalMethod);
    }

    //-- junit

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(PerfTest.class);
    }
}
