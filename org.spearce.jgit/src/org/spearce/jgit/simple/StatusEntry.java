package org.spearce.jgit.simple;

import java.io.File;

/**
 * status a single file (or directory) in the Repository
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
 * </table>
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

	private File filePath;
	private IndexStatus indexStatus;
	private RepoStatus repoStatus;
	
	/**
	 * default ct without setting any members
	 */
	public StatusEntry() {
		// no initialisation
	}
	/**
	 * ct which initialises all the members 
	 * @param filePath the file/directory the status is for
	 * @param indexStatus this represents the status of the file in the working directory compared to the Index.
	 * @param repoStatus the status of the index compared to the repository
	 */
	public StatusEntry(File filePath, IndexStatus indexStatus, RepoStatus repoStatus) {
		this.filePath = filePath;
		this.indexStatus = indexStatus;
		this.repoStatus = repoStatus;
	}
	
	/**
	 * @return file or directory path 
	 */
	public File getFilePath() {
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
	
	
	
}
