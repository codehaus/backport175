/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.filtering;

public class Target {

    /**
     * @test.filtering.MemberFilteringTest.A
     */
    public Target() {
    }

    /**
     * @test.filtering.MemberFilteringTest.B
     */
    public Target(int[][] i, Object[] o, boolean b) {
    }

    /**
     * @test.filtering.MemberFilteringTest.A
     */
    void A(String s, int i, double d, float f, byte b, char c, short t, long l, boolean bool) {
    }

    /**
     * @test.filtering.MemberFilteringTest.B
     */
    void B(String s, int i, double d, float f, byte b, char c, short t, long l, boolean bool) {
    }

    /**
     * @test.filtering.MemberFilteringTest.A
     */
    public String[] A;

    /**
     * @test.filtering.MemberFilteringTest.B
     */
    public int[][] B;
}