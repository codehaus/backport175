/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.parser;

import org.codehaus.backport175.compiler.parser.ast.ASTAnnotation;
import org.codehaus.backport175.compiler.parser.ast.ASTArray;
import org.codehaus.backport175.compiler.parser.ast.ASTBoolean;
import org.codehaus.backport175.compiler.parser.ast.ASTChar;
import org.codehaus.backport175.compiler.parser.ast.ASTFloat;
import org.codehaus.backport175.compiler.parser.ast.ASTHex;
import org.codehaus.backport175.compiler.parser.ast.ASTIdentifier;
import org.codehaus.backport175.compiler.parser.ast.ASTInteger;
import org.codehaus.backport175.compiler.parser.ast.ASTKeyValuePair;
import org.codehaus.backport175.compiler.parser.ast.ASTOct;
import org.codehaus.backport175.compiler.parser.ast.ASTRoot;
import org.codehaus.backport175.compiler.parser.ast.ASTString;
import org.codehaus.backport175.compiler.parser.ast.AnnotationParserVisitor;
import org.codehaus.backport175.compiler.parser.ast.SimpleNode;
import org.codehaus.backport175.compiler.javadoc.RawAnnotation;
import org.codehaus.backport175.compiler.CompilerException;
import org.objectweb.asm.Type;
import org.objectweb.asm.AnnotationVisitor;

import java.lang.reflect.Method;
import java.lang.reflect.Array;

/**
 * The annotation visitor. Visits the annotation elements and adds them to the bytecode of the class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 */
public class AnnotationParser implements AnnotationParserVisitor {

    /**
     * The default value's name.
     */
    public static final String DEFAULT_VALUE_NAME = "value";

    /**
     * The one and only annotation javadoc.
     */
    protected static final org.codehaus.backport175.compiler.parser.ast.AnnotationParser PARSER =
            new org.codehaus.backport175.compiler.parser.ast.AnnotationParser(System.in);

    /**
     * We reference class at parse time. We don't need to avoid reflection.
     */
    protected final Class m_annotationClass;

    /**
     * The ASM bytecode munger.
     */
    protected AnnotationVisitor m_currentBytecodeMunger;

    /**
     * Parses the raw annotation.
     *
     * @param bytecodeMunger
     * @param annotationInterface
     * @param rawAnnotation
     */
    public static void parse(
            final AnnotationVisitor bytecodeMunger,
            final Class annotationInterface,
            final RawAnnotation rawAnnotation) {
        final String interfaceName = annotationInterface.getName();
        final String rawAnnotationValue = rawAnnotation.getValue();

        final StringBuffer representation = new StringBuffer("@");
        representation.append(interfaceName).append('(');
        if (rawAnnotationValue != null) {
            representation.append(rawAnnotationValue);
        }
        representation.append(')');

        try {
            final AnnotationParser annotationParser = new AnnotationParser(bytecodeMunger, annotationInterface);
            annotationParser.visit(PARSER.parse(representation.toString()), null);
        } catch (Throwable e) {
            throw new ParseException(
                    "cannot parse annotation [" + representation.toString() + "] due to: " + e.toString(),
                    e,
                    CompilerException.Location.render(rawAnnotation)
            );
        }
    }

    /**
     * Returns the method matching the annotation value name. The name of the method should be the same as the
     * annotation element name.
     *
     * @param elementName
     * @param annotationInterface
     * @return the method
     */
    public static Method getMethodFor(final String elementName, final Class annotationInterface) {
        StringBuffer javaBeanMethodPostfix = new StringBuffer();
        javaBeanMethodPostfix.append(elementName.substring(0, 1).toUpperCase());
        if (elementName.length() > 1) {
            javaBeanMethodPostfix.append(elementName.substring(1));
        }
        Method method = null;
        Method[] methods = annotationInterface.getDeclaredMethods();
        // look for element methods
        for (int i = 0; i < methods.length; i++) {
            Method elementMethod = methods[i];
            if (elementMethod.getName().equals(elementName)) {
                method = elementMethod;
                break;
            }
        }
        if (method == null) {
            throw new ParseException(
                    "method for the annotation element ["
                    + elementName
                    + "] can not be found in annotation interface ["
                    + annotationInterface.getName()
                    + "]"
            );
        }
        return method;
    }

    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, null);
    }

    public Object visit(ASTRoot node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, null);
    }

    public Object visit(ASTAnnotation node, Object elementName) {
        if (elementName == null) {
            // top level annotation
            handleAnnotation(node, elementName);
        } else {
            // nested annotation
            handleNestedAnnotation(node, (String)elementName);
        }
        return null;
    }

    public Object visit(ASTKeyValuePair node, Object data) {
        String elementName = node.getKey();
        node.jjtGetChild(0).jjtAccept(this, elementName);
        return null;
    }

    public Object visit(ASTIdentifier node, Object elementName) {
        String identifier = node.getValue();
        if (identifier.endsWith(".class")) {
            handleClassIdentifier(identifier, (String)elementName);
        } else if (isJavaReferenceType(identifier)) {
            handleReferenceIdentifier(identifier, (String)elementName);
        } else {
            throw new ParseException(
                    "unsupported format for java type or static reference (enum) [" + elementName + "::" + identifier + "]"
            );
        }
        return null;
    }

    public Object visit(ASTBoolean node, Object elementName) {
        AnnotationValidator.validateBoolean(m_annotationClass, (String)elementName);
        Boolean bool = Boolean.valueOf(node.getValue());
        m_currentBytecodeMunger.visit((String)elementName, bool);
        return null;
    }

    public Object visit(ASTChar node, Object elementName) {
        AnnotationValidator.validateCharacter(m_annotationClass, (String)elementName);
        Character character = new Character(node.getValue().charAt(0));
        m_currentBytecodeMunger.visit((String)elementName, character);
        return null;
    }

    public Object visit(ASTString node, Object elementName) {
        AnnotationValidator.validateString(m_annotationClass, (String)elementName);

        // the node contains the  \" string escapes
        String string;
        if (node.getValue().length() >= 2) {
            string = node.getValue().substring(1, node.getValue().length() - 1);
        } else {
            string = node.getValue();
        }

        m_currentBytecodeMunger.visit((String)elementName, string);
        return null;
    }

    public Object visit(ASTInteger node, Object elementName) {
        String value = node.getValue();
        char lastChar = value.charAt(value.length() - 1);

        Object integer;
        if ((lastChar == 'L') || (lastChar == 'l')) {
            integer = new Long(value.substring(0, value.length() - 1));
        } else if (value.length() > 9) {
            integer = new Long(value);
            AnnotationValidator.validateLong(m_annotationClass, (String)elementName);
        } else {
            integer = new Integer(value);
            AnnotationValidator.validateInteger(m_annotationClass, (String)elementName);
        }

        m_currentBytecodeMunger.visit((String)elementName, integer);
        return null;
    }

    public Object visit(ASTFloat node, Object elementName) {
        String value = node.getValue();
        char lastChar = value.charAt(value.length() - 1);
        Object decimalNumber;
        if ((lastChar == 'D') || (lastChar == 'd')) {
            decimalNumber = new Double(value.substring(0, value.length() - 1));
            AnnotationValidator.validateDouble(m_annotationClass, (String)elementName);
        } else if ((lastChar == 'F') || (lastChar == 'f')) {
            decimalNumber = new Float(value.substring(0, value.length() - 1));
            AnnotationValidator.validateFloat(m_annotationClass, (String)elementName);
        } else {
            decimalNumber = new Double(value);
            AnnotationValidator.validateDouble(m_annotationClass, (String)elementName);
        }
        m_currentBytecodeMunger.visit((String)elementName, decimalNumber);
        return null;
    }

    public Object visit(ASTHex node, Object elementName) {
        throw new UnsupportedOperationException("hex numbers not yet supported");
    }

    public Object visit(ASTOct node, Object elementName) {
        throw new UnsupportedOperationException("octal numbers not yet supported");
    }

    public Object visit(ASTArray node, Object name) {
        String elementName = (String)name;
        AnnotationValidator.validateArray(m_annotationClass, elementName);

        Class elementType = getMethodFor(elementName, m_annotationClass).getReturnType();
        if (!elementType.isArray()) {
            throw new ParseException("type for element [" + elementName + "] is not of type array");
        }
        if (elementType.getComponentType().isArray()) {
            throw new UnsupportedOperationException(
                    "multi dimensional arrays are not supported for element type - was defined by element [" +
                    elementName +
                    "]"
            );
        }

        AnnotationVisitor parentBytecodeMunger = m_currentBytecodeMunger;
        m_currentBytecodeMunger = m_currentBytecodeMunger.visitArray(elementName);
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, name);
        }
        m_currentBytecodeMunger.visitEnd();
        m_currentBytecodeMunger = parentBytecodeMunger;
        return null;
    }

    protected void handleAnnotation(final ASTAnnotation node, final Object elementName) {
        int nr = node.jjtGetNumChildren();
        if (nr == 1) {
            AnnotationValidator.validateAnnotation(m_annotationClass, DEFAULT_VALUE_NAME);
            // default value
            node.jjtGetChild(0).jjtAccept(this, DEFAULT_VALUE_NAME);
        } else {
            AnnotationValidator.validateAnnotation(m_annotationClass, (String)elementName);
            // key-value pairs
            for (int i = 0; i < nr; i++) {
                node.jjtGetChild(i).jjtAccept(this, elementName);
            }
        }
    }

    protected void handleNestedAnnotation(final ASTAnnotation node, final String elementName) {
        AnnotationValidator.validateAnnotation(m_annotationClass, elementName);

        Class elementType = getMethodFor(elementName, m_annotationClass).getReturnType();
        if (elementType.isArray()) {
            // if we have an array of annotations
            elementType = elementType.getComponentType();
        }
        AnnotationVisitor parentBytecodeVisitor = m_currentBytecodeMunger;

        m_currentBytecodeMunger = m_currentBytecodeMunger.visitAnnotation(
                elementName, Type.getDescriptor(elementType)
        );
        handleAnnotation(node, elementName);

        m_currentBytecodeMunger.visitEnd();
        m_currentBytecodeMunger = parentBytecodeVisitor;
    }

    protected Object handleClassIdentifier(final String identifier, final String elementName) {
        AnnotationValidator.validateClass(m_annotationClass, elementName);

        int index = identifier.lastIndexOf('.');
        String componentClassName = identifier.substring(0, index);
        int dimension = 0;
        while (componentClassName.endsWith("[]")) {
            dimension++;
            componentClassName = componentClassName.substring(0, componentClassName.length() - 2);
        }
        Class componentClass;
        boolean isComponentPrimitive = true;
        if (componentClassName.equals("long")) {
            componentClass = long.class;
        } else if (componentClassName.equals("int")) {
            componentClass = int.class;
        } else if (componentClassName.equals("short")) {
            componentClass = short.class;
        } else if (componentClassName.equals("double")) {
            componentClass = double.class;
        } else if (componentClassName.equals("float")) {
            componentClass = float.class;
        } else if (componentClassName.equals("byte")) {
            componentClass = byte.class;
        } else if (componentClassName.equals("char")) {
            componentClass = char.class;
        } else if (componentClassName.equals("boolean")) {
            componentClass = boolean.class;
        } else if (componentClassName.equals("java.lang.String")) {
            componentClass = String.class;
        } else {
            isComponentPrimitive = false;
            try {
                componentClass = Class.forName(componentClassName, false, m_annotationClass.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new ParseException(
                        "could not load class [" + componentClassName + "] due to: " + e.toString(), e
                );
            }
        }
        if (isComponentPrimitive) {
            if (dimension <= 0) {
                Type componentType = Type.getType(componentClass);
                m_currentBytecodeMunger.visit(elementName, componentType);
            } else {
                Class arrayClass = Array.newInstance(componentClass, new int[dimension]).getClass();
                Type componentType = Type.getType(arrayClass);
                m_currentBytecodeMunger.visit(elementName, componentType);
            }
        } else {
            String componentType = Type.getType(componentClass).getDescriptor();
            for (int i = 0; i < dimension; i++) {
                componentType = "[" + componentType;
            }
            Type type = Type.getType(componentType);
            m_currentBytecodeMunger.visit(elementName, type);
        }
        return null;
    }

    protected Object handleReferenceIdentifier(final String identifier, final String elementName) {
        AnnotationValidator.validateEnum(m_annotationClass, elementName);

        int index = identifier.lastIndexOf('.');
        String className = identifier.substring(0, index);
        String fieldName = identifier.substring(index + 1, identifier.length());
        try {
            // TODO m_annotationClass might be higher in the CL than a referenced identifier
            Class clazz = Class.forName(className, false, m_annotationClass.getClassLoader());
            m_currentBytecodeMunger.visitEnum(elementName, Type.getDescriptor(clazz), fieldName);
        } catch (Exception e) {
            throw new ParseException(
                    "could not access reference field [" + identifier + "] due to: " + e.toString(), e
            );
        }
        return null;
    }

    protected boolean isJavaReferenceType(final String valueAsString) {
        int first = valueAsString.indexOf('.');
        int last = valueAsString.lastIndexOf('.');
        int comma = valueAsString.indexOf(',');
        if ((first > 0) && (last > 0) && (first != last) && (comma < 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates a new munging annotation visitor.
     *
     * @param bytecodeVisitor
     * @param annotationClass
     */
    protected AnnotationParser(final AnnotationVisitor bytecodeVisitor, final Class annotationClass) {
        m_currentBytecodeMunger = bytecodeVisitor;
        m_annotationClass = annotationClass;
    }
}