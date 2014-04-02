package org.kie.asset.management.command;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;


public abstract class GitCommand extends AbstractCommand {

    private static final String REPOSITORIES_LOCATION = System.getProperty("org.uberfire.nio.git.dir", ".niogit");

    protected Git get(String repository) throws IOException {

        return get(REPOSITORIES_LOCATION, repository);
    }
    
    protected Git get(String location, String repository) throws IOException {
        File repositoryRoot = new File(location + File.separator + repository);

        if (repositoryRoot.exists()) {
            return Git.open(repositoryRoot);
        }

        throw new IllegalArgumentException("No repository under " + repository);
    }
}
