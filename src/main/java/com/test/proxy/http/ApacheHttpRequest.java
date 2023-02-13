// ------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
// ------------------------------------------------------------

package com.test.proxy.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URISyntaxException;
import java.net.URL;

public class ApacheHttpRequest extends HttpEntityEnclosingRequestBase {
    private final String method;

    protected ApacheHttpRequest(HttpMethod method, URL url, HttpHeaders headers) throws URISyntaxException {
        this.method = method.name();
        setURI(url.toURI());
        headers.stream().forEach(header -> addHeader(header.getName(), header.getValue()));
    }

    @Override
    public String getMethod() {
        return method;
    }
}