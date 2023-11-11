package com.epam.reportportal.extension.gitlab.command;

import static com.epam.ta.reportportal.commons.Predicates.isNull;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

import com.epam.reportportal.extension.ProjectMemberCommand;
import com.epam.reportportal.extension.gitlab.client.GitlabClientProvider;
import com.epam.reportportal.extension.gitlab.utils.TicketMapper;
import com.epam.reportportal.extension.util.CommandParamUtils;
import com.epam.reportportal.extension.util.RequestEntityConverter;
import com.epam.reportportal.extension.util.RequestEntityValidator;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

public class PostTicketCommand extends ProjectMemberCommand<Ticket> {

  private final GitlabClientProvider gitlabClientProvider;
  private final RequestEntityConverter requestEntityConverter;

  public PostTicketCommand(ProjectRepository projectRepository,
      GitlabClientProvider gitlabClientProvider, RequestEntityConverter requestEntityConverter) {
    super(projectRepository);
    this.gitlabClientProvider = gitlabClientProvider;
    this.requestEntityConverter = requestEntityConverter;
  }

  @Override
  protected Ticket invokeCommand(Integration integration, Map<String, Object> params) {
    PostTicketRQ ticketRQ = requestEntityConverter.getEntity(CommandParamUtils.ENTITY_PARAM, params,
        PostTicketRQ.class);
    RequestEntityValidator.validate(ticketRQ);
    expect(ticketRQ.getFields(), not(isNull())).verify(UNABLE_INTERACT_WITH_INTEGRATION,
        "External System fields set is empty!");

    String project = GitlabProperties.PROJECT.getParam(integration.getParams())
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            "Project key is not specified."));

    Map<String, List<String>> queryParams = ticketRQ.getFields().stream()
        .filter(form -> form.getValue() != null && !form.getValue().isEmpty())
        .collect(Collectors.toMap(PostFormField::getId, PostFormField::getValue));

    ticketRQ.getFields().stream().filter(field -> !CollectionUtils.isEmpty(field.getNamedValue()))
        .forEach(field -> queryParams.put(field.getId(),
            field.getNamedValue().stream().map(it -> String.valueOf(it.getId())).collect(
                Collectors.toList())));

    try {
      return TicketMapper.toTicket(
          gitlabClientProvider.get(integration.getParams()).postIssue(project, queryParams));
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
    }
  }

  @Override
  public String getName() {
    return "postTicket";
  }
}
