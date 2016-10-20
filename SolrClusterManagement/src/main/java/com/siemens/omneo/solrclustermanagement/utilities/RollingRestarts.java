package com.siemens.omneo.solrclustermanagement.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

//TODO test
public class RollingRestarts {

    public static List<String> rollingRestart(Map<String, Integer> instanceMap, int milliBetweenRestarts, int restartServersAtATime,
                                              String solrInstallDir, String user, String password, int sshPort,
                                              String startUpOptions, String memory) {
        List<String> failedInstanceList = new ArrayList<>(instanceMap.size());

        //TODO create and run threads to restart solr instances

        return failedInstanceList;
    }

    private static class RestartRunnable implements Callable<Boolean> {

        String host;
        int solrPort;
        String solrInstallDir;
        String user;
        String password;
        int sshPort;
        String startUpOptions;
        String memory;

        public RestartRunnable(String host, int solrPort, String solrInstallDir, String user, String password,
                               int sshPort, String startUpOptions, String memory) {
            this.host = host;
            this.solrPort = solrPort;
            this.solrInstallDir = solrInstallDir;
            this.user = user;
            this.password = password;
            this.sshPort = sshPort;
            this.startUpOptions = startUpOptions;
            this.memory = memory;
        }

        @Override
        public Boolean call() throws Exception {
            return SolrStartStopRestart.restartSolrIntance(this.host, this.solrPort, this.solrInstallDir, this.user,
                    this.password, this.sshPort, this.startUpOptions, this.memory);
        }
    }


}
