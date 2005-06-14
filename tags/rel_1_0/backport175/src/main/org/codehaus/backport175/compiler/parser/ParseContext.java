/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.parser;

import org.objectweb.asm.AnnotationVisitor;

/**
 * Context for the parser sessions.
 *
 * TODO: make immutable
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>     *
 */
class ParseContext {
    public String elementName;
    public Class annotationType;
    public Class expectedType;
    public AnnotationVisitor munger;

    public ParseContext(final String elementName, 
                        final Class annotationType,
                        final Class expectedType,
                        final AnnotationVisitor munger) {
        this.elementName = elementName;
        this.annotationType = annotationType;
        this.expectedType = expectedType;
        this.munger = munger;
    }

    public ParseContext(final String elementName, final Class annotationType, final AnnotationVisitor munger) {
        this(elementName, annotationType, null, munger);
    }
}
