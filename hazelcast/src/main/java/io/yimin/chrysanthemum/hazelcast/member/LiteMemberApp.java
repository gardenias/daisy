package io.yimin.chrysanthemum.hazelcast.member;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.UserCodeDeploymentConfig;
import com.hazelcast.core.DistributedObjectEvent;
import com.hazelcast.core.DistributedObjectListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class LiteMemberApp {
  public static void main(String[] args) {
    Config config = new Config();
    config.setLiteMember(true);

    final GroupConfig groupConfig = new GroupConfig();
    groupConfig.setName("Lebaishi");
    groupConfig.setPassword("Wahaha");
    config.setGroupConfig(groupConfig);

    UserCodeDeploymentConfig userCodeDeploymentConfig = config.getUserCodeDeploymentConfig();
    userCodeDeploymentConfig.setWhitelistedPrefixes("com.huobi.hazelcast.module");
    userCodeDeploymentConfig.setClassCacheMode(UserCodeDeploymentConfig.ClassCacheMode.OFF);
    userCodeDeploymentConfig.setProviderMode(UserCodeDeploymentConfig.ProviderMode.LOCAL_AND_CACHED_CLASSES);
    userCodeDeploymentConfig.setEnabled(true);

    config.setUserCodeDeploymentConfig(userCodeDeploymentConfig);

    final HazelcastInstance lite = Hazelcast.newHazelcastInstance(config);
    lite.addDistributedObjectListener(new DistributedObjectListener() {
      @Override
      public void distributedObjectCreated(DistributedObjectEvent distributedObjectEvent) {
        System.out.println(distributedObjectEvent.getDistributedObject().getName());
      }

      @Override
      public void distributedObjectDestroyed(DistributedObjectEvent distributedObjectEvent) {
        System.out.println(distributedObjectEvent.getDistributedObject().getName());
      }
    });
  }
}
