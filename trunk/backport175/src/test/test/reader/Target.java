/**************************************************************************************
 * Copyright (c) Jonas Bon�r, Alexandre Vasseur. All rights reserved.                 *
 * http://backport175.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the LGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package test.reader;

/**
 * @test.reader.TestAnnotations.VoidTyped
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
     * @test.reader.TestAnnotations.VoidTyped
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
     * @test.reader.TestAnnotations.VoidTyped
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
     * @test.reader.TestAnnotations.VoidTyped
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
//    @Target.Test(test="test")
    public void method() {
    }

//    @java.lang.reader.Retention(java.lang.reader.RetentionPolicy.RUNTIME)
//    public static @interface Test {
//        String test();
//    }
}