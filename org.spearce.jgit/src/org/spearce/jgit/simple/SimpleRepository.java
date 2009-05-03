/*
 * Copyright (C) 2009, Shawn O. Pearce <spearce@spearce.org>
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.spearce.jgit.errors.NoRemoteRepositoryException;
import org.spearce.jgit.errors.RevisionSyntaxException;
import org.spearce.jgit.ignore.IgnoreRules;
import org.spearce.jgit.lib.Commit;
import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.lib.GitIndex;
import org.spearce.jgit.lib.NullProgressMonitor;
import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.ProgressMonitor;
import org.spearce.jgit.lib.Ref;
import org.spearce.jgit.lib.RefUpdate;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.lib.RepositoryConfig;
import org.spearce.jgit.lib.Tree;
import org.spearce.jgit.lib.WorkDirCheckout;
import org.spearce.jgit.lib.GitIndex.Entry;
import org.spearce.jgit.lib.RefUpdate.Result;
import org.spearce.jgit.transport.FetchResult;
import org.spearce.jgit.transport.RefSpec;
import org.spearce.jgit.transport.RemoteConfig;
import org.spearce.jgit.transport.Transport;
import org.spearce.jgit.transport.URIish;
import org.spearce.jgit.util.Validate;

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
	 * handle all ignore rules for this repository
	 */
	private IgnoreRules ignores;
	
	/**
	 * Factory method for creating a SimpleRepository analog to git-init
	 * in a working directory. 
	 * @param workdir
	 * @return the freshly initialised {@link SimpleRepository}
	 * @throws IOException 
	 */
	public static SimpleRepository init(File workdir) 
	throws IOException {
		Validate.notNull(workdir, "workdir must not be null!");
		SimpleRepository repo = new SimpleRepository();
		repo.initRepository(workdir, ".git");
		return repo;
	}
	
	/**
	 * Create a SimpleRepository for an already existing local git 
	 * repository structure.
	 * 
	 * @param workdir the directory with the existing git repository 
	 * @return {@link SimpleRepository} or <code>null</code> if the given workdir doesn't contain a git repository
	 * @throws Exception 
	 */
	public static SimpleRepository existing(File workdir)
	throws Exception {
		Validate.notNull(workdir, "workdir must not be null!");
		Validate.isTrue(workdir.exists(), "The workdir {0} doesn't exist!", workdir);
		
		final File repoDir = new File(workdir, ".git");
		if (!repoDir.exists()) {
			return null;
		}
		
		SimpleRepository repo = new SimpleRepository();

		repo.db = new Repository(repoDir);
		repo.ignores = new IgnoreRules(repo.db);

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
		repo.initRepository(workdir, ".git");
		repo.addRemote(remoteName, uri, branch, true, null);
		repo.fetch(uri, null);
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
	 * Init a freshl local {@code Repository} int the gitDir
	 * @param workdir for the repository
	 * @param gitDir usually <code>.git</code>
	 * @throws IOException 
	 */
	private void initRepository(File workdir, String gitDir) 
	throws IOException {
		final File repoDir = new File(workdir, gitDir);
		db = new Repository(repoDir);
		db.create();

		db.getConfig().setBoolean("core", null, "bare", false);
		db.getConfig().save();
		
		ignores = new IgnoreRules(db);
	}
	
	/**
	 * Setup a new remote reference.
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
	public void addRemote(final String remoteName, final URIish uri, final String branchName, 
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

		// setup the default remote branch for branchName
		db.getConfig().setString(RepositoryConfig.BRANCH_SECTION,
				branchName, "remote", remoteName);
		db.getConfig().setString(RepositoryConfig.BRANCH_SECTION,
				branchName, "merge", Constants.R_HEADS + branchName);

		db.getConfig().save();
	}

	/**
	 * Fetch all new objects from the given branches/tags (want) 
	 * from the foreign uri.
	 * 
	 * @param uri either a foreign git uri 
	 * @param monitor for showing the progress. If <code>null</code> a {@code NullProgressMonitor} will be used
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	  public void fetch(final URIish uri, ProgressMonitor monitor) 
	  throws URISyntaxException, IOException {
		Set<String> want = Collections.emptySet();
	    fetch(uri, want, monitor);
	  }
	  
	/**
	 * Fetch all new objects from the given branches/tags (want) 
	 * from the foreign uri.
	 * 
	 * @param uri either a foreign git uri 
	 * @param want Set of branches, tags, etc which should be fetched
	 * @param monitor for showing the progress. If <code>null</code> a {@code NullProgressMonitor} will be used
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public void fetch(final URIish uri, Set<String> want, ProgressMonitor monitor) 
	throws URISyntaxException, IOException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		String currentBranchName = db.getBranch();
		
		//X TODO this should finally use Transport.open(db, uri); but this balls out currently! 
		final Transport tn = Transport.open(db, "origin");
		
		FetchResult fetchResult;
		try {
			fetchResult = tn.fetch(monitor, null);
		} finally {
			tn.close();
		}
		final Ref head = fetchResult.getAdvertisedRef(Constants.HEAD);
		if (head == null || head.getObjectId() == null) {
			return;
		}

		//now add the Ref for /refs/heads/[branchname]  
		RefUpdate uMaster = db.updateRef(Constants.R_HEADS + currentBranchName);
		uMaster.setNewObjectId(head.getObjectId());
		Result result = uMaster.forceUpdate();
		
		//X TODO REMOVE DEBUGGING OUTPUT!
		System.out.println("updateRef " + uMaster + " returned Resulr=" + result);
		
		//X TODO Shawn, how can I write it to the disk? After the checkout I am missing /refs/heads/master 
	}


	/**
	 * Fetch the current branch from the given remoteName and merge the changes
	 * into the actual HEAD. 
	 * @param remoteName
	 */
	public void pull(String remoteName) {
		//X TODO
	}
	
	/**
	 * Fetch the indicated branch from the given remoteName and merge the changes
	 * into the actual HEAD. 
	 * @param uri
	 * @param branch
	 */
	public void pull(URIish uri, String branch) {
		//X TODO
	}

	/**
	 * Checkout the given branch from the local repository.
	 * This command makes no remote connections!
	 * 
	 * @param branchName or refspec, e.g. &quot;master&quot;
	 * @param monitor for showing the progress. If <code>null</code> a {@code NullProgressMonitor} will be used
	 * @throws IOException 
	 */
	public void checkout(String branchName, ProgressMonitor monitor) throws IOException {
		if (branchName == null) {
			throw new IllegalArgumentException("branch must not be null");
		}
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		/*X Shawn, please review this!
		if (!Constants.HEAD.equals(branch)) {
			db.writeSymref(Constants.HEAD, branch);
		}
		*/
		
		ObjectId headId = db.resolve(Constants.R_HEADS + branchName);

		if (headId == null) {
			throw new RevisionSyntaxException(branchName, "cannot find head of branch ");
		}
		
		final Commit commit = db.mapCommit(headId);
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

	/**
	 * Add a given file or directory to the index.
	 * @param toAdd file or directory which should be added
	 * @return a List with all added files
	 * @throws Exception
	 */
	public List<File> add(File toAdd) 
	throws Exception {
		List<File> addedFiles = new ArrayList<File>();
		
		if  (toAdd == null) {
			throw new IllegalArgumentException("toAdd must not be null!");
		}
		
		if (!toAdd.getAbsolutePath().startsWith(db.getWorkDir().getAbsolutePath())) {
			throw new IllegalArgumentException("toAdd must be within repository " + db.getWorkDir());
		}
		
		// the relative path inside the repo
		String repoPath =  toAdd.getAbsolutePath().substring(db.getWorkDir().getAbsolutePath().length());
		
		GitIndex index = db.getIndex();
		
		//check for ignored files!
		if (ignores.isIgnored(toAdd)) {
			return addedFiles;
		}

		if (toAdd.isDirectory()) {
			for(File f : toAdd.listFiles()) {
				// recursively add files
				addedFiles.addAll(add(f));
			}
		} else {
			Entry entry = index.getEntry(repoPath);
			if (entry != null) {
				if (!entry.isAssumedValid()) {
					System.out.println("Already tracked - skipping");
					return addedFiles;
				}
			}
		}

		//X TODO this should be implemented using DirCache!
		Entry entry = index.add(db.getWorkDir(), toAdd);
		entry.setAssumeValid(false);
		
		index.write();
		
		addedFiles.add(toAdd);
		
		return addedFiles;
	}
	
}
