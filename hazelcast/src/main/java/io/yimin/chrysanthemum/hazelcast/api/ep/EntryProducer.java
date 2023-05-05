package io.yimin.chrysanthemum.hazelcast.api.ep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.github.benas.randombeans.api.EnhancedRandom;

public class EntryProducer<K, V> implements Callable<Long> {
  private String mapName;
  private Class<K> k;
  private Class<V> v;
  private int count;
  private boolean listable = false;
  private int listSize = 400;

  public EntryProducer(String mapName, Class<K> k, Class<V> v, int count, boolean listable) {
    this.mapName = mapName;
    this.k = k;
    this.v = v;
    this.count = count;
    this.listable = listable;
  }

  @Override
  public Long call() {
    if (count == 0) return 0L;
    ClientConfig clientConfig = new ClientConfig();
    ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
    clientNetworkConfig.addAddress("172.18.4.39", "172.18.4.40", "172.18.4.43");
    clientConfig.setNetworkConfig(clientNetworkConfig);

    HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);
    int sum = 0;
    System.out.println("mapName = " + mapName);
    int tmpCount = count;
    if (listable) {
      final IMap<K, List<V>> map = hazelcastInstance.getMap(mapName);
      while (tmpCount-- > 0) {
        map.putAll(oneHundredOfList(k, v));
        sum += 100;
        System.out.println("sum => " + sum);
      }
    } else {
      final IMap<K, V> map = hazelcastInstance.getMap(mapName);
      while (tmpCount-- > 0) {
        map.putAll(oneHundred(k, v));
        sum += 100;
        System.out.println("sum => " + sum);
      }
    }
    return count * 100L;
  }

  private <T, K> Map<T, List<K>> oneHundredOfList(
          Class<T> tClass,
          Class<K> kClass) {
    Map<T, List<K>> oneThousand = new HashMap<>(1000);
    int count = 1000;
    while (count-- > 0) {
      oneThousand.put(EnhancedRandom.random(tClass), EnhancedRandom.randomListOf(listSize, kClass));
    }
    return oneThousand;
  }

  private <T, K> Map<T, K> oneHundred(
          Class<T> tClass,
          Class<K> kClass) {
    int count = 100;
    Map<T, K> oneThousand = new HashMap<>(count);

    while (count-- > 0) {
      oneThousand.put(EnhancedRandom.random(tClass), EnhancedRandom.random(kClass));
    }
    return oneThousand;
  }
}
