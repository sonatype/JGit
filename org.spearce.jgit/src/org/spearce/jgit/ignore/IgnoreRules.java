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

package org.spearce.jgit.ignore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.spearce.jgit.lib.Repository;

/**
 *  Handle all the Ignore rules defined by git
 *
 */
public class IgnoreRules {
	
	private Repository db;
	
	/**
	 * @param db
	 */
	public IgnoreRules(Repository db) {
		this.db = db;
	}
	
	/**
	 * Checks if the given file or directory is ignored by git via
	 * one of the .gitignore files on the path, .git/info/exclude 
	 * and core.excludesfile
	 * @param toCheckFor which should be checked
	 * @return <code>true</code> if the given path should be ignored
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public boolean isIgnored(File toCheckFor) 
	throws FileNotFoundException, IOException {
		
		if (checkGitignoreFiles(toCheckFor)) {
			return true;
		}
		
		//X TODO check .git/info/excludes
		
		//X TODO check core.excludefiles
		
		return false;
	}

	/**
	 * Check for all .gitignore files along the path until
	 * up to the root directory of this very repository
	 * @param toCheckFor
	 * @return <code>true</code> if the given path should be ignored
	 * @throws IOException, FileNotFoundException 
	 * @throws FileNotFoundException 
	 */
	private boolean checkGitignoreFiles(File toCheckFor) 
	throws IOException, FileNotFoundException {
		if (!toCheckFor.getAbsolutePath().startsWith(db.getWorkDir().getAbsolutePath())) {
			throw new IllegalArgumentException("file must be inside the repositories working directory! " + toCheckFor.getAbsolutePath());
		}
		
		File repoDir = db.getWorkDir();
		File parseDir = toCheckFor;
		do {
			parseDir = parseDir.getParentFile();
			
			File gitignore = new File(parseDir, ".gitignore");
			if (gitignore.exists()) {
				FileInputStream fis = null;
				try {
					fis =  new FileInputStream(gitignore);
					if (parseGitIgnore(new FileInputStream(gitignore), parseDir, toCheckFor)) {
						return true;
					}
				}
				finally {
					// ensure that the file gets closed!
					if (fis != null) {
						fis.close();
					}
				}
			}
		} while (!parseDir.equals(repoDir));
		
		return false;
	}

	/**
	 * Parse each line from the InputStream and check if the toCheckFor file should be ignored.
	 * 
	 * @param gitignore
	 * @param currentDir the current parsing directory
	 * @param toCheckFor
	 * @return <code>true</code> if the given path should be ignored
	 * @throws IOException 
	 */
	private boolean parseGitIgnore(InputStream gitignore, File currentDir, File toCheckFor) 
	throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(gitignore));
		String line;
		if ((line = br.readLine()) != null) {
			if (parseLine(line, currentDir, toCheckFor)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * From the gitignore documentation:
	 * 
	 *  Patterns have the following format:
	 *  <ul>
     *  <li>A blank line matches no files, so it can serve as a separator for readability.</li>
     *  <li>A line starting with # serves as a comment.</li>
     *  <li>An optional prefix ! which negates the pattern; any matching file excluded by a previous pattern 
     *      will become included again. If a negated pattern matches, this will override
     *      lower precedence patterns sources.</li>
     *  <li>If the pattern ends with a slash, it is removed for the purpose of the following description, 
     *      but it would only find a match with a directory. In other words, foo/ will match
     *      a directory foo and paths underneath it, but will not match a regular file or a symbolic link 
     *      foo (this is consistent with the way how pathspec works in general in git).</li>
     *  <li>If the pattern does not contain a slash /, git treats it as a shell glob pattern and checks 
     *      for a match against the pathname without leading directories.</li>
     *  </ul>
	 * @param line
	 * @param currentDir the current parsing directory
	 * @param toCheckFor
	 * @return <code>true</code> if the given path should be ignored
	 */
	private boolean parseLine(String line, File currentDir, File toCheckFor) {
		String pattern = line.trim();

		boolean matchResult = true;
		
		// empty line
		if (pattern.length() == 0) {
			return false;
		}
		
		// comment
		if (pattern.startsWith("#")) {
			return false;
		}
		
		// invert the search
		if (pattern.startsWith("!")) {
			matchResult = false;
		}
		
		// check for absolute paths
		if (pattern.startsWith("/") && pattern.length() > 1) {
			if (toCheckFor.equals(new File(currentDir, pattern.substring(1)))) {
				return matchResult;
			}
		} else {
			if (pattern.equals(toCheckFor.getName())) {
				return matchResult;
			}
		}
		
		//X TODO there are  possibly still a few matching rules missing!
		return false;
	}
	
}