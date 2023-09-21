/*
 * Copyright 2021 EPAM Systems
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

import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.gitlab.command.utils.GitlabMapper;
import com.epam.reportportal.extension.gitlab.command.utils.GitlabProperties;
import com.epam.reportportal.extension.gitlab.rest.client.GitlabClient;
import com.epam.reportportal.extension.gitlab.rest.client.GitlabClientProvider;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.TicketRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class GetIssueCommand implements CommonPluginCommand<Ticket> {


    private final String TICKET_ID = "ticketId";
    private final String PROJECT_ID = "projectId";

    private final TicketRepository ticketRepository;
    private final IntegrationRepository integrationRepository;
    private final GitlabClientProvider gitlabClientProvider;

    public GetIssueCommand(TicketRepository ticketRepository, IntegrationRepository integrationRepository,
                           GitlabClientProvider gitlabClientProvider) {
        this.ticketRepository = ticketRepository;
        this.integrationRepository = integrationRepository;
        this.gitlabClientProvider = gitlabClientProvider;
    }

    @Override
    public String getName() {
        return "getIssue";
    }

    @Override
    public Ticket executeCommand(Map<String, Object> params) {
        final com.epam.ta.reportportal.entity.bts.Ticket ticket = ticketRepository.findByTicketId((String) Optional.ofNullable(params.get(
                        TICKET_ID)).orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, TICKET_ID + " must be provided")))
                .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Ticket not found with id " + TICKET_ID));
        final Long projectId = (Long) Optional.ofNullable(params.get(PROJECT_ID))
                .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, PROJECT_ID + " must be provided"));

        final String btsUrl = GitlabProperties.URL.getParam(params)
                .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
        final String btsProject = GitlabProperties.PROJECT.getParam(params)
                .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Project is not specified."));


        final Integration integration = integrationRepository.findProjectBtsByUrlAndLinkedProject(btsUrl, btsProject, projectId)
                .orElseGet(() -> integrationRepository.findGlobalBtsByUrlAndLinkedProject(btsUrl, btsProject)
                        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
                                "Integration with provided url and project isn't found"
                        )));
        return getTicket(ticket.getTicketId(), btsProject, gitlabClientProvider.get(integration.getParams()));
    }

    private Ticket getTicket(String issueId, String projectId, GitlabClient gitlabClient) {

        try {
            return GitlabMapper.toTicket(gitlabClient.getIssue(issueId, projectId));
        } catch (Exception e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
        }
    }
}
