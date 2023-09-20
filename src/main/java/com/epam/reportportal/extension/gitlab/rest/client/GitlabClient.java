package com.epam.reportportal.extension.gitlab.rest.client;


import com.epam.reportportal.extension.gitlab.utils.GitlabMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Project;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

public class GitlabClient {

    private static final int DEFAULT_PAGE_SIZE = 100;
    public static final String QUERY_PAGE = "page";
    public static final String QUERY_PER_PAGE = "per_page";
    public static final int FIRST_PAGE = 1;
    public static final int SINGLE_PAGE_SIZE = 1;

    private final String baseUrl;
    private final String token;

    GitlabMapper gitlabMapper = new GitlabMapper();

    public GitlabClient(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.token = token;
    }

    public Project getProject(String project) {
        Project response = null;
        Project response2 = null;
        try {
            response2 = gitlabMapper.getObjectMapper().convertValue(getSingleEntity(project, "%s/api/v4/projects/%s"), Project.class);
            response = exchangeRequestOk("%s/api/v4/projects/%s", project, Project.class);
        } catch (Exception e) {
            System.out.println(e);
        }
        return response2;
    }

    public List<Issue> getIssues(String project) {
        List<Issue> response = new LinkedList<>();
        try {
            getLists(project, response, "%s/api/v4/projects/%s/issues");
        } catch (Exception e) {
            System.out.println(e);
        }
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

    private <T> T exchangeRequestOk(String path, String project, Class<T> responseType)  {
        OkHttpClient httpClient = new OkHttpClient();

        HttpUrl.Builder urlBuilder
                = Objects.requireNonNull(HttpUrl.parse(String.format(path, baseUrl, project))).newBuilder();
        urlBuilder.addQueryParameter(QUERY_PER_PAGE, "1");
        urlBuilder.addQueryParameter(QUERY_PAGE, "1");

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP response code: " + response.code());
            }

            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String responseBodyString = responseBody.string();
                return gitlabMapper.getObjectMapper().readValue(responseBodyString, responseType);

            } else {
                throw new IOException("Response body is null");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
