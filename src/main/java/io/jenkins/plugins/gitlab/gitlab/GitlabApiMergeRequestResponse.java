package io.jenkins.plugins.gitlab.gitlab;

public class GitlabApiMergeRequestResponse extends GitlabApiResponse {

    public GitlabApiMergeRequestResponse(String json) {
        super(json, "object_attributes");
    }
}