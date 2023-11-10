package com.epam.reportportal.extension.gitlab.command;

import static java.util.Optional.ofNullable;
import static org.hibernate.bytecode.BytecodeLogger.LOGGER;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.gitlab.client.GitlabClientProvider;
import com.epam.reportportal.extension.gitlab.dto.EpicDto;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import java.util.List;
import java.util.Map;

public class SearchEpicsCommand implements PluginCommand<List<EpicDto>> {

  private final GitlabClientProvider gitlabClientProvider;

  public SearchEpicsCommand(GitlabClientProvider gitlabClientProvider) {
    this.gitlabClientProvider = gitlabClientProvider;
  }

  @Override
  public String getName() {
    return "searchEpics";
  }

  @Override
  public List<EpicDto> executeCommand(Integration integration, Map<String, Object> params) {
    IntegrationParams integrationParams = ofNullable(integration.getParams()).orElseThrow(
        () -> new ReportPortalException(
            ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            "Integration params are not specified."
        ));

    String project = GitlabProperties.PROJECT.getParam(integrationParams)
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            "Project ID is not specified."));
    String term = GitlabProperties.SEARCH_TERM.getParam(params).orElseThrow(
        () -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            "Search term is not specified"));

    try {
      Long groupId = gitlabClientProvider.get(integrationParams).getProject(project).getNamespace()
          .getId();
      return gitlabClientProvider.get(integrationParams).searchEpics(groupId, term);
    } catch (Exception e) {
      LOGGER.error("Issues not found: " + e.getMessage(), e);
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
    }
  }

}
