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

import java.io.IOException;
import java.util.Collections;

public class FieldSetTestCase extends WeaveTestCase {
    {
        regenerate = false;
    }

    public FieldSetTestCase(String name) {
        super(name);
    }

    public void testBefore() throws IOException {
        weaveTest(
			"FieldyHelloWorld",
            "FieldSet",
           makeAdviceAll("before")
            );
    }
}
