/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Matthew Webster, Adrian Colyer, 
 *     Martin Lippert     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.tools;

/**
 * Interface implemented by weaving class loaders to allow classes generated by
 * the weaving process to be defined.
 */
public interface GeneratedClassHandler {
	
	/**
	 * Accept class generated by WeavingAdaptor. The class loader should store
	 * the class definition in its local cache until called upon to load it.
	 * @param name class name
	 * @param bytes class definition
	 */
	public void acceptClass (String name, byte[] bytes);

}
