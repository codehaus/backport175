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


package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.aspectj.bridge.IMessage;
import org.aspectj.util.FileUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.CrosscuttingMembers;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Test;


public class CflowPointcut extends Pointcut {
	private Pointcut entry; // The pointcut inside the cflow() that represents the 'entry' point
	private boolean isBelow;// Is this cflowbelow?
	private int[] freeVars;
	
	private static Hashtable cflowFields = new Hashtable();
	private static Hashtable cflowBelowFields = new Hashtable();
	
	/**
	 * Used to indicate that we're in the context of a cflow when concretizing if's
	 * 
	 * Will be removed or replaced with something better when we handle this
	 * as a non-error
	 */
	public static final ResolvedPointcutDefinition CFLOW_MARKER = 
		new ResolvedPointcutDefinition(null, 0, null, TypeX.NONE, Pointcut.makeMatchesNothing(Pointcut.RESOLVED));

	
	public CflowPointcut(Pointcut entry, boolean isBelow, int[] freeVars) {
	//	System.err.println("Building cflow pointcut "+entry.toString());
		this.entry = entry;
		this.isBelow = isBelow;
		this.freeVars = freeVars;
		this.pointcutKind = CFLOW;
	}

	public Set couldMatchKinds() {
		return Shadow.ALL_SHADOW_KINDS;
	}
	
    public FuzzyBoolean fastMatch(FastMatchInfo type) {
		return FuzzyBoolean.MAYBE;
	}
    
	protected FuzzyBoolean matchInternal(Shadow shadow) {
		//??? this is not maximally efficient
		return FuzzyBoolean.MAYBE;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesDynamically(java.lang.Object, java.lang.Object, java.lang.Object[])
	 */
	public boolean matchesDynamically(Object thisObject, Object targetObject,
			Object[] args) {
		throw new UnsupportedOperationException("cflow pointcut matching not supported by this operation");
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#matchesStatically(java.lang.String, java.lang.reflect.Member, java.lang.Class, java.lang.Class, java.lang.reflect.Member)
	 */
	public FuzzyBoolean matchesStatically(
			String joinpointKind, java.lang.reflect.Member member,
			Class thisClass, Class targetClass,
			java.lang.reflect.Member withinCode) {
		throw new UnsupportedOperationException("cflow pointcut matching not supported by this operation");
	}

	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.CFLOW);
		entry.write(s);
		s.writeBoolean(isBelow);
		FileUtil.writeIntArray(freeVars, s);
		writeLocation(s);
	}
	public static Pointcut read(VersionedDataInputStream s, ISourceContext context) throws IOException {

		CflowPointcut ret = new CflowPointcut(Pointcut.read(s, context), s.readBoolean(), FileUtil.readIntArray(s));
		ret.readLocation(context, s);
		return ret;
	}

	public void resolveBindings(IScope scope, Bindings bindings) {
		if (bindings == null) {
			entry.resolveBindings(scope, null);
			entry.state = RESOLVED;
			freeVars = new int[0];
		} else {
			//??? for if's sake we might need to be more careful here
			Bindings entryBindings = new Bindings(bindings.size());
			
			entry.resolveBindings(scope, entryBindings);
			entry.state = RESOLVED;
			
			freeVars = entryBindings.getUsedFormals();
			
			bindings.mergeIn(entryBindings, scope);
		}
	}
	
	public void resolveBindingsFromRTTI() {
		if (entry.state != RESOLVED) {
			entry.resolveBindingsFromRTTI();
		}
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof CflowPointcut)) return false;
		CflowPointcut o = (CflowPointcut)other;
		return o.entry.equals(this.entry) && o.isBelow == this.isBelow;
	}
    public int hashCode() {
        int result = 17;
        result = 37*result + entry.hashCode();
        result = 37*result + (isBelow ? 0 : 1);
        return result;
    }
	public String toString() {
		return "cflow" + (isBelow ? "below" : "") + "(" + entry + ")";
	}

	protected Test findResidueInternal(Shadow shadow, ExposedState state) {
		throw new RuntimeException("unimplemented");
	}
	
	public Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {

		// Enforce rule about which designators are supported in declare
		if (isDeclare(bindings.getEnclosingAdvice())) {
			inAspect.getWorld().showMessage(IMessage.ERROR,
					WeaverMessages.format(WeaverMessages.CFLOW_IN_DECLARE,isBelow?"below":""),
					bindings.getEnclosingAdvice().getSourceLocation(), null);
			return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}
		
		//make this remap from formal positions to arrayIndices
		IntMap entryBindings = new IntMap();
		for (int i=0, len=freeVars.length; i < len; i++) {
			int freeVar = freeVars[i];
			//int formalIndex = bindings.get(freeVar);
			entryBindings.put(freeVar, i);
		}
		entryBindings.copyContext(bindings);
		//System.out.println(this + " bindings: " + entryBindings);
		
		World world = inAspect.getWorld();
		
		Pointcut concreteEntry;
		
		ResolvedTypeX concreteAspect = bindings.getConcreteAspect();
		
		CrosscuttingMembers xcut = concreteAspect.crosscuttingMembers;		
		Collection previousCflowEntries = xcut.getCflowEntries();
		
		
		entryBindings.pushEnclosingDefinition(CFLOW_MARKER);
		// This block concretizes the pointcut within the cflow pointcut
		try {
			concreteEntry = entry.concretize(inAspect, entryBindings);
		} finally {
			entryBindings.popEnclosingDefinitition();
		}

		List innerCflowEntries = new ArrayList(xcut.getCflowEntries());
		innerCflowEntries.removeAll(previousCflowEntries);

		  
		Object field = getCflowfield(concreteEntry);
		
		// Four routes of interest through this code (did I hear someone say refactor??)
		// 1) no state in the cflow - we can use a counter *and* we have seen this pointcut
		//    before - so use the same counter as before.
		// 2) no state in the cflow - we can use a counter, but this is the first time
		//    we have seen this pointcut, so build the infrastructure.
		// 3) state in the cflow - we need to use a stack *and* we have seen this pointcut
		//    before - so share the stack.
		// 4) state in the cflow - we need to use a stack, but this is the first time 
		//    we have seen this pointcut, so build the infrastructure.
		
		if (freeVars.length == 0) { // No state, so don't use a stack, use a counter.
		  ResolvedMember localCflowField = null;

		  // Check if we have already got a counter for this cflow pointcut
		  if (field != null) {
		   	localCflowField = (ResolvedMember)field; // Use the one we already have
		   	
		  } else {
		  	
		  	// Create a counter field in the aspect
		  	localCflowField = new ResolvedMember(Member.FIELD,concreteAspect,Modifier.STATIC | Modifier.PUBLIC | Modifier.FINAL,
		  		NameMangler.cflowCounter(xcut),TypeX.forName(NameMangler.CFLOW_COUNTER_TYPE).getSignature());
		  
		    // Create type munger to add field to the aspect
		    concreteAspect.crosscuttingMembers.addTypeMunger(world.makeCflowCounterFieldAdder(localCflowField));
		  
		    // Create shadow munger to push stuff onto the stack
		    concreteAspect.crosscuttingMembers.addConcreteShadowMunger(
		  		Advice.makeCflowEntry(world,concreteEntry,isBelow,localCflowField,freeVars.length,innerCflowEntries,inAspect));
		    
		    putCflowfield(concreteEntry,localCflowField); // Remember it
	      }
		  Pointcut ret = new ConcreteCflowPointcut(localCflowField, null,true);
		  ret.copyLocationFrom(this);
		  return ret;
		} else {

			List slots = new ArrayList();
		  
			for (int i=0, len=freeVars.length; i < len; i++) {
				int freeVar = freeVars[i];
				
				// we don't need to keep state that isn't actually exposed to advice
				//??? this means that we will store some state that we won't actually use, optimize this later
				if (!bindings.hasKey(freeVar)) continue; 
				
				int formalIndex = bindings.get(freeVar);
				ResolvedTypeX formalType =
					bindings.getAdviceSignature().getParameterTypes()[formalIndex].resolve(world);
				
				ConcreteCflowPointcut.Slot slot = 
					new ConcreteCflowPointcut.Slot(formalIndex, formalType, i);
				slots.add(slot);
			}
			ResolvedMember localCflowField = null;
			if (field != null) {
				localCflowField = (ResolvedMember)field;
			} else {
		      
			  localCflowField = new ResolvedMember(
				Member.FIELD, concreteAspect, Modifier.STATIC | Modifier.PUBLIC | Modifier.FINAL,
						NameMangler.cflowStack(xcut), 
						TypeX.forName(NameMangler.CFLOW_STACK_TYPE).getSignature());
			  //System.out.println("adding field to: " + inAspect + " field " + cflowField);
						
			  // add field and initializer to inAspect
			  //XXX and then that info above needs to be mapped down here to help with
			  //XXX getting the exposed state right
			  concreteAspect.crosscuttingMembers.addConcreteShadowMunger(
					Advice.makeCflowEntry(world, concreteEntry, isBelow, localCflowField, freeVars.length, innerCflowEntries,inAspect));
			
			  concreteAspect.crosscuttingMembers.addTypeMunger(
				world.makeCflowStackFieldAdder(localCflowField));
			  putCflowfield(concreteEntry,localCflowField);
		    }
			Pointcut ret = new ConcreteCflowPointcut(localCflowField, slots,false);
			ret.copyLocationFrom(this);
			return ret;
		}
		
	}
	
	public static void clearCaches() {
		cflowFields.clear();
		cflowBelowFields.clear();
	}
	
	private Object getCflowfield(Pointcut pcutkey) {
		if (isBelow) {
			return cflowBelowFields.get(pcutkey);
		} else {
			return cflowFields.get(pcutkey);
		}
	}
	
	private void putCflowfield(Pointcut pcutkey,Object o) {
		if (isBelow) {
			cflowBelowFields.put(pcutkey,o);
		} else {
			cflowFields.put(pcutkey,o);
		}
	}

}
