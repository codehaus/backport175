/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.java5;

import test.*;
import test.Target;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

@Target5.Test(test="test")
public class Target5 {

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public static @interface Test {
        String test();
    }

    @Target5.Test(test="test")
    public Target5() {
    }

    @Target5.Test(test="test")
    private String field;

    @Target5.Test(test="test")
    public void method() {
    }


    public static final Field FIELD;
    public static final Method METHOD;
    public static final Constructor CONSTRUCTOR;

    static {
        try {
            FIELD = Target5.class.getDeclaredField("field");
            METHOD = Target5.class.getDeclaredMethod("method", new Class[]{});
            CONSTRUCTOR = Target5.class.getDeclaredConstructor(new Class[]{});
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }


}