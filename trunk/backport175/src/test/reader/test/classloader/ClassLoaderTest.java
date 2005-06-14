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

import junit.framework.TestCase;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.codehaus.backport175.reader.bytecode.AnnotationReader;
import org.codehaus.backport175.reader.bytecode.spi.BytecodeProvider;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ClassLoaderTest extends TestCase {

    private static final String CLASSPATH = "target/testclassloader-classes";

    public void testChildClassLoader() throws Throwable {
        try {
            Class.forName("test.classloader.Target");
            fail("Should not see it");
        } catch (Throwable t) {
            ;
        }

        ClassLoader app = new URLClassLoader(
                new URL[]{new File(CLASSPATH).toURL()}, ClassLoaderTest.class.getClassLoader()
        );
        Class target = Class.forName("test.classloader.Target", false, app);
        Method m = target.getDeclaredMethod("test", new Class[0]);
        try {
            m.invoke(null, new Object[0]);
        } catch (InvocationTargetException e) {
            fail(e.getTargetException().toString());
        }
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ClassLoaderTest.class);
    }
}
