/*
 * Copyright (C) 2007, Robin Rosenberg <robin.rosenberg@dewire.com>
 * Copyright (C) 2007, Shawn O. Pearce <spearce@spearce.org>
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

package org.spearce.jgit.errors;

import java.io.IOException;

import org.spearce.jgit.lib.AnyObjectId;
import org.spearce.jgit.lib.ObjectId;

/**
 * Exception thrown when an object cannot be read from Git.
 */
public class CorruptObjectException extends IOException {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a CorruptObjectException for reporting a problem specified
	 * object id
	 *
	 * @param id
	 * @param why
	 */
	public CorruptObjectException(final AnyObjectId id, final String why) {
		this(id.toObjectId(), why);
	}

	/**
	 * Construct a CorruptObjectException for reporting a problem specified
	 * object id
	 *
	 * @param id
	 * @param why
	 */
	public CorruptObjectException(final ObjectId id, final String why) {
		super("Object " + id.name() + " is corrupt: " + why);
	}

	/**
	 * Construct a CorruptObjectException for reporting a problem not associated
	 * with a specific object id.
	 *
	 * @param why
	 */
	public CorruptObjectException(final String why) {
		super(why);
	}
}
