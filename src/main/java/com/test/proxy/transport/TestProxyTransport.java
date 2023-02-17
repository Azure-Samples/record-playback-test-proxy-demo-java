// ------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
// ------------------------------------------------------------

package com.test.proxy.transport;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import okhttp3.OkHttpClient;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class TestProxyTransport implements HttpClient {
    private HttpClient httpClient = HttpClient.createDefault();
    private final TestProxyVariables testProxyVariables;

    public TestProxyTransport(TestProxyVariables testProxyVariables) {
        if (testProxyVariables.isUseProxy()) {
            this.httpClient = createSSLClient();
        }
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
        if (testProxyVariables.isUseProxy()
                && !testProxyVariables.getProxyHost()
                .equals(azureRequest.getUrl().getHost())) {
            redirectToTestProxy(azureRequest, testProxyVariables);
        }
        return httpClient.send(azureRequest);
    }

    public HttpResponse sendSync(HttpRequest request, Context context) {
        return this.send(request, context).block();
    }

    // createSSLClient() is created a http client
    // and it will circumvent [SSL] verification
    private HttpClient createSSLClient() {
        return new OkHttpAsyncHttpClientBuilder(
                new OkHttpClient.Builder()
                        .sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts())
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        })
                        .build()
        ).build();
    }

    private void redirectToTestProxy(HttpRequest request, TestProxyVariables testProxyVariables) {
        try {
            URL requestUrl = request.getUrl();
            String recordingUpstreamBaseUri = new UrlBuilder()
                    .setScheme(requestUrl.getProtocol())
                    .setHost(requestUrl.getHost())
                    .setPort(requestUrl.getPort())
                    .toUrl().toString();

            request.setHeader("x-recording-upstream-base-uri", recordingUpstreamBaseUri);
            request.setHeader("x-recording-id", testProxyVariables.getRecordingId());
            request.setHeader("x-recording-mode", testProxyVariables.getProxyMode());
            UrlBuilder urlBuilder = UrlBuilder.parse(requestUrl).setHost(testProxyVariables.getProxyHost());
            if (Objects.nonNull(testProxyVariables.getProxyPort())) {
                urlBuilder.setPort(testProxyVariables.getProxyPort());
            }
            request.setUrl(urlBuilder.toUrl());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sslSocketFactory;
    }

    private class TrustAllCerts implements X509TrustManager, TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}