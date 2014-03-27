package org.kie.asset.management.command;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class CloneRepositoryCommand extends GitCommand {

    @Override
    public ExecutionResults execute(CommandContext commandContext) throws Exception {

        String gitRepo = (String) getParameter(commandContext, "GitRepository");
        
        String fromURI = "file:////" + System.getProperty("org.uberfire.nio.git.dir", ".niogit") + File.separator + gitRepo;
        File workingCopyDir = new File(System.getProperty("java.io.tmpdir")  + System.currentTimeMillis() + File.separator + gitRepo);
        Git.cloneRepository()
        .setBare( false )
        .setCloneAllBranches( true )
        .setURI( fromURI )
        .setDirectory( workingCopyDir )
        .call();

        ExecutionResults results = new ExecutionResults();
        results.setData("WorkingCopyDir", workingCopyDir.getParentFile().getAbsolutePath());

        return results;
    }
}
