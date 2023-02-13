// ------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
// ------------------------------------------------------------

package com.test.proxy.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.test.proxy.transport.TestProxyVariables;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

public final class ApacheHttpClient implements HttpClient {
    private final org.apache.http.client.HttpClient httpClient;
    private final TestProxyVariables testProxyVariables;

    public ApacheHttpClient() {
        this.httpClient = createSSLClient();
        testProxyVariables = null;
    }

    protected ApacheHttpClient(TestProxyVariables testProxyVariables) {
        this.httpClient = createSSLClient();
        this.testProxyVariables = testProxyVariables;
    }

    // send() and sendSync() are called to service
    // http requests. These methods can be used to inject custom code
    // that modifies an http request, which is how we reroute traffic
    // to the proxy. Rerouting is done by 'stashing' the original request
    // in a request header and changing the reqeuested URI address to
    // the address of the test proxy (localhost:5001 by default).
    // The proxy reads the original request URI out of the header and
    // saves it in a JSON-formatted recording file (if in record mode),
    // or reads it from the JSON recording file (if in playback mode).
    // It will then either forward the request to the internet (record
    // mode) or forward the relevant response to your app (playback mode).
    public Mono<HttpResponse> send(HttpRequest azureRequest, Context context) {
        return this.send(azureRequest);
    }

    public Mono<HttpResponse> send(HttpRequest azureRequest) {
        try {
            HttpRequest redirectRequest = azureRequest.copy();
            if (testProxyVariables.isUseProxy()
                    && !testProxyVariables.getProxyHost()
                    .equals(azureRequest.getUrl().getHost())) {
                redirectRequest = redirectToTestProxy(azureRequest, testProxyVariables);
            }
            ApacheHttpRequest apacheRequest = new ApacheHttpRequest(
                    redirectRequest.getHttpMethod(), redirectRequest.getUrl(),
                    redirectRequest.getHeaders());
            apacheRequest.removeHeaders(HTTP.CONTENT_LEN);

            EntityBuilder entityBuilder = EntityBuilder.create()
                    .setContentType(ContentType.APPLICATION_JSON);

            if (azureRequest.getUrl().getPath()
                    .endsWith("/" + testProxyVariables.getProxyMode() + "/start")) {
                apacheRequest.setEntity(entityBuilder.setBinary(
                        JsonSerializerProviders.createInstance(true)
                                .serializeToBytes(new HashMap<String, String>() {{
                                    put("x-recording-file",
                                            testProxyVariables.getCurrentRecordingPath());
                                }})).build());
            } else if (redirectRequest.getBodyAsBinaryData() != null) {
                apacheRequest.setEntity(entityBuilder.setBinary(
                        redirectRequest.getBodyAsBinaryData().toBytes()
                ).build());
            }

            return Mono.just(new ApacheHttpResponse(azureRequest, httpClient.execute(apacheRequest)));

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public HttpResponse sendSync(HttpRequest request, Context context) {
        return this.send(request, context).block();
    }

    // createSSLClient() is created a CloseableHttpClient
    // and it will circumvent [SSL] verification
    private CloseableHttpClient createSSLClient() {
        try {
            return HttpClientBuilder.create()
                    .setSSLSocketFactory(
                            new SSLConnectionSocketFactory(
                                    new SSLContextBuilder()
                                            .loadTrustMaterial(null, (arg0, arg1) -> true)
                                            .build(),
                                    NoopHostnameVerifier.INSTANCE))
                    .build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private HttpRequest redirectToTestProxy(HttpRequest request, TestProxyVariables testProxyVariables) {
        try {
            HttpRequest originRequest = request.copy();
            URL requestUrl = request.getUrl();
            String recordingUpstreamBaseUri = new UrlBuilder()
                    .setScheme(requestUrl.getProtocol())
                    .setHost(requestUrl.getHost())
                    .setPort(requestUrl.getPort())
                    .toUrl().toString();
            originRequest.setHeader("x-recording-upstream-base-uri", recordingUpstreamBaseUri);
            originRequest.setHeader("x-recording-id", testProxyVariables.getRecordingId());
            originRequest.setHeader("x-recording-mode", testProxyVariables.getProxyMode());
            UrlBuilder urlBuilder = UrlBuilder.parse(requestUrl).setHost(testProxyVariables.getProxyHost());
            if (Objects.nonNull(testProxyVariables.getProxyPort())) {
                urlBuilder.setPort(testProxyVariables.getProxyPort());
            }
            originRequest.setUrl(urlBuilder.toUrl());
            return originRequest;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}