/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.nested;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 * 
 * @test.nested.Target.OneLevel( @test.nested.Target.Simple({1, 1}) )
 * @test.nested.Target.TwoLevel( @test.nested.Target.OneLevel( @test.nested.Target.Simple({1, 1}) ) )
 * @test.nested.Target.ThreeLevel( @test.nested.Target.TwoLevel( @test.nested.Target.OneLevel( @test.nested.Target.Simple({1, 1}) ) ) )
 *
 * @test.nested.Target.OneLevelArr( {@test.nested.Target.Simple( {1, 1} )} )
 * @test.nested.Target.TwoLevelArr( {@test.nested.Target.OneLevelArr( {@test.nested.Target.Simple( {1, 1} )} )} )
 * @test.nested.Target.ThreeLevelArr( {@test.nested.Target.TwoLevelArr( {@test.nested.Target.OneLevelArr( {@test.nested.Target.Simple({1, 1} )} )} )} )
 */
public class Target {

    public static interface Simple {
        int[] value();
    }

    public static interface OneLevel {
        Simple value();
    }
    public static interface TwoLevel {
        OneLevel value();
    }
    public static interface ThreeLevel {
        TwoLevel value();
    }

    public static interface OneLevelArr {
        Simple[] value();
    }
    public static interface TwoLevelArr {
        OneLevelArr[] value();
    }
    public static interface ThreeLevelArr {
        TwoLevelArr[] value();
    }
}