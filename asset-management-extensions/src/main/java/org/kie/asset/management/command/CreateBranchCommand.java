package org.kie.asset.management.command;

import org.eclipse.jgit.api.Git;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class CreateBranchCommand extends CloneRepositoryCommand {

    @Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {
    	String workingCopyLocation = null;
    	
    	if (isGitServerEnabled()) {
    		ExecutionResults cloneResults = super.execute(commandContext);
    		workingCopyLocation = (String) cloneResults.getData("WorkingCopyDir");
    	}
        String gitRepo = (String) getParameter(commandContext, "GitRepository");

        String branchName = (String) getParameter(commandContext, "BranchName");
        String startPoint = (String) getParameter(commandContext, "StartPoint");
        String version = (String) getParameter(commandContext, "Version");
        if (version != null && !version.isEmpty()) {
        	branchName = branchName + "-" + version;
        }

        Git git = get(workingCopyLocation, gitRepo);
        org.eclipse.jgit.api.CreateBranchCommand gitCmd = git.branchCreate().setName(branchName);
        if (startPoint != null) {
        	gitCmd.setStartPoint(startPoint);
        }
        gitCmd.call();
        ExecutionResults results = new ExecutionResults();
        
        if (isGitServerEnabled()) {
        	git.push().add(branchName).setForce(true).call();
        }

        return results;
    }
}
