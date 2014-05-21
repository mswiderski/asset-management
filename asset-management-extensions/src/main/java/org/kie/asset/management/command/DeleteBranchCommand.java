package org.kie.asset.management.command;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.RefSpec;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class DeleteBranchCommand extends CloneRepositoryCommand {

    @Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {

    	String workingCopyLocation = null;
    	
    	if (isGitServerEnabled()) {
    		ExecutionResults cloneResults = super.execute(commandContext);
    		workingCopyLocation = (String) cloneResults.getData("WorkingCopyDir");
    	}
        String gitRepo = (String) getParameter(commandContext, "GitRepository");

        String branchName = (String) getParameter(commandContext, "BranchName");
        String force = (String) getParameter(commandContext, "Force");

        boolean forceDelete = true;
        if (force != null) {
            forceDelete = Boolean.parseBoolean(force);
        }

        Git git = get(workingCopyLocation, gitRepo);
        git.branchDelete().setBranchNames("refs/heads/" + branchName).setForce(forceDelete).call();

	      
	
        if (isGitServerEnabled()) {
        	//delete branch on remote 'origin'
        	RefSpec refSpec = new RefSpec()
            .setSource(null)
            .setDestination("refs/heads/" + branchName);
            git.push().setRefSpecs(refSpec).setRemote("origin").call();
  
        }
        ExecutionResults results = new ExecutionResults();

        return results;
    }
}
