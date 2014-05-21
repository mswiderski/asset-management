package org.kie.asset.management.command;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.jgit.api.Git;


public abstract class GitCommand extends AbstractCommand {
	
	// check if remote git server is enabled 
	protected Boolean GIT_SERVER_ENABLED = Boolean.parseBoolean(System.getProperty("org.uberfire.nio.git.ssh.enabled", "true"));
	
	// local access to repository
    private static final String REPOSITORIES_LOCATION = System.getProperty("org.uberfire.nio.git.dir", ".niogit");
    
    // remote access to repository
    private static final String REMOTE_REPOSITORIES_HOST = System.getProperty("org.uberfire.nio.git.ssh.host", "localhost");
    private static final String REMOTE_REPOSITORIES_PORT = System.getProperty("org.uberfire.nio.git.ssh.port", "8001");
    private static final String REMOTE_REPOSITORIES_USER = System.getProperty("org.uberfire.nio.git.ssh.user", "bpmadmin");
    private static final String REMOTE_REPOSITORIES_PASSWORD = System.getProperty("org.uberfire.nio.git.ssh.password", "admin1234;");
    private static final String REMOTE_REPOSITORIES_LOCATION = "ssh://{0}:{1}@{2}:{3}/{4}";

    protected Git get(String repository) throws IOException {

        return get(REPOSITORIES_LOCATION, repository);
    }
    
    protected Git get(String location, String repository) throws IOException {
    	if (location == null) {
    		location = REPOSITORIES_LOCATION;
    	}
        File repositoryRoot = new File(location + File.separator + repository);

        if (repositoryRoot.exists()) {
            return Git.open(repositoryRoot);
        }

        throw new IllegalArgumentException("No repository under " + repository);
    }
    
    protected String getRepositoryLocation(String repository) {
    	if (repository.endsWith(".git")) {
    		repository = repository.substring(0, repository.length() - 4);
    	}
    	return MessageFormat.format(REMOTE_REPOSITORIES_LOCATION, 
    			REMOTE_REPOSITORIES_USER,
    			REMOTE_REPOSITORIES_PASSWORD,
    			REMOTE_REPOSITORIES_HOST,
    			REMOTE_REPOSITORIES_PORT,
    			repository);
    }
    
    protected boolean isGitServerEnabled() {
    	return GIT_SERVER_ENABLED;
    }
}
