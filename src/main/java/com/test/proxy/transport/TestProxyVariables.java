// ------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
// ------------------------------------------------------------

package com.test.proxy.transport;

import com.azure.core.http.HttpClient;

import java.io.File;
import java.nio.file.Paths;

public class TestProxyVariables {
    private boolean useProxy;
    private String proxyMode;
    private String proxyHost;
    private Integer proxyPort;
    private String currentRecordingPath;
    private String recordingId;
    private HttpClient httpClient;

    public TestProxyVariables(boolean useProxy, String proxyHost,
                              Integer proxyPort, String proxyMode) {
        this.useProxy = useProxy;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyMode = proxyMode;
        this.currentRecordingPath =
                Paths.get(System.getProperty("user.dir"),
                                "recordings", "RecordAndPlaybackTestProxyDemo.json")
                        .toString().replace(File.separator, "/");
        this.httpClient = new TestProxyTransport(this);
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
