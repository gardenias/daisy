package io.yimin.chrysanthemum.hazelcast.client;

import java.util.function.Function;

import com.huobi.mulan.imdg.HazelcastDelegate;
import io.yimin.chrysanthemum.hazelcast.HazelcastTest;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import io.github.benas.randombeans.api.Randomizer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.github.benas.randombeans.randomizers.text.StringRandomizer.aNewStringRandomizer;

public class DistributeObjectDataRandom extends HazelcastTest {

  private static final String[] address = new String[]{"172.18.1.61", "172.18.1.62", "172.18.1.63"};
  private static HazelcastDelegate clientInstance;

  @BeforeAll
  static void setUp() {
    clientInstance = getClientInstance(address);
  }

  @Test
  void imap() {
    final IMap<String, String> firstMap = clientInstance.getMap("first_map");
    fillData(aNewStringRandomizer(10, 100, System.currentTimeMillis()),
        (Randomizer<String> randomizer) -> firstMap.put(randomizer.getRandomValue(), randomizer.getRandomValue()), 10_000);
  }

  private <T, V> void fillData(Randomizer<T> randomizer, Function<Randomizer<T>, V> stringFunction, int count) {
    while (count-- >= 0) {
      stringFunction.apply(randomizer);
      if (count % 1000 == 0) {
        System.out.println("count = " + count);
      }
    }
  }

  @Test
  void iqueue() {
    final IQueue<String> firstQueue = clientInstance.getQueue("first_queue");
//    fillData(aNewStringRandomizer(10, 100, System.currentTimeMillis()),
//        (Randomizer<String> randomizer) -> firstQueue.add(randomizer.getRandomValue()), 100_000);
    System.out.println(firstQueue.parallelStream().mapToInt((String::length)).sum());
  }

  @Test
  void executor() {
    final IExecutorService firstExecutor = clientInstance.getExecutorService("first_executor");

  }

  @AfterAll
  static void afterAll() {
    clientInstance.hazelcastInstance.shutdown();
  }

}
