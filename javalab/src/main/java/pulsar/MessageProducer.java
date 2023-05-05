package pulsar;

import org.apache.commons.lang3.RandomUtils;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

import java.util.concurrent.TimeUnit;

public class MessageProducer {
  private static ProducerBuilder<String> producerBuilder;
  private static Producer<String> pulsarProducer;
  public static boolean batchingEnabled = true;
  public static int batchingMaxPublishDelayMs = 1;
  public static long startTime = System.nanoTime();

  public static int processors = Runtime.getRuntime().availableProcessors();

  public static void main(String[] args) throws PulsarClientException, InterruptedException {
    final PulsarClient pulsarClient = PulsarClient.builder()
            .ioThreads(((int) (processors * 1.5)))
            .connectionsPerBroker(3)
            .statsInterval(0, TimeUnit.SECONDS)
            .serviceUrl("pulsar://localhost:6650").build();

    producerBuilder = pulsarClient
            //        .newProducer(Schema.INT64)
            .newProducer(Schema.STRING)
            .enableBatching(batchingEnabled)
            .batchingMaxPublishDelay(batchingMaxPublishDelayMs, TimeUnit.MILLISECONDS)
            .blockIfQueueFull(true)
            .messageRoutingMode(MessageRoutingMode.SinglePartition)
            .maxPendingMessages(10000);

    final String topicName = "persistent://public/default/random_sum_test";
    pulsarProducer = producerBuilder
            .producerName("RandomPulsarProducer")
            .initialSequenceId(123)
            .topic(topicName)
            .create();
    System.out.println(" producer start ");
    long i = 0;
    while (i++ < 100_000_000) {
      pulsarProducer.sendAsync(i + "");

      if (i % 1000 == 0) {
        System.out.println(i);
        TimeUnit.MILLISECONDS.sleep(1);
      }
    }
  }
}
