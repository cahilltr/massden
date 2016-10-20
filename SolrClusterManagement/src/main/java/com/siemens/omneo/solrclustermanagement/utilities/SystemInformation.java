package com.siemens.omneo.solrclustermanagement.utilities;


import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.solr.client.solrj.impl.HttpSolrClient.*;

public class SystemInformation {

    public static NamedList getSystemInfoForInstance(String host, int port) throws IOException, SolrServerException {

        SolrClient sysClient = new Builder("http://" + host + ":" + port + "/solr").build();
        SolrQuery sysQuery = new SolrQuery();
        sysQuery.setRequestHandler("/admin/info/system");

        QueryResponse rsp = sysClient.query(sysQuery);
        String os = (String) rsp.getResponse().findRecursive("system", "name");
        String ver = (String) rsp.getResponse().findRecursive("system", "version");

        System.out.println("OS: " + os + " " + ver);

        return rsp.getResponse();
    }

    public static Map<String, String> tester(String host, int port) throws IOException, SolrServerException {

        SolrClient sysClient = new Builder("http://" + host + ":" + port + "/solr").build();
        SolrQuery sysQuery = new SolrQuery();
        sysQuery.setRequestHandler("/admin/info/system");

        QueryResponse rsp = sysClient.query(sysQuery);
        List<Object> list = rsp.getResponse().getAll("memory");


        Map<String, String> memoryMap = new HashMap<>(2);

        NamedList<Object> memoryList = (NamedList) ((NamedList) rsp.getResponse().get("jvm")).get("memory");
        NamedList<Object> rawMemoryList = (NamedList<Object>) memoryList.get("raw");

        memoryMap.put("free", (String)memoryList.get("free"));
        memoryMap.put("total", (String)memoryList.get("total"));
        memoryMap.put("max", (String)memoryList.get("max"));
        memoryMap.put("used", (String)memoryList.get("used"));
        memoryMap.put("rawfree", ((Long)rawMemoryList.get("free")).toString());
        memoryMap.put("rawtotal", ((Long)rawMemoryList.get("total")).toString());
        memoryMap.put("rawused", ((Long)rawMemoryList.get("used")).toString());
        memoryMap.put("rawmax", ((Long)rawMemoryList.get("max")).toString());
        memoryMap.put("rawused%", ((Double)rawMemoryList.get("used%")).toString());

        return memoryMap;
    }

}
//{
//        responseHeader=   {
//        status=0,
//        QTime=98
//        },
//        mode=std,
//        solr_home=/opt/solr/server/solr,
//        lucene=   {
//        solr-spec-version=6.2.1,
//        solr-impl-version=6.2.1 43ab70147eb494324a1410f7a9f16a896a59bc6f - shalin - 2016-09-15 05:20:53,
//        lucene-spec-version=6.2.1,
//        lucene-impl-version=6.2.1 43ab70147eb494324a1410f7a9f16a896a59bc6f - shalin - 2016-09-15 05:15:20
//        },
//        jvm=   {
//        version=1.8.0_101 25.101-b13,
//        name=Oracle Corporation OpenJDK 64-Bit Server VM,
//        spec=      {
//        vendor=Oracle Corporation,
//        name=Java Platform API Specification,
//        version=1.8
//        },
//        jre=      {
//        vendor=Oracle Corporation,
//        version=1.8.0_101
//        },
//        vm=      {
//        vendor=Oracle Corporation,
//        name=OpenJDK 64-Bit Server VM,
//        version=25.101-b13
//        },
//        processors=1,
//        memory=      {
//        free=475.8 MB,
//        total=490.7 MB,
//        max=490.7 MB,
//        used=14.9 MB (%3),
//        raw=         {
//        free=498945240,
//        total=514523136,
//        max=514523136,
//        used=15577896,
//        used%=3.0276376143365495
//        }
//        },
//        jmx=      {
//        bootclasspath=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.101-3.b13.el6_8.x86_64/jre/lib/resources.jar:         /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.101-3.b13.el6_8.x86_64/jre/lib/rt.jar:         /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.101-3.b13.el6_8.x86_64/jre/lib/sunrsasign.jar:         /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.101-3.b13.el6_8.x86_64/jre/lib/jsse.jar:         /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.101-3.b13.el6_8.x86_64/jre/lib/jce.jar:         /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.101-3.b13.el6_8.x86_64/jre/lib/charsets.jar:         /usr/lib/jvm/java-1.8.0-openjdk-1.8.0.101-3.b13.el6_8.x86_64/jre/lib/jfr.jar:/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.101-3.b13.el6_8.x86_64/jre/classes,
//        classpath=/opt/solr/server/lib/javax.servlet-api-3.1.0.jar:         /opt/solr/server/lib/jetty-continuation-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-deploy-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-http-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-io-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-jmx-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-rewrite-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-security-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-server-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-servlet-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-servlets-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-util-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-webapp-9.3.8.v20160314.jar:         /opt/solr/server/lib/jetty-xml-9.3.8.v20160314.jar:         /opt/solr/server/lib/ext/jcl-over-slf4j-1.7.7.jar:         /opt/solr/server/lib/ext/jul-to-slf4j-1.7.7.jar:         /opt/solr/server/lib/ext/log4j-1.2.17.jar:         /opt/solr/server/lib/ext/slf4j-api-1.7.7.jar:         /opt/solr/server/lib/ext/slf4j-log4j12-1.7.7.jar:/opt/solr/server/resources,
//        commandLineArgs=         [
//        -Xms512m,
//        -Xmx512m,
//        -XX:NewRatio=3,
//        -XX:SurvivorRatio=4,
//        -XX:TargetSurvivorRatio=90,
//        -XX:MaxTenuringThreshold=8,
//        -XX:+UseConcMarkSweepGC,
//        -XX:+UseParNewGC,
//        -XX:ConcGCThreads=4,
//        -XX:ParallelGCThreads=4,
//        -XX:+CMSScavengeBeforeRemark,
//        -XX:PretenureSizeThreshold=64m,
//        -XX:+UseCMSInitiatingOccupancyOnly,
//        -XX:CMSInitiatingOccupancyFraction=50,
//        -XX:CMSMaxAbortablePrecleanTime=6000,
//        -XX:+CMSParallelRemarkEnabled,
//        -XX:+ParallelRefProcEnabled,
//        -verbose:gc,
//        -XX:+PrintHeapAtGC,
//        -XX:+PrintGCDetails,
//        -XX:+PrintGCDateStamps,
//        -XX:+PrintGCTimeStamps,
//        -XX:+PrintTenuringDistribution,
//        -XX:+PrintGCApplicationStoppedTime,
//        -Xloggc:/opt/solr/server/logs/solr_gc.log,
//        -Djetty.port=8983,
//        -DSTOP.PORT=7983,
//        -DSTOP.KEY=solrrocks,
//        -Duser.timezone=UTC,
//        -Djetty.home=/opt/solr/server,
//        -Dsolr.solr.home=/opt/solr/server/solr,
//        -Dsolr.install.dir=/opt/solr,
//        -Xss256k,
//        -XX:OnOutOfMemoryError=/opt/solr/bin/oom_solr.sh 8983 /opt/solr/server/logs
//        ],
//        startTime=Thu Oct 20 13:45:47         EDT 2016,
//        upTimeMS=6313
//        }
//        },
//        system=   {
//        name=Linux,
//        arch=amd64,
//        availableProcessors=1,
//        systemLoadAverage=0.29,
//        version=2.6.32-573.el6.x86_64,
//        committedVirtualMemorySize=2671099904,
//        freePhysicalMemorySize=2654416896,
//        freeSwapSpaceSize=1040183296,
//        processCpuLoad=0.00631768953068592,
//        processCpuTime=1680000000,
//        systemCpuLoad=0.07073555956678701,
//        totalPhysicalMemorySize=3027509248,
//        totalSwapSpaceSize=1040183296,
//        maxFileDescriptorCount=4096,
//        openFileDescriptorCount=108,
//        uname=Linux cluster.master 2.6.32-573.el6.x86_64 #1 SMP Thu Jul 23 15:44:03      UTC 2015 x86_64 x86_64 x86_64 GNU/Linux,
//        uptime= 17:45:53      up 4 min,
//        1      user,
//        load average:0.29,
//        0.18,
//        0.08
//        }
//        }