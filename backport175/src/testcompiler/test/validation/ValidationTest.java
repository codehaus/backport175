/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.validation;

import junit.framework.TestCase;

import org.codehaus.backport175.compiler.parser.AnnotationValidationException;
import org.codehaus.backport175.compiler.AnnotationC;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class ValidationTest extends TestCase {

    private static Method intM0;
    private static Method intM1;
    private static Method intM2;

    private static Method floatM0;
    private static Method floatM1;
    private static Method floatM2;

    private static Method intArrM0;
    private static Method intArrM1;
    private static Method intArrM2;

    private static Method typeRefM0;
    private static Method typeRefM1;
    private static Method typeRefM2;

    private static Method enumM0;
    private static Method enumM1;
    private static Method enumM2;

    private static final String CLASSPATH = "target/test-classes";
    private static final String SOURCE_DIR = "src/testcompiler/test/validation/";
    private static final AnnotationC.StdEventHandler EVENT_HANDLER = new AnnotationC.StdEventHandler(true);

    static {
        try {
            intM0 = IntAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno0", new Class[]{});
            intM1 = IntAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno1", new Class[]{});
            intM2 = IntAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno2", new Class[]{});

            floatM0 = FloatAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno0", new Class[]{});
            floatM1 = FloatAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno1", new Class[]{});
            floatM2 = FloatAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno2", new Class[]{});

            intArrM0 = IntArrayAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno0", new Class[]{});
            intArrM1 = IntArrayAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno1", new Class[]{});
            intArrM2 = IntArrayAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno2", new Class[]{});

            typeRefM0 = TypeRefAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno0", new Class[]{});
            typeRefM1 = TypeRefAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno1", new Class[]{});
            typeRefM2 = TypeRefAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno2", new Class[]{});

            enumM0 = EnumAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno0", new Class[]{});
            enumM1 = EnumAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno1", new Class[]{});
            enumM2 = EnumAnnoTarget.class.getDeclaredMethod("nonWellFormedAnno2", new Class[]{});
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    public static interface IntAnno {
        int value();
    }

    public static interface FloatAnno {
        float value();
    }

    public static interface IntArrayAnno {
        int[] value();
    }

    public static interface TypeRefAnno {
        Class value();
    }

    public static interface EnumAnno {
        MyEnum value();
    }

    public ValidationTest(String name) {
        super(name);
    }

    public void testInt() {
        try {
            AnnotationC.compile(
                    new String[]{},
                    new String[]{SOURCE_DIR + "IntAnnoTarget.java"},
                    new String[]{CLASSPATH},
                    CLASSPATH,
                    new String[]{},
                    EVENT_HANDLER
            );
        } catch (AnnotationValidationException e) {
            // TODO check the error message in exception
            return;
        }
        fail("AnnotationValidationException should have been thrown");
    }

    public void testFloat() {
        try {
            AnnotationC.compile(
                    new String[]{},
                    new String[]{SOURCE_DIR + "FloatAnnoTarget.java"},
                    new String[]{CLASSPATH},
                    CLASSPATH,
                    new String[]{},
                    EVENT_HANDLER
            );
        } catch (AnnotationValidationException e) {
            // TODO check the error message in exception
            return;
        }
        fail("AnnotationValidationException should have been thrown");
    }

    public void testPrimitiveArray() {
        try {
            AnnotationC.compile(
                    new String[]{},
                    new String[]{SOURCE_DIR + "IntArrayAnnoTarget.java"},
                    new String[]{CLASSPATH},
                    CLASSPATH,
                    new String[]{},
                    EVENT_HANDLER
            );
        } catch (AnnotationValidationException e) {
            // TODO check the error message in exception
            return;
        }
        fail("AnnotationValidationException should have been thrown");
    }

    public void testTypeRef() {
        try {
            AnnotationC.compile(
                    new String[]{},
                    new String[]{SOURCE_DIR + "TypeRefAnnoTarget.java"},
                    new String[]{CLASSPATH},
                    CLASSPATH,
                    new String[]{},
                    EVENT_HANDLER
            );
        } catch (AnnotationValidationException e) {
            // TODO check the error message in exception
            return;
        }
        fail("AnnotationValidationException should have been thrown");
    }

    public void testEnum() {
        try {
            AnnotationC.compile(
                    new String[]{},
                    new String[]{SOURCE_DIR + "EnumAnnoTarget.java"},
                    new String[]{CLASSPATH},
                    CLASSPATH,
                    new String[]{},
                    EVENT_HANDLER
            );
        } catch (AnnotationValidationException e) {
            // TODO check the error message in exception
            return;
        }
        fail("AnnotationValidationException should have been thrown");
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ValidationTest.class);
    }
}
