package org.kie.org.jbpm.asset.management;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public abstract class AbstractTestCase {
	
	protected static final String TEMP_LOCATION = System.getProperty("java.io.tmpdir") + File.separator + ".niogit";
	
	protected void setupTestGitRepo(String fromURI, String alias, String username, String password) throws Exception {
		
		Git.cloneRepository()
        .setBare( true )
        .setCloneAllBranches( true )
        .setURI( fromURI )
        .setDirectory( new File(TEMP_LOCATION + File.separator + alias) )
        .setCredentialsProvider( new UsernamePasswordCredentialsProvider(username, password) )
        .call();
		
		System.setProperty("org.uberfire.nio.git.dir", TEMP_LOCATION);

		
	}

	protected void cleanTestGitRepo() {
		System.clearProperty("org.uberfire.nio.git.dir");
		File niogit = new File(TEMP_LOCATION);
		
		removeFiles(niogit);
	}
	
	protected void removeFiles(File directory) {
		if (directory != null && directory.exists()) {
			
			File[] files = directory.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					removeFiles(file);
				}
				file.delete();
			}
			
			directory.delete();
		}
	}
}
