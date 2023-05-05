package io.yimin.chrysanthemum.hazelcast.memory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import io.github.benas.randombeans.api.EnhancedRandom;
import io.github.benas.randombeans.randomizers.number.LongRandomizer;
import io.github.benas.randombeans.randomizers.text.StringRandomizer;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.huobi.mulan.imdg.model.AccountIdAndSubaccountIdKey;
import com.huobi.mulan.imdg.model.AccountIdKey;
import com.huobi.mulan.imdg.model.FrozenSubaccountMapKey;
import com.huobi.mulan.imdg.model.HzAvailableBalance;
import com.huobi.mulan.imdg.model.HzFrozenSubaccount;
import com.huobi.mulan.imdg.model.HzReloadCommand;
import com.huobi.mulan.imdg.model.HzSubaccount;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static io.github.benas.randombeans.api.EnhancedRandom.randomListOf;

public class MemoryCostDetector {

  private HazelcastInstance hazelcastInstance;
  private IMap<Long, String> normalMap;
  private IMap<AccountIdAndSubaccountIdKey, HzAvailableBalance> availableMap;
  private IMap<FrozenSubaccountMapKey, HzFrozenSubaccount> frozenSubaccount;
  @Getter
  private IMap<AccountIdAndSubaccountIdKey, HzSubaccount> subaccountMap;
  private IMap<AccountIdKey, List<Long>> subaccountIdMapping;
  private IQueue<HzReloadCommand> queue;
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  private int size = 1_000;

  @BeforeEach
  void setUp() {
    ClientConfig clientConfig = new ClientConfig();
    ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
    clientNetworkConfig.addAddress("172.18.4.41", "172.18.4.42", "172.18.4.44");
    clientConfig.setNetworkConfig(clientNetworkConfig);

    final GroupConfig groupConfig = new GroupConfig();
    groupConfig.setName("hazelcast-test-yimin");
    groupConfig.setPassword("c2Q9HdbT2442n2ua");
    clientConfig.setGroupConfig(groupConfig);

    hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

    normalMap = hazelcastInstance.getMap("normal_map");
//    availableMap = hazelcastInstance.getMap("memory_cost_detector_available_map");
//    frozenSubaccount = hazelcastInstance.getMap("memory_cost_detector_frozen_subaccount");
//    subaccountMap = hazelcastInstance.getMap("memory_cost_detector_subaccount_map");
//    subaccountIdMapping = hazelcastInstance.getMap("memory_cost_detector_subaccount_id_mapping");
//    queue = hazelcastInstance.getQueue("memory_cost_detector_queue");
    size = 20_000;
  }

  @Test
  void normalMap() {
    final LongRandomizer longRandomizer = LongRandomizer.aNewLongRandomizer();
    final StringRandomizer stringRandomizer = StringRandomizer.aNewStringRandomizer();
    long key = 1;
    while (size-- > 0) {
      normalMap.put(key++, stringRandomizer.getRandomValue());
    }
  }

  @Test
  void deleteMap() {
    hazelcastInstance.getMap("split_merge_test").clear();
  }

  @Test
  void availableMap() throws InterruptedException {
    availableMap = hazelcastInstance.getMap("split_merge_test");

    Long subaccountId = 9999L;
    Long accountId;

    int times = 1;
    while (times++ <= 10) {
      Long index = 999L;

      BigDecimal balance;
      while (index >= 0) {
        accountId = index;
        balance = BigDecimal.valueOf(1_980);
        availableMap.put(AccountIdAndSubaccountIdKey.of(accountId, subaccountId),
                HzAvailableBalance.builder().balance(balance).version((long) times).build());
        index--;
      }
      System.out.println("Sleeping");
      TimeUnit.MINUTES.sleep(1);
    }
  }

  @Test
  void availableMapDataCheck() {
    availableMap = hazelcastInstance.getMap("split_merge_test");

    Long subaccountId = 9999L;
    Long accountId;

    Long index = 999L;
    while (index >= 0) {
      accountId = index;
      final HzAvailableBalance hzAvailableBalance = availableMap.get(AccountIdAndSubaccountIdKey.of(accountId, subaccountId));
      System.out.println("hzAvailableBalance.toString() = " + hzAvailableBalance.toString());
      index--;
    }
  }

  @Test
  void availableMapMapMemoryCostDetect() {
    System.out.println(random(AccountIdAndSubaccountIdKey.class));
    System.out.println(random(HzAvailableBalance.class));
    while (size-- > 0) {
      availableMap.put(random(AccountIdAndSubaccountIdKey.class), random(HzAvailableBalance.class));
    }
  }

  @Test
  void queueMemoryCostDetect() {
    while (size-- > 0) {
      queue.add(EnhancedRandom.random(HzReloadCommand.class));
    }
  }

  @Test
  void frozenSubaccountMapMemoryCostDetect() throws InterruptedException {
    System.out.println(random(FrozenSubaccountMapKey.class));
    System.out.println(random(HzFrozenSubaccount.class));
    while (size-- > 0) {
      executorService.submit(() -> {
        int count = 1_000;
        while (count-- > 0) {
          frozenSubaccount.set(random(FrozenSubaccountMapKey.class), random(HzFrozenSubaccount.class));
        }
        System.out.println("1_000 inserted");
      });
      if (size % 5 == 0) {
        TimeUnit.SECONDS.sleep(15);
        System.out.println("size = " + size);
      }
    }
  }

  @Test
  void subaccountMapMapMemoryCostDetect() throws InterruptedException {
    while (size-- > 0) {
      executorService.submit(() -> {
        int count = 10;
        while (count-- > 0) {
          subaccountMap.putAsync(random(AccountIdAndSubaccountIdKey.class), random(HzSubaccount.class));
        }
        System.out.println(count + " inserted");
      });
      if (size % 5 == 0) {
        TimeUnit.SECONDS.sleep(10);
        System.out.println("size = " + size);
      }
    }
  }

  @Test
  void subaccountIdMappingMapMemoryCostDetect() {
    System.out.println(random(AccountIdKey.class));
    while (size-- > 0) {
      subaccountIdMapping.put(random(AccountIdKey.class), randomListOf(500, Long.class));
    }
  }

}
