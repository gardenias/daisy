package io.yimin.chrysanthemum.hazelcast.client;

import com.hazelcast.client.config.ClientNetworkConfig;

public interface NetWorkConfiguration {
  void config(ClientNetworkConfig clientNetworkConfig);
}
