package com.siemens.omneo.SolrClusterManagement.Utilities;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;

public class SolrStartStopRestart {

    public static boolean startSolrInstance(String host, int solrPort, String installDir, String user, int sshPort) throws JSchException, IOException {

        JSch jSch = new JSch();

        Session session = jSch.getSession(user, host, sshPort);

//        UserInfo userInfo = new UserInfo() {
//            @Override
//            public String getPassphrase() {
//                return "vagrant";
//            }
//
//            @Override
//            public String getPassword() {
//                return "vagrant";
//            }
//
//            @Override
//            public boolean promptPassword(String s) {
//                return false;
//            }
//
//            @Override
//            public boolean promptPassphrase(String s) {
//                return false;
//            }
//
//            @Override
//            public boolean promptYesNo(String s) {
//                return false;
//            }
//
//            @Override
//            public void showMessage(String s) {
//
//            }
//        };

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

//        session.setUserInfo(userInfo);
        session.setPassword("vagrant");
        session.connect();

        Channel channel = session.openChannel("exec");
        String command = "sudo " + installDir + "/bin/solr start -p " + solrPort;
        ((ChannelExec)channel).setCommand(command);
        ((ChannelExec)channel).setErrStream(System.err);

        handleChannelCommunication(channel);

        channel.disconnect();
        session.disconnect();

        return true;
    }

    public static boolean stopSolrInstance(String host, int solrPort, String installDir, String user, int sshPort,
                                           String solrStopKey) throws JSchException, IOException {

        JSch jSch = new JSch();

        Session session = jSch.getSession(user, host, sshPort);


        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

//        session.setUserInfo(userInfo);
        session.setPassword("vagrant");
        session.connect();

        Channel channel = session.openChannel("exec");
        String command = "sudo " + installDir + "/bin/solr stop -p " + solrPort + " -k " + solrStopKey;
        ((ChannelExec)channel).setCommand(command);
        ((ChannelExec)channel).setErrStream(System.err);

        handleChannelCommunication(channel);

        channel.disconnect();
        session.disconnect();
        return true;
    }

    private static void handleChannelCommunication(Channel channel) throws JSchException, IOException {
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
                System.out.println("exit-status: "+channel.getExitStatus());
                break;
            }
            try{Thread.sleep(1000);}catch(Exception ee){}
        }
        channel.disconnect();
    }


    //com.jcraft.jsch.JSchException: reject HostKey - cause its not in known hosts

}
