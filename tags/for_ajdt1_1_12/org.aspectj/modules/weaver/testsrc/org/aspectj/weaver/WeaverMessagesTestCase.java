/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import junit.framework.TestCase;

/**
 * @author colyer
 *
 */
public class WeaverMessagesTestCase extends TestCase {
	
	public void testAllMessagesDefined() {
		
		Class wmClass = WeaverMessages.class;
		Field[] fields = wmClass.getDeclaredFields();
		List fieldList = new ArrayList();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			if (f.getType() == String.class) {
				try {
					String key = (String) f.get(null);
					String value = WeaverMessages.format(key);
					assertFalse("Each key should be unique",fieldList.contains(key));
					fieldList.add(key);
//					System.out.println(key + "," + value);
				} catch (IllegalAccessException ex) {					
				} catch(MissingResourceException mrEx) {
					fail("Missing resource: " + mrEx);
				}
			}
		}
	}

}
