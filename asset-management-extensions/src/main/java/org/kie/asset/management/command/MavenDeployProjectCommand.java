package org.kie.asset.management.command;

import java.io.ByteArrayInputStream;
import java.net.URI;

import javax.enterprise.inject.spi.BeanManager;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.guvnor.common.services.builder.Builder;
import org.guvnor.common.services.builder.LRUBuilderCache;
import org.guvnor.common.services.project.model.GAV;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.ProjectService;
import org.guvnor.m2repo.backend.server.ExtendedM2RepoService;
import org.kie.asset.management.util.CDIUtils;
import org.kie.asset.management.util.NamedLiteral;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.Path;

public class MavenDeployProjectCommand extends AbstractCommand {
	
	private static final Logger logger = LoggerFactory.getLogger(MavenDeployProjectCommand.class);

	@Override
	public ExecutionResults execute(CommandContext ctx) throws Exception {
		ExecutionResults executionResults = new ExecutionResults();
		String deployOutcome = "UNKNOWN";
		String uri = (String) getParameter(ctx, "Uri");
		String branchToBuild = (String) getParameter(ctx, "BranchToBuild");
		
		String projectUri = "default://"+branchToBuild+"@"+uri;
		String gav = (String) getParameter(ctx, "GAV");
		String[] gavElements = gav.split(":");
		
		BeanManager beanManager = CDIUtils.lookUpBeanManager(ctx);
		logger.debug("BeanManager " + beanManager);
		
		LRUBuilderCache builderCache = CDIUtils.createBean(LRUBuilderCache.class, beanManager);
		logger.debug("BuilderCache " + builderCache);
		
		IOService ioService = CDIUtils.createBean(IOService.class, beanManager, new NamedLiteral("ioStrategy"));
		logger.debug("IoService " + ioService);
		if (ioService != null) {
			Path projectPath  = ioService.get(URI.create(projectUri));
			logger.debug("Project path is " + projectPath);
			
			ProjectService projectService = CDIUtils.createBean(ProjectService.class, beanManager);
			Project project = projectService.resolveProject(Paths.convert(projectPath));
			
			Builder builder = builderCache.assertBuilder(project);
		
			ExtendedM2RepoService m2RepoService = CDIUtils.createBean(ExtendedM2RepoService.class, beanManager);		
			logger.debug("M2RepoService " + m2RepoService);
					
			if (m2RepoService != null) {
				try {
					final InternalKieModule kieModule = (InternalKieModule) builder.getKieModule();
	                final ByteArrayInputStream input = new ByteArrayInputStream( kieModule.getBytes() );
	                m2RepoService.deployJar( input, new GAV(gavElements[0], gavElements[1], gavElements[2]));
	                
	                deployOutcome = "SUCCESSFUL";
				} catch (Exception e) {
					deployOutcome = "FAILURE";
				}
				
			}
		}
		executionResults.setData("MavenDeployOutcome", deployOutcome);
		

		return executionResults;
	}

	
}
