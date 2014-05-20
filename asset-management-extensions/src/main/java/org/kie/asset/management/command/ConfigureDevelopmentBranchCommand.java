package org.kie.asset.management.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.POMService;
import org.guvnor.common.services.project.service.ProjectService;
import org.jbpm.executor.cdi.CDIUtils;
import org.jbpm.executor.cdi.NamedLiteral;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.repositories.Repository;
import org.uberfire.backend.repositories.RepositoryService;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.DirectoryStream;
import org.uberfire.java.nio.file.Files;
import org.uberfire.java.nio.file.Path;

public class ConfigureDevelopmentBranchCommand extends AbstractCommand {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigureDevelopmentBranchCommand.class);

	@Override
	public ExecutionResults execute(CommandContext ctx) throws Exception {
		ExecutionResults executionResults = new ExecutionResults();
		
		
		String repository = (String) getParameter(ctx, "GitRepository");
		if (repository.endsWith(".git")) {
			repository = repository.substring(0, repository.length() - 4);
		}
		String branchToUpdate = (String) getParameter(ctx, "BranchName");
		String version = (String) getParameter(ctx, "Version");
		if (version == null) {
			version = "1.0.0-SNAPSHOT";
		} else if (!version.endsWith("-SNAPSHOT")) {
			version = version.concat("-SNAPSHOT");
		}
		
				
		BeanManager beanManager = CDIUtils.lookUpBeanManager(ctx);
		logger.debug("BeanManager " + beanManager);
		
		POMService pomService = CDIUtils.createBean(POMService.class, beanManager);		
		logger.debug("POMService " + pomService);
				
		IOService ioService = CDIUtils.createBean(IOService.class, beanManager, new NamedLiteral("ioStrategy"));
		logger.debug("IoService " + ioService);
		if (ioService != null) {

			
			ProjectService projectService = CDIUtils.createBean(ProjectService.class, beanManager);
	
			RepositoryService repositoryService = CDIUtils.createBean(RepositoryService.class, beanManager);
			logger.debug("RepositoryService " + repositoryService);
			
			if (repositoryService != null) {
					
				Repository repo = repositoryService.getRepository(repository);
				
				Map<String, Object> config = new HashMap<String, Object>();
				config.put("branch", branchToUpdate);
				
				repo = repositoryService.updateRepository(repo, config);
				logger.debug("Updated repository " + repo);
	
				// update all pom.xml files of projects on the dev branch				
				Set<Project> projects = getProjects(repo, ioService, projectService);
				
				for (Project project : projects) {
					
					POM pom = pomService.load(project.getPomXMLPath());
					pom.getGav().setVersion(version);
					pomService.save(project.getPomXMLPath(), pom, null, "Update project version on development branch");
					executionResults.setData(project.getProjectName() +  "-GAV", pom.getGav().toString());
				}
			}
		}

		return executionResults;
	}

    private Set<Project> getProjects( final Repository repository, final IOService ioService, final ProjectService projectService ) {
        final Set<Project> authorizedProjects = new HashSet<Project>();
        if ( repository == null ) {
            return authorizedProjects;
        }
        final Path repositoryRoot = Paths.convert(repository.getRoot());
        final DirectoryStream<org.uberfire.java.nio.file.Path> nioRepositoryPaths = ioService.newDirectoryStream( repositoryRoot );
        for ( org.uberfire.java.nio.file.Path nioRepositoryPath : nioRepositoryPaths ) {
            if ( Files.isDirectory( nioRepositoryPath ) ) {
                final org.uberfire.backend.vfs.Path projectPath = Paths.convert( nioRepositoryPath );
                final Project project = projectService.resolveProject( projectPath );
                if ( project != null ) {
                    authorizedProjects.add( project );
                    
                }
            }
        }
        return authorizedProjects;
    }
	
}
