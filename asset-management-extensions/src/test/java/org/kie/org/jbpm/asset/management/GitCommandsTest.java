package org.kie.org.jbpm.asset.management;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.drools.core.process.instance.impl.WorkItemImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.asset.management.command.ListCommitsCommand;
import org.kie.asset.management.model.CommitInfo;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;

public class GitCommandsTest extends AbstractTestCase {

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
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testListCommitsCommand() throws Exception {
        ListCommitsCommand command = new ListCommitsCommand();

        ExecutionResults results = command.execute(context);

        assertNotNull(results);
        List<CommitInfo> commits = (List<CommitInfo>) results.getData("Commits");

        assertEquals(10, commits.size());
        for (CommitInfo commitInfo : commits) {
            System.out.println(commitInfo);
        }

    }
}
