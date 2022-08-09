package io.jenkins.plugins.gitlab;

import lombok.Data;
import org.kohsuke.stapler.export.Exported;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CauseData {
    private String gitlabMergeRequestTitle;
    private String gitlabMergeRequestDescription;
    private String gitlabMergeRequestUrl;
    private Integer gitlabMergeRequestId;
    private Integer gitlabMergeRequestIid;
    private String gitlabMergeRequestSourceBranch;
    private String gitlabMergeRequestTargetBranch;
    private Integer gitlabMergeRequestProjectId;
    private String gitlabMergeRequestProjectName;
    private String gitlabMergeRequestProjectWebUrl;
    private String gitlabMergeRequestLastCommitId;
    private String gitlabMergeRequestSshUrl;
    private String gitlabMergeRequestWebUrl;
    private List gitlabMergeRequestLabels;
    private String gitlabMergeRequestUserName;
    private String gitlabMergeRequestUserUsername;
    private String gitlabMergeRequestAuthorUsername;
    private String gitlabJobPipelineName;

    @Exported
    public Map<String, String> fillBuildVariables() {
        Map<String, String> env  = new HashMap<String, String>();

        env.put("gitlabMergeRequestTitle", gitlabMergeRequestTitle);
        env.put("gitlabMergeRequestDescription", gitlabMergeRequestDescription);
        env.put("gitlabMergeRequestUrl", gitlabMergeRequestUrl);
        env.put("gitlabMergeRequestId", gitlabMergeRequestId == null ? "" : gitlabMergeRequestId.toString());
        env.put("gitlabMergeRequestIid", gitlabMergeRequestIid == null ? "" : gitlabMergeRequestIid.toString());
        env.put("gitlabMergeRequestSourceBranch", gitlabMergeRequestSourceBranch);
        env.put("gitlabMergeRequestTargetBranch", gitlabMergeRequestTargetBranch);
        env.put("gitlabMergeRequestProjectId", gitlabMergeRequestProjectId == null ? "" : gitlabMergeRequestProjectId.toString());
        env.put("gitlabMergeRequestProjectName", gitlabMergeRequestProjectName);
        env.put("gitlabMergeRequestProjectWebUrl", gitlabMergeRequestProjectWebUrl);
        env.put("gitlabMergeRequestLastCommitId", gitlabMergeRequestLastCommitId);
        env.put("gitlabMergeRequestSshUrl", gitlabMergeRequestSshUrl);
        env.put("gitlabMergeRequestWebUrl", gitlabMergeRequestWebUrl);
        env.put("gitlabMergeRequestLabels", String.join(",", gitlabMergeRequestLabels));
        env.put("gitlabMergeRequestUserName", gitlabMergeRequestUserName);
        env.put("gitlabMergeRequestUserUsername", gitlabMergeRequestUserUsername);
        env.put("gitlabMergeRequestAuthorUsername", gitlabMergeRequestAuthorUsername);
        env.put("gitlabJobPipelineName", gitlabJobPipelineName);

        return env;
    }
}
