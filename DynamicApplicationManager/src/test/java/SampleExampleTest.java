import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.server.MiniYARNCluster;
import org.apache.hadoop.yarn.util.Records;
import org.junit.*;

import java.io.*;
import java.net.URL;
import java.util.Collections;


import static org.junit.Assert.*;

/**
 * Stuff
 * https://github.com/spring-projects/spring-hadoop/blob/55d2ba323820bb8caeaea05daafdeeef77a63bdc/spring-yarn/spring-yarn-test/src/main/java/org/springframework/yarn/test/support/StandaloneYarnCluster.java
 * http://stackoverflow.com/questions/16000840/write-a-file-in-hdfs-with-java
 * https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/WritingYarnApplications.html
 * hadoop-yarn-project/hadoop-yarn/hadoop-yarn-client/src/test/java/org/apache/hadoop/yarn/client/api/impl/TestNMClient.java
 */
public class SampleExampleTest {

  protected static MiniYARNCluster yarnCluster = null;
  private static MiniDFSCluster dfsCluster = null;
  protected static Configuration conf = new YarnConfiguration();
  private static URL url;

  @BeforeClass
  public static void setUp() throws Exception {

    conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, "target/" + "testCluster" + "-dfs");
    dfsCluster = new MiniDFSCluster.Builder(conf).numDataNodes(3).build();
    dfsCluster.waitClusterUp();

    FileSystem fs = dfsCluster.getFileSystem();
    FSDataOutputStream fin = fs.create(new Path("/tmp/simpleapp.jar"));
    fin.write(IOUtils.toByteArray(new FileInputStream("/Users/cahillt/massden/DynamicApplicationManager/target/DynamicApplicationManager-1.0-SNAPSHOT.jar")));
    fin.close();
    fs.exists(new Path("/tmp/simpleapp.jar"));


    conf.setInt(YarnConfiguration.RM_SCHEDULER_MINIMUM_ALLOCATION_MB, 128);
    if (yarnCluster == null) {
      yarnCluster = new MiniYARNCluster(
              "TestCluster-" + System.currentTimeMillis(), 1, 3, 1);
      yarnCluster.init(conf);
      yarnCluster.start();
      //get the address
      Configuration yarnClusterConfig = yarnCluster.getConfig();
      System.out.println("MiniYARN ResourceManager published address: " +
              yarnClusterConfig.get(YarnConfiguration.RM_ADDRESS));
      System.out.println("MiniYARN ResourceManager published web address: " +
              yarnClusterConfig.get(YarnConfiguration.RM_WEBAPP_ADDRESS));
      String webapp = yarnClusterConfig.get(YarnConfiguration.RM_WEBAPP_ADDRESS);
      assertTrue("Web app address still unbound to a host at " + webapp,
              !webapp.startsWith("0.0.0.0"));
      System.out.println("Yarn webapp is at "+ webapp);
      url = Thread.currentThread().getContextClassLoader()
              .getResource("yarn-site.xml");
      if (url == null) {
        throw new RuntimeException(
                "Could not find 'yarn-site.xml' dummy file in classpath");
      }
      //write the document to a buffer (not directly to the file, as that
      //can cause the file being written to get read -which will then fail.
      ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
      yarnClusterConfig.writeXml(bytesOut);
      bytesOut.close();
      //write the bytes to the file in the classpath
      OutputStream os = new FileOutputStream(new File(url.getPath()));
      os.write(bytesOut.toByteArray());
      os.close();
    }
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      System.out.println("setup thread sleep interrupted. message=" + e.getMessage());
    }
  }

  @AfterClass
  public static void tearDown() throws IOException {
    dfsCluster.shutdown();
    if (yarnCluster != null) {
      try {
        yarnCluster.stop();
      } finally {
        yarnCluster = null;
      }
    }
  }

  @Test
  public void tester() throws Exception {
    String[] args = {"'echo \"hello\"'", "1", "hdfs:///tmp/simpleapp.jar"};
//    String[] args = {"echo 'hello'", "2", "file:/Users/cahillt/massden/DynamicApplicationManagertarget/DynamicApplicationManager-1.0-SNAPSHOT.jar"};
    Client client = new Client();
    System.out.println("MINE: " + yarnCluster.getResourceManager().getServiceState());
    client.run(args, conf);
//    ApplicationMasterAsync temp = new ApplicationMasterAsync("echo 'hello'", 2, conf);
//    temp.runMainLoop();

  }

  @Test
  public void tester_new() throws IOException, YarnException, InterruptedException {
    final String command = "";
    final int n = 2;
//    final Path jarPat

    // Create yarnClient
    YarnClient yarnClient = YarnClient.createYarnClient();
    yarnClient.init(conf);
    yarnClient.start();

    // Create application via yarnClient
    YarnClientApplication app = yarnClient.createApplication();

    // Set up the container launch context for the application master
    ContainerLaunchContext amContainer =
            Records.newRecord(ContainerLaunchContext.class);
    amContainer.setCommands(
//            Collections.singletonList(
////                    "$JAVA_HOME/bin/java" +
//                            " echo 'hello' " +
//                            " 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" +
//                            " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"
//            )
//    );
//    amContainer.setCommands(
            Collections.singletonList(
//                    "$JAVA_HOME/bin/java" +
                    "java -cp simpleapp.jar" +
//                            " -Xms1M -Xmx256M" +
                            " ApplicationMasterAsync" +
                            " " + command +
                            " " + String.valueOf(n) +
                            " 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" +
                            " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"
            )
    );



//    // Setup jar for ApplicationMaster
//    LocalResource appMasterJar = Records.newRecord(LocalResource.class);
//    setupAppMasterJar(jarPath, appMasterJar);
//    amContainer.setLocalResources(
//            Collections.singletonMap("simpleapp.jar", appMasterJar));
//
//    // Setup CLASSPATH for ApplicationMaster
//    Map<String, String> appMasterEnv = new HashMap<>();
//    setupAppMasterEnv(appMasterEnv);
//    amContainer.setEnvironment(appMasterEnv);

    // Set up resource type requirements for ApplicationMaster
    Resource capability = Records.newRecord(Resource.class);
    capability.setMemory(5);
    capability.setVirtualCores(1);

    // Finally, set-up ApplicationSubmissionContext for the application
    ApplicationSubmissionContext appContext =
            app.getApplicationSubmissionContext();
    appContext.setApplicationName("simple-yarn-app"); // application name
    appContext.setAMContainerSpec(amContainer);
    appContext.setResource(capability);
    appContext.setQueue("default"); // queue

    // Submit application
    ApplicationId appId = appContext.getApplicationId();
    System.out.println("Submitting application " + appId);
    yarnClient.submitApplication(appContext);

    ApplicationReport appReport = yarnClient.getApplicationReport(appId);
    YarnApplicationState appState = appReport.getYarnApplicationState();
    while (appState != YarnApplicationState.FINISHED &&
            appState != YarnApplicationState.KILLED &&
            appState != YarnApplicationState.FAILED) {
      Thread.sleep(100);
      appReport = yarnClient.getApplicationReport(appId);
      appState = appReport.getYarnApplicationState();
    }

    System.out.println(
            "Application " + appId + " finished with" +
                    " state " + appState +
                    " at " + appReport.getFinishTime());

  }

}