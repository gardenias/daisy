package io.yimin.chrysanthemum.hazelcast.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class HazelcastJMXMonitorTest {

  @Test
  void name() throws IOException, MalformedObjectNameException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {

    final ObjectName mapMBeanName = new ObjectName("com.hazelcast:instance=_hzInstance_1_hazelcast-test-yimin,type=IMap,name=");

//    int port = 10080;
    MBeanServerConnection mbsc1 = getmBeanServerConnection("172.18.4.41", 10090);
    print(mapMBeanName, mbsc1);
    MBeanServerConnection mbsc2 = getmBeanServerConnection("172.18.4.42", 10090);
    print(mapMBeanName, mbsc2);
    MBeanServerConnection mbsc3 = getmBeanServerConnection("172.18.4.44", 10080);
    print(mapMBeanName, mbsc3);
  }

  private MBeanServerConnection getmBeanServerConnection(String hostname, int port) throws IOException {
    final String serviceURL = "service:jmx:rmi://" + hostname + ":" + port + "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
    System.out.println(serviceURL);
    JMXServiceURL url = new JMXServiceURL(serviceURL);
    JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
    return jmxc.getMBeanServerConnection();
  }

  private void print(ObjectName mapMBeanName, MBeanServerConnection mbsc) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
    System.out.println(mbsc.getAttribute(mapMBeanName, "size"));
    System.out.println(mbsc.getAttribute(mapMBeanName, "localOwnedEntryCount"));
    System.out.println(mbsc.getAttribute(mapMBeanName, "localOwnedEntryMemoryCost"));
    System.out.println(mbsc.getAttribute(mapMBeanName, "localTotalGetLatency"));
  }


}
