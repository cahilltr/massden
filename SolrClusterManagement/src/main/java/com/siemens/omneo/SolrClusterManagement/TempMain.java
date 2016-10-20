package com.siemens.omneo.SolrClusterManagement;

import com.jcraft.jsch.JSchException;
import com.siemens.omneo.SolrClusterManagement.Utilities.SolrStartStopRestart;

import java.io.IOException;

public class TempMain {

    public static void main(String[] args) throws IOException, JSchException, InterruptedException {

        SolrStartStopRestart.startSolrInstance("cluster.master", 8983, "/opt/solr","vagrant", 22);

        Thread.sleep(10000);

        SolrStartStopRestart.stopSolrInstance("cluster.master", 8983, "/opt/solr","vagrant", 22, "solrrocks");
    }
}
