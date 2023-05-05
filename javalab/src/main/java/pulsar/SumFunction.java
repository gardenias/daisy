package pulsar;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

public class SumFunction implements Function<String, Long> {

  @Override
  public Long process(String inputStr, Context context) {

    long input = Long.parseLong(inputStr);
    return input + 1;
  }
}
