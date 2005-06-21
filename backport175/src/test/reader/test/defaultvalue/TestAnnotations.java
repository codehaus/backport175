/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.defaultvalue;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class TestAnnotations {

    public static interface IntAnno {
        /**
         * @org.codehaus.backport175.DefaultValue(3)
         */
        int value();
    }

    public static interface StringAnno {
        /**
         * @org.codehaus.backport175.DefaultValue("hello")
         */
        String s();
    }

    public static interface FloatArrayAnno {
        /**
         * @org.codehaus.backport175.DefaultValue({0f, 1f})
         */
        float[] ff();
    }

    public static interface AnnoArrayAnno {

        /**
         * @org.codehaus.backport175.DefaultValue({@test.defaultvalue.AnAnno(message="one"), @test.defaultvalue.AnAnno(message="two")})
         */
        AnAnno[] annos();
    }

    public static interface AnAnno {
        String message();
    }

    public static class SomeNestedClass {}

    public static interface DefaultedClassAnno {
        /**
         * @org.codehaus.backport175.DefaultValue(test.defaultvalue.TestAnnotations.SomeNestedClass.class)
         */
        Class klass();
    }

}
