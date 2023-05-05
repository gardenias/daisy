package io.yimin.chrysanthemum.hazelcast.api.bigdecimal;


import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class ToBigIntegerArrayAndScalaIntBigDecimal implements DataSerializable {
  private BigDecimal data;

  @Override public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
    BigInteger bigInt = data.unscaledValue();
    byte[] bytes = bigInt.toByteArray();
    objectDataOutput.writeInt(bytes.length);
    objectDataOutput.write(bytes);

    objectDataOutput.writeInt(data.scale());
  }

  @Override public void readData(ObjectDataInput objectDataInput) throws IOException {
    byte[] bytes = new byte[objectDataInput.readInt()];
    objectDataInput.readFully(bytes);
    final BigInteger bigInt = new BigInteger(bytes);
    int scale = objectDataInput.readInt();
    this.data = new BigDecimal(bigInt, scale);
  }

  public BigDecimal getData() {
    return data;
  }

  public void setData(BigDecimal data) {
    this.data = data;
  }
}
