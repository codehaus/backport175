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

import org.aspectj.weaver.Checker;
import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
import org.aspectj.bridge.*;
import org.aspectj.bridge.MessageHandler;

public class CheckerTestCase extends WeaveTestCase {
	{
		regenerate = false;
	}

	public CheckerTestCase(String name) {
		super(name);
	}
    

    public void testStaticTjp() throws IOException {
    	Checker checker = new Checker(
    		new DeclareErrorOrWarning(true, makePointcutPrintln(), "hey, we found a println"));
    	
    	MessageHandler handler = new MessageHandler();
    	world.setMessageHandler(handler);
    	
        weaveTest("HelloWorld", "IdHelloWorld", checker);
        assertEquals(1, handler.numMessages(IMessage.ERROR, false));
        
        handler = new MessageHandler();
    	world.setMessageHandler(handler);
        weaveTest("FancyHelloWorld", "IdFancyHelloWorld", checker);
        assertEquals(3, handler.numMessages(IMessage.ERROR, false));
    } 
}
