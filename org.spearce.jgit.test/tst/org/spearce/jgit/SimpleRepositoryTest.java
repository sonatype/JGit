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

package org.spearce.jgit;

import java.io.File;

import org.spearce.jgit.lib.RepositoryTestCase;
import org.spearce.jgit.simple.SimpleRepository;
import org.spearce.jgit.transport.URIish;

/**
 * JUnit test for {@link SimpleRepository} 
 */
public class SimpleRepositoryTest extends RepositoryTestCase {

	private static final String REPO_LOCATION = "trash/simpleRepo/";
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		File repoDir = new File(REPO_LOCATION);
		recursiveDelete(repoDir);

		super.tearDown();
	}

	public void testInit() throws Exception {
		File repoDir = new File(REPO_LOCATION);
		recursiveDelete(repoDir);
		SimpleRepository srep = SimpleRepository.init(repoDir);
		assertNotNull(srep);
		assertTrue(repoDir.exists());
	}
	
	public void testClone() throws Exception {
		File repoDir = new File(REPO_LOCATION);
		recursiveDelete(repoDir);
		URIish uri = new URIish("file://" + trash.getAbsolutePath());
		SimpleRepository srep = SimpleRepository.clone(repoDir, "origin", uri, "master", null);
		assertNotNull(srep);
	}
	
	public void testCheckout() throws Exception {
		File repoDir = new File(REPO_LOCATION);
		recursiveDelete(repoDir);
		URIish uri = new URIish("file://" + trash.getAbsolutePath());
		SimpleRepository srep = SimpleRepository.clone(repoDir, "origin", uri, "master", null);
		assertNotNull(srep);
		
		srep.gitCheckout("master", null);
		File testFile = new File(REPO_LOCATION, "master.txt");
		assertTrue(testFile.exists());
	}
	
}
