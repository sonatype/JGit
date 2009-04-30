/*
 * Copyright (C) 2009, Shawn O. Pearce <spearce@spearce.org>
 * Copyright (C) 2009, Mark Struberg <struberg@yahoo.org>
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.spearce.jgit.errors.NoRemoteRepositoryException;
import org.spearce.jgit.errors.NotSupportedException;
import org.spearce.jgit.errors.RevisionSyntaxException;
import org.spearce.jgit.errors.TransportException;
import org.spearce.jgit.lib.Commit;
import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.lib.GitIndex;
import org.spearce.jgit.lib.NullProgressMonitor;
import org.spearce.jgit.lib.ProgressMonitor;
import org.spearce.jgit.lib.Ref;
import org.spearce.jgit.lib.RefComparator;
import org.spearce.jgit.lib.RefUpdate;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.lib.RepositoryConfig;
import org.spearce.jgit.lib.Tree;
import org.spearce.jgit.lib.WorkDirCheckout;
import org.spearce.jgit.transport.FetchResult;
import org.spearce.jgit.transport.RefSpec;
import org.spearce.jgit.transport.RemoteConfig;
import org.spearce.jgit.transport.Transport;
import org.spearce.jgit.transport.URIish;

/**
 * High level operations to work with a {@code Repository}
 * This class contains a lot  
 */
public class SimpleRepository {

	/**
	 * the Repository all the operations should work on
	 */
	private Repository db;
	
	/**
	 * the HEAD after a fetch
	 */
	private Ref head;
	
	/**
	 * Factory method for creating a SimpleRepository analog to git-init
	 * in a working directory. 
	 * @param workdir
	 * @return the freshly initialised {@link SimpleRepository}
	 * @throws IOException 
	 */
	public static SimpleRepository init(File workdir) 
	throws IOException {
		SimpleRepository repo = new SimpleRepository();
		repo.gitInit(workdir);
		return repo;
	}
	
	/**
	 * Factory method for creating a SimpleRepository analog to git-clone
	 * in a working directory. 
	 * Please note that this function doesn't perform a checkout!
	 * 
	 * @param workdir
	 * @param remoteName 
	 * @param uri 
	 * @param branch 
	 * @param monitor for showing the progress. If <code>null</code> a {@code NullProgressMonitor} will be used
	 * @return the freshly cloned {@link SimpleRepository}
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public static SimpleRepository clone(File workdir, String remoteName, URIish uri, String branch, ProgressMonitor monitor) 
	throws IOException, URISyntaxException {
		SimpleRepository repo = new SimpleRepository();
		repo.gitInit(workdir);
		repo.gitClone(remoteName, uri, branch, true, null);
		repo.gitFetch(remoteName, monitor);
		return repo;
	}
	
	/**
	 * Factory method for creating a SimpleRepository based on a 
	 * existing {@link Repository}
	 * @param db the {@link Repository} to wrap
	 * @return a new SimpleRepository which uses the given {@link Repository}
	 */
	public static SimpleRepository wrap(Repository db) {
		SimpleRepository repo = new SimpleRepository();
		repo.db = db;
		return repo;
	}

	/**
	 * A SimpleRepository may only be created with one of the factory methods.
	 * @see #init(File)
	 * @see #clone(File, String, URIish, String, ProgressMonitor)
	 * @see #wrap(Repository)
	 */
	private SimpleRepository() {
		// private ct to disallow external object creation
	}
		
	/**
	 * @return the underlying {@link Repository}
	 */
	public Repository getRepository() {
		return db;
	}
	
	/**
	 * Close the underlying repository
	 */
	public void close() {
		db.close();
	}

	
	/**
	 * Init a fresh local .git {@code Repository}
	 * @param workdir for the repository
	 * @throws IOException 
	 */
	public void gitInit(File workdir) 
	throws IOException {
		gitInit(workdir, ".git");
	}

	/**
	 * Init a freshl local {@code Repository} int the gitDir
	 * @param workdir for the repository
	 * @param gitDir usually <code>.git</code>
	 * @throws IOException 
	 */
	public void gitInit(File workdir, String gitDir) 
	throws IOException {
		final File repoDir = new File(workdir, gitDir);
		db = new Repository(repoDir);
		db.create();

		db.getConfig().setBoolean("core", null, "bare", false);
		db.getConfig().save();
	}
	
	/**
	 * Setup a clone from a remote repo.
	 * 
	 * @param remoteName like 'origin'
	 * @param uri to clone from 
	 * @param branchName to clone initially, e.g. <code>master</code>
	 * @param allSelected
	 *            true when all branches have to be fetched (indicates wildcard
	 *            in created fetch refspec), false otherwise.
	 * @param selectedBranches
	 *            collection of branches to fetch. Ignored when allSelected is
	 *            true.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public void gitClone(final String remoteName, final URIish uri, final String branchName, 
			final boolean allSelected, final Collection<Ref> selectedBranches) 
	throws URISyntaxException, IOException {
		if (branchName == null) {
			throw new NoRemoteRepositoryException(uri, "cannot checkout; no HEAD advertised by remote");
		}
		
		// add remote configuration
		final RemoteConfig rc = new RemoteConfig(db.getConfig(), remoteName);
		rc.addURI(uri);

		final String dst = Constants.R_REMOTES + rc.getName();
		RefSpec wcrs = new RefSpec();
		wcrs = wcrs.setForceUpdate(true);
		wcrs = wcrs.setSourceDestination(Constants.R_HEADS + "*", dst + "/*");

		if (allSelected) {
			rc.addFetchRefSpec(wcrs);
		} else {
			for (final Ref ref : selectedBranches)
				if (wcrs.matchSource(ref))
					rc.addFetchRefSpec(wcrs.expandFromSource(ref));
		}

		rc.update(db.getConfig());
		db.getConfig().save();

		// setup the default remote branch for branchName
		db.getConfig().setString(RepositoryConfig.BRANCH_SECTION,
				branchName, "remote", remoteName);
		db.getConfig().setString(RepositoryConfig.BRANCH_SECTION,
				branchName, "merge", Constants.R_HEADS + branchName);

		db.getConfig().save();
	}

	/**
	 * Fetch all new objects from the foreign repo
	 * @param remoteName like 'origin'
	 * @param monitor for showing the progress. If <code>null</code> a {@code NullProgressMonitor} will be used
	 * @throws URISyntaxException 
	 * @throws NotSupportedException 
	 * @throws TransportException 
	 */
	public void gitFetch(final String remoteName, ProgressMonitor monitor) 
	throws NotSupportedException, URISyntaxException, TransportException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		final Transport tn = Transport.open(db, remoteName);
		
		FetchResult r;
		try {
			r = tn.fetch(monitor, null);
		} finally {
			tn.close();
		}
		assert r != null;
		head = guessHEAD(r);
		assert head != null;
	}

	/**
	 * TODO review if we really need this!
	 * @param result
	 * @return HEAD 
	 */
	private Ref guessHEAD(final FetchResult result) {
		final Ref idHEAD = result.getAdvertisedRef(Constants.HEAD);
		final List<Ref> availableRefs = new ArrayList<Ref>();
		Ref head = null;
		for (final Ref r : result.getAdvertisedRefs()) {
			final String n = r.getName();
			if (!n.startsWith(Constants.R_HEADS))
				continue;
			availableRefs.add(r);
			if (idHEAD == null || head != null)
				continue;
			if (r.getObjectId().equals(idHEAD.getObjectId()))
				head = r;
		}
		Collections.sort(availableRefs, RefComparator.INSTANCE);
		if (idHEAD != null && head == null)
			head = idHEAD;
		return head;
	}

	/**
	 * @param remoteName
	 */
	public void gitPull(String remoteName) {
		//X TODO
	}
	
	/**
	 * @param uri
	 * @param branch
	 */
	public void gitPull(URIish uri, String branch) {
		//X TODO
	}

	/**
	 * @param branch or refspec, e.g. &quot;master&quot;
	 * @param monitor for showing the progress. If <code>null</code> a {@code NullProgressMonitor} will be used
	 * @throws IOException 
	 */
	public void gitCheckout(String branch, ProgressMonitor monitor) throws IOException {
		if (branch == null) {
			throw new IllegalArgumentException("branch must not be null");
		}
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		if (!Constants.HEAD.equals(branch)) {
			db.writeSymref(Constants.HEAD, branch);
		}
		
		Ref branchRef = head;
		if (branchRef == null) {
			throw new RevisionSyntaxException(branch, "cannot find branch ");
		}
		
		final Commit commit = db.mapCommit(branchRef.getObjectId());
		final RefUpdate u = db.updateRef(Constants.HEAD);
		u.setNewObjectId(commit.getCommitId());
		u.forceUpdate();

		final GitIndex index = new GitIndex(db);
		final Tree tree = commit.getTree();
		final WorkDirCheckout co;

		co = new WorkDirCheckout(db, db.getWorkDir(), index, tree);
		co.checkout();
		index.write();
	}


}
