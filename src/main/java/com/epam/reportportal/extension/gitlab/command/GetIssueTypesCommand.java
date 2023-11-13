package com.epam.reportportal.extension.gitlab.command;

import com.epam.reportportal.extension.ProjectMemberCommand;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import java.util.List;
import java.util.Map;

public class GetIssueTypesCommand extends ProjectMemberCommand<List<String>> {

  public GetIssueTypesCommand(ProjectRepository projectRepository) {
    super(projectRepository);
  }

  @Override
  public String getName() {
    return "getIssueTypes";
  }

  @Override
  protected List<String> invokeCommand(Integration integration, Map<String, Object> params) {
    return List.of("Issue", "Incident");
  }
}
