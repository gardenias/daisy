package io.yimin.chrysanthemum.hazelcast.api.task;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.transaction.TransactionContext;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import io.yimin.chrysanthemum.hazelcast.util.XMStringGenerator;

@Getter
@ToString
@Slf4j
public class SlowTask implements Callable<Long>, HazelcastInstanceAware, DataSerializable {
  private XMStringGenerator xmStringGenerator;

  private HazelcastInstance hazelcastInstance;
  private Long key;
  private String mapName;
  private int x;
  private boolean quick;

  public SlowTask() {
  }

  public SlowTask(Long key) {
    this(key, false);
  }

  public SlowTask(Long key, boolean quick) {
    this(key, "default", 1, quick);
  }

  public SlowTask(Long key, String mapName) {
    this(key, mapName, 1, false);
  }

  public SlowTask(Long key, String mapName, boolean quick) {
    this(key, mapName, 1, quick);
  }

  public SlowTask(Long key, String mapName, int x) {
    this(key, mapName, x, false);
  }

  public SlowTask(Long key, String mapName, int x, boolean quick) {
    this.key = key;
    this.mapName = mapName;
    this.x = x;

    this.quick = quick;
  }

  @Override
  public Long call() {
    String base = String.valueOf(this.key);
    xmStringGenerator = new XMStringGenerator(this.x);
    final String data = xmStringGenerator.xMString();
    final int dataSize = data.length();

    long size = 0L;

    int count = quick ? 512 : 1;

    while (count-- > 0) {
      final TransactionContext transactionContext = hazelcastInstance.newTransactionContext();
      transactionContext.beginTransaction();
      transactionContext.<Long, String>getMap(mapName).put(Long.valueOf(base + count), data);
      size += dataSize;
      transactionContext.commitTransaction();
      
      if (count % 10 == 0) {
        log.info("map {} size => {}", mapName, size);
      }
    }

    return size;
  }

  @Override
  public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
    this.hazelcastInstance = hazelcastInstance;
  }

  @Override
  public void writeData(ObjectDataOutput output) throws IOException {
    output.writeLong(this.key);
    output.writeUTF(this.mapName);
    output.writeInt(this.x);
    output.writeBoolean(this.quick);
  }

  @Override
  public void readData(ObjectDataInput input) throws IOException {
    this.key = input.readLong();
    this.mapName = input.readUTF();
    this.x = input.readInt();
    this.quick = input.readBoolean();
  }
}
