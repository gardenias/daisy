package io.yimin.chrysanthemum.hazelcast.transactions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory.clientNetworkConfig;

public class TransactionTest {
  protected static final String TRANSACTION_TEST = "transaction_test";
  HazelcastInstance instance;

  @BeforeEach
  void setUp() {
    instance = HazelcastClientInstanceFactory.client(
        config ->
            config.setNetworkConfig(
                clientNetworkConfig(networkConfig -> networkConfig.addAddress("172.18.1.61", "172.18.1.62", "172.18.1.63"))
            ),
        config -> {
          final ClientUserCodeDeploymentConfig userCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();
          userCodeDeploymentConfig.addClass("com.huobi.hazelcast.api.task.SlowTask");
          userCodeDeploymentConfig.addClass("com.huobi.hazelcast.util.XMStringGenerator");
          config.setUserCodeDeploymentConfig(userCodeDeploymentConfig);
        });


    final IMap<Integer, Integer> map = instance.getMap(TRANSACTION_TEST);
    map.put(1, 1);
  }


  @Test
  void singleTransaction() {
    incrementData();
    System.out.println(instance.getMap(TRANSACTION_TEST).get(1));
  }

  @Test
  void concurrentIncrement() {
    int nThreads = 100;
    final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
    final CompletableFuture[] futures = IntStream.range(1, 101).mapToObj((i) -> {
      System.out.println("i = " + i);
      return CompletableFuture.runAsync(this::incrementData, executorService);
    }).toArray(CompletableFuture[]::new);
    CompletableFuture.allOf(futures).join();

    System.out.println(instance.getMap(TRANSACTION_TEST).get(1));
  }

  private void incrementData() {
    final TransactionOptions options = new TransactionOptions();
    options.setTransactionType(TransactionOptions.TransactionType.ONE_PHASE);
    final TransactionContext transactionContext = instance.newTransactionContext(options);
    transactionContext.beginTransaction();

    final TransactionalMap<Integer, Integer> transactionalMap = transactionContext.getMap(TRANSACTION_TEST);
    final Integer old = transactionalMap.getForUpdate(1);
    final boolean replaced = transactionalMap.replace(1, old, old + 1);
    System.out.println(replaced);
    transactionContext.commitTransaction();
  }
}
