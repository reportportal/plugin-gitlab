
package com.epam.reportportal.extension.gitlab.rest.client;

import com.epam.reportportal.extension.gitlab.command.utils.GitlabProperties;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.gitlab4j.api.GitLabApi;
import org.jasypt.util.text.BasicTextEncryptor;

public class GitlabClientProvider {

    protected BasicTextEncryptor textEncryptor;

    public GitlabClientProvider(BasicTextEncryptor textEncryptor) {
        this.textEncryptor = textEncryptor;
    }

    public GitLabApi apiClientFactory(IntegrationParams integrationParams) {

        String credentials = GitlabProperties.API_TOKEN.getParam(integrationParams)
                .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Api token is not specified."));
        String url = GitlabProperties.URL.getParam(integrationParams)
                .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
                        "Url to the Gitlab is not specified."
                ));

        GitLabApi gitLabApi = new GitLabApi(url, credentials);
        return gitLabApi;
    }


}
