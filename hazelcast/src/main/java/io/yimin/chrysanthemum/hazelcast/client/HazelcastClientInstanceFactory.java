package io.yimin.chrysanthemum.hazelcast.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastClientInstanceFactory {

  public static HazelcastInstance client(Configuration... configs) {
    final ClientConfig clientConfig = new ClientConfig();

    for (Configuration config : configs) {
      config.config(clientConfig);
    }

    return HazelcastClient.newHazelcastClient(clientConfig);
  }

  public static ClientNetworkConfig clientNetworkConfig(NetWorkConfiguration... netWorkConfigurations) {
    final ClientNetworkConfig networkConfig = new ClientNetworkConfig();

    for (NetWorkConfiguration netWorkConfiguration : netWorkConfigurations) {
      netWorkConfiguration.config(networkConfig);
    }
    return networkConfig;
  }

  public static HazelcastInstance defaultClientConfig(String[] addresses) {
    return client(
        clientConfig -> {
          final ClientNetworkConfig networkConfig = new ClientNetworkConfig();
          networkConfig.addAddress(addresses);
          clientConfig.setNetworkConfig(networkConfig);
        },
        clientConfig -> {
          clientConfig.setProperty("hazelcast.client.event.thread.count", "1");
          clientConfig.setProperty("hazelcast.client.io.input.thread.count", "8");
          clientConfig.setProperty("hazelcast.client.response.thread.count", "8");
          clientConfig.setProperty("hazelcast.client.invocation.timeout.seconds", "5");
        }
    );
  }
}



