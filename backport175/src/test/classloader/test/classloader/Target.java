/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.classloader;

import org.codehaus.backport175.reader.Annotations;

/**
 * @test.classloader.Anno(aClass=test.classloader.SomeClass.class)
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Target {

    public static void test() throws Throwable {
        Anno anno = (Anno)Annotations.getAnnotation(Anno.class, Target.class);
        if (anno==null || !anno.aClass().equals(SomeClass.class)) {
            throw new Exception("failed to access annotation");
        }
    }
}
