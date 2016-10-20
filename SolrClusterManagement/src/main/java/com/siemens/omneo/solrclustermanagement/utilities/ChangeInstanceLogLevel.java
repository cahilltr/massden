package com.siemens.omneo.solrclustermanagement.utilities;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChangeInstanceLogLevel {
    private final static Logger logger = LoggerFactory.getLogger(ChangeInstanceLogLevel.class);

    //Solrj Equivalent of curl -s http://localhost:8983/solr/admin/info/logging --data-binary "set=root:WARN&wt=json"
    public static boolean changeLogLevel(String host, int port, String loggingEndpoint, String level) throws IOException, SolrServerException {
        SolrClient solrClient = new HttpSolrClient.Builder("http://" + host + ":" + port + "/solr").build();
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler("/admin/info/system/logging");
        solrQuery.set("set", loggingEndpoint + ":" + level);

        QueryResponse rsp = solrClient.query(solrQuery);
        return ((int)rsp.getResponse().findRecursive("responseHeader","status")) == 0;
    }

    public static List<String> changeClusterLogLevels(Map<String, Integer> hostPortMap, String loggingEndpoint, String level) {
        List<String> failedInstanceList = new ArrayList<>(hostPortMap.size());

        for (Map.Entry<String, Integer> entry : hostPortMap.entrySet()) {

            int failedChangeCount = 0;
            while (failedChangeCount < 3) {
                boolean success = false;
                try {
                    success = changeLogLevel(entry.getKey(), entry.getValue(), loggingEndpoint, level);
                } catch (IOException e) {
                    logger.error("IOException", e);
                } catch (SolrServerException e) {
                    logger.error("SolrServerException", e);
                }
                if (success)
                    break;
                else
                    failedChangeCount++;
            }
        }
        return failedInstanceList;
    }
}
