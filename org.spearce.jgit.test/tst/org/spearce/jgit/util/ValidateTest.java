/*
 * Copyright (C) 2008, Mark Struberg <mark.struberg@yahoo.de>
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

import junit.framework.TestCase;

public class ValidateTest extends TestCase {

	public void testNotNull() {
		Validate.notNull(new Integer(42));
		
		try {
			Integer nullInt = null;
			Validate.notNull(nullInt);
			fail();
		}
		catch(IllegalArgumentException e) {
			String msg = e.getMessage();
			assertEquals("The validated object is null!", msg);
		}
		
		try {
			Integer nullInt = null;
			Validate.notNull(nullInt, "testMessageText Integer={0} btw!", new Integer(42));
			fail();
		}
		catch(IllegalArgumentException e) {
			String msg = e.getMessage();
			assertEquals("testMessageText Integer=42 btw!", msg);
		}
	}

	public void testNotNullWithSingleStringParam() {		
		try {
			Integer nullInt = null;
			Validate.notNull(nullInt, "oh gosh, it is null");
			fail();
		}
		catch(IllegalArgumentException e) {
			String msg = e.getMessage();
			assertEquals("oh gosh, it is null", msg);
		}
	}

	public void testNotNullWithSingleNonStringParam() {		
		try {
			Integer nullInt = null;
			Validate.notNull(nullInt, new Integer(42));
			fail();
		}
		catch(IllegalArgumentException e) {
			String msg = e.getMessage();
			assertEquals("Validation failed: [42]", msg);
		}
	}
	
	public void testNotNullWithMessageFormatParam() {		
		try {
			Integer nullInt = null;
			Validate.notNull(nullInt, "testMessageText Integer={0} btw!", new Integer(42));
			fail();
		}
		catch(IllegalArgumentException e) {
			String msg = e.getMessage();
			assertEquals("testMessageText Integer=42 btw!", msg);
		}
	}
	
}
