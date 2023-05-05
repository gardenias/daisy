package io.yimin.google.gson;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MappingsParser {
  public static void main(String[] args) throws IOException {
    String mappings = "/Users/yimin/Dev/manual/design/Clearing/tasks/broker-mappings.json";

    FileInputStream inputStream = new FileInputStream(mappings);
    StringWriter output = new StringWriter();
    IOUtils.copy(inputStream, output, StandardCharsets.UTF_8);
    Map<String, Map<String, String>> all = new HashMap<>();
    new Gson().fromJson(output.toString(), Map.class).forEach((k, v) -> {
      Map<String, String> value = (Map<String, String>) v;
      String method = value.get("method");
      if (method != null) {
        int i1 = method.indexOf('(');
        method = method.substring(0, i1);
        int i = method.lastIndexOf(' ');

        String substring = method.substring(i).trim();
        if (substring.startsWith("com.huobi")) {
          String[] split = substring.split("\\.");
          String controller = split[split.length - 2];
          method = split[split.length - 1];

          Map<String, String> orDefault = all.computeIfAbsent(controller, k1 -> new HashMap<>(20));
          orDefault.put((String) k, method);
        }
      }
    });

    int i = 1, j = 1;
    Set<String> controllers = all.keySet();
    List<String> keys = controllers.stream().sorted().collect(Collectors.toList());
    String title = null;
    for (String k : keys) {
      if (!k.substring(0, 1).equals(title)) {
        title = k.substring(0, 1);
        //        System.out.println("== " + title);
      }
      Map<String, String> v = all.get(k);
      //      System.out.println("|" + (i++) + "|" + k + "| | |");
      System.out.println("|" + k + "| | |");
      for (Map.Entry<String, String> e : v.entrySet()) {
        String path = e.getKey();
        String method = e.getValue();
        System.out.println("| | |" + path.replace("||", ","));
      }
    }
  }
}
