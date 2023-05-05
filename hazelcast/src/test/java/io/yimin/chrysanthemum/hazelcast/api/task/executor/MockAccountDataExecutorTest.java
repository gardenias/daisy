package io.yimin.chrysanthemum.hazelcast.api.task.executor;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.config.UserCodeDeploymentConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.nio.serialization.DataSerializable;
import com.huobi.mulan.imdg.FrozenSnapshot;
import com.huobi.mulan.imdg.HZResultWithSnapshot;
import com.huobi.mulan.imdg.HazelcastDelegate;
import com.huobi.mulan.imdg.model.AccountIdKey;
import com.huobi.mulan.imdg.model.FrozenSubaccountMapKey;
import com.huobi.mulan.imdg.task.versioning.FreezeTask;
import io.yimin.chrysanthemum.hazelcast.HazelcastTest;
import io.yimin.chrysanthemum.hazelcast.api.task.Task;
import io.yimin.chrysanthemum.hazelcast.api.task.param.AccountIdParam;
import io.yimin.chrysanthemum.hazelcast.util.Randoms;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.LongStream;

class MockAccountDataExecutorTest extends HazelcastTest {

  private static HazelcastDelegate liteMember;

  @BeforeAll
  static void setUp() {
    clientInstance = getClientInstance(etf_cluster);
//    startOneLiteMemberAsUserCodeProvider();
  }

  private static void startOneLiteMemberAsUserCodeProvider() {
    Config config = new Config();
    config.setLiteMember(true);
    final NetworkConfig networkConfig = new NetworkConfig();
    final JoinConfig join = new JoinConfig();
    final TcpIpConfig tcpIpConfig = new TcpIpConfig();
    tcpIpConfig.setMembers(Arrays.asList(etf_cluster));
    join.setTcpIpConfig(tcpIpConfig);
    networkConfig.setJoin(join);
    config.setNetworkConfig(networkConfig);

    UserCodeDeploymentConfig userCodeDeploymentConfig = config.getUserCodeDeploymentConfig();
    userCodeDeploymentConfig.setWhitelistedPrefixes("com.huobi,io.yimin,io.github.benas,org.jeasy.random");
    userCodeDeploymentConfig.setClassCacheMode(UserCodeDeploymentConfig.ClassCacheMode.ETERNAL);
    userCodeDeploymentConfig.setProviderMode(UserCodeDeploymentConfig.ProviderMode.LOCAL_AND_CACHED_CLASSES);
    userCodeDeploymentConfig.setEnabled(true);
    config.setUserCodeDeploymentConfig(userCodeDeploymentConfig);

    liteMember = new HazelcastDelegate(Hazelcast.newHazelcastInstance(config), "performance-lite");
  }

//  @Test
  @RepeatedTest(2)
  void task() {
    final IExecutorService iexecutorService = clientInstance.getExecutorService("mock_data");
    final ExecutorService executorService = Executors.newFixedThreadPool(50);

    Long start = Randoms.longRangeRandomizer.getRandomValue();
    final CompletableFuture[] futures = LongStream.range(start, start + 10).mapToObj((seed) -> {
      final long accountId = seed * 1000;
      System.out.println("accountId = " + accountId);
      Task<AccountIdParam> task = new Task<>();
      task.setParam(AccountIdParam.newBuilder().accountId(accountId).executorClassName(MockAccountDataExecutor.class.getName()).build());

      return CompletableFuture.runAsync(() -> {
        final Future<DataSerializable> dataSerializableFuture = iexecutorService.submitToKeyOwner(task, AccountIdKey.builder().accountId(task.getParam().getAccountId()).build());
        try {
          final DataSerializable result = dataSerializableFuture.get();
          System.out.println(accountId + " = " + ReflectionToStringBuilder.toString(result, ToStringStyle.JSON_STYLE));
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
      }, executorService);

    }).toArray(CompletableFuture[]::new);

    CompletableFuture.allOf(futures).join();
    System.out.println("DONE");
  }

  @Test
  void name() {
    System.out.println(clientInstance.hazelcastInstance.getList("subaccount_identifier").size());
  }

  @Test
  void getFrozenSubaccount() {
//    679189228781026,1000383,546908402951853
    final FrozenSubaccountMapKey key = FrozenSubaccountMapKey.builder()
        .transactionType(1)
        .transactionId(546908402951853L)
        .accountId(679189228781026L)
        .subaccountId(1000383L)
        .build();
    final Object null_frozen_subaccount = clientInstance.getMap("frozen_subaccount").get(key);
    System.out.println(ReflectionToStringBuilder.toString(null_frozen_subaccount, ToStringStyle.JSON_STYLE));
  }

  @Test
  void freeze() {
    final FreezeTask task = FreezeTask.builder()
        .transactionType(1)
        .transactionId(546908402951853L)
        .accountId(679189228781026L)
        .subaccountId(1000383L)
        .amount(BigDecimal.valueOf(1.0034982))
        .build();

    task.setHazelcastInstance(clientInstance.hazelcastInstance);
    final HZResultWithSnapshot<FrozenSnapshot> call = task.call();
    System.out.println(call.getCode());

  }

  @AfterAll
  static void afterAll() throws Exception {
    clientInstance.hazelcastInstance.shutdown();
    liteMember.hazelcastInstance.shutdown();
  }
}
