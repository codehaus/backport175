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

import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.ast.Var;

/**
 * XXX Erik and I need to discuss this hierarchy.  Having FieldRef
 * extend Var is convenient, but hopefully there's a better design.
 * 
 * This is always a static reference.
 */
public class BcelFieldRef extends BcelVar {

	private String className, fieldName;

	public BcelFieldRef(ResolvedTypeX type, String className, String fieldName) {
		super(type, 0);
		this.className = className;
		this.fieldName = fieldName;
	}

	public String toString() {
		return "BcelFieldRef(" + getType() + " " + className + "." + fieldName + ")";
	}

    //public int getSlot() { return slot; }

    public Instruction createLoad(InstructionFactory fact) {
    	return fact.createFieldAccess(className, fieldName, 
    			BcelWorld.makeBcelType(getType()), Constants.GETSTATIC);
    }
    public Instruction createStore(InstructionFactory fact) {
    	return fact.createFieldAccess(className, fieldName, 
    			BcelWorld.makeBcelType(getType()), Constants.PUTSTATIC);
    }

    public InstructionList createCopyFrom(InstructionFactory fact, int oldSlot) {
        throw new RuntimeException("unimplemented");
    }
    
    // this is an array var
//    void appendConvertableArrayLoad(
//        InstructionList il,
//        InstructionFactory fact, 
//        int index,
//        ResolvedTypeX convertTo)
//    {
//        ResolvedTypeX convertFromType = getType().getResolvedComponentType();
//        appendLoad(il, fact);
//        il.append(Utility.createConstant(fact, index));
//        il.append(fact.createArrayLoad(BcelWorld.makeBcelType(convertFromType)));
//        Utility.appendConversion(il, fact, convertFromType, convertTo);
//    }
//
//    void appendConvertableArrayStore(
//        InstructionList il, 
//        InstructionFactory fact, 
//        int index,
//        BcelFieldRef storee) 
//    {
//        ResolvedTypeX convertToType = getType().getResolvedComponentType();
//        appendLoad(il, fact);
//        il.append(Utility.createConstant(fact, index));
//        storee.appendLoad(il, fact);
//        Utility.appendConversion(il, fact, storee.getType(), convertToType);
//        il.append(fact.createArrayStore(BcelWorld.makeBcelType(convertToType)));
//    }
//    
//    InstructionList createConvertableArrayStore(
//        InstructionFactory fact, 
//        int index,
//        BcelFieldRef storee) 
//    {
//        InstructionList il = new InstructionList();
//        appendConvertableArrayStore(il, fact, index, storee);
//        return il;
//    }
//    InstructionList createConvertableArrayLoad(
//        InstructionFactory fact, 
//        int index,
//        ResolvedTypeX convertTo) 
//    {
//        InstructionList il = new InstructionList();
//        appendConvertableArrayLoad(il, fact, index, convertTo);
//        return il;
//    }
}
