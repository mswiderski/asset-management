package org.kie.asset.management.command;

import org.eclipse.jgit.api.Git;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class CreateBranchCommand extends GitCommand {

    @Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {

        String gitRepo = (String) getParameter(commandContext, "GitRepository");

        String branchName = (String) getParameter(commandContext, "BranchName");
        String startPoint = (String) getParameter(commandContext, "StartPoint");
        String version = (String) getParameter(commandContext, "Version");
        if (version != null && !version.isEmpty()) {
        	branchName = branchName + "-" + version;
        }

        Git git = get(gitRepo);
        org.eclipse.jgit.api.CreateBranchCommand gitCmd = git.branchCreate().setName(branchName);
        if (startPoint != null) {
        	gitCmd.setStartPoint(startPoint);
        }
        gitCmd.call();
        ExecutionResults results = new ExecutionResults();

        return results;
    }
}
