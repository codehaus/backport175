/*******************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.reader.bytecode;

import org.codehaus.backport175.compiler.AnnotationC;
import org.codehaus.backport175.reader.bytecode.spi.BytecodeProvider;

import java.io.InputStream;
import java.io.IOException;

/**
 * Default implementation of the {@link org.codehaus.backport175.reader.bytecode.spi.BytecodeProvider}  interface which
 * reads the bytecode from disk.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bon�r</a>
 */
public class DefaultBytecodeProvider implements BytecodeProvider {

    /**
     * Returns the bytecode for a specific class.
     *
     * @param className the fully qualified name of the class
     * @param loader    the class loader that has loaded the class
     * @return the bytecode
     */
    public byte[] getBytecode(final String className, final ClassLoader loader) {
        byte[] bytes;
        InputStream in = null;
        try {
            if (loader != null) {
                in = loader.getResourceAsStream(className.replace('.', '/') + ".class");
            } else {
                in = ClassLoader.getSystemClassLoader().getResourceAsStream(className.replace('.', '/') + ".class");
            }
            bytes = toByteArray(in);
        } catch (IOException e) {
            throw new RuntimeException("could not read in class as byte array due to: " + e.toString());
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                AnnotationC.logWarning("could not close bytecode input stream for class [" + className + "]");
            }
        }
        return bytes;
    }

    /**
     * Reads in the bytecode stream and returns a byte[] array.
     *
     * @param in
     * @return
     * @throws IOException
     */
    private byte[] toByteArray(final InputStream in) throws IOException {
        byte[] bytes = new byte[in.available()];
        int len = 0;
        while (true) {
            int n = in.read(bytes, len, bytes.length - len);
            if (n == -1) {
                if (len < bytes.length) {
                    byte[] c = new byte[len];
                    System.arraycopy(bytes, 0, c, 0, len);
                    bytes = c;
                }
                return bytes;
            }
            len += n;
            if (len == bytes.length) {
                byte[] c = new byte[bytes.length + 1000];
                System.arraycopy(bytes, 0, c, 0, len);
                bytes = c;
            }
        }
    }
}
