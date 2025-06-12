package com.java.design.patterns;

import java.io.IOException;
import java.net.URI;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSearchConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OpenSearchConsumer.class);

    /**
     * Creates and configures an OpenSearch client
     * 
     * @return a configured RestHighLevelClient
     */
    public static RestHighLevelClient createOpenSearchClient() {
        String connString = "http://localhost:9200";

        RestHighLevelClient restHighLevelClient;
        URI connUri = URI.create(connString);
        String userInfo = connUri.getUserInfo();

        if (userInfo == null) {
            // No authentication required
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort())));
        } else {
            // Authentication info present in the connection string (username:password
            // format)
            String[] auth = userInfo.split(":");
            String username = auth[0];
            char[] password = auth.length > 1 ? auth[1].toCharArray() : new char[0];

            CredentialsStore cp = new BasicCredentialsProvider();
            AuthScope authScope = new AuthScope(connUri.getHost(), connUri.getPort());
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

            cp.setCredentials(authScope, credentials);

            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort()))
                            .setHttpClientConfigCallback(
                                    (HttpAsyncClientBuilder httpClientBuilder) -> httpClientBuilder
                                            .setDefaultCredentialsProvider(cp)));
        }

        return restHighLevelClient;
    }

    /**
     * Main method for the OpenSearch consumer application.
     * Implementation to be added for creating an OpenSearch client, configuring
     * Kafka consumer, and consuming Kafka topics to index data in OpenSearch.
     */
    public static void main(String[] args) {

        RestHighLevelClient client = createOpenSearchClient();
        
        try {
            // Use the low-level REST client to create the index without problematic parameters
            Request indexExistsRequest = new Request("HEAD", "/wikimedia");
            boolean indexExists = false;
            
            try {
                Response response = client.getLowLevelClient().performRequest(indexExistsRequest);
                indexExists = response.getStatusLine().getStatusCode() == 200;
            } catch (IOException e) {
                // Index does not exist, will create it
                logger.warn("Index check failed: {}", e.getMessage());
            }
            
            if (!indexExists) {
                // Use PUT method without the cluster_manager_timeout parameter
                Request createIndexRequest = new Request("PUT", "/wikimedia");
                // Use master_timeout instead of cluster_manager_timeout
                createIndexRequest.addParameter("timeout", "30s");
                createIndexRequest.addParameter("master_timeout", "30s"); 
                
                Response response = client.getLowLevelClient().performRequest(createIndexRequest);
                logger.info("Index created successfully. Status: {}", response.getStatusLine().getStatusCode());
            } else {
                logger.info("Index already exists.");
            }
        } catch (IOException e) {
            logger.error("Error creating index: ", e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("Error closing OpenSearch client: ", e);
            }
        }
    }
}
