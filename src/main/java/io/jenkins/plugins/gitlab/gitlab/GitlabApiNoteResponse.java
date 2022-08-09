package io.jenkins.plugins.gitlab.gitlab;

import net.sf.json.JSONObject;

public class GitlabApiNoteResponse extends GitlabApiResponse {
    public final Integer noteableMergeRequestId;
    public final Integer noteableId;
    public final String noteableType;
    public final String noteableUrl;
    public final String noteableDescription;

    public GitlabApiNoteResponse(String json) {
        super(json, "merge_request");

        JSONObject jsonObject = JSONObject.fromObject(json);
        JSONObject attributes = (JSONObject) jsonObject.get("object_attributes");

        this.noteableId = attributes.optInt("id");
        this.noteableMergeRequestId = attributes.optInt("noteable_id");
        this.noteableType = attributes.optString("noteable_type");
        this.noteableUrl = attributes.optString("url");
        this.noteableDescription = attributes.optString("description");
    }
}