/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *      (Andy Clement)
 *******************************************************************************/

package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;

/**
 * This type munger will modify a given class (see the munge() method) to include
 * a field representing a CflowCounter object.
 */
public class BcelCflowCounterFieldAdder extends BcelTypeMunger {
	private ResolvedMember cflowCounterField;
	
	public BcelCflowCounterFieldAdder(ResolvedMember cflowCounterField) {
		super(null,(ResolvedTypeX)cflowCounterField.getDeclaringType());
		this.cflowCounterField = cflowCounterField;
	}

	public boolean munge(BcelClassWeaver weaver) {
		LazyClassGen gen = weaver.getLazyClassGen();
		
		// Only munge one type!
		if (!gen.getType().equals(cflowCounterField.getDeclaringType())) return false;

		// Create the field declaration.
		// Something like: "public static final CflowCounter ajc$cflowCounter$0;"
		Field f = new FieldGen(cflowCounterField.getModifiers(),
		    BcelWorld.makeBcelType(cflowCounterField.getReturnType()),
		    cflowCounterField.getName(),
    		gen.getConstantPoolGen()).getField();
		
    	gen.addField(f,getSourceLocation());

    	// Modify the ajc$preClinit() method to initialize it.
    	// Something like: "ajc$cflowCounter$0 = new CflowCounter();"
		LazyMethodGen clinit = gen.getAjcPreClinit(); //StaticInitializer();
		InstructionList setup = new InstructionList();
		InstructionFactory fact = gen.getFactory();

		setup.append(fact.createNew(new ObjectType(NameMangler.CFLOW_COUNTER_TYPE)));
		setup.append(InstructionFactory.createDup(1));
		setup.append(fact.createInvoke(
			NameMangler.CFLOW_COUNTER_TYPE, 
			"<init>", 
			Type.VOID, 
			new Type[0], 
			Constants.INVOKESPECIAL));


		setup.append(Utility.createSet(fact, cflowCounterField));
		clinit.getBody().insert(setup);

		return true;
	}


	public ResolvedMember getMatchingSyntheticMember(Member member) {
		return null;
	}

	public ResolvedMember getSignature() {
		return cflowCounterField;
	}

	public boolean matches(ResolvedTypeX onType) {
		return onType.equals(cflowCounterField.getDeclaringType());
	}

}
