
package com.epam.reportportal.extension.gitlab.rest.client;

import com.epam.reportportal.extension.gitlab.command.utils.GitlabProperties;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.jasypt.util.text.BasicTextEncryptor;

import java.util.Optional;

public class GitlabClientProvider {

    private GitlabClient gitlabClient;
    protected BasicTextEncryptor textEncryptor;

    public GitlabClientProvider(BasicTextEncryptor textEncryptor) {
        this.textEncryptor = textEncryptor;
    }

    public GitlabClient get(IntegrationParams integrationParams) {
        return Optional.ofNullable(gitlabClient).orElse(createClient(integrationParams));
    }

    private GitlabClient createClient(IntegrationParams integrationParams) {
        String credentials = GitlabProperties.API_TOKEN.getParam(integrationParams)
                .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Api token is not specified."));
        String url = GitlabProperties.URL.getParam(integrationParams)
                .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
                        "Url to the Gitlab is not specified."
                ));
        this.gitlabClient = new GitlabClient(url, credentials);
        return gitlabClient;
    }


}
