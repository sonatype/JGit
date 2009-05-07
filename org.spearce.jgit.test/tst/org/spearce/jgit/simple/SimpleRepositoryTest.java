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

package org.spearce.jgit.simple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.spearce.jgit.lib.Commit;
import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.lib.RepositoryTestCase;
import org.spearce.jgit.lib.GitIndex.Entry;
import org.spearce.jgit.transport.URIish;

/**
 * JUnit test for {@link SimpleRepository} 
 */
public class SimpleRepositoryTest extends RepositoryTestCase {

	private static final String REPO_NAME = "trash/simpleRepo";
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		File repoDir = new File(REPO_NAME);
		recursiveDelete(repoDir);

		super.tearDown();
	}

	public void testInit() throws Exception {
		File repoDir = new File(REPO_NAME);
		recursiveDelete(repoDir);
		SimpleRepository srep = SimpleRepository.init(repoDir);
		assertNotNull(srep);
		assertTrue(repoDir.exists());
	}

	/**
	 * Test if init(repoDir) works correct for an exisging .git repo. 
	 * @throws Exception 
	 */
	public void testExistingRepo() throws Exception {
		File repoDir = new File(REPO_NAME);
		recursiveDelete(repoDir);
		
		// create an initial repo
		cloneTestRepository();
		
		// and now setup SimpleRepository for an existing .git repo
		SimpleRepository srep = SimpleRepository.existing(repoDir);
		assertNotNull(srep);
		assertTrue(repoDir.exists());
		
	}

	public void testClone() throws Exception {
		cloneTestRepository();
		File testFile = new File(REPO_NAME, "master.txt");
		assertTrue(testFile.exists());
	}
	
	public void testCheckout() throws Exception {
		SimpleRepository srep = cloneTestRepository();
		
		srep.checkout("A", null);
		File testFile = new File(REPO_NAME, "master.txt");
		assertTrue(testFile.exists());
	}

	public void testAddCommitPush() throws Exception {
		final String fileNameToAdd = "myNewFile.txt";
		final String commitMessage = "test commit";
		SimpleRepository srep = cloneTestRepository();
		
		// first we add a file to the Index
		File fileToAdd = createNewFile(srep, fileNameToAdd, "This File will be added, sic!");

        Entry indexEntry = srep.getRepository().getIndex().getEntry(fileNameToAdd);
        assertNull("hoops, found an entry for " + fileNameToAdd + " already: " + indexEntry, indexEntry);

        srep.add(fileToAdd, false);
        
        srep.getRepository().getIndex().read();
        assertNotNull("hoops, found no entry for " + fileNameToAdd, srep.getRepository().getIndex().getEntry(fileNameToAdd));
        
        // now we commit the Index against the tree
        srep.commit(null, null, commitMessage);
        
        // verify the commit
        Commit lastLocalCommit = srep.getRepository().mapCommit(Constants.HEAD);
        assertNotNull(lastLocalCommit);
        assertEquals(commitMessage, lastLocalCommit.getMessage());
        
        // push it, baby, push it real good :)
        boolean pushOk = srep.push(null, "origin", "master");
        assertTrue(pushOk);
        
        // verify the push
        Commit lastPushedCommit = db.mapCommit(Constants.HEAD);
        assertNotNull(lastPushedCommit);
        assertEquals(commitMessage, lastPushedCommit.getMessage());
	}

	public void testLsFiles() throws Exception {
		SimpleRepository srep = cloneTestRepository();
		List<LsFileEntry> entries = srep.lsFiles(true, false, false);
		assertNotNull(entries);
		assertFalse(entries.isEmpty());
		for (int i=0; i < entries.size(); i++) {
			System.out.println("LsFileEntry[" + i + "]: " + entries.get(i));
		}
		assertEquals(8, entries.size());
	}
	
	private SimpleRepository cloneTestRepository() 
	throws URISyntaxException, IOException {
		File repoDir = new File(REPO_NAME);
		recursiveDelete(repoDir);
		URIish uri = new URIish("file://" + trash.getAbsolutePath());
		SimpleRepository srep = SimpleRepository.clone(repoDir, "origin", uri, "master", null);
		assertNotNull(srep);
		return srep;
	}

	/**
	 * Create a fresh file in the repository
	 * @param srep
	 * @param fileNameToAdd
	 * @param content
	 * @return the freshly created File
	 * @throws IOException
	 */
	private File createNewFile(SimpleRepository srep, String fileNameToAdd, String content)
	throws IOException {
		File fileToAdd = new File(srep.getRepository().getWorkDir(), fileNameToAdd);
		
		assertFalse("File " + fileToAdd.getAbsolutePath() + " already exists!", fileToAdd.exists());
		
		BufferedWriter out = new BufferedWriter(new FileWriter(fileToAdd));
		out.write(content);
		out.close();
		return fileToAdd;
	}
	

}
