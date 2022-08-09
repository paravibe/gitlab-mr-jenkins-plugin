package io.jenkins.plugins.gitlab.hook;

import hudson.model.Job;
import io.jenkins.plugins.gitlab.gitlab.GitlabApiNoteResponse;
import io.jenkins.plugins.gitlab.jenkins.GitlabRunTrigger;

public class NoteHook extends RequestHook {
    public GitlabApiNoteResponse data;

    public NoteHook(Job<?, ?> job, String requestBody) {
        super(job);

        this.data = new GitlabApiNoteResponse(requestBody);
    }

    public void scheduleBuild() {
        if (!this.shouldProcess()) {
            return;
        }

        super.scheduleBuild(this.data);
    }

    public Boolean shouldProcess() {
        GitlabRunTrigger jobConf = this.getJobConf();

        boolean acceptHook = jobConf.getAcceptNoteHook();

        if (!acceptHook) {
            return false;
        }

        if (!super.shouldProcess(this.data)) {
            return false;
        }

        if (!data.noteableType.equals("MergeRequest")) {
            return false;
        }

        String rebuildComment = jobConf.getRebuildComment();

        if (!rebuildComment.isEmpty() && !data.noteableDescription.equals(rebuildComment)) {
            return false;
        }

        return true;
    }
}
