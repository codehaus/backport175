/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.java5;

@Target.Test(test="test")
public class Target {

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public static @interface Test {
        String test();
    }

    @Target.Test(test="test")
    public Target() {
    }

    @Target.Test(test="test")
    private String field;

    @Target.Test(test="test")
    public void method() {
    }
}