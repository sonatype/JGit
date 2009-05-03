/*
 * Copyright (C) 2009, Mark Struberg <struberg@yahoo.de>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Git Development Community nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spearce.jgit.util;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;

/**
 * Validate contains a few static helper functions which are very simpilar
 * to JUnits Assert. They basically make validating function arguments to
 * be a 1-line call.
 * 
 * All validate functions will throw an {@code IllegalArgumentException}
 * if the validation fails.
 * 
 * All validate functions exist in 3 variants: 
 * 
 * <p>1st function with only the validation option</p>
 * 
 * <p>2nd function with an additional String message parameter. This should
 * be used only if no additional parameters have to be provided. Instead of using
 * String operations to create the message String, the following 3rd variant 
 * should be used.</p>
 * 
 * <p>For each validation function a similar 3rd validation function exists
 * with a list of additional message parameters as Objects in ellipsis form. 
 * This is used instead of simply passing a message String due to performance reasons!
 * When using a message string, all parameters would have to be string concatenated
 * before the call, even if no problem arises which would cost performance.</br>
 * Instead of this, we will concatenate (with spaces) all given msgObjects.toString() 
 * only in case of a failed validation! If the first parameter of the msgObject is a
 * String, it will be taken as the format string for {@code MessageFormat}.</p>
 * 
 * <h3>Examples:</h3>
 * <p>
 * Simply validating an Argument without further message:
 * <pre>
 * public void myFn(String argString, Integer argInt) {
 *     Validate.notNull(argString);
 *     Validate.notNull(argInt);
 *     Validate.isTrue(argInt.intValue > 3);
 * }
 * </pre>
 * <p>
 * 
 * <p>
 * Validating an Argument and adding a message to the IllegalArgumentException:
 * <pre>
 * public void myFn(String argString, Integer argInt) {
 *     Validate.notNull(argInt, "Integer parameter must be set);
 *     Validate.isTrue(argInt.intValue > 3, "Integer parameter must be <=3!");
 * }
 * </pre>
 * <p>
 * 
 * <p>
 * If the first parameter of the msgObject is a String {@code MessageFormat} will be used:
 * <pre>
 *     Validate.isTrue(argInt.intValue > 3, "Integer parameter actually is {0} but must be <=3 !", argInt);
 * </pre>
 * </p>
 * @see MessageFormat
 */
public class Validate {

	/**
	 * Private ct since this class should not be instantiated!
	 */
	private Validate() {
		super();
	}
	
	/**
	 * Validate that o is not <code>null</code>
	 * 
	 * @param o Object to validate
	 */
	public static void notNull(Object o) {
		if (o == null) {
			throw new IllegalArgumentException("The validated object is null!");
		}
	}

	/**
	 * Validate that o is not <code>null</code>
	 * 
	 * @param o Object to validate
	 * @param msg text message to the InvalidArgumentException
	 */
	public static void notNull(Object o, String msg) {
		if (o == null) {
			throw new IllegalArgumentException(msg);
		}
	}	

	/**
	 * Validate that o is not <code>null</code>
	 * 
	 * @param o Object to validate
	 * @param msgObjects additional Objects added as text message to the InvalidArgumentException
	 */
	public static void notNull(Object o, Object... msgObjects) {
		if (o == null) {
			throw new IllegalArgumentException(getMessage(msgObjects));
		}
	}	

	/**
	 * Validate that b is <code>true</code>
	 * 
	 * @param b boolean to validate
	 */
	public static void isTrue(boolean b) {
		if (!b) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Validate that b is <code>true</code>
	 * 
	 * @param b boolean to validate
	 * @param msg text message to the InvalidArgumentException
	 */
	public static void isTrue(boolean b, String msg) {
		if (!b) {
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Validate that b is <code>true</code>
	 * 
	 * @param b boolean to validate
	 * @param msgObjects additional Objects added as text message to the InvalidArgumentException
	 */
	public static void isTrue(boolean b, Object... msgObjects) {
		if (!b) {
			throw new IllegalArgumentException(getMessage(msgObjects));
		}
	}

	/**
	 * Validate that the given String is not <code>null</code> and is not empty.
	 * 
	 * @param string the array to validate
	 */
	public static void notEmpty(String string) {
		if (string == null || string.length() == 0) {
			throw new IllegalArgumentException("The validated String is empty!");
		}
	}
	
	/**
	 * Validate that the given String is not <code>null</code> and is not empty.
	 * 
	 * @param string the array to validate
	 * @param msg text message to the InvalidArgumentException
	 */
	public static void notEmpty(String string, String msg) {
		if (string == null || string.length() == 0) {
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Validate that the given String is not <code>null</code> and is not empty.
	 * 
	 * @param string the array to validate
	 * @param msgObjects additional Objects added as text message to the InvalidArgumentException
	 */
	public static void notEmpty(String string, Object... msgObjects) {
		if (string == null || string.length() == 0) {
			throw new IllegalArgumentException(getMessage(msgObjects));
		}
	}

	/**
	 * Validate that arr is not <code>null</code> and has at least 1 entry.
	 * 
	 * @param arr the array to validate
	 */
	public static void notEmpty(Object[] arr) {
		if (arr == null || arr.length == 0) {
			throw new IllegalArgumentException("The validated Object arrray is empty!");
		}
	}
	
	/**
	 * Validate that arr is not <code>null</code> and is not empty.
	 * 
	 * @param arr the array to validate
	 * @param msg text message to the InvalidArgumentException
	 */
	public static void notEmpty(Object[] arr, String msg) {
		if (arr == null || arr.length == 0) {
			throw new IllegalArgumentException(msg);
		}
	}
	
	/**
	 * Validate that arr is not <code>null</code> and is not empty.
	 * 
	 * @param arr the array to validate
	 * @param msgObjects additional Objects added as text message to the InvalidArgumentException
	 */
	public static void notEmpty(Object[] arr, Object... msgObjects) {
		if (arr == null || arr.length == 0) {
			throw new IllegalArgumentException(getMessage(msgObjects));
		}
	}
	
	/**
	 * Validate that the given Collection is not <code>null</code> and is not empty.
	 * 
	 * @param coll the Collection to validate
	 */
	public static void notEmpty(Collection coll) {
		if (coll == null || coll.isEmpty()) {
			throw new IllegalArgumentException("The validated Collection is empty!");
		}
	}
	
	/**
	 * Validate that the given Collection is not <code>null</code> and is not empty.
	 * 
	 * @param coll the Collection to validate
	 * @param msg text message to the InvalidArgumentException
	 */
	public static void notEmpty(Collection coll, String msg) {
		if (coll == null || coll.isEmpty()) {
			throw new IllegalArgumentException(msg);
		}
	}
	
	/**
	 * Validate that the given Collection is not <code>null</code> and is not empty.
	 * 
	 * @param coll the Collection to validate
	 * @param msgObjects additional Objects added as text message to the InvalidArgumentException
	 */
	public static void notEmpty(Collection coll, Object... msgObjects) {
		if (coll == null || coll.isEmpty()) {
			throw new IllegalArgumentException(getMessage(msgObjects));
		}
	}
	
	/**
	 * Validate that the given Map is not <code>null</code> and is not empty.
	 * 
	 * @param map the array to validate
	 */
	public static void notEmpty(Map map) {
		if (map == null || map.isEmpty()) {
			throw new IllegalArgumentException("The validated Map is empty!");
		}
	}
	
	/**
	 * Validate that the given Map is not <code>null</code> and is not empty.
	 * 
	 * @param map the Map to validate
	 * @param msg text message to the InvalidArgumentException
	 */
	public static void notEmpty(Map map, String msg) {
		if (map == null || map.isEmpty()) {
			throw new IllegalArgumentException(msg);
		}
	}
	
	/**
	 * Validate that the given Map is not <code>null</code> and is not empty.
	 * 
	 * @param map the Map to validate
	 * @param msgObjects additional Objects added as text message to the InvalidArgumentException
	 */
	public static void notEmpty(Map map, Object... msgObjects) {
		if (map == null || map.isEmpty()) {
			throw new IllegalArgumentException(getMessage(msgObjects));
		}
	}
	
	
	/**
	 * private helper function to create an error message from the given Objects
	 * If the first object in msgObjects is of type {@code String} then 
	 * {@code MessageFormat} will be used to format the output message.
	 * 
	 * @param msgObjects
	 * @return concatenated String representation of all the objects
	 */
	private static String getMessage(Object... msgObjects) {
		if (msgObjects.length > 0 && msgObjects[0] instanceof String) {
			MessageFormat form = new MessageFormat((String) msgObjects[0]);
			Object[] params = new Object[msgObjects.length - 1];
			System.arraycopy(msgObjects, 1, params, 0, msgObjects.length - 1);
			return form.format(params);
		}
		else {
			StringBuffer sb = new StringBuffer("Validation failed: [");
			for(int i = 0; i < msgObjects.length; i++) {
				if (i > 0) {
					sb.append(' ');
				}
				sb.append(msgObjects[i]);
			}
			sb.append(']');
			return sb.toString();
		}
	}

}
