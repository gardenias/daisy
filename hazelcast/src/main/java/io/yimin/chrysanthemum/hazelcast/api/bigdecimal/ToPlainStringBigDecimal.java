package io.yimin.chrysanthemum.hazelcast.api.bigdecimal;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.math.BigDecimal;

public class ToPlainStringBigDecimal implements DataSerializable {
  private BigDecimal data;

  @Override public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
    objectDataOutput.writeUTF(data.toPlainString());
  }

  @Override public void readData(ObjectDataInput objectDataInput) throws IOException {
    this.data = new BigDecimal(objectDataInput.readUTF());
  }

  public BigDecimal getData() {
    return data;
  }

  public void setData(BigDecimal data) {
    this.data = data;
  }
}
