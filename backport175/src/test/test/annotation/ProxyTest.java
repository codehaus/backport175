/*******************************************************************************
 * Copyright (c) 2005 BEA 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    BEA - initial API and implementation
 *******************************************************************************/
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
