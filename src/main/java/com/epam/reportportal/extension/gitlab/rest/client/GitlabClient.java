package com.epam.reportportal.extension.gitlab.rest.client;


import com.epam.reportportal.extension.gitlab.rest.client.model.IssueExtended;
import com.epam.reportportal.extension.gitlab.utils.GitlabObjectMapperProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gitlab4j.api.models.Project;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * @author Zsolt Nagyaghy
 */
public class GitlabClient {

    private static final Integer DEFAULT_PAGE_SIZE = 100;
    private static final Integer FIRST_PAGE = 1;
    private static final String QUERY_PAGE = "page";
    private static final String QUERY_PER_PAGE = "per_page";
    private static final String BASE_PATH = "%s/api/v4/projects/%s";
    private static final String ISSUES_PATH = BASE_PATH + "/issues";
    private static final String SINGLE_ISSUES_PATH = ISSUES_PATH + "/%s";
    private static final Map<String, List<String>> pageParams = Map.of(QUERY_PER_PAGE, List.of(DEFAULT_PAGE_SIZE.toString()), QUERY_PAGE, List.of("{page}"));

    private final String baseUrl;
    private final String token;
    ObjectMapper objectMapper = new GitlabObjectMapperProvider().getObjectMapper();

    public GitlabClient(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.token = token;
    }

    public Project getProject(String projectId) {
        String pathUrl = String.format(BASE_PATH, baseUrl, projectId);
        Object singleEntity = singleEntityRequests(pathUrl, Map.of(), HttpMethod.GET);
        return objectMapper.convertValue(singleEntity, Project.class);
    }

    public IssueExtended getIssue(String issueId, String projectId) {
        String pathUrl = String.format(SINGLE_ISSUES_PATH, baseUrl, projectId, issueId);
        Object singleEntity = singleEntityRequests(pathUrl, Map.of(), HttpMethod.GET);
        return objectMapper.convertValue(singleEntity, IssueExtended.class);
    }

    public List<IssueExtended> getIssues(String projectId) {
        List<IssueExtended> response = new LinkedList<>();
        HashMap<String, List<String>> queryParams = new HashMap<>(pageParams);

        String pathUrl = String.format(ISSUES_PATH, baseUrl, projectId);
        getLists(response, pathUrl, queryParams);
        return objectMapper.convertValue(response, new TypeReference<>() {
        });
    }

    public IssueExtended postIssue(String projectId, Map<String, List<String>> queryParams) {
        String pathUrl = String.format(ISSUES_PATH, baseUrl, projectId);
        Object singleEntity = singleEntityRequests(pathUrl, queryParams, HttpMethod.POST);
        return objectMapper.convertValue(singleEntity, IssueExtended.class);

    }

    private <T> T singleEntityRequests(String path, Map<String, List<String>> queryParams, HttpMethod method) {
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        String url = getUrl(path, queryParams);
        return exchangeRequest(entity, restTemplate, url, method);
    }

    private <T> void getLists(List<T> response, String path, Map<String, List<String>> queryParams) {
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        String url = getUrl(path, queryParams);
        exchangeRequest(FIRST_PAGE, response, entity, restTemplate, url);
    }

    private <T> T exchangeRequest(HttpEntity<String> entity, RestTemplate restTemplate, String url, HttpMethod httpMethod) {
        ResponseEntity<T> exchange = restTemplate.exchange(url, httpMethod, entity, new ParameterizedTypeReference<>() {
        });
        return exchange.getBody();
    }

    private <T> void exchangeRequest(int page, List<T> response, HttpEntity<String> entity, RestTemplate restTemplate, String url) {
        ResponseEntity<T[]> exchange = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
        }, Map.of(QUERY_PAGE, page));

        T[] body = exchange.getBody();
        response.addAll(Arrays.asList(Objects.requireNonNull(body)));

        if (body.length == DEFAULT_PAGE_SIZE) {
            exchangeRequest(++page, response, entity, restTemplate, url);
        }
    }

    private String getUrl(String url, Map<String, List<String>> queryParams) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(url);
        queryParams.keySet().forEach(key -> uriComponentsBuilder.queryParam(key, queryParams.get(key)));
        return uriComponentsBuilder
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
