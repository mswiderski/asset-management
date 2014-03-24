package org.kie.asset.management.command;

import org.eclipse.jgit.api.Git;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class CreateBranchCommand extends GitCommand {

    @Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {

        WorkItem workItem = (WorkItem) commandContext.getData("workItem");
        String gitRepo = (String) workItem.getParameter("GitRepository");

        String branchName = (String) workItem.getParameter("BranchName");
        String startPoint = (String) workItem.getParameter("StartPoint");

        Git git = get(gitRepo);
        git.branchCreate().setName(branchName).setStartPoint(startPoint).call();

        ExecutionResults results = new ExecutionResults();

        return results;
    }
}
