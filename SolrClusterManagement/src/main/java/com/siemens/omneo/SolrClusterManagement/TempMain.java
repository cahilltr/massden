package com.siemens.omneo.solrclustermanagement;

import com.jcraft.jsch.JSchException;
import com.siemens.omneo.solrclustermanagement.utilities.ChangeInstanceLogLevel;
import com.siemens.omneo.solrclustermanagement.utilities.SolrStartStopRestart;
import com.siemens.omneo.solrclustermanagement.utilities.SystemInformation;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

public class TempMain {

    public static void main(String[] args) throws IOException, JSchException, InterruptedException, SolrServerException {

//        SolrStartStopRestart.startSolrInstance("cluster.master", 8983, "/opt/solr","root", "vagrant", 22, "", "");

//        SolrStartStopRestart.restartSolrIntance("cluster.master", 8983, "/opt/solr","root", "vagrant", 22, "", "");

//        SystemInformation.tester("cluster.master", 8983);

        ChangeInstanceLogLevel.changeLogLevel("cluster.master", 8983, "root", "WARN");

//        Thread.sleep(10000);
//
//        SolrStartStopRestart.stopSolrInstance("cluster.master", 8983, "/opt/solr","root", "vagrant", 22, "solrrocks");
    }

}
