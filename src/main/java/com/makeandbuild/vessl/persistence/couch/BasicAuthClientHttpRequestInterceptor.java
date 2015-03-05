package com.makeandbuild.vessl.persistence.couch;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class BasicAuthClientHttpRequestInterceptor implements ClientHttpRequestInterceptor, HttpRequestInterceptor {
    private final String encodedCredentials;

    // this version of the constructor is intended for test purposes only
    // the tests for the encoding is included in Encoder implementation
    protected BasicAuthClientHttpRequestInterceptor(String encodedCredentials) {
        this.encodedCredentials = encodedCredentials;
    }

    public BasicAuthClientHttpRequestInterceptor(String userName, String password, Encoder<String> encoder) {
        encodedCredentials = encoder.encode(userName, password);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().add("Authorization", encodedCredentials);
        return execution.execute(request, body);
    }

    @Override
    public void process(org.apache.http.HttpRequest request, HttpContext context) throws HttpException, IOException {
        request.setHeader("Authorization", encodedCredentials);
    }
}
