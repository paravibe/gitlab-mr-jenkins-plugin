package io.jenkins.plugins.gitlab.gitlab;

import io.jenkins.plugins.gitlab.jenkins.GitlabGlobalConfiguration;
import jenkins.model.Jenkins;
import org.gitlab4j.api.GitLabApi;

public class GitlabApiClient {
    public final GitLabApi api;

    public GitlabApiClient() {
        GitlabGlobalConfiguration config = (GitlabGlobalConfiguration) Jenkins.get().getDescriptor(GitlabGlobalConfiguration.class);
        String hostUrl = config.getGitlabHostUrl();
        String apiToken = config.getToken();

        this.api = new GitLabApi(hostUrl, apiToken);
    }
}
