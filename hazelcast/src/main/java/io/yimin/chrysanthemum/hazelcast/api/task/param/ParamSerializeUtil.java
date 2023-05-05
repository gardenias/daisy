package io.yimin.chrysanthemum.hazelcast.api.task.param;

import java.io.IOException;
import java.util.function.Supplier;

import io.yimin.chrysanthemum.hazelcast.api.task.ExecutorParam;
import com.hazelcast.nio.ObjectDataInput;

public final class ParamSerializeUtil {
  private ParamSerializeUtil() {
  }

  public static <T extends ExecutorParam> T read(ObjectDataInput input, Supplier<T> supplier) throws IOException {
    final T mockParam = supplier.get();
    mockParam.readData(input);
    return mockParam;
  }
}
