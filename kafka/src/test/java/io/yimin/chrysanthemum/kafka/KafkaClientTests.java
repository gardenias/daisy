package io.yimin.chrysanthemum.kafka;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsOptions;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Ignore("This is a kafka integration testing")
public class KafkaClientTests {

  protected static final String TEST_ENV_BOOTSTRAP_SERVERS = "172.18.4.185:9092,172.18.4.186:9092,172.18.4.187:9092";
  protected static final String DEV_ENV_BOOTSTRAP_SERVERS = "172.18.4.188:9092,172.18.4.189:9092,172.18.4.190:9092";
  private final RandomStringGenerator stringRandomGenerator =
          new RandomStringGenerator.Builder().withinRange('a', 'z').build();

  @Test
  public void createTopicsAndProduceMessage() throws InterruptedException, ExecutionException, TimeoutException {
    AdminClient adminClient = getAdminClient(DEV_ENV_BOOTSTRAP_SERVERS);
    ArrayList<NewTopic> topics = randomNewTopic(600, "T54499_" + stringRandomGenerator.generate(5) + "_");

    final CreateTopicsOptions options = new CreateTopicsOptions().timeoutMs(((int) (TimeUnit.MINUTES.toMillis(1))));
    final CreateTopicsResult createTopicsResult = adminClient.createTopics(topics, options);
    final KafkaFuture<Void> all = createTopicsResult.all();
    System.out.println("all.get(5, TimeUnit.SECONDS) = " + all.get(5, TimeUnit.MINUTES));
    System.out.println(createTopicsResult.values().keySet());

    final List<String> topicNames = Lists.transform(topics, e -> e.name());
    parallelProduceMessage(topicNames, 1000, 600);
  }

  @Test
  public void mockMessage() {
    final ArrayList<String> topics = Lists.newArrayList("T54499_oTMWnbSj", "T54499_oTMWnasL");
    parallelProduceMessage(topics, 999000, 100);
  }

  private void parallelProduceMessage(List<String> topicNames, int messageByteSize, int count) {
    final List<List<String>> split = Lists.partition(topicNames, 200);
    final ExecutorService executorService = Executors.newFixedThreadPool(split.size());

    final KafkaProducer producer = getKafkaProducer();
    final String messageValue = stringRandomGenerator.generate(messageByteSize);

    final CompletableFuture[] futures = split.stream()
            .map(group -> CompletableFuture.runAsync(() -> group.forEach(topic -> {
              for (int i = 0; i < count; i++) {
                ProducerRecord<Long, String> record = new ProducerRecord<>(topic, System.nanoTime(), messageValue);
                producer.send(record, (metadata, exception) -> {
                  if (exception != null) System.out.println("exception = " + exception);
                });
              }
              System.out.println("topic = " + topic + " DONE");
            }), executorService))
            .toArray(CompletableFuture[]::new);

    CompletableFuture.allOf(futures).join();
    producer.flush();
  }

  @Test
  public void deleteTopics() throws InterruptedException, ExecutionException, TimeoutException {
    AdminClient adminClient = getAdminClient(DEV_ENV_BOOTSTRAP_SERVERS);
    final Collection<String> deleteTopics =
            Collections2.transform(listTopics(adminClient, "test-13_match_result_xxx"), e -> e.name());
    deleteTopics(adminClient, deleteTopics);
  }

  @Test
  public void listTopics() throws InterruptedException, ExecutionException, TimeoutException {
    AdminClient adminClient = getAdminClient(DEV_ENV_BOOTSTRAP_SERVERS);
    //    final String prefix = "T54499_";
    final String prefix = "";
    final Collection<String> topics = Collections2.transform(listTopics(adminClient, prefix), e -> e.name());
    System.out.println(topics.size());
    System.out.println("topics = " + topics);
  }

  @Test
  public void consume() {
    final String topic = "T54499_KtavE_.*";
    String groupId = "T54499_consume_test";
    int consumerCount = 60;
    final ExecutorService executorService = Executors.newFixedThreadPool(consumerCount);
    final CompletableFuture[] futures =
            IntStream.range(0, consumerCount).mapToObj(e -> CompletableFuture.runAsync(() -> {
              final Consumer<Long, String> consumer = getLongStringConsumer(groupId, 10);
              consumer.subscribe(Pattern.compile(topic), new ConsumerRebalanceListener() {
                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                  //          System.out.println("onPartitionsRevoked: partitions = " + partitions);
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                  consumer.seekToBeginning(partitions);
                  System.out.println("onPartitionsAssigned: seekToBeginning partitions = " + partitions.size());
                }
              });
              int times = 0;
              while (true) {
                ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
                if (consumerRecords.isEmpty()) {
                  System.out.println("consumerRecords empty");
                  times++;
                } else {
                  System.out.println("consumerRecords.count() = " + consumerRecords.count());
                  consumerRecords.forEach(record -> {
                    System.out.println("record.offset() = " + record.offset());
                    final String value = record.value();
                    System.out.println(
                            record.key() + "[" + value.getBytes().length + "]:" + StringUtils.abbreviate(value, 10));
                  });

                }
                if (times > 10) break;
              }
              System.out.println(Thread.currentThread().getName() + " EXIST");
            }, executorService)).toArray(CompletableFuture[]::new);

    CompletableFuture.allOf(futures).join();

  }

  private void listOffset(AdminClient adminClient, String groupId) throws InterruptedException, ExecutionException {
    final KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> result =
            adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata();
    final Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = result.get();
    topicPartitionOffsetAndMetadataMap.forEach((e, v) -> {
      System.out.println("topic:" + e.topic() + ", partition:" + e.partition() + ", offset:" + v.offset());
    });
  }

  private ArrayList<NewTopic> randomNewTopic(int count, String topicPrefix) {
    ArrayList<NewTopic> topics = new ArrayList<>(count);

    for (int i = 0; i < count; i++) {
      topics.add(new NewTopic(topicPrefix + randomTopicSuffix(), 1, (short) 1));
    }
    return topics;
  }

  private void deleteTopics(AdminClient adminClient, Collection<String> deleteTopics) {
    final DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(deleteTopics);
    System.out.println(deleteTopicsResult.values().keySet());
  }

  private Collection<TopicListing> listTopics(AdminClient adminClient, String prefix)
          throws InterruptedException, ExecutionException, TimeoutException {
    final ListTopicsResult listTopicsResult = adminClient.listTopics();
    final Collection<TopicListing> topicListings = listTopicsResult.listings().get(1, TimeUnit.MINUTES);
    if (StringUtils.isBlank(prefix)) return topicListings;
    return topicListings.stream().filter(e -> StringUtils.startsWith(e.name(), prefix)).collect(Collectors.toList());
  }

  private String randomTopicSuffix() {
    return stringRandomGenerator.generate(3);
  }

  private KafkaProducer getKafkaProducer() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, DEV_ENV_BOOTSTRAP_SERVERS);
    configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.LongSerializer.class);
    configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.StringSerializer.class);
    configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, ((int) (TimeUnit.SECONDS.toMillis(120))));
    return new KafkaProducer(configs);
  }

  private Consumer<Long, String> getLongStringConsumer(String groupId, int maxPollRecordsCount) {
    Map<String, Object> consumerConfigs = new HashMap<>();
    consumerConfigs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, DEV_ENV_BOOTSTRAP_SERVERS);
    consumerConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    consumerConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.LongDeserializer.class);
    consumerConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.StringDeserializer.class);
    consumerConfigs.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
            "org.apache.kafka.clients.consumer.StickyAssignor");
    consumerConfigs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecordsCount);
    consumerConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    consumerConfigs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
    consumerConfigs.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    consumerConfigs.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, ((int) (TimeUnit.MINUTES.toMillis(1))));
    consumerConfigs.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, ((int) (TimeUnit.MINUTES.toMillis(1))));
    //    consumerConfigs.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1);
    //    consumerConfigs.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 50);

    return (Consumer<Long, String>) new KafkaConsumer(consumerConfigs);
  }

  private AdminClient getAdminClient(String bootstrapServers) {
    Map<String, Object> conf = new HashMap<>();
    conf.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    return KafkaAdminClient.create(conf);
  }
}
