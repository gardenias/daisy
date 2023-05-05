package io.yimin.chrysanthemum.hazelcast;

import com.huobi.mulan.imdg.HazelcastDelegate;
import io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory;

import static io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory.clientNetworkConfig;

public class HazelcastTest {

  protected static final String[] etf_cluster = new String[] {"172.18.1.61", "172.18.1.62", "172.18.1.63"};
  protected static HazelcastDelegate clientInstance;

  protected static HazelcastDelegate getClientInstance(String[] address) {
    return new HazelcastDelegate(HazelcastClientInstanceFactory.client(
            config ->
                    config.setNetworkConfig(
                            clientNetworkConfig(networkConfig -> networkConfig.addAddress(address))
                    )
/*            ,
            config -> {
              final ClientUserCodeDeploymentConfig userCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();
              userCodeDeploymentConfig.addClass("com.huobi.hazelcast.api.task.SlowTask");
              userCodeDeploymentConfig.addClass("com.huobi.hazelcast.util.XMStringGenerator");
              config.setUserCodeDeploymentConfig(userCodeDeploymentConfig);
            }*/
    ), "local-test");
  }
}

