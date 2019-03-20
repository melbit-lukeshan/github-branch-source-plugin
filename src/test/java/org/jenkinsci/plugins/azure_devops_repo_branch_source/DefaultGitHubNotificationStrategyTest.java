/*
 * The MIT License
 *
 * Copyright 2017 Steven Foster
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.azure_devops_repo_branch_source;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.util.LogTaskListener;
import jenkins.scm.api.SCMHeadOrigin;
import jenkins.scm.api.mixin.ChangeRequestCheckoutStrategy;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

public class DefaultGitHubNotificationStrategyTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void given_basicJob_then_singleNotification() throws Exception {
        FreeStyleProject job = j.createFreeStyleProject();
        AzureDevOpsRepoSCMSource src = new AzureDevOpsRepoSCMSource("https://dev.azure.com/lukeshan", "exmaple", "spring");
        FreeStyleBuild run = j.buildAndAssertSuccess(job);
        DefaultAzureDevOpsNotificationStrategy instance = new DefaultAzureDevOpsNotificationStrategy();
        List<AzureDevOpsRepoNotificationRequest> notifications =
                instance.notifications(AzureDevOpsRepoNotificationContext.build(job, run, src, new BranchSCMHead("master")),
                        new LogTaskListener(
                                Logger.getLogger(getClass().getName()), Level.INFO));
        assertThat(notifications, hasSize(1));
    }

    @Test
    public void given_differentSCMheads_then_distinctNotifications() throws Exception {
        FreeStyleProject job = j.createFreeStyleProject();
        AzureDevOpsRepoSCMSource src = new AzureDevOpsRepoSCMSource("https://dev.azure.com/lukeshan", "example", "spring");
        FreeStyleBuild run = j.buildAndAssertSuccess(job);
        DefaultAzureDevOpsNotificationStrategy instance = new DefaultAzureDevOpsNotificationStrategy();
        BranchSCMHead testBranch = new BranchSCMHead("master");
        List<AzureDevOpsRepoNotificationRequest> notificationsA =
                instance.notifications(AzureDevOpsRepoNotificationContext.build(job, run, src, testBranch),
                        new LogTaskListener(Logger.getLogger(getClass().getName()), Level.INFO));
        List<AzureDevOpsRepoNotificationRequest> notificationsB =
                instance.notifications(AzureDevOpsRepoNotificationContext.build(job, run, src,
                        new PullRequestSCMHead("test-pr", "owner", "repo", "branch",
                                1, testBranch, SCMHeadOrigin.DEFAULT, ChangeRequestCheckoutStrategy.MERGE)),
                        new LogTaskListener(Logger.getLogger(getClass().getName()), Level.INFO));
        List<AzureDevOpsRepoNotificationRequest> notificationsC =
                instance.notifications(AzureDevOpsRepoNotificationContext.build(job, run, src,
                        new PullRequestSCMHead("test-pr", "owner", "repo", "branch",
                                1, testBranch, SCMHeadOrigin.DEFAULT, ChangeRequestCheckoutStrategy.HEAD)),
                        new LogTaskListener(Logger.getLogger(getClass().getName()), Level.INFO));
        assertNotEquals(notificationsA, notificationsB);
        assertNotEquals(notificationsB, notificationsC);
        assertNotEquals(notificationsA, notificationsC);
    }

    @Test
    public void given_jobOrRun_then_differentURLs() throws Exception {
        FreeStyleProject job = j.createFreeStyleProject();
        AzureDevOpsRepoSCMSource src = new AzureDevOpsRepoSCMSource("https://dev.azure.com/lukeshan", "example", "spring");
        FreeStyleBuild run = j.buildAndAssertSuccess(job);
        DefaultAzureDevOpsNotificationStrategy instance = new DefaultAzureDevOpsNotificationStrategy();
        String urlA = instance.notifications(AzureDevOpsRepoNotificationContext.build(null, run, src, new BranchSCMHead("master")),
                new LogTaskListener(Logger.getLogger(getClass().getName()), Level.INFO)).get(0).getUrl();
        String urlB = instance.notifications(AzureDevOpsRepoNotificationContext.build(job, null, src, new BranchSCMHead("master")),
                new LogTaskListener(Logger.getLogger(getClass().getName()), Level.INFO)).get(0).getUrl();
        assertNotEquals(urlA, urlB);
    }
}
