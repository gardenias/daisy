package io.yimin.chrysanthemum.hazelcast.api.bigdecimal;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BigDecimalSerializeTests {

  private HazelcastInstance hazelcastInstance;
  private IMap<Integer, ToPlainStringBigDecimal> bigDecimalToPlainString;
  private IMap<Integer, ToBigIntegerArrayAndScalaIntBigDecimal> toBigIntegerArrayAndScalaIntBigDecimalIMap;

  private final MetricRegistry metrics = new MetricRegistry();
  private final Timer bigDecimalToPlainStringWrite = metrics.timer("big_decimal_to_plain_string_write");
  private final Timer bigDecimalToPlainStringRead = metrics.timer("big_decimal_to_plain_string_read");
  private final Timer toBigIntegerArrayAndScalaIntBigDecimalIMapWrite = metrics.timer("to_big_integer_array_and_scala_int_big_decimal_imap_write");
  private final Timer toBigIntegerArrayAndScalaIntBigDecimalIMapRead = metrics.timer("to_big_integer_array_and_scala_int_big_decimal_imap_read");

  public void setUp() {
    final ClientConfig config = new ClientConfig();
    config.setLicenseKey("ENTERPRISE_HD#4Nodes#IAVJNuk0lrOTEb1wmf7FySjaK65HU3812010191010901121001119080410");
    hazelcastInstance = HazelcastClient.newHazelcastClient(config);

    bigDecimalToPlainString = hazelcastInstance.getMap("big_decimal_to_plain_string");
    toBigIntegerArrayAndScalaIntBigDecimalIMap = hazelcastInstance.getMap("to_big_integer_array_and_scala_int_big_decimal_imap");

    startReport();
  }

  @Test
  public void testWriteData() {

    final int size = 10_000;
    final List<ToPlainStringBigDecimal> toPlainStringBigDecimals = EnhancedRandom.randomListOf(size, ToPlainStringBigDecimal.class);
    int index = 1;
    while (index <= size) {
      final Timer.Context context = bigDecimalToPlainStringWrite.time();
      bigDecimalToPlainString.put(index, toPlainStringBigDecimals.get(index - 1));
      context.stop();
      index++;
    }

    index = size;
    while (index > 0) {
      final Timer.Context context = bigDecimalToPlainStringRead.time();
      final ToPlainStringBigDecimal bigDecimal = bigDecimalToPlainString.get(index);
      context.stop();
      assertEquals(0, toPlainStringBigDecimals.get(index - 1).getData().compareTo(bigDecimal.getData()));
      index--;
    }


    final List<ToBigIntegerArrayAndScalaIntBigDecimal> toBigIntegerArrayAndScalaIntBigDecimals = EnhancedRandom.randomListOf(size, ToBigIntegerArrayAndScalaIntBigDecimal.class);

    index = 1;
    while (index <= size) {
      final Timer.Context context = toBigIntegerArrayAndScalaIntBigDecimalIMapWrite.time();
      toBigIntegerArrayAndScalaIntBigDecimalIMap.put(index, toBigIntegerArrayAndScalaIntBigDecimals.get(index - 1));
      context.stop();
      index++;
    }

    index = size;
    while (index > 0) {
      final Timer.Context context = toBigIntegerArrayAndScalaIntBigDecimalIMapRead.time();
      final ToBigIntegerArrayAndScalaIntBigDecimal bigDecimal = toBigIntegerArrayAndScalaIntBigDecimalIMap.get(index);
      context.stop();
      assertEquals(0, toBigIntegerArrayAndScalaIntBigDecimals.get(index - 1).getData().compareTo(bigDecimal.getData()));
      index--;
    }
  }

  private void startReport() {
    ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MICROSECONDS)
        .build();
    reporter.start(5, TimeUnit.SECONDS);
  }

  public void tearDown() throws Exception {
    waitXSeconds(5);
  }

  static void waitXSeconds(int i) {
    try {
      TimeUnit.SECONDS.sleep(i);
    } catch (InterruptedException e) {
    }
  }
}
