/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.reader;

import org.codehaus.backport175.reader.bytecode.AnnotationElement;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class TestAnnotations {
    public static interface VoidTyped {
    }

    public static interface Simple {

        public String val();

        public String s();
    }

    public static interface SimpleNested {

        public Simple nested();
    }

    public static interface SimpleDefaultNested {
        public DefaultString nested();
    }

    public static interface SimpleValueDefaultNested {
        public DefaultString value();
    }

    public static interface StringArray {
        public String[] ss();
    }

    public static interface LongArray {
        public long[] l();
    }

    public static interface SimpleStringArrayNested {
        public StringArray nested();
    }

    public static interface NestedAnnotation {

        public Simple ann();
    }

    public static interface NestedAnnotationArray {

        public Simple[] annArr();
    }

    public static interface DefaultString {

        public String value();
    }

    public static interface DefaultInt {

        public int value();
    }

    public static interface Complex {

        public int i();

        public double[] doubleArr();

        public Class type();

        public Class[] typeArr();

        public AnnotationElement.Type enumeration();
    }
}
