package io.jenkins.plugins.gitlab.jenkins;

import hudson.markup.EscapedMarkupFormatter;
import hudson.triggers.SCMTrigger;
import io.jenkins.plugins.gitlab.CauseData;
import jenkins.model.Jenkins;

import static com.google.common.base.Preconditions.checkNotNull;

public class GitlabSCMTriggerCause extends SCMTrigger.SCMTriggerCause {
    private final CauseData data;

    public GitlabSCMTriggerCause(CauseData data) {
        super("");

        this.data = checkNotNull(data, "data must not be null");
    }

    public CauseData getData() {
        return data;
    }

    @Override
    public String getShortDescription() {
        String desc = "";

        if (Jenkins.get().getMarkupFormatter() instanceof EscapedMarkupFormatter) {
            desc += "!";
            desc += this.data.getGitlabMergeRequestIid();
            desc += ": ";
            desc += this.data.getGitlabMergeRequestTitle();
            desc += " ";
            desc += this.data.getGitlabMergeRequestUrl();
        }
        else {
            desc += "<a href=\"";
            desc += this.data.getGitlabMergeRequestUrl();
            desc += "\" target=\"_blank\">";
            desc += "!";
            desc += this.data.getGitlabMergeRequestIid();
            desc += ": ";
            desc += this.data.getGitlabMergeRequestTitle();
            desc += "</a>";
        }

        return desc;
    }
}
