/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.primitives;

/**
 * @test.primitives.Target$Long(v=123456789123456789, arr={1L, 2L, 3L, 4l})
 * @test.primitives.Target$Integer(v=1, arr={1, 2, 3, 4})
 * @test.primitives.Target.Double(v=1.123456789123456789D, arr={1.0D, 2.4D, 3.56d, 4.0D})
 * @test.primitives.Target.Float(v=1.0F, arr={1.1F, 2.3455f, 3.0F, 4F})
 * @test.primitives.Target.Boolean(v=true, arr={TRUE, false, true, FALSE})
 * @test.primitives.Target.Char(v='a', arr={'b', 'C', 'D', 'e'})
 */
public class Target {

    public static interface Long {
        long v();
        long[] arr();
    }
    public static interface Integer {
        int v();
        int[] arr();
    }
    public static interface Float {
        float v();
        float[] arr();
    }
    public static interface Double {
        double v();
        double[] arr();
    }
    public static interface Boolean {
        boolean v();
        boolean[] arr();
    }
    public static interface Char {
        char v();
        char[] arr();
    }
}