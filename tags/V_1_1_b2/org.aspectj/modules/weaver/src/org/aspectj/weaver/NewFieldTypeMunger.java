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


package org.aspectj.weaver;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.aspectj.weaver.ResolvedTypeMunger.Kind;

public class NewFieldTypeMunger extends ResolvedTypeMunger {
	public NewFieldTypeMunger(ResolvedMember signature, Set superMethodsCalled) {
		super(Field, signature);
		this.setSuperMethodsCalled(superMethodsCalled);
	}

	public ResolvedMember getInitMethod(TypeX aspectType) {
		return AjcMemberMaker.interFieldInitializer(signature, aspectType);
	}

	public void write(DataOutputStream s) throws IOException {
		kind.write(s);
		signature.write(s);
		writeSuperMethodsCalled(s);
	}

	public static ResolvedTypeMunger readField(DataInputStream s, ISourceContext context) throws IOException {
		return new NewFieldTypeMunger(
			ResolvedMember.readResolvedMember(s, context),
			readSuperMethodsCalled(s));
	}
}
