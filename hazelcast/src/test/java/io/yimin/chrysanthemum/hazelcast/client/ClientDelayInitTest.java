package io.yimin.chrysanthemum.hazelcast.client;

import com.hazelcast.client.HazelcastClientOfflineException;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory.client;
import static io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory.clientNetworkConfig;

public class ClientDelayInitTest {

  @Test
  void noDelay() {
    Assertions.assertThrows(IllegalStateException.class,
        () -> client(config -> config.setNetworkConfig(clientNetworkConfig(networkConfig -> networkConfig.addAddress("172.18.1.36:5701")))),
        "Unable to connect to any etf_cluster! The following addresses were tried: [[172.18.1.36]:5701]");
  }

  @Test
  void delay() {
    final HazelcastInstance hazelcastInstance = Assertions.assertDoesNotThrow(
        () -> HazelcastClientInstanceFactory.client(
            config -> config.setNetworkConfig(clientNetworkConfig(networkConfig -> networkConfig.addAddress("172.18.1.36:5701"))),
            config -> {
              final ClientConnectionStrategyConfig connectionStrategyConfig = new ClientConnectionStrategyConfig();
              connectionStrategyConfig.setAsyncStart(true);
              config.setConnectionStrategyConfig(connectionStrategyConfig);
            }
        )
    );
    Assertions.assertThrows(HazelcastClientOfflineException.class, () -> hazelcastInstance.getMap("cannot_get"), "Client connecting to cluster");
  }
}
