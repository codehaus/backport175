/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://backport175.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.annotation;

/**
 * @test.annotation.Annotations.VoidTyped
 * @Simple(val="foo", s="bar")
 * @DefaultString("hello")
 * @StringArray(ss={"hello", "world"})
 * @LongArray(l={1l, 2l, 6l})
 * @NestedAnnotation(ann=@Simple(val="foo"))
 * @NestedAnnotationArray(annArr={@Simple(val="foo"), @Simple(val="bar")})
 * @Complex(i=3, doubleArr={1.1D, 1.2D, 1234.123456d}, type=java.lang.String[][].class)
 */
public class Target {

    /**
     * @test.annotation.Annotations.VoidTyped
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
     *      typeArr={test.annotation.Target[].class, test.annotation.Target.class}
     * )
     */
    public Target() {
    }

    /**
     * @test.annotation.Annotations$VoidTyped
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
     * @test.annotation.Annotations$VoidTyped
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
     *      typeArr={test.annotation.Target[].class, test.annotation.Target.class}
     * )
     */
//    @Target.Test(test="test")
    public void method() {
    }

//    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
//    public static @interface Test {
//        String test();
//    }
}