package org.kie.asset.management.command;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kie.api.runtime.process.WorkItem;
import org.kie.asset.management.model.CommitInfo;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class ListBranchesCommand extends GitCommand {

    @Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {

        WorkItem workItem = (WorkItem) commandContext.getData("workItem");
        String gitRepo = (String) workItem.getParameter("GitRepository");
        String maxCount = (String) workItem.getParameter("MaxCount");
        String branchName = (String) workItem.getParameter("BranchName");

        int maxCommits = 10;
        if (maxCount != null) {
            maxCommits = Integer.parseInt(maxCount);
        }

        Git git = get(gitRepo);

        ObjectId branch = git.getRepository().resolve(Constants.HEAD);
        if (branchName != null) {
            branch = git.getRepository().resolve(branchName);
        }

        Iterable<RevCommit> logs = git.log().add(branch).setMaxCount(maxCommits).call();
        List<CommitInfo> commits = new ArrayList<CommitInfo>();
        for (RevCommit commit : logs) {
            String shortMessage = commit.getShortMessage();
            Date commitDate = new Date(commit.getCommitTime() * 1000L);

            commits.add(new CommitInfo(commit.getId().getName(), shortMessage, commit.getAuthorIdent().getName(), commitDate));
        }

        ExecutionResults results = new ExecutionResults();
        results.setData("Commits", commits);
        return results;
    }
}
