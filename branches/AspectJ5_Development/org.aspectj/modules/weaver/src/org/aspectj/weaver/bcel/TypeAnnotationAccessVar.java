/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;

/**
 */
public class TypeAnnotationAccessVar extends BcelVar {


	private Member stackField;
	private int index;
	BcelVar target;

	/**
	 * @param type The type to convert to from Object
	 * @param stackField the member containing the CFLOW_STACK_TYPE
	 * @param index yeah yeah
	 */
	public TypeAnnotationAccessVar(ResolvedTypeX type, Member stackField, int index) {
		super(type, 0);
		this.stackField = stackField;
		this.index = index;
	}
	
	public TypeAnnotationAccessVar(ResolvedTypeX type,BcelVar theTargetIsStoredHere) {
		super(type,0);
		target = theTargetIsStoredHere;
	}

	public String toString() {
		return "TypeAnnotationAccessVar(" + getType() + " " + stackField + "." + index + ")";
	}

    public Instruction createLoad(InstructionFactory fact) {
		throw new RuntimeException("unimplemented");
    }
    public Instruction createStore(InstructionFactory fact) {
    	throw new RuntimeException("unimplemented");
    }

    public InstructionList createCopyFrom(InstructionFactory fact, int oldSlot) {
        throw new RuntimeException("unimplemented");
    }
    
	public void appendLoad(InstructionList il, InstructionFactory fact) {
		il.append(createLoadInstructions(getType(), fact));
	}

	public InstructionList createLoadInstructions(ResolvedTypeX toType, InstructionFactory fact) {
		InstructionList il = new InstructionList();
		Type jlClass = BcelWorld.makeBcelType(TypeX.JAVA_LANG_CLASS);
		Type jlaAnnotation = BcelWorld.makeBcelType(TypeX.forSignature("Ljava.lang.annotation.Annotation;"));
		il.append(target.createLoad(fact));
        il.append(fact.createInvoke("java/lang/Object","getClass",jlClass,new Type[]{},Constants.INVOKEVIRTUAL));
		il.append(fact.createConstant(new ObjectType(toType.getClassName())));
		il.append(fact.createInvoke("java/lang/Class","getAnnotation",jlaAnnotation,new Type[]{jlClass},Constants.INVOKEVIRTUAL));
		il.append(Utility.createConversion(fact,jlaAnnotation,BcelWorld.makeBcelType(toType)));
		return il;
		
	}

	public void appendLoadAndConvert(
		InstructionList il,
		InstructionFactory fact,
		ResolvedTypeX toType) {
		il.append(createLoadInstructions(toType, fact));				

	}

	public void insertLoad(InstructionList il, InstructionFactory fact) {
		il.insert(createLoadInstructions(getType(), fact));
	}

}