Testing is a crucial step in the software development process, including software deployed to the cloud. Testing software against a live cloud service like Azure can be costly, since services must be provisioned and maintained in order to run the tests.

The Azure SDK team has developed a lightweight test proxy that allows us to record app interactions with Azure and play them back on demand, significantly reducing our testing costs. We’re now excited to share this tool with the broader Azure development community and invite you to try it out for yourself.

This repository contains a sample project that demonstrates integration
of the record and playback test proxy with an app that interacts with
the Azure Cosmos DB Table Storage service.

### Prerequisites

The following prerequisites are required to use this application. Please ensure that you have them all installed locally.

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Maven][maven]
- [Visual Studio Code][vs_code]
- [Install .NET 6.0 or higher][dotnet]
- [Install the test-proxy][test_proxy]

```
dotnet tool update azure.sdk.tools.testproxy --global --add-source https://pkgs.dev.azure.com/azure-sdk/public/_packaging/azure-sdk-for-net/nuget/v3/index.json --version "1.0.0-dev*"
```

Notes: After installing the tool, run it in a terminal or cmd window by typing the command 'test-proxy'.

### Build and Run the sample

1.Clone the repository.

```
git clone https://github.com/Azure-Samples/record-playback-test-proxy-demo-java
cd record-playback-test-proxy-demo-java
```

2.Before running the project, ensure that the following environment variables are set in `config.properties` file:

- COSMOS_CONNECTION_STRING
- USE_PROXY
- PROXY_HOST
- PROXY_PORT
- PROXY_MODE

3.Install package and make jar package for this project.

```
mvn install package
```

4.Run the sample.

```
java -cp record-playback-test-proxy-demo-java-1.0-SNAPSHOT.jar com.test.proxy.CosmosDBTablesExample
```

The included recording file is provided for illustration purposes only, it can't be used to play back the test since the resources associated with it no longer exist in Azure.

This project is intended to be a demo that goes with the following [Azure
SDK blog post][azure_sdk_blog_post].

The test proxy provides record/playback capabilities compatible with Azure SDKs for .NET, Python, Java, JavaScript, Go, and C++. To use it in your testing, you need to be able to reroute your app requests to the test proxy via modifications to the request headers.

<!-- Links -->
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[maven]: https://maven.apache.org/
[vs_code]: https://code.visualstudio.com/download
[test_proxy]: https://github.com/Azure/azure-sdk-tools/tree/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy#installation
[dotnet]: https://dotnet.microsoft.com/download
[azure_sdk_blog_post]: https://devblogs.microsoft.com/azure-sdk/level-up-your-cloud-testing-game-with-the-azure-sdk-test-proxy/
