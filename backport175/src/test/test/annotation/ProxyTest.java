/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.annotation;

import junit.framework.TestCase;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.codehaus.backport175.reader.Annotation;
import org.codehaus.backport175.reader.Annotations;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class ProxyTest extends TestCase {

    public void testAnnOnProxy() {
        // likely to fail for bytecode provider reason but must be gracefull

        Object proxy = Proxy.newProxyInstance(
                ProxyTest.class.getClassLoader(),
                new Class[]{MyIntf.class},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("hello".equals(method.getName())) {
                            return "I am a proxy";
                        }
                        return null;
                    }
                }
        );

        assertEquals("I am a proxy", ((MyIntf)proxy).hello());

        try {
            Annotation[] anns = Annotations.getAnnotations(proxy.getClass());
            assertEquals(0, anns.length);
            fail("not yet there - exception expected");//TODO TBD
        } catch (Throwable e) {
            ;//ok
        }
    }

    //-- junit
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(ProxyTest.class);
    }

    public static interface MyIntf {
        String hello();
    }

}
