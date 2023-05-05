package io.yimin.chrysanthemum.hazelcast.cluster;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.client.config.ConnectionRetryConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClusterStatTest {

  private HazelcastInstance hazelcastInstance;

  @BeforeEach
  void setUp() {
    ClientConfig clientConfig = new ClientConfig();
    ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
    clientNetworkConfig.addAddress("172.18.1.61", "172.18.1.62", "172.18.1.63");
    clientConfig.setNetworkConfig(clientNetworkConfig);
    final GroupConfig groupConfig = new GroupConfig();
    groupConfig.setName("dev");
    clientConfig.setGroupConfig(groupConfig);
    Properties clientProperties = new Properties();
    clientProperties.setProperty("hazelcast.client.invocation.timeout.seconds", "10");
    clientConfig.setProperties(clientProperties);

    final ClientConnectionStrategyConfig connectionStrategyConfig = new ClientConnectionStrategyConfig();
    final ConnectionRetryConfig connectionRetryConfig = new ConnectionRetryConfig();
    connectionRetryConfig.setEnabled(true);
    connectionRetryConfig.setFailOnMaxBackoff(false);
    connectionRetryConfig.setInitialBackoffMillis(2000);
    connectionRetryConfig.setMaxBackoffMillis(60000);
    connectionRetryConfig.setMultiplier(3);
    connectionRetryConfig.setJitter(0.5);
    connectionStrategyConfig.setConnectionRetryConfig(connectionRetryConfig);
    clientConfig.setConnectionStrategyConfig(connectionStrategyConfig);


//    final ClientUserCodeDeploymentConfig userCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();
//    userCodeDeploymentConfig.setEnabled(true);
//    userCodeDeploymentConfig.addClass("");
//    clientConfig.setUserCodeDeploymentConfig(userCodeDeploymentConfig);
    hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);
  }

  @Test
  void initData() {
    final IMap<Long, String> map = hazelcastInstance.getMap("small");
    int errorTimes = 0;
    for (long i = 1; i < 2002000; i++) {
      try {
        map.put(i, String.valueOf(i));
      } catch (RuntimeException e) {
        errorTimes++;
        e.printStackTrace();
      }

      if (i % 1000 == 0) {
        try {
          TimeUnit.SECONDS.sleep(10);
          System.out.print(i);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    System.out.println("errorTimes = " + errorTimes);
  }

  @Test
  void slowTaskConnectionCloseByServer() {

//    hazelcastInstance.getExecutorService()
  }
}
