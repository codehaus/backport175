/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
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
        if (!anno.aClass().equals(SomeClass.class)) {
            throw new Exception("failed to access annotation");
        }
    }
}
