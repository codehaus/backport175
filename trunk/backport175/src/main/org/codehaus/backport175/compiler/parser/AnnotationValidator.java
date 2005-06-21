/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175.compiler.parser;

/**
 * Validates that the type for an annotation value that has been parsed is the correct type, e.g. has a method in the
 * annotation interface that has a return type that is of the same type.
 *
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class AnnotationValidator {

    /**
     * Validates a string value type.
     *
     * @param ctx
     */
    public static void validateString(final ParseContext ctx) {
        if (ctx.elementName == null) {
            return;
        }
        Class expectedType = getExpectedType(ctx);
        if (expectedType != String.class) {
            throw new AnnotationValidationException(createErrorMessage(ctx, String.class.getName()));
        }
    }

    /**
     * Validates a long value type.
     *
     * @param ctx
     */
    public static void validateLong(final ParseContext ctx) {
        if (ctx.elementName == null) {
            return;
        }
        Class expectedType = getExpectedType(ctx);
        if (expectedType != long.class) {
            throw new AnnotationValidationException(createErrorMessage(ctx, long.class.getName()));
        }
    }

    /**
     * Validates a int value type.
     *
     * @param ctx
     */
    public static void validateInteger(final ParseContext ctx) {
        if (ctx.elementName == null) {
            return;
        }
        Class expectedType = getExpectedType(ctx);
        // lets allow int upgraded to long type as well
        if (expectedType != int.class && expectedType != long.class) {
            throw new AnnotationValidationException(createErrorMessage(ctx, int.class.getName()));
        }
    }

    /**
     * Validates a double value type.
     *
     * @param ctx
     */
    public static void validateDouble(final ParseContext ctx) {
        if (ctx.elementName == null) {
            return;
        }
        Class expectedType = getExpectedType(ctx);
        if (expectedType != double.class) {
            throw new AnnotationValidationException(createErrorMessage(ctx, double.class.getName()));
        }
    }

    /**
     * Validates a float value type.
     *
     * @param ctx
     */
    public static void validateFloat(final ParseContext ctx) {
        if (ctx.elementName == null) {
            return;
        }
        Class expectedType = getExpectedType(ctx);
        if (expectedType != float.class) {
            throw new AnnotationValidationException(createErrorMessage(ctx, float.class.getName()));
        }
    }

    /**
     * Validates a char value type.
     *
     * @param ctx
     */
    public static void validateCharacter(final ParseContext ctx) {
        if (ctx.elementName == null) {
            return;
        }
        Class expectedType = getExpectedType(ctx);
        if (expectedType != char.class) {
            throw new AnnotationValidationException(createErrorMessage(ctx, char.class.getName()));
        }
    }

    /**
     * Validates a boolean value type.
     *
     * @param ctx
     */
    public static void validateBoolean(final ParseContext ctx) {
        if (ctx.elementName == null) {
            return;
        }
        Class expectedType = getExpectedType(ctx);
        if (expectedType != boolean.class) {
            throw new AnnotationValidationException(createErrorMessage(ctx, boolean.class.getName()));
        }
    }

    /**
     * Validates a boolean value type.
     *
     * @param ctx
     */
    public static void validateArray(final ParseContext ctx) {
        if (ctx.elementName == null) {
            return;
        }
        if (!ctx.expectedType.isArray()) {
            throw new AnnotationValidationException(createErrorMessage(ctx, "array type"));
        }
        if (ctx.expectedType.getComponentType().isArray()) {
            throw new AnnotationValidationException(
                    "multi-dimensional array types are not supported: " +
                    createErrorMessage(ctx, "multi-dimensional array")
            );
        }
    }

    /**
     * Validates an annotation value type.
     *
     * @param ctx
     */
    public static void validateAnnotation(final ParseContext ctx) {
        if (ctx.elementName == null) {
            return;
        }
        Class expectedType = getExpectedType(ctx);
        if (!expectedType.isInterface()) {
            throw new AnnotationValidationException(createErrorMessage(ctx, "annotation interface type"));
        }
    }

    /**
     * Validates a class value type.
     *
     * @param ctx
     */
    public static void validateClass(final ParseContext ctx) {
        if (ctx.elementName == null) {
            return;
        }
        Class expectedType = getExpectedType(ctx);
        if (expectedType != Class.class) {
            throw new AnnotationValidationException(createErrorMessage(ctx, Class.class.getName()));
        }
    }

    /**
     * Validates an enum value type.
     *
     * @param ctx
     */
    public static void validateEnum(final ParseContext ctx) {
        // TODO how to validate enum? needed? enum value can be of ANY type
    }

    /**
     * Returns the expected type, returns component elements for arrays.
     *
     * @param ctx
     * @return
     */
    private static Class getExpectedType(final ParseContext ctx) {
        Class expectedType = ctx.expectedType;
        if (expectedType.isArray()) {
            expectedType = expectedType.getComponentType();
        }
        return expectedType;
    }

    /**
     * Creates a generic error message.
     *
     * @param ctx
     * @param actualTypeName
     * @return the message
     */
    private static String createErrorMessage(final ParseContext ctx, final String actualTypeName) {
        return new StringBuffer().append("value [").append(ctx.elementName).append("] in annotation [").
                append(ctx.annotationType.getName()).append("] does not have correct type: expected [")
                .append(ctx.expectedType.getName()).append("] was [").append(actualTypeName).append("]").toString();
    }
}
