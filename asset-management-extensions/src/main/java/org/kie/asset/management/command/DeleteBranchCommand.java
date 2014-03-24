package org.kie.asset.management.command;

import org.eclipse.jgit.api.Git;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class DeleteBranchCommand extends GitCommand {

    @Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {

        WorkItem workItem = (WorkItem) commandContext.getData("workItem");
        String gitRepo = (String) workItem.getParameter("GitRepository");

        String branchName = (String) workItem.getParameter("BranchName");
        String force = (String) workItem.getParameter("Force");

        boolean forceDelete = true;
        if (force != null) {
            forceDelete = Boolean.parseBoolean(force);
        }

        Git git = get(gitRepo);
        git.branchDelete().setBranchNames(branchName).setForce(forceDelete).call();

        ExecutionResults results = new ExecutionResults();

        return results;
    }
}
