package org.kie.asset.management.command;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.executor.api.Command;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class GitCommand implements Command {

    private static final String REPOSITORIES_LOCATION = System.getProperty("org.uberfire.nio.git.dir", ".niogit");

    protected Git get(String repository) throws IOException {
        File repositoryRoot = new File(REPOSITORIES_LOCATION + File.separator + repository);

        if (repositoryRoot.exists()) {
            return Git.open(repositoryRoot);
        }

        throw new IllegalArgumentException("No repository under " + repository);
    }

}
