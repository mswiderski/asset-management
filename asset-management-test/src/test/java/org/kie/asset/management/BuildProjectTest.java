package org.kie.asset.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildProjectTest extends AbstractTestCase {

	private static final Logger logger = LoggerFactory.getLogger(BuildProjectTest.class);

	public BuildProjectTest() {
		super(true, true);
	}
	
    @Before
    public void setup() throws Exception {
    	System.setProperty("org.uberfire.nio.git.ssh.enabled", "false");
    }
    
    @After
    public void cleanup() {

    	logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testBuildProjectProcessHappyPath() throws Exception {
				
		RuntimeManager manager = createRuntimeManager("BuildProject.bpmn2");		
		assertNotNull(manager);
		
		RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());
		assertNotNull(engine);
		
		TestWorkItemHandler testWorkItemHandler = getTestWorkItemHandler();
		
		engine.getKieSession().getWorkItemManager().registerWorkItemHandler("async", testWorkItemHandler);
		engine.getKieSession().getWorkItemManager().registerWorkItemHandler("Rest", testWorkItemHandler);
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("ProjectURI", "jbpm-playground/HR");
		parameters.put("BranchName", "master");
		parameters.put("Username", "bpmadmin");
		parameters.put("Password", "secret123");
		parameters.put("ExecServerURL", "http://localhost:8080/kie-wb");
		parameters.put("DeployToRuntime", true);
		
		ProcessInstance processInstance = engine.getKieSession().startProcess("asset-management-kmodule.BuildProject", parameters);
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		
		List<WorkItem> workItems = testWorkItemHandler.getWorkItems();
		assertEquals(1, workItems.size());
		
		WorkItem workItem = workItems.get(0);
		assertNotNull(workItem);
		// Build project from branch
		assertEquals("org.kie.asset.management.command.BuildProjectCommand", workItem.getParameter("CommandClass"));
		assertEquals("jbpm-playground/HR", workItem.getParameter("Uri"));
		assertEquals("master", workItem.getParameter("BranchToBuild"));
		assertEquals("0", workItem.getParameter("Retries"));
		
		// now let's simulate actual build done by guvnor project build service
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("BuildOutcome", "SUCCESSFUL");
		results.put("Errors", new ArrayList());
		results.put("Warnings", new ArrayList());
		results.put("Infos", new ArrayList());
		results.put("GAV", "org.jbpm:HR:1.0");
		
		engine.getKieSession().getWorkItemManager().completeWorkItem(workItem.getId(), results);
		// next node	
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		workItems = testWorkItemHandler.getWorkItems();
		assertEquals(1, workItems.size());
		
		workItem = workItems.get(0);
		assertNotNull(workItem);
		// Deploy to Maven repository
		assertEquals("org.kie.asset.management.command.MavenDeployProjectCommand", workItem.getParameter("CommandClass"));
		assertEquals("jbpm-playground/HR", workItem.getParameter("Uri"));
		assertEquals("master", workItem.getParameter("BranchToBuild"));
		assertEquals("org.jbpm:HR:1.0", workItem.getParameter("GAV"));
		assertEquals("0", workItem.getParameter("Retries"));
		
		// now let's simulate actual maven deploy done by guvnor m2 service
		results = new HashMap<String, Object>();
		results.put("MavenDeployOutcome", "SUCCESSFUL");
		
		engine.getKieSession().getWorkItemManager().completeWorkItem(workItem.getId(), results);
		
		// rest call to deploy to runtime
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		workItems = testWorkItemHandler.getWorkItems();
		assertEquals(1, workItems.size());
		
		workItem = workItems.get(0);
		assertNotNull(workItem);
		// Deploy to runtime
		assertEquals("POST", workItem.getParameter("Method"));
		assertEquals("secret123", workItem.getParameter("Password"));
		assertEquals("bpmadmin", workItem.getParameter("Username"));
		assertEquals("http://localhost:8080/kie-wb/rest/deployment/org.jbpm:HR:1.0/deploy"  , workItem.getParameter("Url"));
		
		// now let's simulate actual maven deploy done by guvnor m2 service
		results = new HashMap<String, Object>();
		
		engine.getKieSession().getWorkItemManager().completeWorkItem(workItem.getId(), results);
		
		assertProcessInstanceCompleted(processInstance.getId(), engine.getKieSession());
		
		
		manager.disposeRuntimeEngine(engine);
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testBuildProjectProcessNoRuntimeDeployPath() throws Exception {
				
		RuntimeManager manager = createRuntimeManager("BuildProject.bpmn2");		
		assertNotNull(manager);
		
		RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());
		assertNotNull(engine);
		
		TestWorkItemHandler testWorkItemHandler = getTestWorkItemHandler();
		
		engine.getKieSession().getWorkItemManager().registerWorkItemHandler("async", testWorkItemHandler);
		engine.getKieSession().getWorkItemManager().registerWorkItemHandler("Rest", testWorkItemHandler);
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("ProjectURI", "jbpm-playground/HR");
		parameters.put("BranchName", "master");
		parameters.put("Username", "bpmadmin");
		parameters.put("Password", "secret123");
		parameters.put("ExecServerURL", "http://localhost:8080/kie-wb");
		parameters.put("DeployToRuntime", false);
		
		ProcessInstance processInstance = engine.getKieSession().startProcess("asset-management-kmodule.BuildProject", parameters);
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		
		List<WorkItem> workItems = testWorkItemHandler.getWorkItems();
		assertEquals(1, workItems.size());
		
		WorkItem workItem = workItems.get(0);
		assertNotNull(workItem);
		// Build project from branch
		assertEquals("org.kie.asset.management.command.BuildProjectCommand", workItem.getParameter("CommandClass"));
		assertEquals("jbpm-playground/HR", workItem.getParameter("Uri"));
		assertEquals("master", workItem.getParameter("BranchToBuild"));
		assertEquals("0", workItem.getParameter("Retries"));
		
		// now let's simulate actual build done by guvnor project build service
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("BuildOutcome", "SUCCESSFUL");
		results.put("Errors", new ArrayList());
		results.put("Warnings", new ArrayList());
		results.put("Infos", new ArrayList());
		results.put("GAV", "org.jbpm:HR:1.0");
		
		engine.getKieSession().getWorkItemManager().completeWorkItem(workItem.getId(), results);
		// next node	
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		workItems = testWorkItemHandler.getWorkItems();
		assertEquals(1, workItems.size());
		
		workItem = workItems.get(0);
		assertNotNull(workItem);
		// Deploy to Maven repository
		assertEquals("org.kie.asset.management.command.MavenDeployProjectCommand", workItem.getParameter("CommandClass"));
		assertEquals("jbpm-playground/HR", workItem.getParameter("Uri"));
		assertEquals("master", workItem.getParameter("BranchToBuild"));
		assertEquals("org.jbpm:HR:1.0", workItem.getParameter("GAV"));
		assertEquals("0", workItem.getParameter("Retries"));
		
		// now let's simulate actual maven deploy done by guvnor m2 service
		results = new HashMap<String, Object>();
		results.put("MavenDeployOutcome", "SUCCESSFUL");
		
		engine.getKieSession().getWorkItemManager().completeWorkItem(workItem.getId(), results);
		
		assertProcessInstanceCompleted(processInstance.getId(), engine.getKieSession());
		
		
		manager.disposeRuntimeEngine(engine);
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testBuildProjectProcessBuildErrorPath() throws Exception {
				
		RuntimeManager manager = createRuntimeManager("BuildProject.bpmn2");		
		assertNotNull(manager);
		
		RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());
		assertNotNull(engine);
		
		TestWorkItemHandler testWorkItemHandler = getTestWorkItemHandler();
		
		engine.getKieSession().getWorkItemManager().registerWorkItemHandler("async", testWorkItemHandler);
		engine.getKieSession().getWorkItemManager().registerWorkItemHandler("Rest", testWorkItemHandler);
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("ProjectURI", "jbpm-playground/HR");
		parameters.put("BranchName", "master");
		parameters.put("Username", "bpmadmin");
		parameters.put("Password", "secret123");
		parameters.put("ExecServerURL", "http://localhost:8080/kie-wb");
		parameters.put("DeployToRuntime", true);
		
		ProcessInstance processInstance = engine.getKieSession().startProcess("asset-management-kmodule.BuildProject", parameters);
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		
		List<WorkItem> workItems = testWorkItemHandler.getWorkItems();
		assertEquals(1, workItems.size());
		
		WorkItem workItem = workItems.get(0);
		assertNotNull(workItem);
		// Build project from branch
		assertEquals("org.kie.asset.management.command.BuildProjectCommand", workItem.getParameter("CommandClass"));
		assertEquals("jbpm-playground/HR", workItem.getParameter("Uri"));
		assertEquals("master", workItem.getParameter("BranchToBuild"));
		assertEquals("0", workItem.getParameter("Retries"));
		
		// now let's simulate actual build done by guvnor project build service
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("BuildOutcome", "FAILURE");
		results.put("Errors", new ArrayList());
		results.put("Warnings", new ArrayList());
		results.put("Infos", new ArrayList());
		results.put("GAV", "org.jbpm:HR:1.0");
		
		engine.getKieSession().getWorkItemManager().completeWorkItem(workItem.getId(), results);
		
		// get user task with build error details
		
		List<Long> taskIds = engine.getTaskService().getTasksByProcessInstanceId(processInstance.getId());
		assertNotNull(taskIds);
		assertEquals(1, taskIds.size());
		
		Long taskId = taskIds.get(0);
		
		Task task = engine.getTaskService().getTaskById(taskId);
		String taskName = task.getNames().get(0).getText();
		assertEquals("Build failure details", taskName);
		
		engine.getTaskService().claim(taskId, "john");
		engine.getTaskService().start(taskId, "john");
		engine.getTaskService().complete(taskId, "john", null);
		
		assertProcessInstanceCompleted(processInstance.getId(), engine.getKieSession());		
		
		manager.disposeRuntimeEngine(engine);
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testBuildProjectProcessMavenDeployErrorPath() throws Exception {
				
		RuntimeManager manager = createRuntimeManager("BuildProject.bpmn2");		
		assertNotNull(manager);
		
		RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());
		assertNotNull(engine);
		
		TestWorkItemHandler testWorkItemHandler = getTestWorkItemHandler();
		
		engine.getKieSession().getWorkItemManager().registerWorkItemHandler("async", testWorkItemHandler);
		engine.getKieSession().getWorkItemManager().registerWorkItemHandler("Rest", testWorkItemHandler);
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("ProjectURI", "jbpm-playground/HR");
		parameters.put("BranchName", "master");
		parameters.put("Username", "bpmadmin");
		parameters.put("Password", "secret123");
		parameters.put("ExecServerURL", "http://localhost:8080/kie-wb");
		parameters.put("DeployToRuntime", true);
		
		ProcessInstance processInstance = engine.getKieSession().startProcess("asset-management-kmodule.BuildProject", parameters);
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		
		List<WorkItem> workItems = testWorkItemHandler.getWorkItems();
		assertEquals(1, workItems.size());
		
		WorkItem workItem = workItems.get(0);
		assertNotNull(workItem);
		// Build project from branch
		assertEquals("org.kie.asset.management.command.BuildProjectCommand", workItem.getParameter("CommandClass"));
		assertEquals("jbpm-playground/HR", workItem.getParameter("Uri"));
		assertEquals("master", workItem.getParameter("BranchToBuild"));
		assertEquals("0", workItem.getParameter("Retries"));
		
		// now let's simulate actual build done by guvnor project build service
		Map<String, Object> results = new HashMap<String, Object>();
		results.put("BuildOutcome", "SUCCESSFUL");
		results.put("Errors", new ArrayList());
		results.put("Warnings", new ArrayList());
		results.put("Infos", new ArrayList());
		results.put("GAV", "org.jbpm:HR:1.0");
		
		engine.getKieSession().getWorkItemManager().completeWorkItem(workItem.getId(), results);
		// now it's time to do maven deploy
		assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());
		workItems = testWorkItemHandler.getWorkItems();
		assertEquals(1, workItems.size());
		
		workItem = workItems.get(0);
		assertNotNull(workItem);
		// Deploy to Maven repository
		assertEquals("org.kie.asset.management.command.MavenDeployProjectCommand", workItem.getParameter("CommandClass"));
		assertEquals("jbpm-playground/HR", workItem.getParameter("Uri"));
		assertEquals("master", workItem.getParameter("BranchToBuild"));
		assertEquals("org.jbpm:HR:1.0", workItem.getParameter("GAV"));
		assertEquals("0", workItem.getParameter("Retries"));
		
		// now let's simulate actual maven deploy done by guvnor m2 service
		results = new HashMap<String, Object>();
		results.put("MavenDeployOutcome", "FAILURE");
		
		engine.getKieSession().getWorkItemManager().completeWorkItem(workItem.getId(), results);
		
		// get user task with build error details
		
		List<Long> taskIds = engine.getTaskService().getTasksByProcessInstanceId(processInstance.getId());
		assertNotNull(taskIds);
		assertEquals(1, taskIds.size());
		
		Long taskId = taskIds.get(0);
		
		Task task = engine.getTaskService().getTaskById(taskId);
		String taskName = task.getNames().get(0).getText();
		assertEquals("Maven Deploy Failure details", taskName);
		
		engine.getTaskService().claim(taskId, "john");
		engine.getTaskService().start(taskId, "john");
		engine.getTaskService().complete(taskId, "john", null);
		
		assertProcessInstanceCompleted(processInstance.getId(), engine.getKieSession());		
		
		manager.disposeRuntimeEngine(engine);
	}
}
