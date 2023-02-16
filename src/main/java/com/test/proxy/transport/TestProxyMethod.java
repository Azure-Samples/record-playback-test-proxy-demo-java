// ------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
// ------------------------------------------------------------

package com.test.proxy.transport;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JsonSerializerProviders;

import java.util.HashMap;

public class TestProxyMethod {

    // startTextProxy() will initiate a record or playback session by POST-ing a
    // request
    // to a running instance of the test proxy. The test proxy will return a
    // recording ID
    // value in the response header, which we pull out and save as 'x-recording-id'.
    public static String startTestProxy(TestProxyVariables testProxyVariables) {
        return testProxyVariables.getHttpClient()
                .sendSync(new HttpRequest(HttpMethod.POST,
                        "https://" + testProxyVariables.getProxyHost()
                                + ":" + testProxyVariables.getProxyPort()
                                + "/" + testProxyVariables.getProxyMode()
                                + "/start")
                        .setBody(JsonSerializerProviders.createInstance(true)
                                .serializeToBytes(new HashMap<String, String>() {{
                                    put("x-recording-file",
                                            testProxyVariables.getCurrentRecordingPath());
                                }})), Context.NONE)
                .getHeaderValue("x-recording-id");
    }

    // stopTextProxy() instructs the test proxy to stop recording or stop playback,
    // depending on the mode it is running in. The instruction to stop is made by
    // POST-ing a request to a running instance of the test proxy. We pass in the
    // recording
    // ID and a directive to save the recording (when recording is running).
    public static void stopTestProxy(TestProxyVariables testProxyVariables) {
        testProxyVariables.getHttpClient()
                .sendSync(new HttpRequest(HttpMethod.POST,
                        "https://" + testProxyVariables.getProxyHost()
                                + ":" + testProxyVariables.getProxyPort()
                                + "/" + testProxyVariables.getProxyMode()
                                + "/stop")
                        .setHeader("x-recording-id",
                                testProxyVariables.getRecordingId())
                        .setHeader("x-recording-save", "true"), Context.NONE);
    }
}
