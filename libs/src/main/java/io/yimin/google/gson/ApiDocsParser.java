package io.yimin.google.gson;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiDocsParser {
  public static void main(String[] args) throws IOException {
    String mappings = "/Users/yimin/Dev/manual/design/Clearing/tasks/api-docs.json";

    FileInputStream inputStream = new FileInputStream(mappings);
    StringWriter output = new StringWriter();
    IOUtils.copy(inputStream, output, StandardCharsets.UTF_8);

    Arrays.asList(output.toString().split(","));
    Map<String, Object> paths = (Map<String, Object>) new Gson().fromJson(output.toString(), Map.class).get("paths");
    Map<String, List<String>> all = new HashMap<>();
    for (String s : paths.keySet()) {
      all.put(s, new ArrayList<>(2));
      Map<String, Object> info = (Map<String, Object>) paths.get(s);
      Map<String, Object> get = (Map<String, Object>) info.get("get");
      if (get != null) {
        String summary = (String) get.get("summary");
        all.get(s).add("[GET] " + summary);
      }
      Map<String, Object> post = (Map<String, Object>) info.get("post");
      if (post != null) {
        String summary = (String) post.get("summary");
        if (summary != null) {
          all.get(s).add("[POST] = " + summary);
        }
      }
    }

    all.keySet().stream().sorted().collect(Collectors.toList()).forEach(path->{
      List<String> types = all.get(path);
      System.out.printf("| %s | %s\n", path, types.get(0));
      if (types.size() == 2) {
        System.out.printf("| %s | %s\n", " ", types.get(1));

      }
    });

  }
}
