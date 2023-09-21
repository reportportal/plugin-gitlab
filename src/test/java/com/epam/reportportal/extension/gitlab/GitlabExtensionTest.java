package com.epam.reportportal.extension.gitlab;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.gitlab.command.connection.TestConnectionCommand;
import com.epam.reportportal.extension.gitlab.rest.client.GitlabClientProvider;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitlabExtensionTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private IntegrationTypeRepository integrationTypeRepository;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private BasicTextEncryptor textEncryptor;

    @InjectMocks
    private GitlabExtension gitlabExtension = new GitlabExtension(new HashMap<>());

    private Integration integration;

    @BeforeEach
    public void init() {
        Map<String, Object> params = new HashMap<>();
        params.put("url", "https://git.epam.com");
        params.put("project", "id");
        params.put("apiToken", "token");
        IntegrationParams integrationParams = new IntegrationParams(params);
        integration = new Integration(1L, new Project(1L, "ProjectName"), new IntegrationType(),
                integrationParams, LocalDateTime.now());

        Map<String, Object> fields = new HashMap<>();
        fields.put("System.State", "Doing");
        fields.put("System.Title", "Test_Item");

    }

    @Test
    void getCommandToExecute() {
        PluginCommand<?> testConnection = gitlabExtension.getIntegrationCommand("testConnection");
        TestConnectionCommand testConnectionCommand = new TestConnectionCommand(new GitlabClientProvider(textEncryptor));
        assertEquals(testConnectionCommand.getClass(), testConnection.getClass());
    }

}

