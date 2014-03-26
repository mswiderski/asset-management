package org.kie.org.jbpm.asset.management;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.asset.management.command.CreateBranchCommand;
import org.kie.asset.management.command.DeleteBranchCommand;
import org.kie.asset.management.command.ListBranchesCommand;
import org.kie.asset.management.command.ListCommitsCommand;
import org.kie.asset.management.model.BranchInfo;
import org.kie.asset.management.model.CommitInfo;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitCommandsTest extends AbstractTestCase {
	
	private static final Logger logger = LoggerFactory.getLogger(GitCommandsTest.class);

    private WorkItemImpl workItem = null;
    private CommandContext context = null;

    @Before
    public void setup() throws Exception {
        setupTestGitRepo("https://github.com/guvnorngtestuser1/jbpm-console-ng-playground-kjar.git",
        		"jbpm-playground.git", "guvnorngtestuser1", "test1234");
        workItem = new WorkItemImpl();
        workItem.setParameter("GitRepository", "jbpm-playground.git");

        context = new CommandContext();
        context.setData("workItem", workItem);
    }
    
    @After
    public void cleanup() {
    	cleanTestGitRepo();
    	logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testListCommitsCommand() throws Exception {
        ListCommitsCommand command = new ListCommitsCommand();

        ExecutionResults results = command.execute(context);

        assertNotNull(results);
        List<CommitInfo> commits = (List<CommitInfo>) results.getData("Commits");

        assertEquals(10, commits.size());

        Map<String, List<CommitInfo>> commitsPerFile = (Map<String, List<CommitInfo>>) results.getData("CommitsPerFile");
        assertNotNull(commitsPerFile);
        
        for (Map.Entry<String, List<CommitInfo>> entry : commitsPerFile.entrySet()) {
        	logger.info("file {} number of commits {}", entry.getKey(),  entry.getValue().size());
        }

    }

    @SuppressWarnings("unchecked")
	@Test
    public void testCreateAndDeleteBranchCommand() throws Exception {
        CreateBranchCommand command = new CreateBranchCommand();
        
        workItem.setParameter("BranchName", "DEV-BRANCH");
        workItem.setParameter("StartPoint", "adb57db29cf20251b54405922c2e446f7facf866");

        ExecutionResults results = command.execute(context);
        
        ListBranchesCommand listcommand = new ListBranchesCommand();        

        results = listcommand.execute(context);

        assertNotNull(results);
        List<BranchInfo> branches = (List<BranchInfo>) results.getData("Branches");
        assertNotNull(branches);
        
        for (BranchInfo branchInfo : branches) {
        	logger.info("Found branch {}", branchInfo);
        }
        assertEquals(2, branches.size());
        
        DeleteBranchCommand deleteCommand = new DeleteBranchCommand();
        deleteCommand.execute(context);
        
        listcommand = new ListBranchesCommand();        

        results = listcommand.execute(context);

        assertNotNull(results);
        branches = (List<BranchInfo>) results.getData("Branches");
        assertNotNull(branches);
        
        for (BranchInfo branchInfo : branches) {
        	logger.info("Found branch {}", branchInfo);
        }
        assertEquals(1, branches.size());
        
    }
}
