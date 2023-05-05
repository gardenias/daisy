#!/usr/bin/env bash

1. scp ~/Downloads/hazelcast-enterprise-3.11.1.tar.gz wuyimin@hazelcast-test-01.sinnet.huobiidc.com://home/wuyimi
2. mkdir hazelcast; cd hazelcast; mkdir config data logs user-lib; tar -xzf ~/hazelcast-enterprise-3.11.1.tar.gz; ln -s hazelcast-enterprise-3.11.1 default
  rm -rf default/user-lib; ln -s  ../user-lib default/user-lib
3. scp hazelcast*.xml log4j2.properties  wuyimin@hazelcast-test-01.sinnet.huobiidc.com://home/wuyimin/hazelcast/config
4. scp mulan-hazelcast-member/target/dependency/* wuyimin@hazelcast-test-01.sinnet.huobiidc.com://home/wuyimin/hazelcast/user-lib



scp ~/mulan-imdg-1.0-SNAPSHOT.jar wuyimin@hazelcast-test-01.sinnet.huobiidc.com://home/wuyimin \
scp ~/mulan-imdg-1.0-SNAPSHOT.jar wuyimin@hazelcast-test-02.sinnet.huobiidc.com://home/wuyimin \
scp ~/mulan-imdg-1.0-SNAPSHOT.jar wuyimin@hazelcast-test-05.sinnet.huobiidc.com://home/wuyimin \
scp ~/mulan-imdg-1.0-SNAPSHOT.jar wuyimin@hazelcast-test-03.sinnet.huobiidc.com://home/wuyimin \
scp ~/mulan-imdg-1.0-SNAPSHOT.jar wuyimin@hazelcast-test-04.sinnet.huobiidc.com://home/wuyimin \
scp ~/mulan-imdg-1.0-SNAPSHOT.jar wuyimin@hazelcast-test-06.sinnet.huobiidc.com://home/wuyimin


ssh wuyimin@hazelcast-test-01.sinnet.huobiidc.com "cd /hbdata/soft/hazelcast/default; sudo rm -rf user-lib/mulan-imdg-*.jar; sudo mv /home/wuyimin/mulan-imdg-1.0-SNAPSHOT.jar ./user-lib/; sudo bin/stop.sh; sudo bin/start.sh"; \
ssh wuyimin@hazelcast-test-02.sinnet.huobiidc.com "cd /hbdata/soft/hazelcast/default; sudo rm -rf user-lib/mulan-imdg-*.jar; sudo mv /home/wuyimin/mulan-imdg-1.0-SNAPSHOT.jar ./user-lib/; sudo bin/stop.sh; sudo bin/start.sh"; \
ssh wuyimin@hazelcast-test-05.sinnet.huobiidc.com "cd /hbdata/soft/hazelcast/default; sudo rm -rf user-lib/mulan-imdg-*.jar; sudo mv /home/wuyimin/mulan-imdg-1.0-SNAPSHOT.jar ./user-lib/; sudo bin/stop.sh; sudo bin/start.sh"; \
ssh wuyimin@hazelcast-test-03.sinnet.huobiidc.com "cd /hbdata/soft/hazelcast/default; sudo rm -rf user-lib/mulan-imdg-*.jar; sudo mv /home/wuyimin/mulan-imdg-1.0-SNAPSHOT.jar ./user-lib/; sudo bin/stop.sh; sudo bin/start.sh"; \
ssh wuyimin@hazelcast-test-04.sinnet.huobiidc.com "cd /home/wuyimin/hazelcast; rm config/*.xml"; \
ssh wuyimin@hazelcast-test-06.sinnet.huobiidc.com "cd /home/wuyimin/hazelcast; rm config/*.xml"


scp wuyimin@hazelcast-test-01.sinnet.huobiidc.com://home/wuyimin/hazelcast.tar.gz ./

scp /Users/yimin/Downloads/hazelcast-enterprise-3.11/lib/hazelcast-enterprise-all-3.11.jar wuyimin@hazelcast-test-01.sinnet.huobiidc.com://home/wuyimin/hazelcast/; \
scp /Users/yimin/Downloads/hazelcast-enterprise-3.11/lib/hazelcast-enterprise-all-3.11.jar wuyimin@hazelcast-test-02.sinnet.huobiidc.com://home/wuyimin/hazelcast/; \
scp /Users/yimin/Downloads/hazelcast-enterprise-3.11/lib/hazelcast-enterprise-all-3.11.jar wuyimin@hazelcast-test-03.sinnet.huobiidc.com://home/wuyimin/hazelcast/; \
scp /Users/yimin/Downloads/hazelcast-enterprise-3.11/lib/hazelcast-enterprise-all-3.11.jar wuyimin@hazelcast-test-04.sinnet.huobiidc.com://home/wuyimin/hazelcast/; \
scp /Users/yimin/Downloads/hazelcast-enterprise-3.11/lib/hazelcast-enterprise-all-3.11.jar wuyimin@hazelcast-test-05.sinnet.huobiidc.com://home/wuyimin/hazelcast/; \
scp /Users/yimin/Downloads/hazelcast-enterprise-3.11/lib/hazelcast-enterprise-all-3.11.jar wuyimin@hazelcast-test-06.sinnet.huobiidc.com://home/wuyimin/hazelcast/