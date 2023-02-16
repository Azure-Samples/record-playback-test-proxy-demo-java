// ------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
// ------------------------------------------------------------

package com.test.proxy;

import com.azure.core.util.Context;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.models.ListEntitiesOptions;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.test.proxy.transport.TestProxyMethod;
import com.test.proxy.transport.TestProxyVariables;

import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

public class CosmosDBTablesExample {
    private static final String STRING_COSMOS_CONNECTION_STRING = "COSMOS_CONNECTION_STRING";
    private static final String STRING_USE_PROXY = "USE_PROXY";
    private static final String STRING_PROXY_MODE = "PROXY_MODE";
    private static final String STRING_PROXY_PORT = "PROXY_PORT";
    private static final String STRING_PROXY_HOST = "PROXY_HOST";
    private static final String STRING_PROPERTIES_FILE_NAME = "properties/config.properties";

    // Beginning of app code.
    public static void main(String[] args) {

        boolean hasException = false;
        TestProxyVariables testProxyVariables = null;

        try {
            //=====================================================================//
            // Test proxy prologue. The following code is necessary to configure   //
            // the test proxy, as well as to start the record or playback process. //
            //=====================================================================//
            Properties properties = new Properties();
            properties.load(CosmosDBTablesExample.class.getClassLoader()
                    .getResourceAsStream(STRING_PROPERTIES_FILE_NAME));

            testProxyVariables =
                    new TestProxyVariables(
                            Boolean.parseBoolean(properties.getProperty(STRING_USE_PROXY)),
                            properties.getProperty(STRING_PROXY_HOST),
                            Integer.valueOf(properties.getProperty(STRING_PROXY_PORT)),
                            properties.getProperty(STRING_PROXY_MODE));

            if (testProxyVariables.isUseProxy()) {
                testProxyVariables.setRecordingId(
                        TestProxyMethod.startTestProxy(testProxyVariables));
            }
            //=========================================================================================//
            // End of test proxy prologue. Original test code starts here. Everything after this point //
            // represents an app interacting with the Azure Table Storage service.                     //
            //=========================================================================================//

            // New instance of TableClient class referencing the server-side table
            TableServiceClient tableServiceClient =
                    new TableServiceClientBuilder()
                            .connectionString(properties.getProperty(STRING_COSMOS_CONNECTION_STRING))
                            .httpClient(testProxyVariables.getHttpClient())
                            .buildClient();
            TableClient tableClient =
                    tableServiceClient.createTableIfNotExists("adventureworks");
            if (Objects.isNull(tableClient)) {
                tableClient = tableServiceClient.getTableClient("adventureworks");
            }

            // Create new item using composite key constructor
            TableEntity prod1 = new TableEntity("gear-surf-surfboards", "68719518388");
            prod1.addProperty("Name", "Ocean Surfboard");
            prod1.addProperty("Quantity", 8);
            prod1.addProperty("Sale", true);

            // Add new item to server-side table
            tableClient.createEntity(prod1);

            // Read a single item from container
            TableEntity product = tableClient.getEntity("gear-surf-surfboards", "68719518388");
            System.out.println("Single product:");
            System.out.println(product.getProperties().get("Name"));

            // Read multiple items from container
            TableEntity prod2 = new TableEntity("gear-surf-surfboards", "68719518390");
            prod2.addProperty("Name", "Sand Surfboard");
            prod2.addProperty("Quantity", 5);
            prod2.addProperty("Sale", false);
            tableClient.createEntity(prod2);

            System.out.println("Multiple products:");
            tableClient.listEntities(
                            new ListEntitiesOptions().setFilter("PartitionKey eq 'gear-surf-surfboards'"),
                            Duration.ofSeconds(60), Context.NONE)
                    .forEach(tableEntity -> System.out.println(tableEntity.getProperties().get("Name")));

            tableClient.deleteTable();
        } catch (Exception ex) {
            hasException = true;
            ex.printStackTrace();
        } finally {
            //=============================================================================//
            // Test proxy epilogue - necessary to stop the test proxy. Note that if you do //
            // not stop the test proxy after recording, your recording WILL NOT be saved!  //
            //=============================================================================//
            if (Objects.nonNull(testProxyVariables)
                    && testProxyVariables.isUseProxy()
                    && Objects.nonNull(testProxyVariables.getHttpClient())) {
                try {
                    TestProxyMethod.stopTestProxy(testProxyVariables);
                } catch (Exception ex) {
                    hasException = true;
                    ex.printStackTrace();
                }
            }
        }
        if (hasException) {
            System.exit(1);
        }
        System.exit(0);
    }
}