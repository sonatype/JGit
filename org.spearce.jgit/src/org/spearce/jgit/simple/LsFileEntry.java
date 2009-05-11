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

import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.util.Validate;

/**
 * This class represents one line of the information returned by
 * {@code SimpleRepository#lsFiles(boolean, boolean, boolean)} 
 *
 */
public class LsFileEntry {
	/**
	 * Possible status of each file 
	 */
	public static enum LsFileStatus {
		/**
		 * file is cached in the Index 
		 */
		CACHED, 
		/**
		 * file is unmerged 
		 */
		UNMERGED, 
		/**
		 * file has been removed from the working folder 
		 */
		REMOVED, 
		/**
		 * file has been changed in the working folder 
		 */
		CHANGED, 
		/**
		 * files on the filesystem need to be removed due to file/directory conflicts for checkout-index to succeed 
		 */
		KILLED, 
		/**
		 * file has other/undefined/unknown status 
		 */
		OTHER
	}
	
	private String 	     filePath;
	private LsFileStatus status;
	private ObjectId     objectId;

	/**
	 * @param filePath
	 * @param status
	 * @param objectId
	 */
	public LsFileEntry(String filePath, LsFileStatus status, ObjectId objectId) {
		Validate.notNull(filePath, "filePath must not be null");
		
		this.filePath = filePath;
		
		if (status == null) {
			this.status = LsFileStatus.OTHER;
		}
		else {
			this.status = status;
		}
		
		this.objectId = objectId;
	}
	
	/**
	 * @return the file this entry is for 
	 */
	public String getFilePath() {
		return filePath;
	}

	
	/**
	 * @param filePath the file this entry is for
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	
	/**
	 * @return status of this entry
	 */
	public LsFileStatus getStatus() {
		return status;
	}
	
	/**
	 * @param status of this entry
	 */
	public void setStatus(LsFileStatus status) {
		this.status = status;
	}
	
	/**
	 * @return the SHA-1 ObjectId on the Index if is existing
	 */
	public ObjectId getObjectId() {
		return objectId;
	}
	
	/**
	 * @param objectId the SHA-1 ObjectId on the Index if is existing
	 */
	public void setObjectId(ObjectId objectId) {
		this.objectId = objectId;
	}
	
	/**
	 * String representation of this entry
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder("FileEntry[");
		sb.append(filePath);
		sb.append(", ").append(status);
		sb.append(", ").append(objectId);
		sb.append("]");
		return sb.toString();
	}
}
