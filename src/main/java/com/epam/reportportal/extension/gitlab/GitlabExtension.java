package com.epam.reportportal.extension.gitlab;

import com.epam.reportportal.extension.*;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.reportportal.extension.event.StartLaunchEvent;
import com.epam.reportportal.extension.gitlab.command.*;
import com.epam.reportportal.extension.gitlab.command.binary.GetFileCommand;
import com.epam.reportportal.extension.gitlab.command.connection.TestConnectionCommand;
import com.epam.reportportal.extension.gitlab.event.launch.StartLaunchEventListener;
import com.epam.reportportal.extension.gitlab.event.plugin.PluginEventHandlerFactory;
import com.epam.reportportal.extension.gitlab.event.plugin.PluginEventListener;
import com.epam.reportportal.extension.gitlab.info.impl.PluginInfoProviderImpl;
import com.epam.reportportal.extension.gitlab.rest.client.GitlabClientProvider;
import com.epam.reportportal.extension.gitlab.utils.MemoizingSupplier;
import com.epam.reportportal.extension.util.RequestEntityConverter;
import com.epam.ta.reportportal.dao.*;
import org.jasypt.util.text.BasicTextEncryptor;
import org.pf4j.Extension;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Zsolt Nagyaghy
 */
@Extension
public class GitlabExtension implements ReportPortalExtensionPoint, DisposableBean {

    private static final String PLUGIN_ID = "Gitlab";
    public static final String BINARY_DATA_PROPERTIES_FILE_ID = "binary-data.properties";

    private static final String DOCUMENTATION_LINK_FIELD = "documentationLink";
    private static final String DOCUMENTATION_LINK = "https://reportportal.io/docs/plugins/GitlabBTS";

    private final Supplier<Map<String, PluginCommand<?>>> pluginCommandMapping = new MemoizingSupplier<>(this::getCommands);

    private final Supplier<Map<String, CommonPluginCommand<?>>> commonPluginCommandMapping = new MemoizingSupplier<>(this::getCommonCommands);

    private final String resourcesDir;

    private final Supplier<ApplicationListener<PluginEvent>> pluginLoadedListenerSupplier;
    private final Supplier<ApplicationListener<StartLaunchEvent>> startLaunchEventListenerSupplier;

    private final Supplier<GitlabClientProvider> gitlabClientProviderSupplier;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private IntegrationTypeRepository integrationTypeRepository;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RequestEntityConverter requestEntityConverter;

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private BasicTextEncryptor textEncryptor;

    public GitlabExtension(Map<String, Object> initParams) {
        resourcesDir = IntegrationTypeProperties.RESOURCES_DIRECTORY.getValue(initParams).map(String::valueOf).orElse("");

        pluginLoadedListenerSupplier = new MemoizingSupplier<>(() -> new PluginEventListener(PLUGIN_ID, new PluginEventHandlerFactory(
                integrationTypeRepository,
                integrationRepository,
                new PluginInfoProviderImpl(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID)
        )));
        startLaunchEventListenerSupplier = new MemoizingSupplier<>(() -> new StartLaunchEventListener(launchRepository));

        gitlabClientProviderSupplier = new MemoizingSupplier<>(() -> new GitlabClientProvider(textEncryptor));
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
        ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
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
        ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
                ApplicationEventMulticaster.class
        );
        applicationEventMulticaster.removeApplicationListener(pluginLoadedListenerSupplier.get());
        applicationEventMulticaster.removeApplicationListener(startLaunchEventListenerSupplier.get());
    }

    private Map<String, CommonPluginCommand<?>> getCommonCommands() {
        List<CommonPluginCommand<?>> commands = new ArrayList<>();
        commands.add(new GetFileCommand(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID));
        commands.add(new GetIssueCommand(integrationRepository, gitlabClientProviderSupplier.get()));
        return commands.stream().collect(Collectors.toMap(NamedPluginCommand::getName, it -> it));
    }

    private Map<String, PluginCommand<?>> getCommands() {
        List<PluginCommand<?>> commands = new ArrayList<>();
        commands.add(new TestConnectionCommand(gitlabClientProviderSupplier.get()));
        commands.add(new GetIssueTypesCommand(projectRepository, gitlabClientProviderSupplier.get()));
        commands.add(new GetIssuesCommand(gitlabClientProviderSupplier.get()));
        commands.add(new GetIssueFieldsCommand(projectRepository));
        commands.add(new PostTicketCommand(projectRepository,
                requestEntityConverter,
                gitlabClientProviderSupplier.get()
        ));
        return commands.stream().collect(Collectors.toMap(NamedPluginCommand::getName, it -> it));
    }
}
