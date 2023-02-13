// ------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
// ------------------------------------------------------------

package com.test.proxy.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.test.proxy.transport.TestProxyVariables;

public final class ApacheHttpClientProvider implements HttpClientProvider {

    // Override the default http client via [ApacheHttpClient] when using the test proxy.
    // If not using the proxy, the default http client will be used.
    public HttpClient createInstance(TestProxyVariables testProxyVariables) {
        try {
            if (!testProxyVariables.isUseProxy()) {
                return this.createInstance();
            }
            return new ApacheHttpClient(testProxyVariables);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HttpClient createInstance() {
        return HttpClient.createDefault();
    }
}
