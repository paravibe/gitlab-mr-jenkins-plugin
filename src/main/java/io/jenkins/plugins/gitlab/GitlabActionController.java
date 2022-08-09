package io.jenkins.plugins.gitlab;

import com.google.common.base.Splitter;
import hudson.model.Item;
import hudson.model.Job;
import hudson.util.HttpResponses;
import io.jenkins.plugins.gitlab.hook.MergeRequestHook;
import io.jenkins.plugins.gitlab.hook.NoteHook;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GitlabActionController {

    private static final Logger LOGGER = Logger.getLogger(GitlabActionController.class.getName());

    public void processRequest(String jobName, StaplerRequest request, StaplerResponse response) {
        String method = request.getMethod();
        Iterator<String> restOfPathParts = Splitter.on('/').omitEmptyStrings().split(request.getRestOfPath()).iterator();
        Job<?, ?> job = resolveJob(jobName, restOfPathParts);

        if (job == null || !method.equals("POST")) {
            LOGGER.log(Level.WARNING, "Method is not POST");

            throw HttpResponses.ok();
        }

        String eventHeader = request.getHeader("X-Gitlab-Event");

        if (eventHeader == null) {
            LOGGER.log(Level.WARNING, "Missing X-Gitlab-Event header");

            throw HttpResponses.ok();
        }

        String requestBody = getRequestBody(request);

        switch (eventHeader) {
            case "Merge Request Hook":
                new MergeRequestHook(job, requestBody).scheduleBuild();

                throw HttpResponses.ok();

            case "Note Hook":
                new NoteHook(job, requestBody).scheduleBuild();

                throw HttpResponses.ok();
        }
    }

    private Job<?, ?> resolveJob(final String projectName, final Iterator<String> restOfPathParts) {
        try {
            final Jenkins jenkins = Jenkins.get();

            Item item = jenkins.getItemByFullName(projectName);

            if (item instanceof Job<?, ?>) {
                return (Job<?, ?>) item;
            }
        }
        catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Job not found: {0}", projectName);

            return null;
        }

        return null;
    }

    private String getRequestBody(StaplerRequest request) {
        String requestBody;

        try {
            Charset charset = request.getCharacterEncoding() == null ?  UTF_8 : Charset.forName(request.getCharacterEncoding());
            requestBody = IOUtils.toString(request.getInputStream(), charset);
        }
        catch (IOException e) {
            throw HttpResponses.error(500, "Failed to read request body");
        }

        return requestBody;
    }
}
