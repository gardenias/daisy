package io.yimin.chrysanthemum.hazelcast.performance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import io.yimin.chrysanthemum.hazelcast.api.task.SlowTask;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.core.HazelcastInstance;
import io.github.benas.randombeans.randomizers.text.StringRandomizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory.client;
import static io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory.clientNetworkConfig;

public class FillDataTest {
  private static final StringRandomizer STRING_RANDOMIZER = StringRandomizer.aNewStringRandomizer();
  private static final int N_THREADS = 10;

  @BeforeEach
  void setUp() {
    final Config liteMemberConfig = new Config("lite-member");
    NetworkConfig netWorkConfig = new NetworkConfig();
    JoinConfig joinConfig = new JoinConfig();
    final TcpIpConfig tcpIpConfig = new TcpIpConfig();
    tcpIpConfig.addMember("172.18.1.61");
    tcpIpConfig.addMember("172.18.1.62");
    tcpIpConfig.addMember("172.18.1.63");
    joinConfig.setTcpIpConfig(tcpIpConfig);
    netWorkConfig.setJoin(joinConfig);
    liteMemberConfig.setNetworkConfig(netWorkConfig);
    HazelcastInstance lite = Hazelcast.newHazelcastInstance(liteMemberConfig);
  }

  private static HazelcastInstance getClient() {
    return client(
        config ->
            config.setNetworkConfig(clientNetworkConfig(networkConfig -> networkConfig.addAddress("172.18.1.61", "172.18.1.62", "172.18.1.63"))),
        config -> {
          final ClientUserCodeDeploymentConfig userCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();
          userCodeDeploymentConfig.addClass("com.huobi.hazelcast.api.task.SlowTask");
          userCodeDeploymentConfig.addClass("com.huobi.hazelcast.util.XMStringGenerator");
          config.setUserCodeDeploymentConfig(userCodeDeploymentConfig);
        }

    );
  }

  @Test
  void hugeDataFillByMultiClients() throws InterruptedException {
    final ExecutorService executorService = Executors.newFixedThreadPool(N_THREADS);

    final String mapName = STRING_RANDOMIZER.getRandomValue();

    final CountDownLatch countDownLatch = new CountDownLatch(N_THREADS);
    for (int i = 0; i < N_THREADS * 100000; i++) {
      int finalI = i;
      executorService.submit(() -> {
        try (final HazelcastDelegate delegate = new HazelcastDelegate(getClient())) {
          fillXGData(delegate.instance, mapName, 6, (long) finalI);
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        } finally {
          countDownLatch.countDown();
        }
      });
    }

    long remainingTaskNum;
    while ((remainingTaskNum = countDownLatch.getCount()) > 0) {
      countDownLatch.await(10, TimeUnit.SECONDS);
      System.out.println("Remaining task num " + remainingTaskNum);
    }

    System.out.println("DONE");
  }

  @Test
  void remoteXGData() throws ExecutionException, InterruptedException {
    final HazelcastInstance instance = getClient();
    final String mapName = STRING_RANDOMIZER.getRandomValue();
    fillXGData(instance, mapName, 3, 1L);
    instance.shutdown();
  }

  private void fillXGData(HazelcastInstance instance, String mapName, int x, long key) throws InterruptedException, ExecutionException {
    final SlowTask slowTask = new SlowTask(key, mapName, x, true);
    instance.getExecutorService("default").submitToKeyOwner(slowTask, slowTask.getKey()).get();
  }


  @Test
  void executeLocalSlow() {
    new HazelcastDelegate(getClient()).execute(delegate -> {
      final SlowTask slowTask = new SlowTask(System.currentTimeMillis(), STRING_RANDOMIZER.getRandomValue(), 1);
      slowTask.setHazelcastInstance(delegate.instance);
      final Long result = slowTask.call();
      System.out.println(result);
    });
  }

  @Test
  void executeLocalQuick() {
    new HazelcastDelegate(getClient()).execute(delegate -> {
      final SlowTask slowTask = new SlowTask(System.currentTimeMillis(), STRING_RANDOMIZER.getRandomValue(), 1, true);
      slowTask.setHazelcastInstance(delegate.instance);
      final Long result = slowTask.call();
      System.out.println(result);
    });
  }

  class HazelcastDelegate implements AutoCloseable {
    final HazelcastInstance instance;
    boolean closed = false;

    HazelcastDelegate(HazelcastInstance hazelcastInstance) {
      this.instance = hazelcastInstance;
    }

    void execute(Consumer<HazelcastDelegate> consumer) {
      consumer.andThen(HazelcastDelegate::close);
    }

    @Override
    public void close() {
      if (!closed)
        instance.shutdown();
      closed = true;
    }
  }

}
