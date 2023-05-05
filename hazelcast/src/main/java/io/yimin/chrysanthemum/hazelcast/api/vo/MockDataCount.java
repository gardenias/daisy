package io.yimin.chrysanthemum.hazelcast.api.vo;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MockDataCount implements DataSerializable {
  private long num;
  private long size;

  @Override
  public void writeData(ObjectDataOutput output) throws IOException {
    output.writeLong(num);
    output.writeLong(size);
  }

  @Override
  public void readData(ObjectDataInput input) throws IOException {
    this.num = input.readLong();
    this.size = input.readLong();
  }
}
