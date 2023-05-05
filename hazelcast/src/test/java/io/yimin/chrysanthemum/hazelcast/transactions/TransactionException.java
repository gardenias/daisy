package io.yimin.chrysanthemum.hazelcast.transactions;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.TransactionalMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class TransactionException implements Callable<Long>, DataSerializable, HazelcastInstanceAware {
  private HazelcastInstance instance;
  private long seconds;

  public TransactionException(long seconds) {
    this.seconds = seconds;
  }

  @Override
  public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
    objectDataOutput.writeLong(seconds);
  }

  @Override
  public void readData(ObjectDataInput objectDataInput) throws IOException {
    this.seconds = objectDataInput.readLong();
  }

  @Override
  public Long call() throws Exception {
    log.info("===== {}", System.currentTimeMillis());
    final TransactionOptions transactionOptions = new TransactionOptions();
    transactionOptions.setTimeout(seconds, TimeUnit.SECONDS);
    transactionOptions.setTransactionType(TransactionOptions.TransactionType.ONE_PHASE);
    final TransactionContext transactionContext = instance.newTransactionContext(transactionOptions);
    transactionContext.beginTransaction();

    TimeUnit.SECONDS.sleep(5);
    final TransactionalMap<Object, Object> transactionalMap = transactionContext.getMap("transaction_exception");
    final Object forUpdate = transactionalMap.getForUpdate("hello");
    boolean doException = true;
    if (doException) {
      log.info("===== {}", System.currentTimeMillis());
      throw new RuntimeException("Wahaha");
    }
    transactionalMap.replace("hello", forUpdate, "Hazelcast");
    transactionContext.commitTransaction();
    log.info("===== {}", System.currentTimeMillis());
    return seconds;
  }

  @Override
  public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
    this.instance = hazelcastInstance;
  }
}
