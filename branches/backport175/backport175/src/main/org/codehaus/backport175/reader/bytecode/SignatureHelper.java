/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.reader.bytecode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * Returns JVM type signature for a members and types.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class SignatureHelper {
    /**
     * Returns JVM type signature for a constructor.
     *
     * @param constructor
     * @return
     */
    public static String getConstructorSignature(final Constructor constructor) {
        return getMethodSignature(constructor.getParameterTypes(), Void.TYPE);
    }

    /**
     * Returns JVM type signature for a method.
     *
     * @param method
     * @return
     */
    public static String getMethodSignature(final Method method) {
        return getMethodSignature(method.getParameterTypes(), method.getReturnType());
    }

    /**
     * Returns JVM type signature for a field.
     *
     * @param field
     * @return
     */
    public static String getFieldSignature(final Field field) {
        return getClassSignature(field.getType());
    }

    /**
     * Returns JVM type signature for given class.
     *
     * @param klass
     * @return
     */
    public static String getClassSignature(Class klass) {
        StringBuffer buf = new StringBuffer();
        while (klass.isArray()) {
            buf.append('[');
            klass = klass.getComponentType();
        }
        if (klass.isPrimitive()) {
            if (klass == Integer.TYPE) {
                buf.append('I');
            } else if (klass == Byte.TYPE) {
                buf.append('B');
            } else if (klass == Long.TYPE) {
                buf.append('J');
            } else if (klass == Float.TYPE) {
                buf.append('F');
            } else if (klass == Double.TYPE) {
                buf.append('D');
            } else if (klass == Short.TYPE) {
                buf.append('S');
            } else if (klass == Character.TYPE) {
                buf.append('C');
            } else if (klass == Boolean.TYPE) {
                buf.append('Z');
            } else if (klass == Void.TYPE) {
                buf.append('V');
            } else {
                throw new InternalError();
            }
        } else {
            buf.append('L' + klass.getName().replace('.', '/') + ';');
        }
        return buf.toString();
    }

    /**
     * Returns JVM type signature for given list of parameters and return type.
     *
     * @param paramTypes
     * @param retType
     * @return
     */
    private static String getMethodSignature(final Class[] paramTypes, final Class retType) {
        StringBuffer buf = new StringBuffer();
        buf.append('(');
        for (int i = 0; i < paramTypes.length; i++) {
            buf.append(getClassSignature(paramTypes[i]));
        }
        buf.append(')');
        buf.append(getClassSignature(retType));
        return buf.toString();
    }
}
