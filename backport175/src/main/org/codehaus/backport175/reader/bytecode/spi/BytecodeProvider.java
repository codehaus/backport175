/*******************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.reader.bytecode.spi;

/**
 * Callback interface that all vendors that wants to be able to control which bytecode is read when retrieving the
 * annotations should implement.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public interface BytecodeProvider {

    /**
     * Returns the bytecode for a specific class.
     *
     * @param className the fully qualified name of the class
     * @param loader    the class loader that has loaded the class
     * @return the bytecode
     */
    byte[] getBytecode(String className, ClassLoader loader);
}
