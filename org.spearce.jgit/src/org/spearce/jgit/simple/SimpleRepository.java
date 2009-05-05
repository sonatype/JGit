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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.spearce.jgit.errors.CommitException;
import org.spearce.jgit.errors.NotSupportedException;
import org.spearce.jgit.errors.RevisionSyntaxException;
import org.spearce.jgit.errors.TransportException;
import org.spearce.jgit.ignore.IgnoreRules;
import org.spearce.jgit.lib.Commit;
import org.spearce.jgit.lib.Constants;
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
		//X TODO commit.setEncoding();

		GitIndex index = db.getIndex();
		Entry[] idxEntries = index.getMembers();

		// create the Tree from the Index
		/*X DELETE THIS 
	 	Tree t = new Tree(db);
		for (Entry e: idxEntries) {
			t.addFile(e.getName());
			t.setId(e.getObjectId());
		}
		*/
		/*X DELETE THIS!
		Tree t = db.mapTree(Constants.HEAD);
		
		for (Entry e: idxEntries) {
			String repoRelativePath = e.getName();
			
			TreeEntry treeMember = t.findBlobMember(repoRelativePath);
			// we always want to delete it from the current tree, since if it's
			// updated, we'll add it again
			if (treeMember != null) {
				treeMember.delete();
			}
			t.addFile(repoRelativePath);
			TreeEntry newMember = t.findBlobMember(repoRelativePath);

			newMember.setId(e.getObjectId());
			System.out.println("New member id for " + repoRelativePath
					+ ": " + newMember.getId() + " idx id: "
					+ e.getObjectId());
		}
		*/
		ObjectId indexId = index.writeTree();
		Tree t = db.mapTree(indexId);
		commit.setTree(t);

		
		ObjectWriter writer = new ObjectWriter(db);
		commit.setCommitId(writer.writeCommit(commit));

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
	 * Push the commits from the branchLocale to the branchRemote on the repo
	 * with the uri.
	 * @param uri
	 * @param branchLocale
	 * @param branchRemote
	 */
	public void push(URIish uri, String branchLocale, String branchRemote) {
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
	 * @return a List with all added files
	 * @throws Exception
	 */
	public List<File> add(File toAdd) 
	throws Exception {
		Validate.notNull(toAdd,"File toAdd must not be null!");
		
		Validate.isTrue(toAdd.getAbsolutePath().startsWith(db.getWorkDir().getAbsolutePath()),
				        "File toAdd must be within repository {0}!", db.getWorkDir());
		
		List<File> addedFiles = new ArrayList<File>();
				
		GitIndex index = db.getIndex();
		index.read();
		
		//recursively add files and directories
		add(index, addedFiles, toAdd);
		
		index.write();
		
		addedFiles.add(toAdd);
		
		return addedFiles;
	}

	
	private void add(GitIndex index, List<File> addedFiles,	File toAdd) 
	throws FileNotFoundException, IOException, Exception, UnsupportedEncodingException {
		// the relative path inside the repo
		String repoPath =  toAdd.getAbsolutePath().substring(db.getWorkDir().getAbsolutePath().length());

		//check for ignored files!
		if (ignores.isIgnored(toAdd)) {
			return;
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
					return;
				}
			}
		}

		//X TODO this should be implemented using DirCache!
		Entry entry = index.add(db.getWorkDir(), toAdd);
		entry.setAssumeValid(false);
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
