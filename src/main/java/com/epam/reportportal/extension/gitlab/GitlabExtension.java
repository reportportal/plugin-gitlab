package com.epam.reportportal.extension.gitlab;

import static com.epam.ta.reportportal.commons.Predicates.isNull;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static org.hibernate.bytecode.BytecodeLogger.LOGGER;

import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.IntegrationGroupEnum;
import com.epam.reportportal.extension.NamedPluginCommand;
import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.reportportal.extension.event.StartLaunchEvent;
import com.epam.reportportal.extension.gitlab.client.GitlabClientProvider;
import com.epam.reportportal.extension.gitlab.command.GetIssueCommand;
import com.epam.reportportal.extension.gitlab.command.GetIssuesCommand;
import com.epam.reportportal.extension.gitlab.command.GitlabProperties;
import com.epam.reportportal.extension.gitlab.command.RetrieveCreationParamsCommand;
import com.epam.reportportal.extension.gitlab.command.RetrieveUpdateParamsCommand;
import com.epam.reportportal.extension.gitlab.command.SearchEpicsCommand;
import com.epam.reportportal.extension.gitlab.command.SearchMilestonesCommand;
import com.epam.reportportal.extension.gitlab.command.SearchUsersCommand;
import com.epam.reportportal.extension.gitlab.command.TestConnectionCommand;
import com.epam.reportportal.extension.gitlab.event.launch.StartLaunchEventListener;
import com.epam.reportportal.extension.gitlab.event.plugin.PluginEventHandlerFactory;
import com.epam.reportportal.extension.gitlab.event.plugin.PluginEventListener;
import com.epam.reportportal.extension.gitlab.info.impl.PluginInfoProviderImpl;
import com.epam.reportportal.extension.gitlab.utils.MemoizingSupplier;
import com.epam.reportportal.extension.gitlab.utils.TicketMapper;
import com.epam.reportportal.extension.util.RequestEntityValidator;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.AllowedValue;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.jasypt.util.text.BasicTextEncryptor;
import org.pf4j.Extension;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.CollectionUtils;

/**
 * @author Zsolt Nagyaghy
 */
@Extension
//TODO: Move BtsExtension methods to commands
public class GitlabExtension implements ReportPortalExtensionPoint, DisposableBean, BtsExtension {

  public static final String BINARY_DATA_PROPERTIES_FILE_ID = "binary-data.properties";
  private static final String PLUGIN_ID = "GitLab";
  private static final String DOCUMENTATION_LINK_FIELD = "documentationLink";
  private static final String DOCUMENTATION_LINK = "https://reportportal.io/docs/plugins/GitlabBTS";
  private final String resourcesDir;
  private final Supplier<ApplicationListener<PluginEvent>> pluginLoadedListenerSupplier;
  private final Supplier<ApplicationListener<StartLaunchEvent>> startLaunchEventListenerSupplier;
  private final Supplier<GitlabClientProvider> gitlabClientProviderSupplier;
  private final Supplier<Map<String, PluginCommand<?>>> pluginCommandMapping = new MemoizingSupplier<>(
      this::getCommands);
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private IntegrationTypeRepository integrationTypeRepository;
  @Autowired
  private IntegrationRepository integrationRepository;
  @Autowired
  private ProjectRepository projectRepository;
  @Autowired
  private LaunchRepository launchRepository;
  @Autowired
  private LogRepository logRepository;
  @Autowired
  private BasicTextEncryptor textEncryptor;
  private final Supplier<Map<String, CommonPluginCommand<?>>> commonPluginCommandMapping = new MemoizingSupplier<>(
      this::getCommonCommands);

  public GitlabExtension(Map<String, Object> initParams) {
    resourcesDir = IntegrationTypeProperties.RESOURCES_DIRECTORY.getValue(initParams)
        .map(String::valueOf).orElse("");

    pluginLoadedListenerSupplier = new MemoizingSupplier<>(
        () -> new PluginEventListener(PLUGIN_ID, new PluginEventHandlerFactory(
            integrationTypeRepository,
            integrationRepository,
            new PluginInfoProviderImpl(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID)
        )));
    startLaunchEventListenerSupplier = new MemoizingSupplier<>(
        () -> new StartLaunchEventListener(launchRepository));

    gitlabClientProviderSupplier = new MemoizingSupplier<>(
        () -> new GitlabClientProvider(textEncryptor));
  }

  @Override
  public Map<String, ?> getPluginParams() {
    Map<String, Object> params = new HashMap<>();
    params.put(ALLOWED_COMMANDS, new ArrayList<>(pluginCommandMapping.get().keySet()));
    params.put(DOCUMENTATION_LINK_FIELD, DOCUMENTATION_LINK);
    params.put(COMMON_COMMANDS, new ArrayList<>(commonPluginCommandMapping.get().keySet()));
    return params;
  }

  @Override
  public PluginCommand<?> getIntegrationCommand(String commandName) {
    return pluginCommandMapping.get().get(commandName);
  }

  @Override
  public CommonPluginCommand<?> getCommonCommand(String commandName) {
    return commonPluginCommandMapping.get().get(commandName);
  }

  @Override
  public IntegrationGroupEnum getIntegrationGroup() {
    return IntegrationGroupEnum.BTS;
  }

  @PostConstruct
  public void createIntegration() {
    initListeners();
  }

  private void initListeners() {
    ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(
        AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
        ApplicationEventMulticaster.class
    );
    applicationEventMulticaster.addApplicationListener(pluginLoadedListenerSupplier.get());
    applicationEventMulticaster.addApplicationListener(startLaunchEventListenerSupplier.get());
  }

  @Override
  public void destroy() {
    removeListeners();
  }

  private void removeListeners() {
    ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(
        AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
        ApplicationEventMulticaster.class
    );
    applicationEventMulticaster.removeApplicationListener(pluginLoadedListenerSupplier.get());
    applicationEventMulticaster.removeApplicationListener(startLaunchEventListenerSupplier.get());
  }

  private Map<String, CommonPluginCommand<?>> getCommonCommands() {
    List<CommonPluginCommand<?>> commands = new ArrayList<>();
    commands.add(new RetrieveCreationParamsCommand(textEncryptor));
    commands.add(new RetrieveUpdateParamsCommand(textEncryptor));
    commands.add(new GetIssueCommand(gitlabClientProviderSupplier.get(), integrationRepository));
    return commands.stream().collect(Collectors.toMap(NamedPluginCommand::getName, it -> it));
  }

  private Map<String, PluginCommand<?>> getCommands() {
    List<PluginCommand<?>> commands = new ArrayList<>();
    commands.add(new TestConnectionCommand(gitlabClientProviderSupplier.get()));
    commands.add(new GetIssuesCommand(gitlabClientProviderSupplier.get()));
    commands.add(new SearchUsersCommand(gitlabClientProviderSupplier.get()));
    commands.add(new SearchMilestonesCommand(gitlabClientProviderSupplier.get()));
    commands.add(new SearchEpicsCommand(gitlabClientProviderSupplier.get()));
    return commands.stream().collect(Collectors.toMap(NamedPluginCommand::getName, it -> it));
  }

  @Override
  public boolean testConnection(Integration system) {
    return false;
  }

  @Override
  public Optional<Ticket> getTicket(String id, Integration system) {
    GitlabClientProvider gitlabClientProvider = gitlabClientProviderSupplier.get();

    String project = GitlabProperties.PROJECT.getParam(system.getParams())
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            "Project key is not specified."));

    try {
      return Optional.of(TicketMapper.toTicket(
          gitlabClientProvider.get(system.getParams()).getIssue(id, project)));
    } catch (Exception e) {
      LOGGER.error("Issue not found: " + e.getMessage(), e);
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
    }
  }

  @Override
  public Ticket submitTicket(PostTicketRQ ticketRQ, Integration system) {
    GitlabClientProvider gitlabClientProvider = gitlabClientProviderSupplier.get();
    RequestEntityValidator.validate(ticketRQ);
    expect(ticketRQ.getFields(), not(isNull())).verify(UNABLE_INTERACT_WITH_INTEGRATION,
        "External System fields set is empty!");

    String project = GitlabProperties.PROJECT.getParam(system.getParams())
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
          gitlabClientProvider.get(system.getParams()).postIssue(project, queryParams));
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
    }
  }

  @Override
  public List<PostFormField> getTicketFields(String issueType, Integration system) {
    return List.of(
        PostFormField.builder().id("title").fieldName("title").fieldType("string").isRequired(true)
            .build(),
        PostFormField.builder().id("description").fieldName("Description").fieldType("string")
            .build(),
        PostFormField.builder().id("issue_type").fieldName("Issue type").fieldType("string")
            .definedValues(List.of(
                new AllowedValue("issue", "issue"),
                new AllowedValue("incident", "incident")))
            .build(),
        PostFormField.builder().id("confidential").fieldName("Confidential").fieldType("string")
            .definedValues(List.of(
                new AllowedValue("false", "No"),
                new AllowedValue("true", "Yes")))
            .build(),
        PostFormField.builder().id("assignee_id").fieldName("Assignee").fieldType("autocomplete")
            .commandName("searchUsers").build(),
        PostFormField.builder().id("due_date").fieldName("Due Date").fieldType("string").build(),
        PostFormField.builder().id("labels").fieldName("Labels").fieldType("multiAutocomplete")
            .commandName("searchLabels").build(),
        PostFormField.builder().id("milestone_id").fieldName("Milestone").fieldType("autocomplete")
            .commandName("searchMilestones").build(),
        PostFormField.builder().id("epic_id").fieldName("Epic").fieldType("autocomplete")
            .commandName("searchEpics").build(),
        PostFormField.builder().id("weight").fieldName("Weight").fieldType("integer").build(),
        PostFormField.builder().id("assignee_ids").fieldName("Assignees")
            .fieldType("multiAutocomplete").commandName("searchUsers").build()
    );
  }

  @Override
  public List<String> getIssueTypes(Integration system) {
    return List.of("Issue", "Incident");
  }
}
