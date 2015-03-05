package com.makeandbuild.vessl.persistence.couch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public abstract class CouchDBBaseImpl {
    protected String baseUrl;
    protected String databaseName;
    protected RestTemplate template;
    protected final Object myLock = new Object();

    public CouchDBBaseImpl() {
    }

    public CouchDBBaseImpl(RestTemplate template) {
        this.template = template;
    }


    public <T> T putForObject(String url, Object request, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return execute(url, request, HttpMethod.PUT, responseType, uriVariables);
    }


    protected <T> T execute(String url, final Object data, HttpMethod method, Class<T> responseType, Map<String, ?> urlParams) {
        return template.execute(url, method, new HttpEntityRequestCallback(data), new HttpMessageConverterExtractor<T>(responseType, template.getMessageConverters()), urlParams);
    }


    public RestTemplate getTemplate() {
        return template;
    }

    public void setTemplate(RestTemplate template) {
        this.template = template;
    }

    protected Object getLock() {
        return myLock;
    }


    protected class AcceptHeaderRequestCallback implements RequestCallback {

        private final Class<?> responseType;

        private AcceptHeaderRequestCallback(Class<?> responseType) {
            this.responseType = responseType;
        }

        @SuppressWarnings("unchecked")
        public void doWithRequest(ClientHttpRequest request) throws IOException {
            if (responseType != null) {
                List<MediaType> allSupportedMediaTypes = new ArrayList<MediaType>();
                for (HttpMessageConverter<?> messageConverter : template.getMessageConverters()) {
                    if (messageConverter.canRead(responseType, null)) {
                        List<MediaType> supportedMediaTypes = messageConverter.getSupportedMediaTypes();
                        for (MediaType supportedMediaType : supportedMediaTypes) {
                            if (supportedMediaType.getCharSet() != null) {
                                supportedMediaType =
                                        new MediaType(supportedMediaType.getType(), supportedMediaType.getSubtype());
                            }
                            allSupportedMediaTypes.add(supportedMediaType);
                        }
                    }
                }
                if (!allSupportedMediaTypes.isEmpty()) {
                    MediaType.sortBySpecificity(allSupportedMediaTypes);
                    request.getHeaders().setAccept(allSupportedMediaTypes);
                }
            }
        }
    }


    /**
     * Request callback implementation that writes the given object to the request stream.
     */
    protected class HttpEntityRequestCallback extends AcceptHeaderRequestCallback {

        private final HttpEntity requestEntity;

        private HttpEntityRequestCallback(Object requestBody) {
            this(requestBody, null);
        }

        @SuppressWarnings("unchecked")
        private HttpEntityRequestCallback(Object requestBody, Class<?> responseType) {
            super(responseType);
            if (requestBody instanceof HttpEntity) {
                this.requestEntity = (HttpEntity) requestBody;
            } else if (requestBody != null) {
                this.requestEntity = new HttpEntity(requestBody);
            } else {
                this.requestEntity = HttpEntity.EMPTY;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
            super.doWithRequest(httpRequest);
            if (!requestEntity.hasBody()) {
                HttpHeaders httpHeaders = httpRequest.getHeaders();
                HttpHeaders requestHeaders = requestEntity.getHeaders();
                if (!requestHeaders.isEmpty()) {
                    httpHeaders.putAll(requestHeaders);
                }
                if (httpHeaders.getContentLength() == -1) {
                    httpHeaders.setContentLength(0L);
                }
            } else {
                Object requestBody = requestEntity.getBody();
                Class<?> requestType = requestBody.getClass();
                HttpHeaders requestHeaders = requestEntity.getHeaders();
                MediaType requestContentType = requestHeaders.getContentType();
                for (HttpMessageConverter messageConverter : template.getMessageConverters()) {
                    if (messageConverter.canWrite(requestType, requestContentType)) {
                        if (!requestHeaders.isEmpty()) {
                            httpRequest.getHeaders().putAll(requestHeaders);
                        }
                        messageConverter.write(requestBody, requestContentType, httpRequest);
                        return;
                    }
                }
                String message = "Could not write request: no suitable HttpMessageConverter found for request type [" +
                        requestType.getName() + "]";
                if (requestContentType != null) {
                    message += " and content type [" + requestContentType + "]";
                }
                throw new RestClientException(message);
            }
        }
    }


    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }


}



