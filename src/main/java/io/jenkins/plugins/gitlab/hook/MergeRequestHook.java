package io.jenkins.plugins.gitlab.hook;

import hudson.model.Job;
import io.jenkins.plugins.gitlab.gitlab.GitlabApiMergeRequestResponse;
import io.jenkins.plugins.gitlab.jenkins.GitlabRunTrigger;

public class MergeRequestHook extends RequestHook {
    public GitlabApiMergeRequestResponse data;

    public MergeRequestHook(Job<?, ?> job, String requestBody) {
        super(job);

        this.data = new GitlabApiMergeRequestResponse(requestBody);
    }

    public void scheduleBuild() {
        if (!this.shouldProcess()) {
            return;
        }

        super.scheduleBuild(this.data);
    }

    public Boolean shouldProcess() {
        GitlabRunTrigger jobConf = this.getJobConf();

        boolean acceptHook = jobConf.getAcceptMergeRequestHook();

        if (!acceptHook) {
            return false;
        }

        if (!super.shouldProcess(this.data)) {
            return false;
        }

        // New commit was pushed.
        if (data.action.equals("update") && !data.oldRev.isEmpty()) {
            return true;
        }

        // Skip on change or close.
        if ((data.action.isEmpty() || !data.action.equals("open")) && !data.assigneeChanged) {
            return false;
        }

        return true;
    }
}
