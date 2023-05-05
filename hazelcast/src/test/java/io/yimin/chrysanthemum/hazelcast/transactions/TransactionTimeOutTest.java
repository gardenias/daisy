package io.yimin.chrysanthemum.hazelcast.transactions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.huobi.mulan.imdg.HazelcastDelegate;
import io.yimin.chrysanthemum.hazelcast.HazelcastTest;
import io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import org.junit.jupiter.api.Test;

import static io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory.clientNetworkConfig;

public class TransactionTimeOutTest extends HazelcastTest {


  @Test
  void transactionTimeout() {
    HazelcastClientInstanceFactory.client(
        config ->
            config.setNetworkConfig(
                clientNetworkConfig(networkConfig -> networkConfig.addAddress(etf_cluster))
            ),
        config -> {
          final ClientUserCodeDeploymentConfig userCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();
          userCodeDeploymentConfig.setEnabled(true);
          userCodeDeploymentConfig.addClass("io.yimin.chrysanthemum.hazelcast.transactions.TransactionException");
          config.setUserCodeDeploymentConfig(userCodeDeploymentConfig);

        });
    final HazelcastDelegate clientInstance = getClientInstance(etf_cluster);
    clientInstance.getMap("transaction_exception").set("hello", "world");
    final long startTime = System.currentTimeMillis();
    System.out.println("startTime = " + startTime);
    try {
      clientInstance.getExecutorService("transaction_un_finished").submit(new TransactionException(10)).get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    final Object o = clientInstance.getMap("transaction_exception").get("hello");
    long endTime = System.currentTimeMillis();
    System.out.println("duration:" + (endTime - startTime));
    System.out.println(o);

    final TransactionOptions transactionOptions = new TransactionOptions();
    transactionOptions.setTimeout(5, TimeUnit.SECONDS);
    transactionOptions.setTransactionType(TransactionOptions.TransactionType.ONE_PHASE);
    final TransactionContext transactionContext = clientInstance.hazelcastInstance.newTransactionContext(transactionOptions);
    transactionContext.beginTransaction();
    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    final TransactionalMap<Object, Object> transactionalMap = transactionContext.getMap("transaction_exception");
    final Object forUpdate = transactionalMap.getForUpdate("hello");
    transactionalMap.replace("hello", forUpdate, "Hazelcast");

    System.out.println("duration:" + (System.currentTimeMillis() - endTime));
  }
}
