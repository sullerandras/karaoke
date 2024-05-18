/*
 * KAROKE - Karaoke player for everyone! It can play .kar and .emk files.
 * Copyright (C) 2024  Andras Suller
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 1 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA  02110-1301 USA.
 */
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
  /**
   * Represents a karaoke file name and it's search term.
   * The search term is the lowercase version of the file name, excluding path and
   * file extension.
   */
  static class FileNameSearchTerm {
    String fileName;
    String searchTerm;

    public FileNameSearchTerm(String original) {
      this.fileName = original;
      int lastSlash = original.lastIndexOf('/');
      if (lastSlash < 0) {
        lastSlash = original.lastIndexOf('\\');
      }
      if (lastSlash < 0) {
        lastSlash = 0;
      }

      int lastDot = original.lastIndexOf('.');
      if (lastDot < 0) {
        lastDot = original.length();
      }

      this.searchTerm = original.substring(lastSlash, lastDot).toLowerCase();
    }
  }

  private static List<FileNameSearchTerm> allKarFiles = null;
  private static Object allKarFilesLock = new Object();

  public static List<String> search(String searchString) {
    try {
      if (searchString == null || searchString.strip().length() < 2) {
        return List.of();
      }

      Keywords keywords = new Keywords(searchString);

      synchronized (allKarFilesLock) {
        if (allKarFiles == null) {
          allKarFiles = listAllKarFilesWithCache();
        }
      }

      List<String> results = allKarFiles.stream()
          .filter(f -> keywords.matches(f.searchTerm))
          .map(f -> f.fileName)
          .limit(50)
          .collect(Collectors.toList());

      return results;
    } catch (IOException e) {
      e.printStackTrace();
      return List.of();
    }
  }

  public static boolean matches(String file, String keywords) {
    return file.toLowerCase().contains(keywords.toLowerCase());
  }

  private static List<FileNameSearchTerm> listAllKarFilesWithCache() throws IOException {
    String cacheFileName = "kars.cache";
    Path cachePath = Paths.get(cacheFileName);
    if (Files.exists(cachePath)) {
      System.out.println("Reading kar files from cache");
      return Files.lines(cachePath)
          .sorted()
          .map(FileNameSearchTerm::new)
          .collect(Collectors.toList());
    } else {
      System.out.println("Listing kar files and writing cache");
      List<FileNameSearchTerm> files = listAllKarFiles();
      Files.write(cachePath, files.stream().map(f -> f.fileName).collect(Collectors.toList()));
      return files;
    }
  }

  private static List<FileNameSearchTerm> listAllKarFiles() throws IOException {
    List<FileNameSearchTerm> files = Files.walk(Paths.get("."), FileVisitOption.FOLLOW_LINKS)
        .filter(Files::isRegularFile)
        .filter(path -> path.toString().toLowerCase().endsWith(".kar"))
        .sorted()
        .map(path -> new FileNameSearchTerm(relativePath(path)))
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
