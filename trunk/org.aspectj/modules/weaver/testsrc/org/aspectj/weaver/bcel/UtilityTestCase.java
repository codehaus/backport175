/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;
import java.io.*;

import junit.framework.TestCase;

import org.apache.bcel.util.ClassPath;

public class UtilityTestCase extends TestCase {

    public UtilityTestCase(String name) {
        super(name);
    }

    public void disassembleTest(String name) throws IOException {
        BcelWorld world = new BcelWorld("../bcweaver/bin");
        
        LazyClassGen clazz = new LazyClassGen((BcelObjectType) world.resolve(name));
        clazz.print();
        System.out.println();
    }


    public void testHelloWorld() throws IOException {
        disassembleTest("Test");
    }
    public void testFancyHelloWorld() throws IOException {
        disassembleTest("FancyHelloWorld");
    }
//    public void testSwitchy() throws IOException {
//        disassembleTest("TestSwitchy");
//    }
    
    public static void main(String[] args) throws IOException {
    	BcelWorld world = new BcelWorld();
        LazyClassGen clazz = new LazyClassGen((BcelObjectType)world.resolve(args[0]));
        clazz.print();
    } 
}

