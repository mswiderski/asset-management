package org.kie.asset.management.command;

import org.eclipse.jgit.api.Git;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class DeleteBranchCommand extends GitCommand {

    @Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {

        String gitRepo = (String) getParameter(commandContext, "GitRepository");

        String branchName = (String) getParameter(commandContext, "BranchName");
        String force = (String) getParameter(commandContext, "Force");

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
