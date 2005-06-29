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
import org.codehaus.backport175.compiler.SourceLocation;
import org.codehaus.backport175.DefaultValue;
import org.objectweb.asm.Type;
import org.objectweb.asm.AnnotationVisitor;

import java.lang.reflect.Array;

/**
 * The annotation visitor. Visits the annotation elements and adds them to the bytecode of the class.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér </a>
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
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
     * The loader of the annotated class
     */
    protected ClassLoader m_annotatedClassClassLoader;

    /**
     * The top level annotation bytecode munger.
     */
    protected AnnotationVisitor m_bytecodeMunger;

    /**
     * Parses the raw annotation.
     *
     * @param bytecodeMunger
     * @param rawAnnotation
     */
    public static void parse(final AnnotationVisitor bytecodeMunger, final RawAnnotation rawAnnotation, final ClassLoader annotatedClassClassLoader) {
        parse(bytecodeMunger, rawAnnotation, null, annotatedClassClassLoader);
    }

    /**
     * Parses the raw annotation for an annotation default value, whose type checking depends
     * on the annotation element method desc if specified.
     *
     * @param bytecodeMunger
     * @param rawAnnotation
     * @param desc expected type for annotation default value. If null, assume we visit a regular annotation
     */
    public static void parse(final AnnotationVisitor bytecodeMunger, final RawAnnotation rawAnnotation, String desc, final ClassLoader annotatedClassClassLoader) {
        final String interfaceName = rawAnnotation.getAnnotationClass().getName().replace('/', '.');
        final String rawAnnotationValue = rawAnnotation.getValue();

        final StringBuffer representation = new StringBuffer("@");
        representation.append(interfaceName).append('(');
        if (rawAnnotationValue != null) {
            representation.append(rawAnnotationValue);
        }
        representation.append(')');

        try {
            final AnnotationParser annotationParser;
            if (desc != null) {
                annotationParser = new AnnotationDefaultValueParser(
                    bytecodeMunger,
                    rawAnnotation.getAnnotationClass(),
                    desc,
                    annotatedClassClassLoader
                );
            } else {
                annotationParser = new AnnotationParser(
                        bytecodeMunger,
                        rawAnnotation.getAnnotationClass(),
                        annotatedClassClassLoader
                );
            }
            annotationParser.visit(PARSER.parse(representation.toString().replace('\n', ' ')), null);
        } catch (AnnotationValidationException ave) {
            // update the source location
            ave.setLocation(SourceLocation.render(rawAnnotation));
            throw ave;
        } catch (Throwable e) {
            // parser grammar error
            throw new ParseException(
                    "cannot parse annotation [" + representation.toString() + "] due to: " + e.getMessage(),
                    e,
                    SourceLocation.render(rawAnnotation)
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

        ctx.munger.visit(ctx.elementName, Boolean.valueOf(node.getValue()));
        return null;
    }

    public Object visit(ASTChar node, Object data) {
        ParseContext ctx = (ParseContext)data;
        AnnotationValidator.validateCharacter(ctx);

        char[] value = node.getValue().trim().toCharArray();
        Character character;
        if (value.length == 1) {
            character = new Character(value[0]);
        } else if (value.length == 3 && value[0] == '\'' && value[2] == '\'') {
            character = new Character(value[1]);
        } else {
            throw new ParseException("could not parse character [" + value + "]");
        }

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
        string = unescapeQuotes(string);

        ctx.munger.visit(ctx.elementName, string);
        return null;
    }

    public Object visit(ASTInteger node, Object data) {
        ParseContext ctx = (ParseContext)data;
        byte b = 127;
        //short s = java.lang.Integer.MAX_VALUE;
        String value = node.getValue();
        char lastChar = value.charAt(value.length() - 1);

        Object boxed;
        if ((lastChar == 'L') || (lastChar == 'l')) {
            boxed = new Long(value.substring(0, value.length() - 1));
        } else {
            boxed = new Long(value);
            long lValue = ((Long)boxed).longValue();
            if (lValue > Integer.MAX_VALUE || lValue < Integer.MIN_VALUE) {
                // a real long
                AnnotationValidator.validateLong(ctx);
            } else if (lValue > Short.MAX_VALUE || lValue < Short.MIN_VALUE) {
                // an int is fine
                AnnotationValidator.validateInteger(ctx);
                // only downgrade if expects an int
                if (ctx.expectedType == int.class) {
                    boxed = new Integer(value);
                }
            } else if (lValue > Byte.MAX_VALUE || lValue < Byte.MIN_VALUE) {
                // a short is fine
                AnnotationValidator.validateShort(ctx);
                // only downgrade if expects a short
                if (ctx.expectedType == int.class) {
                    boxed = new Integer(value);
                } else if (ctx.expectedType == short.class) {
                    boxed = new Short(value);
                }
            } else {
                // lets use a byte
                AnnotationValidator.validateByte(ctx);
                // only downgrade if expects a byte
                if (ctx.expectedType == int.class) {
                    boxed = new Integer(value);
                } else if (ctx.expectedType == short.class) {
                    boxed = new Short(value);
                } else if (ctx.expectedType == byte.class) {
                    boxed = new Byte(value);
                }
            }
        }

        System.err.println("?????? " + ctx.expectedType.toString() + " : " + value);
//        else {
//            boxed = new Integer(value);
//            AnnotationValidator.validateInteger(ctx);
//            // lets upgrade the int if we expect a long (else proxy will give us a null)
//            if (ctx.expectedType == long.class) {
//                boxed = new Long(value);
//            }
//        }

        ctx.munger.visit(ctx.elementName, boxed);
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
        ParseContext newCtx = new ParseContext(ctx.elementName, ctx.annotationType, ctx.expectedType.getComponentType(), newMunger);

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

        AnnotationParser newParser = new AnnotationParser(newMunger, annotationType, m_annotatedClassClassLoader);
        newParser.handleAnnotation(node, newCtx);

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
                componentClass = forName(componentClassName, m_annotatedClassClassLoader);
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
            Class clazz = forName(className, m_annotatedClassClassLoader);
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
    private AnnotationParser(final AnnotationVisitor bytecodeVisitor, final Class annotationClass, final ClassLoader annotatedClassClassLoader) {
        m_bytecodeMunger = bytecodeVisitor;
        m_annotationClass = annotationClass;
        m_annotatedClassClassLoader = annotatedClassClassLoader;
    }

    /**
     * A specific parser that does type checking based on the annotation element method to handle
     * annotation default value
     *
     * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
     */
    private static class AnnotationDefaultValueParser extends AnnotationParser {

        /**
         * The annotation value expected type descripor
         */
        private final String m_typeDesc;

        /**
         * The annotation value expected class, lazily set upon first lookup
         */
        private Class m_typeClassLazy = null;

        /**
         * Constructor
         *
         * @param bytecodeVisitor
         * @param annotationClass
         * @param desc
         */
        private AnnotationDefaultValueParser(final AnnotationVisitor bytecodeVisitor, final Class annotationClass, final String desc, final ClassLoader annotatedClassClassLoader) {
            super(bytecodeVisitor, annotationClass, annotatedClassClassLoader);
            m_typeDesc = desc;
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
            if (!valueName.equals(DEFAULT_VALUE_NAME)) {
                throw new IllegalArgumentException("value name must be 'value' for annotation default value");
            }
            if (m_typeClassLazy == null) {
                m_typeClassLazy = getClassFromTypeDesc(m_typeDesc, m_annotatedClassClassLoader);
            }

            return m_typeClassLazy;
        }
    }

    /**
     * Returns the class for a given type descriptor, looking from the annotationClass class loader
     *
     * @param typeDesc
     * @param loader
     * @return
     */
    private static Class getClassFromTypeDesc(String typeDesc, ClassLoader loader) {
        Type type = Type.getType(typeDesc);
        int dimension = typeDesc.startsWith("[")?type.getDimensions():0;
        Type componentType = typeDesc.startsWith("[")?type.getElementType():type;

        Class componentClass;
        if (componentType.equals(Type.LONG_TYPE)) {
            componentClass = long.class;
        } else if (componentType.equals(Type.INT_TYPE)) {
            componentClass = int.class;
        } else if (componentType.equals(Type.SHORT_TYPE)) {
            componentClass = short.class;
        } else if (componentType.equals(Type.DOUBLE_TYPE)) {
            componentClass = double.class;
        } else if (componentType.equals(Type.FLOAT_TYPE)) {
            componentClass = float.class;
        } else if (componentType.equals(Type.BYTE_TYPE)) {
            componentClass = byte.class;
        } else if (componentType.equals(Type.CHAR_TYPE)) {
            componentClass = char.class;
        } else if (componentType.equals(Type.BOOLEAN_TYPE)) {
            componentClass = boolean.class;
        } else if (componentType.equals(Type.getType(String.class))) {
            componentClass = String.class;
        } else {
            try {
                componentClass = forName(componentType.getClassName(), loader);
            } catch (ClassNotFoundException e) {
                throw new ParseException("could not load class for type [" + typeDesc + "] due to: " + e.toString(), e);
            }
        }

        if (dimension <= 0) {
            return componentClass;
        } else {
            Class arrayClass = Array.newInstance(componentClass, new int[dimension]).getClass();
            return arrayClass;
        }
    }

    /**
     * Do a Class.forName, from the annotation class loader or the thread class loader
     * if the annotation belongs to boot class loader.
     *
     * @param name the class name to load
     * @param loader the loader to look from
     * @return
     */
    private static Class forName(String name, ClassLoader loader) throws ClassNotFoundException {
        String lookupName = name;
        ClassNotFoundException firstNfe = null;
        while (true) {
            try {
                return Class.forName(lookupName, false, loader);
            } catch (ClassNotFoundException nfe) {
                if (firstNfe == null) firstNfe = nfe;
                int index = lookupName.lastIndexOf('.');
                if (index > 0) {
                    char[] lookup = new char[lookupName.length()];
                    lookupName.getChars(0, index, lookup, 0);
                    lookup[index] = '$';
                    lookupName.getChars(index+1, lookup.length, lookup, index+1);
                    lookupName = new String(lookup);
                } else {
                    throw firstNfe;
                }
            }
        }
    }

    /**
     * Unescaped escaped double quotes
     *  
     * @param string
     * @return
     */
    private static String unescapeQuotes(String string) {
        final int index = string.indexOf("\\\"");
        if (index >= 0) {
            char[] newString = new char[string.length()-1];
            int j = 0;
            for (int i = 0; i < string.length(); i++) {
                if (i == index) {
                    newString[j] = '\"';
                    i++;
                } else {
                    newString[j] = string.charAt(i);
                }
                j++;
            }
            return unescapeQuotes(new String(newString));
        } else {
            return string;
        }
    }

}