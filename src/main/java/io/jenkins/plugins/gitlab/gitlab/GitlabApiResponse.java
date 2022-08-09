package io.jenkins.plugins.gitlab.gitlab;

import io.jenkins.plugins.gitlab.jenkins.GitlabGlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.User;

import java.util.ArrayList;
import java.util.List;

public class GitlabApiResponse {

    public final Integer assigneeId;
    public final Integer authorId;
    public final String title;
    public final String description;
    public final Integer id;
    public final Integer iid;
    public final String mergeCommitSha;
    public final boolean workInProgress;
    public final String state;
    public final String action;
    public final String url;
    public final String sourceBranch;
    public final String targetBranch;
    public final boolean assigneeChanged;
    public final Integer projectId;
    public final String projectName;
    public final String projectWebUrl;
    public final String lastCommitId;
    public final String oldRev;
    public final String sshUrl;
    public final String webUrl;
    public final List labels;
    public final Integer userId;
    public final String userUsername;
    public final String userName;
    public final String authorUsername;

    public GitlabApiResponse(String json, String attributeKey) {
        JSONObject jsonObject = JSONObject.fromObject(json);

        JSONObject attributes = (JSONObject) jsonObject.get(attributeKey);
        JSONObject user = jsonObject.optJSONObject("user");
        JSONObject changes = jsonObject.optJSONObject("changes");
        JSONObject project = jsonObject.optJSONObject("project");
        JSONObject commit = attributes != null ? attributes.optJSONObject("last_commit") : null;
        JSONObject source = attributes != null ? attributes.optJSONObject("source") : null;

        this.assigneeId = attributes != null ? attributes.optInt("assignee_id") : 0;
        this.authorId = attributes != null ? attributes.optInt("author_id") : 0;
        this.title = attributes != null ? attributes.optString("title") : "";
        this.description = attributes != null ? attributes.optString("description") : "";
        this.id = attributes != null ? attributes.optInt("id") : 0;
        this.iid = attributes != null ? attributes.optInt("iid") : 0;
        this.mergeCommitSha = attributes != null ? attributes.optString("merge_commit_sha") : "";
        this.workInProgress = attributes != null ? (boolean) attributes.get("work_in_progress") : false;
        this.state = attributes != null ? attributes.optString("state") : "";
        this.action = attributes != null ? attributes.optString("action") : "";
        this.url = attributes != null ? attributes.optString("url") : "";
        this.sourceBranch = attributes != null ? attributes.optString("source_branch") : "";
        this.targetBranch = attributes != null ? attributes.optString("target_branch") : "";
        this.assigneeChanged = changes != null && changes.optJSONObject("assignees") != null;
        this.projectId = project != null ? project.optInt("id") : 0;
        this.projectName = project != null ? project.optString("name") : "";
        this.projectWebUrl = project != null ? project.optString("web_url") : "";
        this.lastCommitId = commit != null ? commit.optString("id") : "";
        this.oldRev = attributes != null ? attributes.optString("oldrev") : "";
        this.sshUrl = source != null ? source.optString("ssh_url") : "";
        this.webUrl = source != null ? source.optString("web_url") : "";

        List<Object> labels = new ArrayList<>();
        JSONArray labelsJson = attributes != null ? attributes.optJSONArray("labels") : new JSONArray();

        if (labelsJson.size() != 0) {
            for (Object label : labelsJson) {
                labels.add(((JSONObject) label).get("title"));
            }
        }

        this.labels = labels;

        this.userId = user != null ? user.optInt("id") : 0;
        this.userUsername = user != null ? user.optString("username") : "";
        this.userName = user != null ? user.optString("name") : "";

        String authorUsername = "";

        try {
            GitlabApiClient client = new GitlabApiClient();
            User author = client.api.getUserApi().getUser(Long.valueOf(this.authorId));

            authorUsername = author.getUsername();
        }
        catch (GitLabApiException e) {
            throw new RuntimeException(e);
        }

        this.authorUsername = authorUsername;
    }
}