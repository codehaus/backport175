/*******************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                      *
 * http://backport175.codehaus.org                                                         *
 * --------------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of Apache License Version 2.0 *
 * a copy of which has been included with this distribution in the license.txt file.       *
 *******************************************************************************************/
package org.codehaus.backport175;

/**
 * The annotation to use for annotation default value.
 * <p/>
 * Since annotation default value are typed according to the annotation element, there is no element
 * named "value()" with a given type here. The type will be determined at compile time based on the
 * annotation element type itself that has this default value.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public interface DefaultValue {
}
