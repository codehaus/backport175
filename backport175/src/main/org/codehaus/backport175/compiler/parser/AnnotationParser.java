/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.parser;

import org.codehaus.backport175.compiler.parser.ast.*;
import org.codehaus.backport175.compiler.javadoc.RawAnnotation;
import org.codehaus.backport175.compiler.CompilerException;
import org.objectweb.asm.Type;
import org.objectweb.asm.AnnotationVisitor;

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
     * The top level annotation interface class.
     */
    protected Class m_annotationClass;

    /**
     * The top level annotation bytecode munger.
     */
    protected AnnotationVisitor m_bytecodeMunger;

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
            e.printStackTrace();
            throw new ParseException(
                    "cannot parse annotation [" + representation.toString() + "] due to: " + e.toString(),
                    e,
                    CompilerException.Location.render(rawAnnotation)
            );
        }
    }

    public Object visit(SimpleNode node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, null);
    }

    public Object visit(ASTRoot node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, null);
    }

    public Object visit(ASTAnnotation node, Object data) {
        ParseContext ctx = (ParseContext)data;
        if (ctx == null) {
            // top level annotation
            handleAnnotation(node, new ParseContext(null, m_annotationClass, m_bytecodeMunger));
        } else {
            // nested annotation
            handleNestedAnnotation(node, ctx);
        }
        return null;
    }

    public Object visit(ASTKeyValuePair node, Object data) {
        ParseContext ctx = (ParseContext)data;
        ctx.elementName = node.getKey();

        // set the expected type to use later for validation
        ctx.expectedType = getElementTypeFor(ctx.annotationType, ctx.elementName);

        node.jjtGetChild(0).jjtAccept(this, ctx);
        return null;
    }

    public Object visit(ASTIdentifier node, Object data) {
        ParseContext ctx = (ParseContext)data;
        String identifier = node.getValue();
        if (identifier.endsWith(".class")) {
            handleClassIdentifier(identifier, ctx);
        } else if (isJavaReferenceType(identifier)) {
            handleReferenceIdentifier(identifier, ctx);
        } else {
            throw new ParseException(
                    "unsupported format for java type or static reference (enum) [" +
                    ctx.elementName + "::" + identifier + "]"
            );
        }
        return null;
    }

    public Object visit(ASTBoolean node, Object data) {
        ParseContext ctx = (ParseContext)data;
        AnnotationValidator.validateBoolean(ctx);
        Boolean bool = Boolean.valueOf(node.getValue());
        ctx.munger.visit(ctx.elementName, bool);
        return null;
    }

    public Object visit(ASTChar node, Object data) {
        ParseContext ctx = (ParseContext)data;
        AnnotationValidator.validateCharacter(ctx);
        Character character = new Character(node.getValue().charAt(0));
        ctx.munger.visit(ctx.elementName, character);
        return null;
    }

    public Object visit(ASTString node, Object data) {
        ParseContext ctx = (ParseContext)data;
        AnnotationValidator.validateString(ctx);

        // the node contains the  \" string escapes
        String string;
        if (node.getValue().length() >= 2) {
            string = node.getValue().substring(1, node.getValue().length() - 1);
        } else {
            string = node.getValue();
        }

        ctx.munger.visit(ctx.elementName, string);
        return null;
    }

    public Object visit(ASTInteger node, Object data) {
        ParseContext ctx = (ParseContext)data;

        String value = node.getValue();
        char lastChar = value.charAt(value.length() - 1);

        Object integer;
        if ((lastChar == 'L') || (lastChar == 'l')) {
            integer = new Long(value.substring(0, value.length() - 1));
        } else if (value.length() > 9) {
            integer = new Long(value);
            AnnotationValidator.validateLong(ctx);
        } else {
            integer = new Integer(value);
            AnnotationValidator.validateInteger(ctx);
        }

        ctx.munger.visit(ctx.elementName, integer);
        return null;
    }

    public Object visit(ASTFloat node, Object data) {
        ParseContext ctx = (ParseContext)data;

        String value = node.getValue();
        char lastChar = value.charAt(value.length() - 1);
        Object decimalNumber;
        if ((lastChar == 'D') || (lastChar == 'd')) {
            decimalNumber = new Double(value.substring(0, value.length() - 1));
            AnnotationValidator.validateDouble(ctx);
        } else if ((lastChar == 'F') || (lastChar == 'f')) {
            decimalNumber = new Float(value.substring(0, value.length() - 1));
            AnnotationValidator.validateFloat(ctx);
        } else {
            decimalNumber = new Double(value);
            AnnotationValidator.validateDouble(ctx);
        }
        ctx.munger.visit(ctx.elementName, decimalNumber);
        return null;
    }

    public Object visit(ASTHex node, Object data) {
        throw new UnsupportedOperationException("hex numbers not yet supported");
    }

    public Object visit(ASTOct node, Object data) {
        throw new UnsupportedOperationException("octal numbers not yet supported");
    }

    public Object visit(ASTArray node, Object data) {
        ParseContext ctx = (ParseContext)data;
        AnnotationValidator.validateArray(ctx);

        AnnotationVisitor newMunger = ctx.munger.visitArray(ctx.elementName);
        ParseContext newCtx = new ParseContext(ctx.elementName, ctx.annotationType, ctx.expectedType, newMunger);

        // visit array elements
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, newCtx);
        }
        newMunger.visitEnd();

        return null;
    }


    /**
     * Returns the expected type for an annotation value.
     *
     * @param annotationInterface
     * @param valueName
     * @return the expected type
     */
    protected Class getElementTypeFor(final Class annotationInterface, final String valueName) {
        if (annotationInterface == null) {
            throw new IllegalArgumentException("annotation interface can not be null");
        }
        if (valueName == null) {
            throw new IllegalArgumentException("value name can not be null");
        }
        final Class type;
        try {
            type = annotationInterface.getDeclaredMethod(valueName, new Class[]{}).getReturnType();
        } catch (NoSuchMethodException e) {
            throw new ParseException(
                    "no method in annotation interface [" + annotationInterface.getName() +
                    "] matches the value name [" + valueName + "]"
            );
        }
        return type;
    }

    protected void handleAnnotation(final ASTAnnotation node, final ParseContext ctx) {
        int nr = node.jjtGetNumChildren();
        if (nr == 1) {
            Node childNode = node.jjtGetChild(0);
            if (childNode instanceof ASTKeyValuePair) {
                ctx.elementName = ((ASTKeyValuePair)childNode).getKey();
            } else {
                ctx.elementName = DEFAULT_VALUE_NAME;
            }
            ctx.expectedType = getElementTypeFor(ctx.annotationType, ctx.elementName);

            childNode.jjtAccept(this, ctx);
        } else {
            // key-value pairs
            for (int i = 0; i < nr; i++) {
                node.jjtGetChild(i).jjtAccept(this, ctx);
            }
        }
    }

    protected void handleNestedAnnotation(final ASTAnnotation node, final ParseContext ctx) {
        Class annotationType = getElementTypeFor(ctx.annotationType, ctx.elementName);
        if (annotationType.isArray()) {
            // if we have an array of annotations
            annotationType = annotationType.getComponentType();
        }

        AnnotationVisitor newMunger = ctx.munger.visitAnnotation(
                ctx.elementName, Type.getDescriptor(annotationType)
        );

        // create new context for this new annotation
        ParseContext newCtx = new ParseContext(ctx.elementName, annotationType, ctx.expectedType, newMunger);

        AnnotationValidator.validateAnnotation(newCtx);

        handleAnnotation(node, newCtx);
        newMunger.visitEnd();
    }

    protected Object handleClassIdentifier(final String identifier, final ParseContext ctx) {
        AnnotationValidator.validateClass(ctx);

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
                componentClass = Class.forName(componentClassName, false, ClassLoader.getSystemClassLoader());
            } catch (ClassNotFoundException e) {
                throw new ParseException(
                        "could not load class [" + componentClassName + "] due to: " + e.toString(), e
                );
            }
        }
        if (isComponentPrimitive) {
            if (dimension <= 0) {
                Type componentType = Type.getType(componentClass);
                ctx.munger.visit(ctx.elementName, componentType);
            } else {
                Class arrayClass = Array.newInstance(componentClass, new int[dimension]).getClass();
                Type componentType = Type.getType(arrayClass);
                ctx.munger.visit(ctx.elementName, componentType);
            }
        } else {
            String componentType = Type.getType(componentClass).getDescriptor();
            for (int i = 0; i < dimension; i++) {
                componentType = "[" + componentType;
            }
            Type type = Type.getType(componentType);
            ctx.munger.visit(ctx.elementName, type);
        }
        return null;
    }

    protected Object handleReferenceIdentifier(final String identifier, final ParseContext ctx) {
        AnnotationValidator.validateEnum(ctx);

        int index = identifier.lastIndexOf('.');
        String className = identifier.substring(0, index);
        String fieldName = identifier.substring(index + 1, identifier.length());
        try {
            Class clazz = Class.forName(className, false, ClassLoader.getSystemClassLoader());
            ctx.munger.visitEnum(ctx.elementName, Type.getDescriptor(clazz), fieldName);
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
        m_bytecodeMunger = bytecodeVisitor;
        m_annotationClass = annotationClass;
    }
}