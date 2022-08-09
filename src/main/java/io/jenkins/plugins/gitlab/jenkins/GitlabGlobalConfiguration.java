package io.jenkins.plugins.gitlab.jenkins;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.model.Queue;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import java.util.ArrayList;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

/**
 * Example of Jenkins global configuration.
 */
@Extension
public class GitlabGlobalConfiguration extends GlobalConfiguration {

    /** @return the singleton instance */
    public static GitlabGlobalConfiguration get() {
        return ExtensionList.lookupSingleton(GitlabGlobalConfiguration.class);
    }

    private String gitlabHostUrl;
    private String gitlabApiTokenId;

    public GitlabGlobalConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    public String getGitlabHostUrl() {
        return gitlabHostUrl;
    }

    public String getGitlabApiTokenId() {
        return gitlabApiTokenId;
    }

    public String getToken() {
        String tokenId = this.gitlabApiTokenId;

        StringCredentials stringCredentials = CredentialsMatchers.firstOrNull(
            CredentialsProvider.lookupCredentials(
                StringCredentials.class,
                Jenkins.get(),
                ACL.SYSTEM,
                URIRequirementBuilder.fromUri(null).build()
            ),
            CredentialsMatchers.withId(tokenId)
        );

        if (stringCredentials == null) {
            return null;
        }

        return stringCredentials.getSecret().getPlainText();
    }

    @DataBoundSetter
    public void setGitlabHostUrl(String url) {
        this.gitlabHostUrl = url;
        save();
    }

    @DataBoundSetter
    public void setGitlabApiTokenId(String token) {
        this.gitlabApiTokenId = token;
        save();
    }

    public ListBoxModel doFillGitlabApiTokenIdItems(@AncestorInPath final Item item, @QueryParameter final String token) {
        StandardListBoxModel result = new StandardListBoxModel();

        return result.includeEmptyValue().includeMatchingAs(
            item instanceof Queue.Task ? Tasks.getAuthenticationOf( (Queue.Task) item) : ACL.SYSTEM,
            item,
            StandardCredentials.class,
            new ArrayList<DomainRequirement>(),
            CredentialsMatchers.anyOf(
                CredentialsMatchers.instanceOf(StringCredentials.class)
            )
        ).includeCurrentValue(token);
    }

    public FormValidation doCheckGitlabHostUrl(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("GitLab Host URL is required.");
        }

        return FormValidation.ok();
    }

    public FormValidation doCheckGitlabApiTokenId(@QueryParameter String value) {
        if (value == null || StringUtils.isEmpty(value)) {
            return FormValidation.error("API token is required");
        }

        return FormValidation.ok();
    }
}
