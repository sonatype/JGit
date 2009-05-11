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

package org.spearce.jgit.pgm;


import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.Option;
import org.spearce.jgit.simple.SimpleRepository;
import org.spearce.jgit.simple.StatusEntry;

class Status extends TextBuiltin {
	@Option(name = "--unchanged", aliases = { "-U" }, usage = "also list unchanged files")
	private boolean listUnchanged;

	@Override
	protected void run() throws Exception {
		final SimpleRepository sr = SimpleRepository.wrap(db);
		List<StatusEntry> status = sr.status(listUnchanged, true);
		printStatus(sr, status);
	}

	private void printStatus(SimpleRepository sr, List<StatusEntry> status) 
	throws IOException {
		System.out.println("On branch " + sr.getBranch());
		
		//X TODO this doesn't look like git-branch yet
		for (StatusEntry se : status) {
			System.out.println("file: " + se.getFilePath() +" IndexStatus=" + se.getIndexStatus() + " RepoStatus=" + se.getRepoStatus());
		}
		
	}
}
