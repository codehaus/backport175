/*******************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.reader.proxy;

import org.codehaus.backport175.reader.Annotation;
import org.codehaus.backport175.reader.bytecode.AnnotationElement;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Creates a proxy instance (Java dynamic proxy) for a given reader.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class ProxyFactory {

    /**
     * Creates a new proxy for the annotation specified.
     *
     * @param annotation the annotation data structure abstraction
     * @param loader the class loader for the target class
     * @return the proxy for the annotation
     */
    public static Annotation newAnnotationProxy(final AnnotationElement.Annotation annotation,
                                                final ClassLoader loader) {
        final Class interfaceClass;
        try {
            interfaceClass = Class.forName(annotation.getInterfaceName(), false, loader);
        } catch (ClassNotFoundException e) {
            throw new ResolveAnnotationException("annotation interface [" + annotation.getInterfaceName() + "] could not be found");
        }
        final InvocationHandler handler = new JavaDocAnnotationInvocationHander(interfaceClass, annotation);
        final Object annotationProxy = Proxy.newProxyInstance(
                loader,
                new Class[]{Annotation.class, interfaceClass},
                handler
        );
        return (Annotation)annotationProxy;
    }
}
