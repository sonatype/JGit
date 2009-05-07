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

import org.spearce.jgit.dircache.DirCache;
import org.spearce.jgit.dircache.DirCacheBuildIterator;
import org.spearce.jgit.dircache.DirCacheBuilder;
import org.spearce.jgit.dircache.DirCacheEntry;
import org.spearce.jgit.errors.CommitException;
import org.spearce.jgit.errors.CorruptObjectException;
import org.spearce.jgit.errors.NotSupportedException;
import org.spearce.jgit.errors.RevisionSyntaxException;
import org.spearce.jgit.errors.TransportException;
import org.spearce.jgit.ignore.IgnoreRules;
import org.spearce.jgit.lib.Commit;
import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.lib.FileMode;
import org.spearce.jgit.lib.GitIndex;
import org.spearce.jgit.lib.NullProgressMonitor;
import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.ObjectWriter;
import org.spearce.jgit.lib.PersonIdent;
import org.spearce.jgit.lib.ProgressMonitor;
import org.spearce.jgit.lib.Ref;
import org.spearce.jgit.lib.RefComparator;
import org.spearce.jgit.lib.RefUpdate;
import org.spearce.jgit.lib.Repository;
import org.spearce.jgit.lib.RepositoryConfig;
import org.spearce.jgit.lib.Tree;
import org.spearce.jgit.lib.WorkDirCheckout;
import org.spearce.jgit.lib.GitIndex.Entry;
import org.spearce.jgit.lib.RefUpdate.Result;
import org.spearce.jgit.simple.LsFileEntry.FileStatus;
import org.spearce.jgit.transport.FetchResult;
import org.spearce.jgit.transport.PushResult;
import org.spearce.jgit.transport.RefSpec;
import org.spearce.jgit.transport.RemoteConfig;
import org.spearce.jgit.transport.RemoteRefUpdate;
import org.spearce.jgit.transport.Transport;
import org.spearce.jgit.transport.URIish;
import org.spearce.jgit.transport.RemoteRefUpdate.Status;
import org.spearce.jgit.treewalk.FileTreeIterator;
import org.spearce.jgit.treewalk.TreeWalk;
import org.spearce.jgit.treewalk.filter.PathFilter;
import org.spearce.jgit.treewalk.filter.TreeFilter;
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
	 * @param workDir of the repository
	 * @return the freshly initialised {@link SimpleRepository}
	 * @throws IOException 
	 */
	public static SimpleRepository init(File workDir) 
	throws IOException {
		Validate.notNull(workDir, "workdir must not be null!");
		SimpleRepository repo = new SimpleRepository();
		repo.initRepository(workDir, ".git");
		return repo;
	}
	
	/**
	 * Create a SimpleRepository for an already existing local git 
	 * repository structure.
	 * 
	 * @param workDir of the existing git repository 
	 * @return {@link SimpleRepository} or <code>null</code> if the given repoName doesn't contain a git repository
	 * @throws Exception 
	 */
	public static SimpleRepository existing(File workDir)
	throws Exception {
		Validate.notNull(workDir, "workdir must not be null!");
		
		final File repoDir = new File(workDir, ".git");
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
	 * in a working directory. This will also checkout the content.  
	 * 
	 * @param workDir
	 * @param remoteName 
	 * @param uri 
	 * @param branch 
	 * @param monitor for showing the progress. If <code>null</code> a {@code NullProgressMonitor} will be used
	 * @return the freshly cloned {@link SimpleRepository}
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public static SimpleRepository clone(File workDir, String remoteName, URIish uri, String branch, ProgressMonitor monitor) 
	throws IOException, URISyntaxException {
		SimpleRepository repo = new SimpleRepository();
		repo.initRepository(workDir, ".git");
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		repo.addRemote(remoteName, uri, branch, true, null);
		Ref head = repo.fetch(remoteName, monitor);
		repo.checkout(head.getObjectId(), head.getName());
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
		repo.ignores = new IgnoreRules(db);
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
	 * @return the name of the current branch
	 * @throws IOException 
	 */
	public String getBranch() 
	throws IOException {
		return db.getBranch();
	}
	
	/**
	 * Init a freshl local {@code Repository} int the gitDir
	 * @param workDir of the repository
	 * @param gitDir usually <code>.git</code>
	 * @throws IOException 
	 */
	private void initRepository(File workDir, String gitDir) 
	throws IOException {
		final File repoDir = new File(workDir, gitDir);
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
		Validate.notNull(branchName, "Branch name must not be null!");
		
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
		Validate.notNull(uri, "URI to fetch from must not be null!");
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		final Transport tn = Transport.open(db, uri);
		
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
	 * Commit the changes accumulated in the Index 
	 * @param author if <code>null</code> the default author of the {@code Repository} will be used 
	 * @param committer if <code>null</code> the author will be used 
	 * @param commitMsg commit message
	 * @throws IOException 
	 */
	public void commit(PersonIdent author, PersonIdent committer, String commitMsg) 
	throws IOException {
		Validate.notNull(commitMsg, "Commit message must not be null!");
		
		if (author == null) {
			author = new PersonIdent(db);
		}
		
		if (committer == null) {
			committer = author;
		}
		
		ObjectId currentHeadId = db.resolve(Constants.HEAD);
		ObjectId[] parentIds = new ObjectId[]{currentHeadId};

		Commit commit = new Commit(db, parentIds);
		commit.setAuthor(author);
		commit.setCommitter(committer);
		commit.setMessage(commitMsg);

		final ObjectWriter ow = new ObjectWriter(db);
		final DirCache dc = DirCache.lock(db);
		commit.setTreeId(dc.writeTree(ow));
		commit.setCommitId(ow.writeCommit(commit));
		dc.unlock();

		final RefUpdate ru = db.updateRef(Constants.HEAD);
		ru.setNewObjectId(commit.getCommitId());
		ru.setRefLogMessage(buildReflogMessage(commitMsg, false), false);
		if (ru.forceUpdate() == RefUpdate.Result.LOCK_FAILURE) {
			throw new CommitException(commit.getCommitId(), "reflog locked!"); 
		}

		//X TODO remove debug info!
		System.out.println("commit: objectId=" + commit.getCommitId());
	}

	/**
	 * Convenience function to push a branch with a given name to a uri.
	 * 
	 * @see #push(ProgressMonitor, URIish, String, String, String, boolean, boolean, String) 
	 * @param monitor
	 * @param uri
	 * @param branchName will be used as name for both the local and remote branch
	 * @return <code>true</code> if the push was ok 
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public boolean push(ProgressMonitor monitor, URIish uri, String branchName) 
	throws URISyntaxException, IOException {
		return push(monitor, uri, null, branchName, branchName, false, false, null);
	}

	/**
	 * Convenience function to push a branch with a given name to a remote specified via it's name.
	 * 
	 * @see #push(ProgressMonitor, URIish, String, String, String, boolean, boolean, String) 
	 * @param monitor
	 * @param remoteName e.g. &quot;origin&qout;
	 * @param branchName will be used as name for both the local and remote branch
	 * @return <code>true</code> if the push was ok 
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public boolean push(ProgressMonitor monitor, String remoteName, String branchName) 
	throws URISyntaxException, IOException {
		return push(monitor, null, remoteName, branchName, branchName, false, false, null);
	}

	/**
	 * Push the commits from the branchLocale to the branchRemote on the repo
	 * with the uri.
	 * 
	 * @param monitor for showing the progress. If <code>null</code> a {@code NullProgressMonitor} will be used
	 * @param uri if given, we will use that very uri to push too
	 * @param remoteName if no uri is given, we will use this remote to push to
	 * @param localBranchName
	 * @param remoteBranchName
	 * @param pushAllBranches
	 * @param pushTags
	 * @param receivePack Path to the git-receive-pack program on the remote end. Sometimes useful when pushing 
	 * 		to a remote repository over ssh, and you do not have the program in a directory on the default $PATH.
	 * @return <code>true</code> if the push was ok 
	 * @throws IOException 
	 * @throws URISyntaxException
	 */
	public boolean push(ProgressMonitor monitor, URIish uri, String remoteName, String localBranchName, 
			            String remoteBranchName, boolean pushAllBranches, boolean pushTags,
			           String receivePack)
	throws URISyntaxException, IOException {
		Validate.isTrue(uri != null || remoteName != null, "either uri or remoteName must be set!");
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		if (remoteBranchName == null) {
			remoteBranchName = localBranchName;
		}
		
		List<RefSpec> refSpecs = new ArrayList<RefSpec>();
		
		if (pushAllBranches) {
			refSpecs.add(Transport.REFSPEC_PUSH_ALL);
		}
		
		if (pushTags) {
			refSpecs.add(Transport.REFSPEC_TAGS);
		}
		
		if (!pushAllBranches && localBranchName != null) {
			RefSpec rs = new RefSpec("+refs/heads/" + localBranchName +":refs/heads/" + remoteBranchName);
			refSpecs.add(rs);
		}
		
		final List<Transport> transports;
		
		if (uri != null) {
			transports = new ArrayList<Transport>();
			transports.add(Transport.open(db, uri));
		}
		else {
			transports = Transport.openAll(db, remoteName);
		}
		
		for (final Transport transport : transports) {
			if (receivePack != null) {
			 	transport.setOptionReceivePack(receivePack);
			}

			final Collection<RemoteRefUpdate> toPush = transport.findRemoteRefUpdatesFor(refSpecs);

			final PushResult result;
			try {
				result = transport.push(monitor, toPush);
			} finally {
				transport.close();
			}
			
			// evaluate PushResult and make upToDate Check
			for (final RemoteRefUpdate rru : result.getRemoteUpdates()) {
				Status status = rru.getStatus();
				if (status == Status.REJECTED_NODELETE ||
					status == Status.REJECTED_NONFASTFORWARD ||
					status == Status.REJECTED_OTHER_REASON ||
					status == Status.REJECTED_REMOTE_CHANGED ) {
					// oooh, we don't succeed!
					
					//X TODO we are not quite verbose here ...
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Checkout the given branch from the local repository.
	 * This command makes no remote connections!
	 * 
	 * @param branchName or refspec, e.g. &quot;master&quot;
	 * @param monitor for showing the progress. If <code>null</code> a {@code NullProgressMonitor} will be used
	 * @throws IOException 
	 */
	public void checkout(String branchName, ProgressMonitor monitor) 
	throws IOException {
		Validate.notNull(branchName, "Branch name must not be null!");
		
		if (!Constants.HEAD.equals(branchName)) {
			db.writeSymref(Constants.HEAD, branchName);
		}
		
		ObjectId headId = db.resolve(branchName);

		if (headId == null) {
			throw new RevisionSyntaxException(branchName, "cannot find head of branch ");
		}
		
		checkout(headId, branchName);
	}


	/**
	 * Add a given file or directory to the index.
	 * @param toAdd file or directory which should be added
	 * @param alsoRemove remove files missing from the directory?
	 * @throws Exception
	 */
	public void add(File toAdd, boolean alsoRemove)
	throws Exception {
		Validate.notNull(toAdd, "File toAdd must not be null!");
		final File root = db.getWorkDir();
		final String toAddCanon = toAdd.getCanonicalPath();
		final String rootCanon = root.getCanonicalPath();
		Validate.isTrue(toAddCanon.startsWith(rootCanon),
				"File toAdd must be within repository {0}!", root);

		final ObjectWriter ow = new ObjectWriter(db);
		final DirCache dc = DirCache.lock(db);
		final DirCacheBuilder edit = dc.builder();
		final TreeWalk tw = new TreeWalk(db);
		tw.reset();
		if (toAddCanon.equals(rootCanon))
			tw.setFilter(TreeFilter.ALL);
		else
			tw.setFilter(PathFilter.create(toAddCanon.substring(
					rootCanon.length() + 1).replace('\\', '/')));
		tw.addTree(new DirCacheBuildIterator(edit));
		tw.addTree(new FileTreeIterator(root));
		while (tw.next()) {
			final DirCacheBuildIterator i;
			final FileTreeIterator d;
			final DirCacheEntry e;
			
			if (tw.getRawMode(0) == 0) {
				// Entry doesn't yet exist in the index.  If its an ignored
				// path name, skip over the entry.
				//
				final File f = new File(root, tw.getPathString());
				if (ignores.isIgnored(f))
					continue;
			}

			if (tw.isSubtree()) {
				// The index doesn't allow trees directly, we need to
				// recurse and process only leaf nodes.
				//
				tw.enterSubtree();
				continue;
			}

			i = tw.getTree(0, DirCacheBuildIterator.class);
			d = tw.getTree(1, FileTreeIterator.class);

			if (tw.getRawMode(0) == 0) {
				e = new DirCacheEntry(tw.getRawPath());
				edit.add(e);

			} else if (tw.getRawMode(1) == 0) {
				// Entry is no longer in the directory, but is still in the
				// index.  If we aren't supposed to process removals, keep
				// the entry in the cache.
				//
				if (!alsoRemove)
					edit.add(i.getDirCacheEntry());
				continue;

			} else if (FileMode.SYMLINK.equals(tw.getFileMode(0))) {
				// Entry exists as a symlink. We can't process that in Java.
				//
				edit.add(i.getDirCacheEntry());
				continue;

			} else {
				e = i.getDirCacheEntry();
				edit.add(e);
			}

			final FileMode mode = d.getEntryFileMode();
			if (FileMode.GITLINK.equals(mode)) {
				// TODO: FileTreeIterator doesn't implement objectId right
				// for a GITLINK yet.
				//
				e.setLength(0);
				e.setLastModified(0);
				e.setObjectId(d.getEntryObjectId());

			} else if (e.getLength() != d.getEntryLength()
					|| !timestampMatches(e, d)) {
				final File f = new File(root, tw.getPathString());
				e.setLength((int) d.getEntryLength());
				e.setLastModified(d.getEntryLastModified());
				e.setObjectId(ow.writeBlob(f));
			}
			e.setFileMode(mode);
		}
		if (!edit.commit())
			throw new IOException("Can't update index");
	}

	/**
	 * Show information about files in the Index and the working tree.
	 * @param cached list cached files in the output
	 * @param deleted list deleted files in the output
	 * @param ignored list ignored files in the output
	 * @return list of all files 
	 * @throws IOException 
	 * @throws CorruptObjectException 
	 */
	public List<LsFileEntry> lsFiles(boolean cached, boolean deleted, boolean ignored) 
	throws CorruptObjectException, IOException {
		ArrayList<LsFileEntry> fileEntries = new ArrayList<LsFileEntry>();
		
		//X TODO for now this only reads the index, but we must also check for changes in the workDir!
		final DirCache cache = DirCache.read(db);
		for (int i = 0; i < cache.getEntryCount(); i++) {
			final DirCacheEntry ent = cache.getEntry(i);
			
			//X TODO this is surey not enough ;)
			FileStatus fs = FileStatus.CACHED;
			LsFileEntry fileEntry = new LsFileEntry(ent.getPathString(), fs, ent.getObjectId());
			fileEntries.add(fileEntry);
		}
		return fileEntries;
	}
	
	
	//------------------------------------
	// private helper functions
	//------------------------------------
	
	private static boolean timestampMatches(final DirCacheEntry indexEntry,
			final FileTreeIterator workEntry) {
		final long tIndex = indexEntry.getLastModified();
		final long tWork = workEntry.getEntryLastModified();

		// C-Git under Windows stores timestamps with 1-seconds resolution,
		// so we need to check to see if this is the case here, and possibly
		// fix the timestamp of the resource to match the resolution of the
		// index.
		//
		// It also appears the timestamp in Java on Linux may also be rounded
		// in which case the index timestamp may have subseconds, but not
		// the timestamp from the workspace resource.
		//
		// If either timestamp looks rounded we skip the subscond part.
		//
		if (tIndex % 1000 == 0 || tWork % 1000 == 0)
			return tIndex / 1000 == tWork / 1000;
		else
			return tIndex == tWork;
	}

	/**
	 * Fetch from the given remote and try to detect the advertised head.
	 * This function is used by {@code #clone(File, String, URIish, String, ProgressMonitor)} 
	 * @param remoteName
	 * @param monitor
	 * @return Ref with the detected head
	 * @throws NotSupportedException
	 * @throws URISyntaxException
	 * @throws TransportException
	 */
	private Ref fetch(final String remoteName, ProgressMonitor monitor) 
	throws NotSupportedException, URISyntaxException, TransportException {
		final Transport tn = Transport.open(db, remoteName);
		
		FetchResult fetchResult;
		try {
			fetchResult = tn.fetch(monitor, null);
		} finally {
			tn.close();
		}
		return guessHEAD(fetchResult);
	}
	
	/**
	 * guess the head from the advertised Ref of the FetchResult
	 * @param result
	 * @return Ref with the detected head
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
	 * Checkout the headId into the working directory
	 * @param headId
	 * @param branch internal branch name, e.g. refs/heads/master
	 * @throws IOException
	 */
	private void checkout(ObjectId headId, String branch) throws IOException {
		if (!Constants.HEAD.equals(branch))
			db.writeSymref(Constants.HEAD, branch);

		final Commit commit = db.mapCommit(headId);
		final RefUpdate u = db.updateRef(Constants.HEAD);
		u.setNewObjectId(commit.getCommitId());
		Result result = u.forceUpdate();
		
		//X TODO REMOVE DEBUGGING OUTPUT!
		System.out.println("updateRef " + u + " returned Result=" + result);

		final GitIndex index = db.getIndex();
		final Tree tree = commit.getTree();
		final WorkDirCheckout co;

		co = new WorkDirCheckout(db, db.getWorkDir(), index, tree);
		co.checkout();
		index.write();
		
		printIndex();
	}

	/**
	 * build the message for the ref log
	 * @param commitMessage
	 * @param amending if commit amends the previous one
	 * @return the reflog message
	 */
	private String buildReflogMessage(String commitMessage, boolean amending) {
		String firstLine = commitMessage;
		int newlineIndex = commitMessage.indexOf("\n");
		if (newlineIndex > 0) {
			firstLine = commitMessage.substring(0, newlineIndex);
		}
		String commitStr = amending ? "\tcommit (amend):" : "\tcommit: ";
		String message = commitStr + firstLine;
		return message;
	}


	/**
	 * This is only a test function and should be re/moved finally!
	 */
	public void printIndex() {
		GitIndex idx;
		try {
			idx = db.getIndex();
			
			System.out.println("Index is changed: " + idx.isChanged());
			Entry[] entries = idx.getMembers();
			for (Entry e : entries) {
				System.out.println(e.toString());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
}
