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
     * @param annotationInterface
     * @param valueName
     */
    public static void validateString(final Class annotationInterface, final String valueName) {
        Class expectedType = getExpectedTypeFor(annotationInterface, valueName);
        if (expectedType != String.class) {
            throw new AnnotationValidationException(
                    createErrorMessage(annotationInterface, valueName, String.class.getName(), expectedType)
            );
        }
    }

    /**
     * Validates a long value type.
     *
     * @param annotationInterface
     * @param valueName
     */
    public static void validateLong(final Class annotationInterface, final String valueName) {
        Class expectedType = getExpectedTypeFor(annotationInterface, valueName);
        if (expectedType != long.class) {
            throw new AnnotationValidationException(
                    createErrorMessage(annotationInterface, valueName, long.class.getName(), expectedType)
            );
        }
    }

    /**
     * Validates an integer value type.
     *
     * @param annotationInterface
     * @param valueName
     */
    public static void validateInteger(final Class annotationInterface, final String valueName) {
        Class expectedType = getExpectedTypeFor(annotationInterface, valueName);
        if (expectedType != int.class) {
            throw new AnnotationValidationException(
                    createErrorMessage(annotationInterface, valueName, int.class.getName(), expectedType)
            );
        }
    }

    /**
     * Validates a double value type.
     *
     * @param annotationInterface
     * @param valueName
     */
    public static void validateDouble(final Class annotationInterface, final String valueName) {
        Class expectedType = getExpectedTypeFor(annotationInterface, valueName);
        if (expectedType != double.class) {
            throw new AnnotationValidationException(
                    createErrorMessage(annotationInterface, valueName, double.class.getName(), expectedType)
            );
        }
    }

    /**
     * Validates a float value type.
     *
     * @param annotationInterface
     * @param valueName
     */
    public static void validateFloat(final Class annotationInterface, final String valueName) {
        Class expectedType = getExpectedTypeFor(annotationInterface, valueName);
        if (expectedType != int.class) {
            throw new AnnotationValidationException(
                    createErrorMessage(annotationInterface, valueName, float.class.getName(), expectedType)
            );
        }
    }

    /**
     * Validates a character value type.
     *
     * @param annotationInterface
     * @param valueName
     */
    public static void validateCharacter(final Class annotationInterface, final String valueName) {
        Class expectedType = getExpectedTypeFor(annotationInterface, valueName);
        if (expectedType != char.class) {
            throw new AnnotationValidationException(
                    createErrorMessage(annotationInterface, valueName, char.class.getName(), expectedType)
            );
        }
    }

    /**
     * Validates a boolean value type.
     *
     * @param annotationInterface
     * @param valueName
     */
    public static void validateBoolean(final Class annotationInterface, final String valueName) {
        Class expectedType = getExpectedTypeFor(annotationInterface, valueName);
        if (expectedType != boolean.class) {
            throw new AnnotationValidationException(
                    createErrorMessage(annotationInterface, valueName, boolean.class.getName(), expectedType)
            );
        }
    }

    /**
     * Validates an array value type.
     *
     * @param annotationInterface
     * @param valueName
     */
    public static void validateArray(final Class annotationInterface, final String valueName) {
        Class expectedType = getExpectedTypeFor(annotationInterface, valueName);
        if (expectedType.isArray()) {
            throw new AnnotationValidationException(
                    createErrorMessage(annotationInterface, valueName, "array type", expectedType)
            );
        }
    }

    /**
     * Validates an annotation value type.
     *
     * @param annotationInterface
     * @param valueName
     */
    public static void validateAnnotation(final Class annotationInterface, final String valueName) {
        Class expectedType = getExpectedTypeFor(annotationInterface, valueName);
        if (expectedType.isInterface()) { // TODO can we do better than this?
            throw new AnnotationValidationException(
                    createErrorMessage(annotationInterface, valueName, "annotation interface type", expectedType)
            );
        }
    }

    /**
     * Validates a class value type.
     *
     * @param annotationInterface
     * @param valueName
     */
    public static void validateClass(final Class annotationInterface, final String valueName) {
        Class expectedType = getExpectedTypeFor(annotationInterface, valueName);
        if (expectedType != Class.class) {
            throw new AnnotationValidationException(
                    createErrorMessage(annotationInterface, valueName, Class.class.getName(), expectedType)
            );
        }
    }

    /**
     * Validates an enum value type.
     *
     * @param annotationInterface
     * @param valueName
     */
    public static void validateEnum(final Class annotationInterface, final String valueName) {
        // TODO how to validate enum? needed? enum value can be of ANY type
    }

    /**
     * Returns the expected type for an annotation value.
     *
     * @param annotationInterface
     * @param valueName
     * @return the expected type
     */
    private static Class getExpectedTypeFor(final Class annotationInterface, final String valueName) {
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

    /**
     * Creates a generic error message.
     *
     * @param annotationInterface
     * @param valueName
     * @param expectedType
     * @param actualType
     * @return the message
     */
    private static String createErrorMessage(
            final Class annotationInterface,
            final String valueName,
            final String expectedType,
            final Class actualType) {
        return new StringBuffer().append("value [").append(valueName).append("] in annotation [").
                append(annotationInterface.getName()).append("] does not have correct type: expected [")
                .append(expectedType).append("] was [").append(actualType.getName()).append("]").toString();
    }
}
