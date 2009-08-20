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

package org.spearce.jgit.ignore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.spearce.jgit.lib.Commit;
import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.lib.GitIndex;
import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.RepositoryTestCase;
import org.spearce.jgit.lib.Tree;
import org.spearce.jgit.lib.WorkDirCheckout;


/**
 * JUnit tests for {@link IgnoreRules} 
 */
public class IgnoreRulesTest extends RepositoryTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		
		// now checkout into this directory
		ObjectId headId = db.resolve(Constants.HEAD);
		final Commit commit = db.mapCommit(headId);
		final GitIndex index = db.getIndex();
		final Tree tree = commit.getTree();
		final WorkDirCheckout co;

		co = new WorkDirCheckout(db, db.getWorkDir(), index, tree);
		co.checkout();
		index.write();

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}


	public void testRootDotIgnoreFile() throws Exception {
		createGitIgnoreFile(trash, "a");
		createGitIgnoreFile(new File(trash, "b"), "b2.txt");
		
		IgnoreRules i = new IgnoreRules(db);
		
		assertTrue(i.isIgnored(new File(trash, "a/a1.txt")));
				
		assertFalse(i.isIgnored(new File(trash, "b/b1.txt")));

		assertTrue(i.isIgnored(new File(trash, "b/b2.txt")));
		
		assertFalse(i.isIgnored(new File(trash, "b/b2Xtxt")));
	}


	public void testRootDotIgnoreFileWildcards() throws Exception {
		createGitIgnoreFile(trash, "c/c*.txt");
		
		IgnoreRules i = new IgnoreRules(db);
		
		assertTrue(i.isIgnored(new File(trash, "c/c1.txt")));
		assertTrue(i.isIgnored(new File(trash, "c/c2.txt")));
		assertFalse(i.isIgnored(new File(trash, "c/c")));
	}
	
	/**
	 * create a .gitignore file in the given baseDir and fill it with the given content
	 * @param baseDir
	 * @param content
	 * @throws IOException
	 */
	private void createGitIgnoreFile(File baseDir, String content) throws IOException {
		File gitIgnoreFile = new File(baseDir, ".gitignore");

		// ensure the file does not yet exist!
		if (gitIgnoreFile.exists()) {
			gitIgnoreFile.delete();
		}
		
        BufferedWriter out = new BufferedWriter(new FileWriter(gitIgnoreFile));
        out.write(content);
        out.close();
	}
	
}
