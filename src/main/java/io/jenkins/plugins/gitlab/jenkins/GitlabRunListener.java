package io.jenkins.plugins.gitlab.jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.gitlab.CauseData;
import io.jenkins.plugins.gitlab.gitlab.GitlabApiClient;
import jenkins.model.Jenkins;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.CommitStatus;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class GitlabRunListener extends RunListener<Run<?, ?>> {
    private static final Logger LOGGER = Logger.getLogger(GitlabRunListener.class.getName());

    @Override
    public void onInitialize(Run<?, ?> build) {
        System.out.println("initialized");
    }

    @Override
    public void onStarted(Run<?, ?> build, TaskListener listener) {
        process(build, listener, 0);
    }

    @Override
    public void onCompleted(Run<?, ?> build, TaskListener listener) {
        process(build, listener, 1);
    }

    public void process(Run<?, ?> build, TaskListener listener, Integer status) {
        GitlabRunTrigger trigger = GitlabRunTrigger.getJobTrigger(build.getParent());

        if (trigger == null) {
            return;
        }

        Cause cause = build.getCause(GitlabSCMTriggerCause.class);

        if (cause == null) {
            return;
        }

        if (status == 0 && !cause.getShortDescription().isEmpty()) {
            try {
                build.setDescription(cause.getShortDescription());
            }
            catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to set build description");
            }
        }

        CauseData mrData = ((GitlabSCMTriggerCause) cause).getData();

        if (mrData == null) {
            return;
        }

        Integer projectId = mrData.getGitlabMergeRequestProjectId();
        Integer mrId = mrData.getGitlabMergeRequestIid();

        if (projectId == 0 || mrId == 0) {
            return;
        }

        String url = Jenkins.get().getRootUrl();
        if (url != null && !url.endsWith("/")) {
            url += "/";
        }
        url += build.getUrl();
        Integer buildNumber = build.getNumber();
        String message = "";
        Constants.CommitBuildState buildState = Constants.CommitBuildState.RUNNING;
        Result result = build.getResult();

        if (trigger.getPublishBuildMessagesOn()) {
            if (result == Result.SUCCESS) {
                message = trigger.getMessageSuccess() != null ? trigger.getMessageSuccess() : null;
            }
            else if (result == Result.UNSTABLE) {
                message = trigger.getMessageUnstable() != null ? trigger.getMessageUnstable() : null;
            }
            else if (result == Result.FAILURE) {
                message = trigger.getMessageFailure() != null ? trigger.getMessageFailure() : null;
            }
            else if (result == Result.ABORTED) {
                message = trigger.getMessageAborted() != null ? trigger.getMessageAborted() : null;
            }
            else if (result == null && status == 0) {
                message = trigger.getMessageStarted() != null ? trigger.getMessageStarted() : null;
            }

            assert message != null;
            if (!message.isEmpty()) {
                if (trigger.getMessageAppendUrl()) {
                    String consoleUrl = url + (url.endsWith("/") ? "console" : "/console");
                    message += " " + consoleUrl;
                }

                // Replace env variables.
                try {
                    EnvVars envVars = build.getEnvironment(listener);

                    for (Map.Entry<String, String> entry : envVars.entrySet()) {
                        message = message.replace("$" + entry.getKey(), entry.getValue());
                    }
                }
                catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        try {
            GitlabApiClient client = new GitlabApiClient();
            long projectIdLong = Long.parseLong(String.valueOf(projectId));

            // Post comment with a status.
            if (!message.isEmpty()) {
                client.api.getNotesApi().createMergeRequestNote(projectIdLong, Long.parseLong(String.valueOf(mrId)), message, null, false);
            }

            // Set pipeline status.
            String sha = mrData.getGitlabMergeRequestLastCommitId();

            if (sha.isEmpty()) {
                return;
            }

            String pipelineName = trigger.getPipelineName();

            if (pipelineName == null || pipelineName.isEmpty()) {
                pipelineName = "Jenkins";
            }

            CommitStatus commitStatus = new CommitStatus();
            commitStatus.setName(pipelineName);
            commitStatus.setTargetUrl(url);
            commitStatus.setId(Long.parseLong(String.valueOf(buildNumber)));

            if (status == 1) {
                if (build.getResult() == Result.SUCCESS) {
                    buildState = Constants.CommitBuildState.SUCCESS;
                }
                else if (build.getResult() == Result.FAILURE || build.getResult() == Result.UNSTABLE) {
                    buildState = Constants.CommitBuildState.FAILED;
                }
                else if (build.getResult() == Result.ABORTED) {
                    buildState = Constants.CommitBuildState.CANCELED;
                }
            }

            client.api.getCommitsApi().addCommitStatus(projectIdLong, sha, buildState, commitStatus);
        }
        catch (GitLabApiException e) {
            LOGGER.log(Level.WARNING, "Something went wrong");
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }
}
