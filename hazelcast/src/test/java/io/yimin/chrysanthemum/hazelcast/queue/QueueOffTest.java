package io.yimin.chrysanthemum.hazelcast.queue;

import java.util.ArrayList;
import java.util.List;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.huobi.mulan.imdg.model.HzReloadCommand;

public class QueueOffTest {
  protected static final int BATCH_SIZE = 5_000;
  private HazelcastInstance hazelcastInstance;
  private IQueue<HzReloadCommand> queue;

  private long start;

  @BeforeEach
  void setUp() {
    ClientConfig clientConfig = new ClientConfig();

    ClientNetworkConfig networkConfig = new ClientNetworkConfig();
    networkConfig.addAddress("172.18.4.43");
    clientConfig.setNetworkConfig(networkConfig);
    hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

    queue = hazelcastInstance.getQueue("hazelcast_queue_reload_command_ACCOUNTID_TO_SUBACCOUNTID");
    start = 152L;
  }

  @Test
  void loadTest() {
    int total = 9_900_000;
    List<HzReloadCommand> batch = new ArrayList<>(BATCH_SIZE);
    while (start++ < total) {
      HzReloadCommand hzReloadCommand = HzReloadCommand.builder()
              .accountId(start)
              .subaccountId(0L)
              .transactionId(0L)
              .transactionType(0)
              .reloadType(2)
              .key("2#" + start)
              .build();
      batch.add(hzReloadCommand);
      if (batch.size() >= BATCH_SIZE) {
        queue.addAll(batch);
        System.out.println("=====start : " + start);
        batch.clear();
      }
    }
    queue.addAll(batch);
    System.out.println("=====start : " + start);
  }
}
