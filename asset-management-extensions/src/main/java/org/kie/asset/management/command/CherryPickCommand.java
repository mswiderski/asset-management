package org.kie.asset.management.command;

import java.util.List;

import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.CherryPickResult.CherryPickStatus;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class CherryPickCommand extends GitCommand {

    @SuppressWarnings("unchecked")
	@Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {

    	String gitRepoLocation = (String) getParameter(commandContext, "WorkingCopyDir");
        String gitRepo = (String) getParameter(commandContext, "GitRepository");
        String toBranchName = (String) getParameter(commandContext, "ToBranchName");
        String directPush = (String) getParameter(commandContext, "DirectPush");

        List<String> commits = (List<String>) getParameter(commandContext, "Commits");

        boolean push = true;
        if (directPush != null) {
        	push = Boolean.parseBoolean(directPush);
        }
        
        Git git = get(gitRepoLocation, gitRepo);
        
        git.checkout().setCreateBranch(true).setName(toBranchName)
        .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
        .setStartPoint("origin/"+toBranchName).call();
        
        org.eclipse.jgit.api.CherryPickCommand gitCommand = git.cherryPick();
        
        for (String commit : commits) {
        	ObjectId commitId = git.getRepository().resolve(commit);
        	gitCommand.include(commitId);
        }
        
        CherryPickResult result = gitCommand.call();
        String outcome = result.getStatus().name();
        
        if (result.getStatus().equals(CherryPickStatus.OK) && push) {
        	git.push().setForce(true).call();
        }

        ExecutionResults results = new ExecutionResults();
        results.setData("CherryPickResult", outcome);

        return results;
    }
}
