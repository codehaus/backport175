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


package org.aspectj.weaver.patterns;

import java.io.*;

import junit.framework.TestCase;

import org.aspectj.weaver.*;
import org.aspectj.weaver.bcel.*;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.util.FuzzyBoolean;

public class WithinTestCase extends TestCase {		

	World world = new BcelWorld();

	public WithinTestCase(String name) {
		super(name);
	}
	
	public void testMatch() throws IOException {
		Shadow getOutFromArrayList = new TestShadow(
			Shadow.FieldGet, 
			Member.fieldFromString("java.io.PrintStream java.lang.System.out"),
			TypeX.forName("java.util.ArrayList"),
			world);

		checkMatch(makePointcut("within(*)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.*)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.lang.*)"), getOutFromArrayList, FuzzyBoolean.NO);
		checkMatch(makePointcut("within(java.util.List+)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.uti*.List+)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.uti*..*)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.*List)"), getOutFromArrayList, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.List*)"), getOutFromArrayList, FuzzyBoolean.NO);
		
		
		Shadow getOutFromEntry = new TestShadow(
			Shadow.FieldGet, 
			Member.fieldFromString("java.io.PrintStream java.lang.System.out"),
			TypeX.forName("java.util.Map$Entry"),
			world);
			
		checkMatch(makePointcut("within(*)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.*)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.Map.*)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util..*)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.util.Map..*)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.lang.*)"), getOutFromEntry, FuzzyBoolean.NO);
		checkMatch(makePointcut("within(java.util.List+)"), getOutFromEntry, FuzzyBoolean.NO);
		checkMatch(makePointcut("within(java.util.Map+)"), getOutFromEntry, FuzzyBoolean.YES);
		checkMatch(makePointcut("within(java.lang.Object+)"), getOutFromEntry, FuzzyBoolean.YES);
		
		//this is something we should in type patterns tests
		//checkMatch(makePointcut("within(*List)"), getOut, FuzzyBoolean.NO);

	}

	public Pointcut makePointcut(String pattern) {
		Pointcut pointcut0 = Pointcut.fromString(pattern);
		
		Bindings bindingTable = new Bindings(0);
        IScope scope = new SimpleScope(world, FormalBinding.NONE);
        
        pointcut0.resolveBindings(scope, bindingTable);		
		Pointcut pointcut1 = pointcut0;
		return pointcut1.concretize1(null, new IntMap());
	}

	
	private void checkMatch(Pointcut p, Shadow s, FuzzyBoolean shouldMatch) throws IOException {
		FuzzyBoolean doesMatch = p.match(s);
		assertEquals(p + " matches " + s, shouldMatch, doesMatch);
		checkSerialization(p);
	}
	
	private void checkSerialization(Pointcut p) throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bo);
		p.write(out);
		out.close();
		
		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
		DataInputStream in = new DataInputStream(bi);
		Pointcut newP = Pointcut.read(in, null);
		
		assertEquals("write/read", p, newP);	
	}
	
}
