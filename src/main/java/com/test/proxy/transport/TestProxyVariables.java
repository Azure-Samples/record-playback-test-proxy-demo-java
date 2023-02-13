// ------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
// ------------------------------------------------------------

package com.test.proxy.transport;

import com.azure.core.http.HttpClient;
import com.test.proxy.http.ApacheHttpClientProvider;

import java.io.File;
import java.nio.file.Paths;

public class TestProxyVariables {
    private boolean useProxy = true;
    private String proxyMode = "record";
    private String proxyHost = "localhost";
    private Integer proxyPort = 5001;
    private String currentRecordingPath = null;
    private String recordingId = null;
    private HttpClient httpClient = null;

    public TestProxyVariables(boolean useProxy, String proxyHost,
                              Integer proxyPort, String proxyMode) {
        this.useProxy = useProxy;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyMode = proxyMode;
        this.currentRecordingPath =
                Paths.get(System.getProperty("user.dir"),
                                "recordings", "RecordAndPlaybackTestProxyDemo.json")
                        .toString().replace(File.separator, "./");
        try {
            this.httpClient = new ApacheHttpClientProvider().createInstance(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isUseProxy() {
        return useProxy;
    }

    public String getCurrentRecordingPath() {
        return currentRecordingPath;
    }

    public String getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(String recordingId) {
        this.recordingId = recordingId;
    }

    public String getProxyMode() {
        return proxyMode;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }
}
