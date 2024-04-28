package karaoke;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Searcher {
  static class StringAndLowercaseString {
    String original;
    String lowercase;

    public StringAndLowercaseString(String original) {
      this.original = original;
      this.lowercase = original.toLowerCase();
    }
  }

  private static List<StringAndLowercaseString> allKarFiles = null;

  public static List<String> search(String searchString) {
    try {
      if (searchString == null || searchString.strip().length() < 2) {
        return List.of();
      }

      Keywords keywords = new Keywords(searchString);

      if (allKarFiles == null) {
        allKarFiles = listAllKarFiles();
      }
      List<String> results = allKarFiles.stream()
          .filter(f -> keywords.matches(f.lowercase))
          .map(f -> f.original)
          .sorted()
          .collect(Collectors.toList());
      if (results.size() > 50) {
        return results.subList(0, 50);
      } else {
        return results;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return List.of();
    }
  }

  public static boolean matches(String file, String keywords) {
    return file.toLowerCase().contains(keywords.toLowerCase());
  }

  private static List<StringAndLowercaseString> listAllKarFiles() throws IOException {
    List<StringAndLowercaseString> files = Files.walk(Paths.get("."), FileVisitOption.FOLLOW_LINKS)
        .filter(Files::isRegularFile)
        .filter(path -> path.toString().toLowerCase().endsWith(".kar"))
        .map(path -> new StringAndLowercaseString(relativePath(path)))
        .collect(Collectors.toList());

    return files;
  }

  public static String relativePath(File file) {
    return relativePath(file.toPath());
  }

  public static String relativePath(Path path) {
    try {
      return new File(".").getAbsoluteFile().getParentFile().toPath().relativize(path.toAbsolutePath())
          .toString();
    } catch (IllegalArgumentException e) {
      return path.toAbsolutePath().toString();
    }
  }
}
