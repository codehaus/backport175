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

import org.aspectj.weaver.ISourceContext;

public class DeclareErrorOrWarning extends Declare {
	private boolean isError;
	private Pointcut pointcut;
	private String message;

	public DeclareErrorOrWarning(boolean isError, Pointcut pointcut, String message) {
		this.isError = isError;
		this.pointcut = pointcut;
		this.message = message;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("declare ");
		if (isError) buf.append("error: ");
		else buf.append("warning: ");
		buf.append(pointcut);
		buf.append(": ");
		buf.append("\"");
		buf.append(message);
		buf.append("\";");
		return buf.toString();
	}
	
	public boolean equals(Object other) { 
		if (!(other instanceof DeclareErrorOrWarning)) return false;
		DeclareErrorOrWarning o = (DeclareErrorOrWarning)other;
		return (o.isError == isError) &&
			o.pointcut.equals(pointcut) &&
			o.message.equals(message);
	}
    
    public int hashCode() {
        int result = isError ? 19 : 23;
        result = 37*result + pointcut.hashCode();
        result = 37*result + message.hashCode();
        return result;
    }


	public void write(DataOutputStream s) throws IOException {
		s.writeByte(Declare.ERROR_OR_WARNING);
		s.writeBoolean(isError);
		pointcut.write(s);
		s.writeUTF(message);
		writeLocation(s);
	}

	public static Declare read(DataInputStream s, ISourceContext context) throws IOException {
		Declare ret = new DeclareErrorOrWarning(
			s.readBoolean(),
			Pointcut.read(s, context),
			s.readUTF()
		);
		ret.readLocation(context, s);
		return ret;
	}


	public boolean isError() {
		return isError;
	}

	public String getMessage() {
		return message;
	}

	public Pointcut getPointcut() {
		return pointcut;
	}
	
    public void resolve(IScope scope) {
    	pointcut = pointcut.resolve(scope);  	
    }
    
	public boolean isAdviceLike() {
		return true;
	}

}
