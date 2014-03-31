package org.kie.asset.management;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.asset.management.command.CherryPickCommand;
import org.kie.asset.management.command.CloneRepositoryCommand;
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
    	cleanTestGitRepo();
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
    public void testCreateBranchCommand() throws Exception {
        CreateBranchCommand command = new CreateBranchCommand();
        
        workItem.setParameter("BranchName", "DEV-BRANCH");

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
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateBranchAndCherryPickCommand() throws Exception {
        // create dev branch in bare repository
    	CreateBranchCommand command = new CreateBranchCommand();
        
        workItem.setParameter("BranchName", "DEV-BRANCH");
        workItem.setParameter("StartPoint", "5b24cb037228e1f1791dac292af29b9e2fcb3b35");

        ExecutionResults results = command.execute(context);
        // list branches on bare repository
        ListBranchesCommand listcommand = new ListBranchesCommand();        

        results = listcommand.execute(context);

        assertNotNull(results);
        List<BranchInfo> branches = (List<BranchInfo>) results.getData("Branches");
        assertNotNull(branches);
        
        for (BranchInfo branchInfo : branches) {
        	logger.info("Found branch {}", branchInfo);
        }
        assertEquals(2, branches.size());
        
        ListCommitsCommand listCommand = new ListCommitsCommand();
        
        workItem.setParameter("MaxCount", "1");
        results = listCommand.execute(context);
        List<CommitInfo> commitsFound = (List<CommitInfo>) results.getData("Commits");
        assertEquals(1, commitsFound.size());
        String messageBeforeCherryPick = commitsFound.get(0).getMessage();
        
        // now let's clone to working copy
        CloneRepositoryCommand cloneCommand = new CloneRepositoryCommand();
        results = cloneCommand.execute(context);
        
        String workingCopyDir = (String) results.getData("WorkingCopyDir");
        removeAfterTest(workingCopyDir);
        
        CherryPickCommand cherryPickCommand = new CherryPickCommand();
        List<String> commits = new ArrayList<String>();        
        commits.add("3ed7d21eab4f9c29a815e708a8858046501886f1");
        
        workItem.setParameter("Commits", commits);
        workItem.setParameter("GitRepositoryLocation", workingCopyDir);
        
        results = cherryPickCommand.execute(context);
        String cherryPickResult = (String) results.getData("CherryPickResult");
        assertEquals("OK", cherryPickResult);
        
        listCommand = new ListCommitsCommand();
        
        workItem.setParameter("MaxCount", "1");
        results = listCommand.execute(context);

        assertNotNull(results);
        commitsFound = (List<CommitInfo>) results.getData("Commits");
        assertEquals(1, commitsFound.size());
        String message = commitsFound.get(0).getMessage();
        
        assertNotEquals(messageBeforeCherryPick, message);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCreateBranchAndCherryPickCommandConflict() throws Exception {
        // create dev branch in bare repository
    	CreateBranchCommand command = new CreateBranchCommand();
        
        workItem.setParameter("BranchName", "DEV-BRANCH");
        workItem.setParameter("StartPoint", "5b24cb037228e1f1791dac292af29b9e2fcb3b35");

        ExecutionResults results = command.execute(context);
        // list branches on bare repository
        ListBranchesCommand listcommand = new ListBranchesCommand();        

        results = listcommand.execute(context);

        assertNotNull(results);
        List<BranchInfo> branches = (List<BranchInfo>) results.getData("Branches");
        assertNotNull(branches);
        
        for (BranchInfo branchInfo : branches) {
        	logger.info("Found branch {}", branchInfo);
        }
        assertEquals(2, branches.size());
        
        ListCommitsCommand listCommand = new ListCommitsCommand();
        
        workItem.setParameter("MaxCount", "1");
        results = listCommand.execute(context);
        List<CommitInfo> commitsFound = (List<CommitInfo>) results.getData("Commits");
        assertEquals(1, commitsFound.size());
        String messageBeforeCherryPick = commitsFound.get(0).getMessage();
        
        // now let's clone to working copy
        CloneRepositoryCommand cloneCommand = new CloneRepositoryCommand();
        results = cloneCommand.execute(context);
        
        String workingCopyDir = (String) results.getData("WorkingCopyDir");
        removeAfterTest(workingCopyDir);
        
        CherryPickCommand cherryPickCommand = new CherryPickCommand();
        List<String> commits = new ArrayList<String>();        
        commits.add("81e02974268254b23f6285aabca910ac80156457");
        commits.add("3ed7d21eab4f9c29a815e708a8858046501886f1");        
        
        workItem.setParameter("Commits", commits);
        workItem.setParameter("GitRepositoryLocation", workingCopyDir);
        
        results = cherryPickCommand.execute(context);
        String cherryPickResult = (String) results.getData("CherryPickResult");
        assertEquals("CONFLICTING", cherryPickResult);
        
        listCommand = new ListCommitsCommand();
        
        workItem.setParameter("MaxCount", "1");
        results = listCommand.execute(context);

        assertNotNull(results);
        commitsFound = (List<CommitInfo>) results.getData("Commits");
        assertEquals(1, commitsFound.size());
        String message = commitsFound.get(0).getMessage();
        
        assertEquals(messageBeforeCherryPick, message);
        
    }
}
