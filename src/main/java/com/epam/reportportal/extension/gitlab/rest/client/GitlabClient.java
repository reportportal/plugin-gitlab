package com.epam.reportportal.extension.gitlab.rest.client;


import com.epam.reportportal.extension.gitlab.rest.client.model.IssueExtended;
import com.epam.reportportal.extension.gitlab.utils.GitlabMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.gitlab4j.api.models.Project;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

public class GitlabClient {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int FIRST_PAGE = 1;
    private static final int SINGLE_PAGE_SIZE = 1;
    private static final String QUERY_PAGE = "page";
    private static final String QUERY_PER_PAGE = "per_page";
    private static final String BASE_PATH = "%s/api/v4/projects/%s";
    private static final String ISSUES_PATH = BASE_PATH + "/issues";

    private final String baseUrl;
    private final String token;

    GitlabMapper gitlabMapper = new GitlabMapper();

    public GitlabClient(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.token = token;
    }

    public Project getProject(String project) {
        Object singleEntity = getSingleEntity(project, BASE_PATH);
        return gitlabMapper.getObjectMapper().convertValue(singleEntity, Project.class);
    }

    public List<IssueExtended> getIssues(String project) {
        List<IssueExtended> response = new LinkedList<>();
        getLists(project, response, ISSUES_PATH);
        return gitlabMapper.getObjectMapper().convertValue(response, new TypeReference<>() {
        });
    }

    private <T> T getSingleEntity(String project, String path) {
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        String url = getUrl(path, project);
        return exchangeRequest(entity, restTemplate, url);
    }

    private <T> void getLists(String project, List<T> response, String path) {
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        String url = getUrl(path, project);
        exchangeRequest(FIRST_PAGE, response, entity, restTemplate, url);
    }

    private <T> T exchangeRequest(HttpEntity<String> entity, RestTemplate restTemplate, String url) {
        ResponseEntity<T> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
        }, Map.of(QUERY_PER_PAGE, SINGLE_PAGE_SIZE, QUERY_PAGE, FIRST_PAGE));
        return exchange.getBody();
    }

    private <T> void exchangeRequest(int page, List<T> response, HttpEntity<String> entity, RestTemplate restTemplate, String url) {
        ResponseEntity<T[]> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
        }, Map.of(QUERY_PER_PAGE, DEFAULT_PAGE_SIZE, QUERY_PAGE, page));

        T[] body = exchange.getBody();
        response.addAll(Arrays.asList(Objects.requireNonNull(body)));

        if (body.length == DEFAULT_PAGE_SIZE) {
            exchangeRequest(++page, response, entity, restTemplate, url);
        }
    }

    private String getUrl(String path, String project) {
        return UriComponentsBuilder.fromHttpUrl(String.format(path, baseUrl, project))
                .queryParam(QUERY_PER_PAGE, "{per_page}")
                .queryParam(QUERY_PAGE, "{page}")
                .encode()
                .toUriString();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }


}
