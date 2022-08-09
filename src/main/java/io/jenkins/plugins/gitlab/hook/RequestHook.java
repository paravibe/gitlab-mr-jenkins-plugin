package io.jenkins.plugins.gitlab.hook;

import hudson.model.Action;
import hudson.model.CauseAction;
import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import io.jenkins.plugins.gitlab.CauseData;
import io.jenkins.plugins.gitlab.gitlab.GitlabApiClient;
import io.jenkins.plugins.gitlab.gitlab.GitlabApiResponse;
import io.jenkins.plugins.gitlab.jenkins.GitlabRunTrigger;
import io.jenkins.plugins.gitlab.jenkins.GitlabSCMTriggerCause;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.CommitStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class RequestHook {
    private final Job<?, ?> job;
    private final GitlabRunTrigger jobConf;
    public GitlabApiResponse data;

    private static final Logger LOGGER = Logger.getLogger(RequestHook.class.getName());

    public RequestHook(Job<?, ?> job) {
        this.job = job;
        this.jobConf = GitlabRunTrigger.getJobTrigger(job);
    }

    public Job<?, ?> getJob() {
        return this.job;
    }

    public GitlabRunTrigger getJobConf() {
        return this.jobConf;
    }

    public void scheduleBuild(GitlabApiResponse data) {
        int projectBuildDelay = 0;

        if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
            ParameterizedJobMixIn.ParameterizedJob abstractProject = (ParameterizedJobMixIn.ParameterizedJob) job;

            if (abstractProject.getQuietPeriod() > projectBuildDelay) {
                projectBuildDelay = abstractProject.getQuietPeriod();
            }
        }

        retrieveScheduleJob(job).scheduleBuild2(projectBuildDelay, createActions(data));
        updateMergeRequest(data);
    }

    private ParameterizedJobMixIn retrieveScheduleJob(final Job<?, ?> job) {
        return new ParameterizedJobMixIn() {
            @Override
            protected Job asJob() {
                return job;
            }
        };
    }

    private Action[] createActions(GitlabApiResponse mrData) {
        List<Action> actions = new ArrayList<>();

        CauseData data = new CauseData();
        data.setGitlabMergeRequestTitle(mrData.title);
        data.setGitlabMergeRequestDescription(mrData.description);
        data.setGitlabMergeRequestUrl(mrData.url);
        data.setGitlabMergeRequestId(mrData.id);
        data.setGitlabMergeRequestIid(mrData.iid);
        data.setGitlabMergeRequestSourceBranch(mrData.sourceBranch);
        data.setGitlabMergeRequestTargetBranch(mrData.targetBranch);
        data.setGitlabMergeRequestProjectId(mrData.projectId);
        data.setGitlabMergeRequestProjectName(mrData.projectName);
        data.setGitlabMergeRequestProjectWebUrl(mrData.projectWebUrl);
        data.setGitlabMergeRequestLastCommitId(mrData.lastCommitId);
        data.setGitlabMergeRequestSshUrl(mrData.sshUrl);
        data.setGitlabMergeRequestWebUrl(mrData.webUrl);
        data.setGitlabMergeRequestLabels(mrData.labels);
        data.setGitlabMergeRequestUserName(mrData.userName);
        data.setGitlabMergeRequestUserUsername(mrData.userUsername);
        data.setGitlabMergeRequestAuthorUsername(mrData.authorUsername);

        String pipelineName = this.jobConf.getPipelineName();

        if (pipelineName == null || pipelineName.isEmpty()) {
            pipelineName = "Jenkins";
        }

        data.setGitlabJobPipelineName(pipelineName);

        actions.add(new CauseAction(new GitlabSCMTriggerCause(data)));

        return actions.toArray(new Action[actions.size()]);
    }

    public Boolean shouldProcess(GitlabApiResponse data) {
        if (this.jobConf == null) {
            return false;
        }

        boolean repositoryAllowed = false;
        SCMTriggerItem item = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(this.job);
        GitSCM gitSCM = getGitSCM(item);
        List<RemoteConfig> repositories = gitSCM.getRepositories();

        for (RemoteConfig repository : repositories) {
            List<URIish> uris = repository.getURIs();

            for (URIish uri : uris) {
                if (
                        (!data.sshUrl.isEmpty() && data.sshUrl.equals(uri.toString()))
                        || (!data.webUrl.isEmpty() &&data.webUrl.equals(uri.toString()))
                ) {
                    repositoryAllowed = true;

                    break;
                }
            }

            if (repositoryAllowed) {
                break;
            }
        }

        if (!repositoryAllowed) {
            return false;
        }

        Integer jobAssigneeId = this.jobConf.getAssigneeFilter();

        // Skip on assignee.
        if (jobAssigneeId != null && !data.assigneeId.equals(jobAssigneeId)) {
            return false;
        }

        // Skip on draft.
        if (this.jobConf.getSkipDraftMergeRequests() && data.workInProgress) {
            return false;
        }

        String jobSourceBranch = this.jobConf.getSourceBranchFilter();
        String jobTargetBranch = this.jobConf.getTargetBranchFilter();

        if (!jobSourceBranch.isEmpty()) {
            if (!data.sourceBranch.matches(jobSourceBranch)) {
                return false;
            }
        }

        if (!jobTargetBranch.isEmpty()) {
            if (!data.targetBranch.matches(jobTargetBranch)) {
                return false;
            }
        }

        if (this.jobConf.getLabelsFilterList() != null) {
            HashSet<String> set = new HashSet<>(this.jobConf.getLabelsFilterList());
            set.retainAll(data.labels);

            if (set.size() == 0) {
                return false;
            }
        }

        return true;
    }

    private GitSCM getGitSCM(SCMTriggerItem item) {
        if (item != null) {
            for (SCM scm : item.getSCMs()) {
                if (scm instanceof GitSCM) {
                    return (GitSCM) scm;
                }
            }
        }
        return null;
    }

    private void updateMergeRequest(GitlabApiResponse mrData) {
        try {
            GitlabApiClient client = new GitlabApiClient();

            if (mrData.projectId == 0 || mrData.iid == 0) {
                return;
            }

            GitlabRunTrigger trigger = GitlabRunTrigger.getJobTrigger(this.job);
            String message = trigger != null ? trigger.getMessageScheduled() : null;
            long projectIdLong = Long.parseLong(String.valueOf(mrData.projectId));

            // Post comment with a status.
            if (message != null && !message.equals("")) {
                client.api.getNotesApi().createMergeRequestNote(projectIdLong, Long.parseLong(String.valueOf(mrData.iid)), message);
            }

            // Set pipeline status.
            if (mrData.lastCommitId.isEmpty() || mrData.lastCommitId.equals("null")) {
                return;
            }

            String pipelineName = this.jobConf.getPipelineName();

            if (pipelineName == null || pipelineName.isEmpty()) {
                pipelineName = "Jenkins";
            }

            CommitStatus commitStatus = new CommitStatus();
            commitStatus.setName(pipelineName);

            client.api.getCommitsApi().addCommitStatus(
                projectIdLong,
                mrData.lastCommitId,
                Constants.CommitBuildState.PENDING,
                commitStatus
            );
        }
        catch (GitLabApiException e) {
            LOGGER.log(Level.WARNING, "Something went wrong");
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }
}
