/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Test;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ArgsAnnotationPointcut extends NameBindingPointcut {

	private AnnotationPatternList arguments;
	
	/**
	 * 
	 */
	public ArgsAnnotationPointcut(AnnotationPatternList arguments) {
		super();
		this.arguments = arguments;
	}
		
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#fastMatch(org.aspectj.weaver.patterns.FastMatchInfo)
	 */
	public FuzzyBoolean fastMatch(FastMatchInfo info) {
		return FuzzyBoolean.MAYBE;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#match(org.aspectj.weaver.Shadow)
	 */
	public FuzzyBoolean match(Shadow shadow) {
		arguments.resolve(shadow.getIWorld());
		FuzzyBoolean ret =
			arguments.matches(shadow.getIWorld().resolve(shadow.getArgTypes()));
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindings(org.aspectj.weaver.patterns.IScope, org.aspectj.weaver.patterns.Bindings)
	 */
	protected void resolveBindings(IScope scope, Bindings bindings) {
		arguments.resolveBindings(scope, bindings, true);
		if (arguments.ellipsisCount > 1) {
			scope.message(IMessage.ERROR, this,
					"uses more than one .. in args (compiler limitation)");
		}
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#resolveBindingsFromRTTI()
	 */
	protected void resolveBindingsFromRTTI() {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#concretize1(org.aspectj.weaver.ResolvedTypeX, org.aspectj.weaver.IntMap)
	 */
	protected Pointcut concretize1(ResolvedTypeX inAspect, IntMap bindings) {
		if (isDeclare(bindings.getEnclosingAdvice())) {
			  // Enforce rule about which designators are supported in declare
			  inAspect.getWorld().showMessage(IMessage.ERROR,
			  		WeaverMessages.format(WeaverMessages.ARGS_IN_DECLARE),
					bindings.getEnclosingAdvice().getSourceLocation(), null);
			  return Pointcut.makeMatchesNothing(Pointcut.CONCRETE);
		}
		AnnotationPatternList list = arguments.resolveReferences(bindings);
		return new ArgsAnnotationPointcut(list);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.Pointcut#findResidue(org.aspectj.weaver.Shadow, org.aspectj.weaver.patterns.ExposedState)
	 */
	public Test findResidue(Shadow shadow, ExposedState state) {
		int len = shadow.getArgCount();
	
		// do some quick length tests first
		int numArgsMatchedByEllipsis = (len + arguments.ellipsisCount) - arguments.size();
		if (numArgsMatchedByEllipsis < 0) return Literal.FALSE;  // should never happen
		if ((numArgsMatchedByEllipsis > 0) && (arguments.ellipsisCount == 0)) {
			return Literal.FALSE; // should never happen
		}
		// now work through the args and the patterns, skipping at ellipsis
    	Test ret = Literal.TRUE;
    	int argsIndex = 0;
    	for (int i = 0; i < arguments.size(); i++) {
			if (arguments.get(i) == AnnotationTypePattern.ELLIPSIS) {
				// match ellipsisMatchCount args
				argsIndex += numArgsMatchedByEllipsis;
			} else if (arguments.get(i) == AnnotationTypePattern.ANY) {
				argsIndex++;
			} else {
				// match the argument type at argsIndex with the ExactAnnotationTypePattern
				// we know it is exact because nothing else is allowed in args
				ExactAnnotationTypePattern ap = (ExactAnnotationTypePattern)arguments.get(i);
				TypeX argType = shadow.getArgType(argsIndex);
				ResolvedTypeX rArgType = argType.resolve(shadow.getIWorld());
				if (rArgType == ResolvedTypeX.MISSING) {
	                  IMessage msg = new Message(
	                    WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_ARG_TYPE,argType.getName()),
	                    "",IMessage.ERROR,shadow.getSourceLocation(),null,new ISourceLocation[]{getSourceLocation()});
	            }
				if (ap.matches(rArgType).alwaysTrue()) {
					argsIndex++;
					continue;
				} else {
					// we need a test...
					// TODO: binding
					ResolvedTypeX rAnnType = ap.annotationType.resolve(shadow.getIWorld());
					ret = Test.makeAnd(ret,Test.makeHasAnnotation(shadow.getArgVar(argsIndex),rAnnType));
					argsIndex++;
				}				
			}
		}   	
    	return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Pointcut.ATARGS);
		arguments.write(s);
		writeLocation(s);
	}
	
	public static Pointcut read(DataInputStream s, ISourceContext context) throws IOException {
		AnnotationPatternList annotationPatternList = AnnotationPatternList.read(s,context);
		ArgsAnnotationPointcut ret = new ArgsAnnotationPointcut(annotationPatternList);
		ret.readLocation(context, s);
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ArgsAnnotationPointcut)) return false;
		ArgsAnnotationPointcut other = (ArgsAnnotationPointcut) obj;
		return other.arguments.equals(arguments);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 17 + 37*arguments.hashCode();
	}
	
	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("@args");
        buf.append(arguments.toString());
        return buf.toString();
    }
}