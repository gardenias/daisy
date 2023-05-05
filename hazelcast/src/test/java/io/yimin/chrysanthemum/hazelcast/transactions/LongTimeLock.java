package io.yimin.chrysanthemum.hazelcast.transactions;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.huobi.mulan.imdg.model.AccountIdAndSubaccountIdKey;
import com.huobi.mulan.imdg.model.HzAvailableBalance;

import static io.yimin.chrysanthemum.hazelcast.client.HazelcastClientInstanceFactory.clientNetworkConfig;

public class LongTimeLock {
  HazelcastInstance instance;

  @BeforeEach
  void setUp() {
    instance = HazelcastClientInstanceFactory.client(
        config ->
            config.setNetworkConfig(
                clientNetworkConfig(networkConfig -> networkConfig.addAddress("172.18.4.41", "172.18.4.42", "172.18.4.44"))
            )
    );
  }

  @Test
  void longTimeLock() throws InterruptedException {
    final TransactionOptions options = new TransactionOptions();
    options.setTransactionType(TransactionOptions.TransactionType.ONE_PHASE);
    final TransactionContext transactionContext = instance.newTransactionContext(options);
    transactionContext.beginTransaction();

    final TransactionalMap<AccountIdAndSubaccountIdKey, HzAvailableBalance> transactionalMap = transactionContext.getMap("test-3_subaccount_available");
    final AccountIdAndSubaccountIdKey key = AccountIdAndSubaccountIdKey.of(18566L, 320924L);

    final HzAvailableBalance forUpdate = transactionalMap.getForUpdate(key);
    TimeUnit.MINUTES.sleep(1);
    System.out.println("forUpdate = " + ReflectionToStringBuilder.toString(forUpdate, ToStringStyle.JSON_STYLE));
    transactionContext.commitTransaction();
  }


  @Test
  void name() {
    List<Long> longs = Lists.newArrayList(10L, 11L, 2L, 12L);

    final Iterator<Long> iterator = longs.iterator();
    while (iterator.hasNext()) {
      final Long next = iterator.next();
      if (next <= 10) {
        iterator.remove();
      }
    }
    System.out.println(longs.size());
  }
}
