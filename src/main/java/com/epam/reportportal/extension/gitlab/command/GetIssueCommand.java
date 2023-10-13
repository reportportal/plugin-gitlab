/*
 * Copyright 2023 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.reportportal.extension.gitlab.command;

import static org.hibernate.bytecode.BytecodeLogger.LOGGER;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.gitlab.command.utils.GitlabProperties;
import com.epam.reportportal.extension.gitlab.command.utils.TicketMapper;
import com.epam.reportportal.extension.gitlab.rest.client.GitlabClientProvider;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import java.util.Map;

/**
 * @author <a href="mailto:andrei_piankouski@epam.com">Andrei Piankouski</a>
 */
public class GetIssueCommand implements PluginCommand<Ticket> {

  private final GitlabClientProvider gitlabClientProvider;

  public GetIssueCommand(GitlabClientProvider gitlabClientProvider) {
    this.gitlabClientProvider = gitlabClientProvider;
  }

  @Override
  public Ticket executeCommand(Integration integration, Map<String, Object> params) {
    String project = GitlabProperties.PROJECT.getParam(params)
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Project key is not specified."));
    String issueId = GitlabProperties.ISSUE_ID.getParam(params)
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Issue id is not specified."));

    try {
      return TicketMapper.toTicket(gitlabClientProvider.get(integration.getParams()).getIssue(issueId, project));
    } catch (Exception e) {
      LOGGER.error("Issue not found: " + e.getMessage(), e);
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
    }
  }

  @Override
  public String getName() {
    return "getIssue";
  }
}
