package io.yimin.chrysanthemum.hazelcast.api.serialize;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.hazelcast.internal.serialization.impl.JavaDefaultSerializers;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public final class BigDecimalSerializeUtil {

  private static JavaDefaultSerializers.BigIntegerSerializer bigIntegerSerializer = new JavaDefaultSerializers.BigIntegerSerializer();

  private BigDecimalSerializeUtil() {
  }

  public static BigDecimal read(final ObjectDataInput in) throws IOException {
    BigInteger bigInt = bigIntegerSerializer.read(in);
    int scale = in.readInt();
    return new BigDecimal(bigInt, scale);
  }

  public static void write(final ObjectDataOutput out, final BigDecimal obj) throws IOException {
    BigInteger bigInt = obj.unscaledValue();
    int scale = obj.scale();
    bigIntegerSerializer.write(out, bigInt);
    out.writeInt(scale);
  }
}
