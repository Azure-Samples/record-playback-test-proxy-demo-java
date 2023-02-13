// ------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
// ------------------------------------------------------------

package com.test.proxy.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

public class ApacheHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final HttpEntity entity;

    protected ApacheHttpResponse(HttpRequest request, org.apache.http.HttpResponse apacheResponse) {
        super(request);
        this.statusCode = apacheResponse.getStatusLine().getStatusCode();
        this.headers = new HttpHeaders();
        Arrays.stream(apacheResponse.getAllHeaders())
                .forEach(header -> headers.put(header.getName(), header.getValue()));
        this.entity = apacheResponse.getEntity();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeaderValue(String s) {
        return headers.getValue(s);
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public Flux<ByteBuffer> getBody() {
        return getBodyAsByteArray().map(ByteBuffer::wrap).flux();
    }

    public Mono<byte[]> getBodyAsByteArray() {
        try {
            if (Objects.nonNull(entity)) {
                return Mono.just(EntityUtils.toByteArray(entity));
            }
            return Mono.just(new byte[0]);
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(String::new);
    }

    public Mono<String> getBodyAsString(Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }
}