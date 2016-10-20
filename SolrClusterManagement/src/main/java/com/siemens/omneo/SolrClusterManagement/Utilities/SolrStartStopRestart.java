package com.siemens.omneo.SolrClusterManagement.Utilities;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//Currently only handles a username and password variation of command and does not support Sudo

public class SolrStartStopRestart {

    public static boolean startSolrInstance(String host, int solrPort, String installDir, String user, String password,
                                            int sshPort) throws JSchException, IOException {

        JSch jSch = new JSch();

        Session session = jSch.getSession(user, host, sshPort);

        Properties config = System.getProperties();
        if (!config.containsKey("StrictHostKeyChecking"))
            config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.setPassword(password);
        session.connect();

        String command = installDir + "/bin/solr start -p " + solrPort;
        executeCommand(session,command);

        session.disconnect();

        return true;
    }

    public static boolean stopSolrInstance(String host, int solrPort, String installDir, String user, String password,
                                           int sshPort, String solrStopKey) throws JSchException, IOException {

        JSch jSch = new JSch();
        Session session = jSch.getSession(user, host, sshPort);

        Properties config = System.getProperties();
        if (!config.containsKey("StrictHostKeyChecking"))
            config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.setPassword(password);
        session.connect();

        String command = installDir + "/bin/solr stop -p " + solrPort + " -k " + solrStopKey;


        executeCommand(session, command);

        session.disconnect();
        return true;
    }

    //TODO understand this better
    private static void executeCommand(Session session, String command) throws JSchException, IOException {
        Channel channel = session.openChannel("exec");
        ((ChannelExec)channel).setCommand(command);
        ((ChannelExec)channel).setErrStream(System.err);

        InputStream in = channel.getInputStream();
        channel.connect();

        byte[] tmp=new byte[1024];
        while(true){
            while(in.available()>0){
                int i=in.read(tmp, 0, 1024);
                if(i<0)break;
                System.out.print(new String(tmp, 0, i));
            }
            if(channel.isClosed()){
                if(in.available()>0) continue;
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try{Thread.sleep(1000);}catch(Exception ee){}
        }
        channel.disconnect();
    }


    //com.jcraft.jsch.JSchException: reject HostKey - cause its not in known hosts

}
