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

package org.eclipse.jgit.simple;

/**
 * Status of a single file (or directory) in the Repository
 * 
 * The following situations may appear:
 * <table summary="possible file status cases">
 * 	<tr><th>IndexStatus</th><th>RepoStatus</th><th>Comment</th></tr>
 * 	<tr><td>UNTRACKED</td><td>UNTRACKED</td><td>file is not checked in</td></tr>				
 * 	<tr><td>ADDED</td><td>UNTRACKED</td><td>file got added to the index</td></tr>			
 * 	<tr><td>MODIFIED</td><td>UNTRACKED</td><td>file got changed afterwards</td></tr>
 * 	<tr><td>ADDED</td><td>UNTRACKED</td><td>changes of file got added to the index again</td></tr>
 * 	<tr><td>UNCHANGED</td><td>UNCHANGED</td><td>file got committed. the sha1 on disk/index/repo is identical now.</td></tr> 
 * 	<tr><td>MODIFIED</td><td>UNCHANGED</td><td>file got changed on the filesystem</td></tr>
 * 	<tr><td>ADDED</td><td>ADDED</td><td>file got added to the index. The sha1 on disk and index are identical, but repo is different.</td></tr>
 * 	<tr><td>MODIFIED</td><td>ADDED</td><td>file got again changed on the filesystem</td></tr>
 * 	<tr><td>DELETED</td><td>ADDED</td><td>file got deleted locally</td></tr>
 * 	<tr><td>DELETED</td><td>REMOVED</td><td>deletion got git-added. file is marked as to be removed from the repository.</td></tr>
 * 	<tr><td>UNTRACKED</td><td>UNTRACKED</td><td>deletion got committed</td></tr>				
 * 	<tr><td>UNTRACKED</td><td>REMOVED</td><td>still available in the working directory but marked for deletion in the Index, e.g. via `git rm --cached foo`</td></tr>				
 * </table>
 * 
 * @see SimpleRepository#status()
 * @see SimpleRepository#status(boolean, boolean)
 */
public class StatusEntry {


	/**
	 * Possible index status of each file in the repository.
	 * This is basically a comparison of the status of the file in the 
	 * working directory and in the Index.  
	 */
	public static enum IndexStatus {
		/**
		 * file has been created on the filesystem but is not yet tracked via git 
		 */
		UNTRACKED,
		
		/**
		 * file has been git-added and is now cached in the Index 
		 */
		ADDED, 
		
		/**
		 * file is tracked in the Index, but modified on the local file system 
		 */
		MODIFIED, 
		
		/**
		 * file has been deleted locally on the filesystem, but is still in the Index. 
		 */
		DELETED, 
		
		/**
		 * file is in the git Index but unchanged. The sha1 of the file on the filesystem
		 * and in the Index are identical.
		 */
		UNCHANGED
	}
	
	/**
	 * Possible status of the repository.
	 * This is the difference of a file in the Index and the HEAD of the Repository. 
	 */
	public static enum RepoStatus {
		/**
		 * file is not present in the repository.
		 */
		UNTRACKED,
		
		/**
		 * the file got added to the Index and is now different to the Repository.
		 */
		ADDED,
		
		/**
		 * file got marked as 'to be removed' in the Index with git-rm.
		 */
		REMOVED,
		
		/**
		 * file status of Index and HEAD are the same.
		 */
		UNCHANGED
		
		
	}

	private String filePath;
	private IndexStatus indexStatus;
	private RepoStatus repoStatus;
	
	/**
	 * ct which initialises all the members 
	 * @param filePath the file/directory the status is for
	 * @param indexStatus this represents the status of the file in the working directory compared to the Index.
	 * @param repoStatus the status of the index compared to the repository
	 */
	public StatusEntry(String filePath, IndexStatus indexStatus, RepoStatus repoStatus) {
		this.filePath = filePath;
		this.indexStatus = indexStatus;
		this.repoStatus = repoStatus;
	}
	
	/**
	 * @return file or directory path 
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * @return the status of the filesystem compared to the Index
	 */
	public IndexStatus getIndexStatus() {
		return indexStatus;
	}
	
	/**
	 * @return the status of the Index compared to the Repository
	 */
	public RepoStatus getRepoStatus() {
		return repoStatus;
	}
	
	
	/**
	 * String representation of this entry
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder("StatusEntry[");
		sb.append(filePath);
		sb.append(", ").append(indexStatus);
		sb.append(", ").append(repoStatus);
		sb.append("]");
		return sb.toString();
	}

}
