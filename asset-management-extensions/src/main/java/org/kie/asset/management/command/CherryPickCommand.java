package org.kie.asset.management.command;

import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.CherryPickResult.CherryPickStatus;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CherryPickCommand extends GitCommand {

	private static final Logger logger = LoggerFactory.getLogger(CherryPickCommand.class);
	
	@Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {

    	String gitRepoLocation = (String) getParameter(commandContext, "WorkingCopyDir");
        String gitRepo = (String) getParameter(commandContext, "GitRepository");
        String toBranchName = (String) getParameter(commandContext, "ToBranchName");
        String directPush = (String) getParameter(commandContext, "DirectPush");
        String commitsString = (String) getParameter(commandContext, "Commits");
        String[] commits = commitsString.split(",");
        //List<String> commits = (List<String>) getParameter(commandContext, "Commits");

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
        } else {
        	logger.warn("Cherry pick failed with outcome {} due to {}", outcome, result.getFailingPaths());
        }

        ExecutionResults results = new ExecutionResults();
        results.setData("CherryPickResult", outcome);

        return results;
    }
}
