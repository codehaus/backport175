/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.compiler.validation;

import junit.framework.TestCase;

import org.codehaus.backport175.compiler.AnnotationC;
import org.codehaus.backport175.compiler.MessageHandler;
import org.codehaus.backport175.compiler.CompilerException;
import org.codehaus.backport175.compiler.SourceLocation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
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

    private static final String CLASSPATH = "target/testcompiler-classes";
    private static final String SOURCE_DIR = "src/test/compiler/test/compiler/validation/";
    private static final BufferedMessageHandler MESSAGE_HANDLER = new BufferedMessageHandler();

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
        MESSAGE_HANDLER.flush();
        AnnotationC.compile(
                new String[]{},
                new String[]{SOURCE_DIR + "IntAnnoTarget.java"},
                new String[]{CLASSPATH},
                CLASSPATH,
                new String[]{},
                MESSAGE_HANDLER,
                false
        );
        assertEquals(1, MESSAGE_HANDLER.compilerExceptions.size());
        assertEquals(0, MESSAGE_HANDLER.acceptedLocations.size());
        assertEquals("org.codehaus.backport175.compiler.parser.AnnotationValidationException: value [value] in annotation [test.compiler.validation.ValidationTest$IntAnno] does not have correct type: expected [int] was [java.lang.Class]",
                     MESSAGE_HANDLER.compilerExceptions.get(0).toString());
    }

    public void testFloat() {
        MESSAGE_HANDLER.flush();
        AnnotationC.compile(
                new String[]{},
                new String[]{SOURCE_DIR + "FloatAnnoTarget.java"},
                new String[]{CLASSPATH},
                CLASSPATH,
                new String[]{},
                MESSAGE_HANDLER,
                false
        );
        assertEquals(1, MESSAGE_HANDLER.compilerExceptions.size());
        assertEquals(0, MESSAGE_HANDLER.acceptedLocations.size());
        assertEquals("org.codehaus.backport175.compiler.parser.AnnotationValidationException: value [value] in annotation [test.compiler.validation.ValidationTest$FloatAnno] does not have correct type: expected [float] was [array type]",
                     MESSAGE_HANDLER.compilerExceptions.get(0).toString());
    }

    public void testPrimitiveArray() {
        MESSAGE_HANDLER.flush();
        AnnotationC.compile(
                new String[]{},
                new String[]{SOURCE_DIR + "IntArrayAnnoTarget.java"},
                new String[]{CLASSPATH},
                CLASSPATH,
                new String[]{},
                MESSAGE_HANDLER,
                false
        );
        assertEquals(1, MESSAGE_HANDLER.compilerExceptions.size());
        assertEquals(0, MESSAGE_HANDLER.acceptedLocations.size());
        assertTrue(MESSAGE_HANDLER.compilerExceptions.get(0).toString().startsWith(
                    "org.codehaus.backport175.compiler.parser.ParseException: cannot parse annotation [@test.compiler.validation.ValidationTest$IntArrayAnno({{1, 2, 3, 4}, 1)] due to: Encountered \")\" at line 1, column 71"
        ));
    }

    public void testTypeRef() {
        MESSAGE_HANDLER.flush();
        AnnotationC.compile(
                new String[]{},
                new String[]{SOURCE_DIR + "TypeRefAnnoTarget.java"},
                new String[]{CLASSPATH},
                CLASSPATH,
                new String[]{},
                MESSAGE_HANDLER,
                false
        );
        assertEquals(1, MESSAGE_HANDLER.compilerExceptions.size());
        assertEquals(0, MESSAGE_HANDLER.acceptedLocations.size());
    }

    public void testEnum() {
        MESSAGE_HANDLER.flush();
        AnnotationC.compile(
                new String[]{},
                new String[]{SOURCE_DIR + "EnumAnnoTarget.java"},
                new String[]{CLASSPATH},
                CLASSPATH,
                new String[]{},
                MESSAGE_HANDLER,
                false
        );
        assertEquals(1, MESSAGE_HANDLER.compilerExceptions.size());
        assertEquals(0, MESSAGE_HANDLER.acceptedLocations.size());
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ValidationTest.class);
    }

    /**
     * A message handler that keeps the errors and accepted location and can be flushed
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    static class BufferedMessageHandler implements MessageHandler {

        List compilerExceptions = new ArrayList();
        List acceptedLocations = new ArrayList();

        public void info(String message) {
            ;//skip
        }

        public void error(CompilerException exception) {
            compilerExceptions.add(exception);
        }

        public void accept(SourceLocation sourceLocation) {
            acceptedLocations.add(sourceLocation);
        }

        /**
         * Flush the errors and source locations
         */
        public void flush() {
            compilerExceptions.clear();
            acceptedLocations.clear();
        }

        /**
         * Helper to stdout
         */
        public void dump() {
            for (Iterator iterator = compilerExceptions.iterator(); iterator.hasNext();) {
                CompilerException compilerException = (CompilerException) iterator.next();
                System.out.println("ERROR: "+ compilerException.toString());
            }
            for (Iterator iterator = acceptedLocations.iterator(); iterator.hasNext();) {
                SourceLocation sourceLocation = (SourceLocation) iterator.next();
                System.out.println("OK: " + sourceLocation.toString());
            }
        }
    }
}
