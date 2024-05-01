package karaoke.midi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ConvertAllEmkToKar {
  public static void main(String[] args) throws IOException {
    // Lists all files recursively in the "./EMK" directory.
    List<Path> files = Files.walk(Paths.get("./EMK/"), FileVisitOption.FOLLOW_LINKS)
        .filter(Files::isRegularFile)
        .sorted()
        .collect(Collectors.toList());

    System.out.println("Found " + files.size() + " files.");

    // Unpack each .emk file to .mid, .lyr, .cur files, then convert them to a .kar file.
    for (Path file : files) {
      try {
        String emkFileName = file.toString();
        System.out.println("Processing " + emkFileName);
        if (emkFileName.toLowerCase().endsWith(".emk")) {
          EmkUnpack.unpackEmk(emkFileName);

          String midFileName = "MIDI_DATA.mid";
          String curFileName = "CURSOR_DATA.bin";
          String lyrFileName = "LYRIC_DATA.txt";
          // Reads the first two line of the *.lyr file to get the song and singer names.
          SongAndSinger songAndSinger = Makekar.lyr2txt(Files.readAllBytes(Paths.get(lyrFileName)), new ByteArrayOutputStream());
          // Calls makeKar() to convert the file to "emkkar/[singer] - [song].kar".
          String outputFileName = "emkkar/"
              + sanitizeFileName(new String(songAndSinger.getSinger(), Charset.forName("Cp874")))
              + " - "
              + sanitizeFileName(new String(songAndSinger.getSong(), Charset.forName("Cp874")))
              + ".kar";
          Makekar.makeKar(midFileName, lyrFileName, curFileName, outputFileName);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static String sanitizeFileName(String fileName) {
    return fileName.strip().replaceAll("[.\\/]", "_");
  }
}
