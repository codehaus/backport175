/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package test.nested;

import org.codehaus.backport175.reader.Annotations;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:jboner@codehaus.org">Jonas Bonér</a>
 */
public class NestedTest extends TestCase {

    public NestedTest(String name) {
        super(name);
    }

    public void testOneLevel() {
        Target.OneLevel annotation = (Target.OneLevel)Annotations.getAnnotation(Target.OneLevel.class, Target.class);
        Target.Simple simple = annotation.value();
        assertEquals(1, simple.value()[0]);
        assertEquals(1, simple.value()[1]);
    }

    public void testTwoLevel() {
        Target.TwoLevel annotation = (Target.TwoLevel)Annotations.getAnnotation(Target.TwoLevel.class, Target.class);
        Target.OneLevel one = annotation.value();
        Target.Simple simple = one.value();
        assertEquals(1, simple.value()[0]);
        assertEquals(1, simple.value()[1]);
    }

    public void testThreeLevel() {
        Target.ThreeLevel annotation = (Target.ThreeLevel)Annotations.getAnnotation(Target.TwoLevel.class, Target.class);
        Target.TwoLevel two = annotation.value();
        Target.OneLevel one = two.value();
        Target.Simple simple = one.value();
        assertEquals(1, simple.value()[0]);
        assertEquals(1, simple.value()[1]);
    }

    public void testOneLevelArr() {
        Target.OneLevelArr annotation = (Target.OneLevelArr)Annotations.getAnnotation(Target.OneLevelArr.class, Target.class);
        Target.Simple[] simple = annotation.value();
        assertEquals(1, simple[0].value()[0]);
        assertEquals(1, simple[0].value()[1]);
    }

    public void testTwoLevelArr() {
        Target.TwoLevelArr annotation = (Target.TwoLevelArr)Annotations.getAnnotation(Target.TwoLevelArr.class, Target.class);
        Target.OneLevelArr[] one = annotation.value();
        Target.Simple[] simple = one[0].value();
        assertEquals(1, simple[0].value()[0]);
        assertEquals(1, simple[0].value()[1]);
    }

    public void testThreeLevelArr() {
        Target.ThreeLevelArr annotation = (Target.ThreeLevelArr)Annotations.getAnnotation(Target.ThreeLevelArr.class, Target.class);
        Target.TwoLevelArr[] two = annotation.value();
        Target.OneLevelArr[] one = two[0].value();
        Target.Simple[] simple = one[0].value();
        assertEquals(1, simple[0].value()[0]);
        assertEquals(1, simple[0].value()[1]);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(NestedTest.class);
    }
}
