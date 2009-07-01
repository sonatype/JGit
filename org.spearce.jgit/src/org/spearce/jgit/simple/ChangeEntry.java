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

package org.spearce.jgit.simple;

import java.util.Date;
import java.util.List;
import java.io.File;

/**
 * This bean represents one line in JGit's git-whatchanged equivalent 
 * @see SimpleRepository#whatchanged(org.spearce.jgit.revwalk.RevSort[], String, String, Date, Date, int)
 */
public class ChangeEntry {
	private String commitHash;
	private String treeHash;
	private String authorName;
	private String authorEmail;
	private Date   authorDate;
	private String committerName;
	private String committerEmail;
	private Date   committerDate;
	private String subject;
	private String body;
	private List<File> files;
	
	/**
	 * @return hash of the commit
	 */
	public String getCommitHash() {
		return commitHash;
	}
	
	/**
	 * @param commitHash
	 */
	public void setCommitHash(String commitHash) {
		this.commitHash = commitHash;
	}

	/**
	 * @return hash of the tree
	 */
	public String getTreeHash() {
		return treeHash;
	}
	
	/**
	 * @param treeHash
	 */
	public void setTreeHash(String treeHash) {
		this.treeHash = treeHash;
	}

	/**
	 * @return name of the author, excluding mail address
	 */
	public String getAuthorName() {
		return authorName;
	}
	
	/**
	 * @param authorName
	 */
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	/**
	 * @return email address of the author
	 */
	public String getAuthorEmail() {
		return authorEmail;
	}
	
	/**
	 * @param authorEmail
	 */
	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}

	/**
	 * @return date of the authors changes
	 */
	public Date getAuthorDate() {
		return authorDate;
	}
	
	/**
	 * @param authorDate
	 */
	public void setAuthorDate(Date authorDate) {
		this.authorDate = authorDate;
	}

	/**
	 * @return name of the committer, excluding his email
	 */
	public String getCommitterName() {
		return committerName;
	}
	
	/**
	 * @param committerName
	 */
	public void setCommitterName(String committerName) {
		this.committerName = committerName;
	}

	/**
	 * @return email address of the committer
	 */
	public String getCommitterEmail() {
		return committerEmail;
	}
	
	/**
	 * @param committerEmail
	 */
	public void setCommitterEmail(String committerEmail) {
		this.committerEmail = committerEmail;
	}

	/**
	 * @return date  when the change got committed
	 */
	public Date getCommitterDate() {
		return committerDate;
	}
	
	/**
	 * @param committerDate
	 */
	public void setCommitterDate(Date committerDate) {
		this.committerDate = committerDate;
	}

	/**
	 * @return subject is the first line of the commit message
	 */
	public String getSubject() {
		return subject;
	}
	
	/**
	 * @param subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return 2nd - n-th line of the commit message
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @param files which have been changed in this commit
	 */
	public void setFiles(List<File> files) {
		this.files = files;
	}

	/**
	 * @return files which have been changed in this commit
	 */
	public List<File> getFiles() {
		return files;
	}
}
