package io.jenkins.plugins.gitlab.jenkins;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.security.csrf.CrumbExclusion;
import hudson.util.HttpResponses;
import io.jenkins.plugins.gitlab.GitlabActionController;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
@Extension
public class GitlabHookUnprotectedRootAction implements UnprotectedRootAction {

    public static final String WEBHOOK_URL = "gitlab";
    private static final Logger LOGGER = Logger.getLogger(GitlabHookUnprotectedRootAction.class.getName());
    private transient final GitlabActionController actionController = new GitlabActionController();

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return WEBHOOK_URL;
    }

    public void getDynamic(final String jobName, final StaplerRequest request, StaplerResponse response) {
        LOGGER.log(Level.INFO, "WebHook called with url: {0}", request.getRequestURIWithQueryString());
        actionController.processRequest(jobName, request, response);
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

    @Extension
    public static class GitlabHookReceiverCrumbExclusion extends CrumbExclusion {

        @Override
        public boolean process(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
            String pathInfo = req.getPathInfo();

            if (pathInfo != null && pathInfo.startsWith('/' + WEBHOOK_URL + '/')) {
                chain.doFilter(req, resp);

                return true;
            }

            return false;
        }
    }
}
