/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.reader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

/**
 * @test.TestAnnotations.VoidTyped
 * @Simple(val="foo", s="bar")
 * @DefaultString("hello")
 * @StringArray(ss={"hello", "world"})
 * @LongArray(l={1l, 2l, 6l})
 * @AliasedLongArray(l={1l, 2l, 6l})
 * @NestedAnnotation(ann=@Simple(val="foo"))
 * @NestedAnnotationArray(annArr={@Simple(val="foo"), @Simple(val="bar")})
 * @Complex(i=3, doubleArr={1.1D, 1.2D, 1234.123456d}, type=java.lang.String[][].class)
 */
public class Target {

    /**
     * @test.TestAnnotations.VoidTyped
     * @Simple(val="foo", s="bar")
     * @DefaultString("hello")
     * @StringArray(ss={"hello", "world"})
     * @LongArray(l={1l, 2l, 6l})
     * @NestedAnnotation(ann=@Simple(val="foo"))
     * @NestedAnnotationArray(annArr={@Simple(val="foo"), @Simple(val="bar")})
     * @Complex(
     *      i=111,
     *      doubleArr={1.1D, 2.2D, 3.3D, 4.4D},
     *      type=double[][][].class,
     *      enumeration=org.codehaus.backport175.reader.bytecode.AnnotationElement$Type.ANNOTATION,
     *      typeArr={test.reader.Target[].class, test.reader.Target.class}
     * )
     */
    public Target() {
    }

    /**
     * @test.TestAnnotations.VoidTyped
     * @Simple(val="foo", s="bar")
     * @DefaultString("hello")
     * @StringArray(ss={"hello", "world"})
     * @LongArray(l={1l, 2l, 6l})
     * @Complex(i=3, doubleArr={1.1D, 1.2D, 1234.123456d}, type=int.class)
     * @NestedAnnotation(ann=@Simple(val="foo"))
     * @NestedAnnotationArray(annArr={@Simple(val="foo"), @Simple(val="bar")})
     */
    private String field;

    /**
     * @test.TestAnnotations.VoidTyped
     * @Simple(val="foo", s="bar")
     * @DefaultString("hello")
     * @StringArray(ss={"hello", "world"})
     * @LongArray(l={1l, 2l, 6l})
     * @NestedAnnotation(ann=@Simple(val="foo"))
     * @NestedAnnotationArray(annArr={@Simple(val="foo"), @Simple(val="bar")})
     * @Complex(
     *      i=111,
     *      doubleArr={1.1D, 2.2D, 3.3D, 4.4D},
     *      type=double[][][].class,
     *      enumeration=org.codehaus.backport175.reader.bytecode.AnnotationElement$Type.ANNOTATION,
     *      typeArr={test.reader.Target[].class, test.reader.Target.class}
     * )
     */
    public void method() {
    }


    public static final Field FIELD;
    public static final Method METHOD;
    public static final Constructor CONSTRUCTOR;

    static {
        try {
            FIELD = Target.class.getDeclaredField("field");
            METHOD = Target.class.getDeclaredMethod("method", new Class[]{});
            CONSTRUCTOR = Target.class.getDeclaredConstructor(new Class[]{});
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }


}