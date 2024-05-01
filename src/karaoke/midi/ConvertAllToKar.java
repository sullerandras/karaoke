package karaoke.midi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ConvertAllToKar {
  public static void main(String[] args) throws IOException {
    // Lists all files recursively in the "./Song" directory.
    List<Path> files = Files.walk(Paths.get("./Song/"))
        .filter(Files::isRegularFile)
        .collect(Collectors.toList());

    System.out.println("Found " + files.size() + " files.");

    // For each *.mid file it finds the corresponding *.cur and *.lyr files.
    files.forEach(file -> {
      try {
        String midFileName = file.toString();
        System.out.println("Processing " + midFileName);
        if (midFileName.toLowerCase().endsWith(".mid")) {
          String curFileName = changeExtension(midFileName, ".cur").replace("Song/", "Cursor/");
          String lyrFileName = changeExtension(midFileName, ".lyr").replace("Song/", "Lyrics/");
          // Reads the first two line of the *.lyr file to get the song and singer names.
          SongAndSinger songAndSinger = Makekar.lyr2txt(Files.readAllBytes(Paths.get(lyrFileName)), new ByteArrayOutputStream());
          // Calls makeKar() to convert the file to "kar/[singer] - [song].kar".
          String outputFileName = "kar/"
              + new String(songAndSinger.getSinger(), Charset.forName("Cp874"))
              + " - "
              + new String(songAndSinger.getSong(), Charset.forName("Cp874"))
              + ".kar";
          Makekar.makeKar(midFileName, lyrFileName, curFileName, outputFileName);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private static String changeExtension(String fileName, String newExtension) {
    return fileName.replaceFirst("[.][^.]+$", newExtension);
  }
}
