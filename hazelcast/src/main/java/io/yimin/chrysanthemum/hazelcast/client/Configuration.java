package io.yimin.chrysanthemum.hazelcast.client;

import com.hazelcast.client.config.ClientConfig;

public interface Configuration {
  void config(ClientConfig clientConfig);
}
