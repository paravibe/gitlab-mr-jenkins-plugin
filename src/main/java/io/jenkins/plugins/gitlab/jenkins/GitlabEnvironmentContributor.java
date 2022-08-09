package io.jenkins.plugins.gitlab.jenkins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;

@Extension
public class GitlabEnvironmentContributor extends EnvironmentContributor {

    @Override
    public void buildEnvironmentFor(@NonNull Run r, @NonNull EnvVars envs, @NonNull TaskListener listener) throws IOException, InterruptedException {
        GitlabSCMTriggerCause cause = (GitlabSCMTriggerCause) r.getCause(GitlabSCMTriggerCause.class);

        if (cause != null) {
            envs.overrideAll(cause.getData().fillBuildVariables());
        }
    }
}
