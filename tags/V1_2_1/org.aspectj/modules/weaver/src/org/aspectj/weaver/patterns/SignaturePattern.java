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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.AdviceSignature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;


public class SignaturePattern extends PatternNode {
	private Member.Kind kind;
	private ModifiersPattern modifiers;
	private TypePattern returnType;
    private TypePattern declaringType;
	private NamePattern name;
    private TypePatternList parameterTypes;
    private ThrowsPattern throwsPattern;
    	
	public SignaturePattern(Member.Kind kind, ModifiersPattern modifiers,
	                         TypePattern returnType, TypePattern declaringType,
	                         NamePattern name, TypePatternList parameterTypes,
	                         ThrowsPattern throwsPattern) {
		this.kind = kind;
		this.modifiers = modifiers;
		this.returnType = returnType;
		this.name = name;
		this.declaringType = declaringType;
		this.parameterTypes = parameterTypes;
		this.throwsPattern = throwsPattern;
	}
	
	
    public SignaturePattern resolveBindings(IScope scope, Bindings bindings) { 
		if (returnType != null) {
			returnType = returnType.resolveBindings(scope, bindings, false, false);
		} 
		if (declaringType != null) {
			declaringType = declaringType.resolveBindings(scope, bindings, false, false);
		}
		if (parameterTypes != null) {
			parameterTypes = parameterTypes.resolveBindings(scope, bindings, false, false);
		}
		if (throwsPattern != null) {
			throwsPattern = throwsPattern.resolveBindings(scope, bindings);
		}
		
    	return this;
    }
    
    public SignaturePattern resolveBindingsFromRTTI() {
		if (returnType != null) {
			returnType = returnType.resolveBindingsFromRTTI(false, false);
		} 
		if (declaringType != null) {
			declaringType = declaringType.resolveBindingsFromRTTI(false, false);
		}
		if (parameterTypes != null) {
			parameterTypes = parameterTypes.resolveBindingsFromRTTI(false, false);
		}
		if (throwsPattern != null) {
			throwsPattern = throwsPattern.resolveBindingsFromRTTI();
		}
		
    	return this;    	
    }
    
    
	public void postRead(ResolvedTypeX enclosingType) {
		if (returnType != null) {
			returnType.postRead(enclosingType);
		} 
		if (declaringType != null) {
			declaringType.postRead(enclosingType);
		}
		if (parameterTypes != null) {
			parameterTypes.postRead(enclosingType);
		}
	}
	
	public boolean matches(Member member, World world) {
		//XXX performance gains would come from matching on name before resolving
		//    to fail fast		
		ResolvedMember sig = member.resolve(world);
		if (sig == null) {
			//XXX
			if (member.getName().startsWith(NameMangler.PREFIX)) {
				return false;
			}
			world.getLint().unresolvableMember.signal(member.toString(), getSourceLocation());
			return false;
		}
		
		// This check should only matter when used from WithincodePointcut as KindedPointcut
		// has already effectively checked this with the shadows kind.
		if (kind != member.getKind()) {
			return false;
		}
		
		if (kind == Member.ADVICE) return true;
		
		if (!modifiers.matches(sig.getModifiers())) return false;
		
		if (kind == Member.STATIC_INITIALIZATION) {
			//System.err.println("match static init: " + sig.getDeclaringType() + " with " + this);
			return declaringType.matchesStatically(sig.getDeclaringType().resolve(world));
		} else if (kind == Member.FIELD) {
			
			if (!returnType.matchesStatically(sig.getReturnType().resolve(world))) return false;
			if (!name.matches(sig.getName())) return false;
			boolean ret = declaringTypeMatch(member.getDeclaringType(), member, world);
			//System.out.println("   ret: " + ret);
			return ret;
		} else if (kind == Member.METHOD) {
			if (!returnType.matchesStatically(sig.getReturnType().resolve(world))) return false;
			if (!name.matches(sig.getName())) return false;
			if (!parameterTypes.matches(world.resolve(sig.getParameterTypes()), TypePattern.STATIC).alwaysTrue()) {
				return false;
			}
			if (!throwsPattern.matches(sig.getExceptions(), world)) return false;
			return declaringTypeMatch(member.getDeclaringType(), member, world);
		} else if (kind == Member.CONSTRUCTOR) {
			if (!parameterTypes.matches(world.resolve(sig.getParameterTypes()), TypePattern.STATIC).alwaysTrue()) {
				return false;
			}
			if (!throwsPattern.matches(sig.getExceptions(), world)) return false;
			return declaringType.matchesStatically(member.getDeclaringType().resolve(world));
			//return declaringTypeMatch(member.getDeclaringType(), member, world);			
		}
		
		return false;
	}

	// for dynamic join point matching
	public boolean matches(JoinPoint.StaticPart jpsp) {
		Signature sig = jpsp.getSignature();
	    if (kind == Member.ADVICE && !(sig instanceof AdviceSignature)) return false;
	    if (kind == Member.CONSTRUCTOR && !(sig instanceof ConstructorSignature)) return false;
	    if (kind == Member.FIELD && !(sig instanceof FieldSignature)) return false;
	    if (kind == Member.METHOD && !(sig instanceof MethodSignature)) return false;
	    if (kind == Member.STATIC_INITIALIZATION && !(jpsp.getKind().equals(JoinPoint.STATICINITIALIZATION))) return false;
	    if (kind == Member.POINTCUT) return false;
			
	    if (kind == Member.ADVICE) return true;

	    if (!modifiers.matches(sig.getModifiers())) return false;
		
		if (kind == Member.STATIC_INITIALIZATION) {
			//System.err.println("match static init: " + sig.getDeclaringType() + " with " + this);
			return declaringType.matchesStatically(sig.getDeclaringType());
		} else if (kind == Member.FIELD) {
			Class returnTypeClass = ((FieldSignature)sig).getFieldType();
			if (!returnType.matchesStatically(returnTypeClass)) return false;
			if (!name.matches(sig.getName())) return false;
			boolean ret = declaringTypeMatch(sig);
			//System.out.println("   ret: " + ret);
			return ret;
		} else if (kind == Member.METHOD) {
			MethodSignature msig = ((MethodSignature)sig);
			Class returnTypeClass = msig.getReturnType();
			Class[] params = msig.getParameterTypes();
			Class[] exceptionTypes = msig.getExceptionTypes();
			if (!returnType.matchesStatically(returnTypeClass)) return false;
			if (!name.matches(sig.getName())) return false;
			if (!parameterTypes.matches(params, TypePattern.STATIC).alwaysTrue()) {
				return false;
			}
			if (!throwsPattern.matches(exceptionTypes)) return false;
			return declaringTypeMatch(sig);
		} else if (kind == Member.CONSTRUCTOR) {
			ConstructorSignature csig = (ConstructorSignature)sig;
			Class[] params = csig.getParameterTypes();
			Class[] exceptionTypes = csig.getExceptionTypes();
			if (!parameterTypes.matches(params, TypePattern.STATIC).alwaysTrue()) {
				return false;
			}
			if (!throwsPattern.matches(exceptionTypes)) return false;
			return declaringType.matchesStatically(sig.getDeclaringType());
			//return declaringTypeMatch(member.getDeclaringType(), member, world);			
		}
			    
		return false;
	}
	
	private boolean declaringTypeMatch(TypeX onTypeUnresolved, Member member, World world) {
		ResolvedTypeX onType = onTypeUnresolved.resolve(world);
		
		// fastmatch
		if (declaringType.matchesStatically(onType)) return true;
		
		Collection declaringTypes = member.getDeclaringTypes(world);
		
		for (Iterator i = declaringTypes.iterator(); i.hasNext(); ) {
			ResolvedTypeX type = (ResolvedTypeX)i.next();
			if (declaringType.matchesStatically(type)) return true;
		}
		return false;
	}

	private boolean declaringTypeMatch(Signature sig) {
		Class onType = sig.getDeclaringType();
		if (declaringType.matchesStatically(onType)) return true;
		
		Collection declaringTypes = getDeclaringTypes(sig);
		
		for (Iterator it = declaringTypes.iterator(); it.hasNext(); ) {
			Class pClass = (Class) it.next();
			if (declaringType.matchesStatically(pClass)) return true;
		}
		
		return false;
	}
	
	private Collection getDeclaringTypes(Signature sig) {
		List l = new ArrayList();
		Class onType = sig.getDeclaringType();
		String memberName = sig.getName();
		if (sig instanceof FieldSignature) {
			Class fieldType = ((FieldSignature)sig).getFieldType();
			Class superType = onType;
			while(superType != null) {
				try {
					Field f =  (superType.getDeclaredField(memberName));
					if (f.getType() == fieldType) {
						l.add(superType);
					}
				} catch (NoSuchFieldException nsf) {}
				superType = superType.getSuperclass();
			}
		} else if (sig instanceof MethodSignature) {
			Class[] paramTypes = ((MethodSignature)sig).getParameterTypes();
			Class superType = onType;
			while(superType != null) {
				try {
					Method m =  (superType.getDeclaredMethod(memberName,paramTypes));
					l.add(superType);
				} catch (NoSuchMethodException nsm) {}
				superType = superType.getSuperclass();
			}
		}
		return l;
	}
	
    public NamePattern getName() { return name; }
    public TypePattern getDeclaringType() { return declaringType; }
    
    public Member.Kind getKind() {
    	return kind;
    }
    
    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	if (modifiers != ModifiersPattern.ANY) {
    		buf.append(modifiers.toString());
    		buf.append(' ');
    	}
    	
    	if (kind == Member.STATIC_INITIALIZATION) {
    		buf.append(declaringType.toString());
    		buf.append(".<clinit>()");
    	} else if (kind == Member.HANDLER) {
    		buf.append("handler(");
    		buf.append(parameterTypes.get(0));
    		buf.append(")");
    	} else {
    		if (!(kind == Member.CONSTRUCTOR)) {
    			buf.append(returnType.toString());
    		    buf.append(' ');
    		}
    		if (declaringType != TypePattern.ANY) {
    			buf.append(declaringType.toString());
    			buf.append('.');
    		}
    		if (kind == Member.CONSTRUCTOR) {
    			buf.append("new");
    		} else {
    		    buf.append(name.toString());
    		}
    		if (kind == Member.METHOD || kind == Member.CONSTRUCTOR) {
    			buf.append(parameterTypes.toString());
    		}
    	}
    	return buf.toString();
    }
    
    public boolean equals(Object other) {
    	if (!(other instanceof SignaturePattern)) return false;
    	SignaturePattern o = (SignaturePattern)other;
    	return o.kind.equals(this.kind)
    		&& o.modifiers.equals(this.modifiers)
    		&& o.returnType.equals(this.returnType)
    		&& o.declaringType.equals(this.declaringType)
    		&& o.name.equals(this.name)
    		&& o.parameterTypes.equals(this.parameterTypes);
    }
    public int hashCode() {
        int result = 17;
        result = 37*result + kind.hashCode();
        result = 37*result + modifiers.hashCode();
        result = 37*result + returnType.hashCode();
        result = 37*result + declaringType.hashCode();
        result = 37*result + name.hashCode();
        result = 37*result + parameterTypes.hashCode();
        return result;
    }
    
	public void write(DataOutputStream s) throws IOException {
		kind.write(s);
		modifiers.write(s);
		returnType.write(s);
		declaringType.write(s);
		name.write(s);
		parameterTypes.write(s);
		throwsPattern.write(s);
		writeLocation(s);
	}

	public static SignaturePattern read(DataInputStream s, ISourceContext context) throws IOException {
		Member.Kind kind = Member.Kind.read(s);
		ModifiersPattern modifiers = ModifiersPattern.read(s);
		TypePattern returnType = TypePattern.read(s, context);
		TypePattern declaringType = TypePattern.read(s, context);
		NamePattern name = NamePattern.read(s);
		TypePatternList parameterTypes = TypePatternList.read(s, context);
		ThrowsPattern throwsPattern = ThrowsPattern.read(s, context);
		SignaturePattern ret = new SignaturePattern(kind, modifiers, returnType, declaringType,
					name, parameterTypes, throwsPattern);
		ret.readLocation(context, s);
		return ret;
	}

	/**
	 * @return
	 */
	public ModifiersPattern getModifiers() {
		return modifiers;
	}

	/**
	 * @return
	 */
	public TypePatternList getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * @return
	 */
	public TypePattern getReturnType() {
		return returnType;
	}

	/**
	 * @return
	 */
	public ThrowsPattern getThrowsPattern() {
		return throwsPattern;
	}

}
