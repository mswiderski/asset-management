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
import org.kie.api.task.TaskService;
import org.kie.asset.management.command.CreateBranchCommand;
import org.kie.asset.management.command.ListBranchesCommand;
import org.kie.asset.management.command.ListCommitsCommand;
import org.kie.asset.management.model.BranchInfo;
import org.kie.asset.management.model.CommitInfo;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;
import org.kie.internal.executor.api.ExecutorService;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PromoteAssetsTest extends AbstractTestCase {

    private static final Logger logger = LoggerFactory.getLogger(PromoteAssetsTest.class);
    private ExecutorService executorService = null;

    public PromoteAssetsTest() {
        super(true, true);
    }

    @Before
    public void setup() throws Exception {    	
    	System.setProperty("org.uberfire.nio.git.ssh.enabled", "false");
    	cleanTestGitRepo();
        setupTestGitRepo("https://github.com/guvnorngtestuser1/jbpm-console-ng-playground-kjar.git",
                "jbpm-playground.git", "guvnorngtestuser1", "test1234");
        executorService = ExecutorServiceFactory.newExecutorService(getEmf());
        executorService.init();
        
        
        CreateBranchCommand createbranchCommand = new CreateBranchCommand();
        CommandContext context = new CommandContext();
        context.setData("GitRepository", "jbpm-playground.git");
        context.setData("BranchName", "Development");
        context.setData("StartPoint", "adb57db29cf20251b54405922c2e446f7facf866");
        createbranchCommand.execute(context);

        createbranchCommand = new CreateBranchCommand();
        context = new CommandContext();
        context.setData("GitRepository", "jbpm-playground.git");
        context.setData("BranchName", "Release");
        context.setData("StartPoint", "adb57db29cf20251b54405922c2e446f7facf866");

        createbranchCommand.execute(context);
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
    public void testPromoteAssetsProcess() throws Exception {

        ListBranchesCommand listcommand = new ListBranchesCommand();
        CommandContext context = new CommandContext();
        context.setData("GitRepository", "jbpm-playground.git");
        ExecutionResults results = listcommand.execute(context);
        List<BranchInfo> branchInfos = (List<BranchInfo>) results.getData("Branches");
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

                //TODO: Change and commit some files here
        RuntimeManager manager = createRuntimeManager("PromoteAssets.bpmn2");
        assertNotNull(manager);

        RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());
        assertNotNull(engine);

        engine.getKieSession().getWorkItemManager().registerWorkItemHandler("async",
                new AsyncWorkItemHandler(executorService, PrintOutCommand.class.getName()));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("GitRepositoryName", "jbpm-playground.git");
        parameters.put("SourceBranchName", "Development");
        parameters.put("TargetBranchName", "Release");
        ProcessInstance processInstance = engine.getKieSession().startProcess("asset-management-kmodule.PromoteAssets", parameters);
        assertProcessInstanceActive(processInstance.getId(), engine.getKieSession());

        Thread.sleep(3000);
        
        TaskService taskService = engine.getTaskService();
        List<Long> pendingTasks = taskService.getTasksByProcessInstanceId(processInstance.getId());
        assertEquals(1, pendingTasks.size());

        Long selectFilesToPromoteSum = pendingTasks.get(0);

        taskService.start(selectFilesToPromoteSum, "salaboy");
        
        
        parameters = new HashMap<String, Object>();
        
        ListCommitsCommand command = new ListCommitsCommand();
        context = new CommandContext();
        // TODO: I need to look for the BranchName inside the task content (variable called: in_source_branch_name)
        context.setData("GitRepository", "jbpm-playground.git");
        context.setData("BranchName", "Development");
        results = command.execute(context);

        assertNotNull(results);
        List<CommitInfo> commits = (List<CommitInfo>) results.getData("Commits");

        assertEquals(10, commits.size());

        String commitsString = (String) results.getData("CommitsString");
        
//        Map<String, List<CommitInfo>> commitsPerFile = (Map<String, List<CommitInfo>>) results.getData("CommitsPerFile");
//        assertNotNull(commitsPerFile);

        parameters.put("out_commits", commitsString);
        parameters.put("out_requires_review", false);
        
        taskService.complete(selectFilesToPromoteSum, "salaboy", parameters);

        Thread.sleep(4000);
        assertProcessInstanceCompleted(processInstance.getId(), engine.getKieSession());

        
        manager.disposeRuntimeEngine(engine);
    }


}
