package io.jenkins.plugins.gitlab.jenkins;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GitlabRunTrigger extends Trigger<Job<?, ?>> {

    private final boolean acceptMergeRequestHook;
    private final boolean acceptNoteHook;
    private final boolean skipDraftMergeRequests;
    private final String rebuildComment;
    private final Integer assigneeFilter;
    private final String sourceBranchFilter;
    private final String targetBranchFilter;
    private final String labelsFilter;
    private final boolean publishBuildMessagesOn;
    private final String pipelineName;
    private final String messageScheduled;
    private final String messageStarted;
    private final String messageSuccess;
    private final String messageFailure;
    private final String messageUnstable;
    private final String messageAborted;
    private final boolean messageAppendUrl;

    @DataBoundConstructor
    public GitlabRunTrigger(
            boolean acceptMergeRequestHook,
            boolean acceptNoteHook,
            boolean skipDraftMergeRequests,
            String rebuildComment,
            Integer assigneeFilter,
            String sourceBranchFilter,
            String targetBranchFilter,
            String labelsFilter,
            boolean publishBuildMessagesOn,
            String pipelineName,
            String messageScheduled,
            String messageStarted,
            String messageSuccess,
            String messageFailure,
            String messageUnstable,
            String messageAborted,
            boolean messageAppendUrl
    ) {
        this.acceptMergeRequestHook = acceptMergeRequestHook;
        this.acceptNoteHook = acceptNoteHook;
        this.skipDraftMergeRequests = skipDraftMergeRequests;
        this.rebuildComment = rebuildComment;
        this.assigneeFilter = assigneeFilter;
        this.sourceBranchFilter = sourceBranchFilter;
        this.targetBranchFilter = targetBranchFilter;
        this.labelsFilter = labelsFilter;
        this.publishBuildMessagesOn = publishBuildMessagesOn;
        this.pipelineName = pipelineName;
        this.messageScheduled = messageScheduled;
        this.messageStarted = messageStarted;
        this.messageSuccess = messageSuccess;
        this.messageFailure = messageFailure;
        this.messageUnstable = messageUnstable;
        this.messageAborted = messageAborted;
        this.messageAppendUrl = messageAppendUrl;
    }

    public boolean getAcceptMergeRequestHook() {
        return acceptMergeRequestHook;
    }

    public boolean getAcceptNoteHook() {
        return acceptNoteHook;
    }

    public Integer getAssigneeFilter() {
        return assigneeFilter;
    }

    public String getSourceBranchFilter() {
        return sourceBranchFilter;
    }

    public String getTargetBranchFilter() {
        return targetBranchFilter;
    }

    public String getLabelsFilter() {
        return labelsFilter;
    }

    public List getLabelsFilterList() {
        return labelsFilter.isEmpty() ? null : Arrays.asList(labelsFilter.split(","));
    }

    public String getRebuildComment() {
        return rebuildComment;
    }

    public boolean getSkipDraftMergeRequests() {
        return skipDraftMergeRequests;
    }

    public boolean getPublishBuildMessagesOn() {
        return publishBuildMessagesOn;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public String getMessageScheduled() {
        return messageScheduled;
    }

    public String getMessageStarted() {
        return messageStarted;
    }

    public String getMessageSuccess() {
        return messageSuccess;
    }

    public String getMessageFailure() {
        return messageFailure;
    }

    public String getMessageUnstable() {
        return messageUnstable;
    }

    public String getMessageAborted() {
        return messageAborted;
    }

    public boolean getMessageAppendUrl() {
        return messageAppendUrl;
    }

    @Extension
    public static final class DescriptorImpl extends TriggerDescriptor {

        @Override
        public boolean isApplicable(Item item) {
            return item instanceof Job && SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(item) != null;
        }

        @Override
        public String getDisplayName() {
            return "GitLab MR build";
        }
    }

    public static GitlabRunTrigger getJobTrigger(Job<?, ?> job) {
        GitlabRunTrigger trigger = null;

        if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
            ParameterizedJobMixIn.ParameterizedJob p = (ParameterizedJobMixIn.ParameterizedJob) job;
            Collection<Trigger> triggerList = p.getTriggers().values();

            for (Trigger t : triggerList) {
                if (t instanceof GitlabRunTrigger) {
                    trigger = (GitlabRunTrigger) t;
                }
            }
        }

        return trigger;
    }
}
