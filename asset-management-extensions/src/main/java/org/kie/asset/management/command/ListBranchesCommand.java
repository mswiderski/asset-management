package org.kie.asset.management.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.kie.api.runtime.process.WorkItem;
import org.kie.asset.management.model.BranchInfo;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class ListBranchesCommand extends GitCommand {

    @Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {

        WorkItem workItem = (WorkItem) commandContext.getData("workItem");
        String gitRepo = (String) workItem.getParameter("GitRepository");

        Git git = get(gitRepo);

        Iterable<Ref> branches = git.branchList().call();
        List<BranchInfo> branchInfos = new ArrayList<BranchInfo>();
        for (Ref branch : branches) {
        	branchInfos.add(new BranchInfo(branch.getObjectId().getName(), branch.getName()));
        }

        ExecutionResults results = new ExecutionResults();
        results.setData("Branches", branchInfos);
        return results;
    }
}
