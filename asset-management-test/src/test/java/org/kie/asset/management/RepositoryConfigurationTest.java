package org.kie.asset.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.executor.commands.PrintOutCommand;
import org.jbpm.executor.impl.wih.AsyncWorkItemHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.asset.management.command.CreateBranchCommand;
import org.kie.asset.management.command.ListBranchesCommand;
import org.kie.asset.management.model.BranchInfo;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;
import org.kie.internal.executor.api.ExecutorService;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryConfigurationTest extends AbstractTestCase {

	private static final Logger logger = LoggerFactory.getLogger(RepositoryConfigurationTest.class);
	private ExecutorService executorService = null;
	
	public RepositoryConfigurationTest() {
		super(true, true);
	}
	
    @Before
    public void setup() throws Exception {
    	cleanTestGitRepo();
        setupTestGitRepo("https://github.com/guvnorngtestuser1/jbpm-console-ng-playground-kjar.git",
        		"jbpm-playground.git", "guvnorngtestuser1", "test1234");
        executorService = ExecutorServiceFactory.newExecutorService(getEmf());
        executorService.init();
    }
    
    @After
    public void cleanup() {
    	cleanTestGitRepo();
    	if (executorService != null) {
    		executorService.destroy();
    	}
    	logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureRepositoryProcess() throws Exception {
		
		ListBranchesCommand listcommand = new ListBranchesCommand();        
		CommandContext context = new CommandContext();
		context.setData("GitRepository", "jbpm-playground.git");
		ExecutionResults results = listcommand.execute(context);
		List<BranchInfo> branchInfos = (List<BranchInfo>) results.getData("Branches");
		assertNotNull(branchInfos);
		assertEquals(1, branchInfos.size());
		
		RuntimeManager manager = createRuntimeManager("ConfigureRepository.bpmn2");		
		assertNotNull(manager);
		
		RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());
		assertNotNull(engine);
		
		engine.getKieSession().getWorkItemManager().registerWorkItemHandler("async",
				new AsyncWorkItemHandler(executorService, PrintOutCommand.class.getName()));
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("RepositoryName", "jbpm-playground.git");
		
		ProcessInstance processInstance = engine.getKieSession().startProcess("asset-management-kmodule.ConfigureRepository", parameters);
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		
		Thread.sleep(3000);		
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		
		Thread.sleep(3000);
		assertProcessInstanceCompleted(processInstance.getId(), engine.getKieSession());
		
		listcommand = new ListBranchesCommand();        
		context = new CommandContext();
		context.setData("GitRepository", "jbpm-playground.git");
		results = listcommand.execute(context);
		branchInfos = (List<BranchInfo>) results.getData("Branches");
		assertNotNull(branchInfos);
		assertEquals(3, branchInfos.size());
		List<String> branchNames = new ArrayList<String>();
		for (BranchInfo branch : branchInfos) {
			logger.info("Branch name {}", branch.getName());
			branchNames.add(branch.getName());			
		}
		
		assertTrue(branchNames.contains("refs/heads/master"));
		assertTrue(branchNames.contains("refs/heads/Development"));
		assertTrue(branchNames.contains("refs/heads/Release"));
		
		manager.disposeRuntimeEngine(engine);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureRepositoryProcessAlreadyExistingBranch() throws Exception {
		
		ListBranchesCommand listcommand = new ListBranchesCommand();        
		CommandContext context = new CommandContext();
		context.setData("GitRepository", "jbpm-playground.git");
		ExecutionResults results = listcommand.execute(context);
		List<BranchInfo> branchInfos = (List<BranchInfo>) results.getData("Branches");
		assertNotNull(branchInfos);
		assertEquals(1, branchInfos.size());
		
		CreateBranchCommand createbranchCommand = new CreateBranchCommand();
		context = new CommandContext();
		context.setData("GitRepository", "jbpm-playground.git");
		context.setData("BranchName", "Development");
		
		createbranchCommand.execute(context);
		
		RuntimeManager manager = createRuntimeManager("ConfigureRepository.bpmn2");		
		assertNotNull(manager);
		
		RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());
		assertNotNull(engine);
		
		engine.getKieSession().getWorkItemManager().registerWorkItemHandler("async",
				new AsyncWorkItemHandler(executorService, PrintOutCommand.class.getName()));
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("RepositoryName", "jbpm-playground.git");
		
		ProcessInstance processInstance = engine.getKieSession().startProcess("asset-management-kmodule.ConfigureRepository", parameters);
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		
		Thread.sleep(3000);		
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		
		List<Long> taskIds = engine.getTaskService().getTasksByProcessInstanceId(processInstance.getId());
		assertNotNull(taskIds);
		assertEquals(1, taskIds.size());
		
		listcommand = new ListBranchesCommand();        
		context = new CommandContext();
		context.setData("GitRepository", "jbpm-playground.git");
		results = listcommand.execute(context);
		branchInfos = (List<BranchInfo>) results.getData("Branches");
		assertNotNull(branchInfos);
		assertEquals(2, branchInfos.size());
		List<String> branchNames = new ArrayList<String>();
		for (BranchInfo branch : branchInfos) {
			logger.info("Branch name {}", branch.getName());
			branchNames.add(branch.getName());			
		}
		
		assertTrue(branchNames.contains("refs/heads/master"));
		assertTrue(branchNames.contains("refs/heads/Development"));
		
		engine.getKieSession().abortProcessInstance(processInstance.getId());
		
		manager.disposeRuntimeEngine(engine);
	}
}
