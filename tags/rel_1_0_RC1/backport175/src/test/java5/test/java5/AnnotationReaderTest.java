/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.java5;

import org.codehaus.backport175.reader.Annotation;
import org.codehaus.backport175.reader.Annotations;
import org.codehaus.backport175.reader.bytecode.AnnotationDefaults;
import org.codehaus.backport175.reader.bytecode.AnnotationElement;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationReaderTest extends TestCase {

    public AnnotationReaderTest(String name) {
        super(name);
    }

    public void testClassAnnReflection() {
        java.lang.annotation.Annotation[] annotations = Target5.class.getAnnotations();
        assertEquals(2, annotations.length);
    }

    public void testMethodAnnReflection() {
        java.lang.annotation.Annotation[] annotations = Target5.METHOD.getAnnotations();
        assertEquals(1, annotations.length);
    }

    public void testJava5ClassAnnotation() {
        Annotation reader = org.codehaus.backport175.reader.Annotations.getAnnotation(
                Target5.Test.class, Target5.class
        );
        Class type = reader.annotationType();
        assertEquals(Target5.Test.class, type);

        Target5.Test test = (Target5.Test)reader;
        assertEquals("test", test.test());
    }

    public void testJava5ConstructorAnnotation() {
        Annotation reader = org.codehaus.backport175.reader.Annotations.getAnnotation(
                Target5.Test.class, Target5.CONSTRUCTOR
        );
        Class type = reader.annotationType();
        assertEquals(Target5.Test.class, type);

        Target5.Test test = (Target5.Test)reader;
        assertEquals("test", test.test());
    }

    public void testJava5MethodAnnotation() {
        Annotation reader = org.codehaus.backport175.reader.Annotations.getAnnotation(
                Target5.Test.class, Target5.METHOD
        );
        Class type = reader.annotationType();
        assertEquals(Target5.Test.class, type);

        Target5.Test test = (Target5.Test)reader;
        assertEquals("test", test.test());
    }

    public void testJava5FieldAnnotation() {
        Annotation reader = org.codehaus.backport175.reader.Annotations.getAnnotation(
                Target5.Test.class, Target5.FIELD
        );
        Class type = reader.annotationType();
        assertEquals(Target5.Test.class, type);

        Target5.Test test = (Target5.Test)reader;
        assertEquals("test", test.test());
    }

    // === for testing Java 5 reflection compatibility ===

    public void testAnnotationCCompiledClassAnnReflection() {
        java.lang.annotation.Annotation[] annotations = test.reader.Target.class.getAnnotations();
        // only one is set with RuntimeRetention..
        assertEquals(1, annotations.length);
        //TODO refine the assert
    }

    //TODO refine the assert
    public void testAnnotationCCompiledMembersAnnReflection() {
        java.lang.annotation.Annotation[] annotations = test.reader.Target.METHOD.getAnnotations();
        assertTrue(annotations.length > 0);

        annotations = test.reader.Target.FIELD.getAnnotations();
        assertTrue(annotations.length > 0);

        annotations = test.reader.Target.CONSTRUCTOR.getAnnotations();
        assertTrue(annotations.length > 0);
    }

    public void testDefaulted() {
        AnnotationElement.Annotation e = AnnotationDefaults.getDefaults(Target5.DefaultedTest.class);
        assertEquals(2, e.getElements().size());
        assertEquals("test=1", (e.getElements().get(0)).toString());
        assertEquals("test2=default", (e.getElements().get(1)).toString());

        Annotation defaulted = Annotations.getAnnotation(Target5.DefaultedTest.class, Target5.class);
        assertNotNull(defaulted);
        assertEquals(1, ((Target5.DefaultedTest)defaulted).test());
        assertEquals("notdefault", ((Target5.DefaultedTest)defaulted).test2());
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(AnnotationReaderTest.class);
    }
}
