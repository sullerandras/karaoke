package karaoke.midi;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Makekar {
  public static void main(String[] args) throws IOException {
    if (args.length != 4) {
      System.out.println("Usage: java -cp classes karaoke.Makekar <input.mid> <input.lyr> <input.cur> <output.kar>");
      System.exit(1);
    }

    String midFileName = args[0];
    String lyrFileName = args[1];
    String curFileName = args[2];
    String outputFileName = args[3];
    makeKar(midFileName, lyrFileName, curFileName, outputFileName);
  }

  public static void makeKar(String midFileName, String lyrFileName, String curFileName, String outputFileName)
      throws IOException {
    byte[] midBuffer = readFile(midFileName);
    byte[] lyrBuffer = readFile(lyrFileName);
    byte[] curBuffer = readFile(curFileName);
    ByteArrayOutputStream kar = new ByteArrayOutputStream();

    // read number of tracks
    int numTrack = getBigEndianWord(midBuffer, 10);
    System.out.println("=======> numTrack: " + numTrack);

    // read track number 1 data length
    int track1Len = getBigEndianDWord(midBuffer, 18);
    System.out.println("=======> track1Len: " + track1Len);

    // add one more track
    numTrack++;
    midBuffer[10] = (byte) (numTrack >> 8);
    midBuffer[11] = (byte) (numTrack & 0xFF);

    // write part1 to .kar file
    copy(kar, midBuffer, 0, 21 + track1Len + 1);

    // Read resolution
    int resolution = getBigEndianWord(midBuffer, 12);
    int timeBase = resolution / 4;

    // Create txt file (Temp.txt) from lyr file
    SongAndSinger songAndSinger = lyr2txt(lyrBuffer);

    int songLen = songAndSinger.getSong().length;
    int singerLen = songAndSinger.getSinger().length;

    int k = 0;
    k = k + 4 + (2 + songLen); // 00 FF 01 XX @T.....
    k = k + 4 + (2 + singerLen); // 00 FF 01 XX @T.....
    k = k + 9; // 4 byte + "Words" = 9

    // Read cur and txt files into buffers
    byte[] txtBuffer = readFile("Temp.txt");

    // write the track2 header 'MTrk'
    kar.write(new byte[] { 0x4D, 0x54, 0x72, 0x6B });

    // calculate the lenght of the second track
    int maxIndex;
    if ((txtBuffer.length - 1) <= (curBuffer.length / 2)) {
      maxIndex = txtBuffer.length - 1;
    } else {
      maxIndex = curBuffer.length / 2;
    }

    // Get text and write text event
    Buffer txt = new Buffer(txtBuffer);
    Buffer cur = new Buffer(curBuffer);

    int lastTime = 0;

    for (int j = 0; j < maxIndex; j++) {
      int ch1 = txt.next();

      int ch2 = cur.next();
      int ch3 = cur.next();

      if ((ch2 == 0xFF) && (ch3 == 0xFF)) {
        continue;
      }

      int currentTime = (ch2 + (ch3 * 256)) * (resolution / 24);

      if (currentTime < lastTime) {
        currentTime = lastTime;
      }

      int deltaTime = currentTime - lastTime;
      lastTime = currentTime;

      int value = deltaTime;

      if (value == 0) {
        //			fputc(0x00, kar);
        k++;
      } else {
        long dwBuffer = value & 0x7F;

        while ((value >>= 7) > 0) {
          dwBuffer <<= 8;
          dwBuffer |= 0x80;
          dwBuffer += (value & 0x7F);
        }
        while (true) {
          //				fputc((BYTE)dwBuffer, kar);
          k++;
          if ((dwBuffer & 0x80) != 0)
            dwBuffer >>= 8;
          else
            break;
        }
      }

      //		fputc(0xFF, tmp);
      k = k + 1;
      //		fputc(0x01, tmp);
      k = k + 1;
      //		fputc(0x01, tmp);
      k = k + 1;
      //		fputc(ch1, tmp);
      k = k + 1;
    }

    k = k + 4; // 4 is end of track2 "00 FF 2F 00"

    //	fprintf(stdout, "Track2 Lenght = %d\n", k);

    int track2Len = k;

    String hexNumber = String.format("%X", track2Len);
    System.out.println("=======> hexNumber: " + hexNumber);

    int hexLen = hexNumber.length();
    System.out.println("=======> hexLen: " + hexLen);

    int ch4 = 0;
    int ch3 = 0;
    int ch2 = 0;
    int ch1 = 0;

    if (hexLen == 1) {
      ch1 = char2num(hexNumber.charAt(0));
    } else if (hexLen == 2) {
      ch1 = char2num(hexNumber.charAt(0)) * 16 + char2num(hexNumber.charAt(1));
    } else if (hexLen == 3) {
      ch1 = char2num(hexNumber.charAt(1)) * 16 + char2num(hexNumber.charAt(2));
      ch2 = char2num(hexNumber.charAt(0));
    } else if (hexLen == 4) {
      ch1 = char2num(hexNumber.charAt(2)) * 16 + char2num(hexNumber.charAt(3));
      ch2 = char2num(hexNumber.charAt(0)) * 16 + char2num(hexNumber.charAt(1));
    }

    // write track2 length
    kar.write(new byte[] { (byte) ch4, (byte) ch3, (byte) ch2, (byte) ch1 });

    // Track name Words
    kar.write(new byte[] { 0x00, (byte) 0xFF, 0x03, 0x05, 0x57, 0x6F, 0x72, 0x64, 0x73 });
    System.out.println("=======> songLen: " + songLen);
    // write song title
    kar.write(new byte[] { 0x00, (byte) 0xFF, 0x01, (byte) (songLen + 2), '@', 'T' });
    kar.write(songAndSinger.getSong());

    // write singer name
    kar.write(new byte[] { 0x00, (byte) 0xFF, 0x01, (byte) (singerLen + 2), '@', 'T' });
    kar.write(songAndSinger.getSinger());

    // Get text and write text event
    txt = new Buffer(txtBuffer);
    cur = new Buffer(curBuffer);

    lastTime = 0;

    for (int j = 0; j < maxIndex; j++) {
      ch1 = txt.next();

      ch2 = cur.next();
      //		fprintf(tmp, "%d  %x ", n, ch2);
      ch3 = cur.next();
      //		fprintf(tmp, "%x", ch3);

      if ((ch2 == 0xFF) && (ch3 == 0xFF)) {
        continue;
      }

      int currentTime = (ch2 + (ch3 * 256)) * (timeBase / 6);
      //  or        currentTime = (ch2 + (ch3 * 256)) * (Resolution / 24);

      if (currentTime < lastTime) {
        currentTime = lastTime;
      }

      //		fprintf(tmp, "--- %d\n", currentTime);
      int deltaTime = currentTime - lastTime;
      lastTime = currentTime;

      int value = deltaTime;

      if (value == 0) {
        kar.write(0);
      } else {
        long dwBuffer = value & 0x7F;

        while ((value >>= 7) > 0) {
          dwBuffer <<= 8;
          dwBuffer |= 0x80;
          dwBuffer += (value & 0x7F);
        }
        while (true) {
          kar.write((byte) dwBuffer);
          if ((dwBuffer & 0x80) != 0)
            dwBuffer >>= 8;
          else
            break;
        }
      }

      kar.write(new byte[] { (byte) 0xFF, 0x01, 0x01, (byte) ch1 });
    }

    // write end of track2
    kar.write(new byte[] { 0x00, (byte) 0xFF, 0x2F, 0x00 });

    // Write the rest to .kar file
    kar.write(midBuffer, 21 + track1Len + 1, midBuffer.length - (21 + track1Len + 1));
    kar.close();

    FileOutputStream output = new FileOutputStream(outputFileName);
    try {
      kar.writeTo(output);
    } finally {
      output.close();
    }
  }

  public static SongAndSinger lyr2txt(byte[] lyrBuffer) throws IOException {
    Buffer buffer = new Buffer(lyrBuffer);
    byte[] song = buffer.readLine();
    byte[] singer = buffer.readLine();
    buffer.readLine();
    buffer.readLine();

    FileOutputStream txt = new FileOutputStream("Temp.txt");
    try {
      txt.write('/');

      while (true) {
        byte[] line = buffer.readLine();
        if (line == null) {
          break;
        }
        if (line.length > 0) {
          txt.write(line);
          txt.write('/');
        }
      }

      return new SongAndSinger(song, singer);
    } finally {
      txt.close();
    }
  }

  private static int char2num(char ch) {
    ch = Character.toLowerCase(ch);
    switch (ch) {
      case '0':
        return 0;
      case '1':
        return 1;
      case '2':
        return 2;
      case '3':
        return 3;
      case '4':
        return 4;
      case '5':
        return 5;
      case '6':
        return 6;
      case '7':
        return 7;
      case '8':
        return 8;
      case '9':
        return 9;
      case 'a':
        return 10;
      case 'b':
        return 11;
      case 'c':
        return 12;
      case 'd':
        return 13;
      case 'e':
        return 14;
      case 'f':
        return 15;
    }

    return 0;
  }

  private static void copy(OutputStream out, byte[] buffer, int offset, int length) throws IOException {
    for (int i = 0; i < length; i++) {
      out.write(buffer[offset + i]);
    }
  }

  public static int getBigEndianWord(byte[] buffer, int offset) {
    byte ch1 = buffer[offset];
    byte ch2 = buffer[offset + 1];
    int n1 = ((int) ch1) & 0xFF;
    int n2 = ((int) ch2) & 0xFF;
    return (n1 << 8) + n2;
  }

  public static int getBigEndianDWord(byte[] buffer, int offset) {
    byte ch1 = buffer[offset];
    byte ch2 = buffer[offset + 1];
    byte ch3 = buffer[offset + 2];
    byte ch4 = buffer[offset + 3];
    int n1 = ((int) ch1) & 0xFF;
    int n2 = ((int) ch2) & 0xFF;
    int n3 = ((int) ch3) & 0xFF;
    int n4 = ((int) ch4) & 0xFF;
    return (n1 << 24) + (n2 << 16) + (n3 << 8) + n4;
  }

  public static byte[] readFile(String fileName) throws IOException {
    Path path = Paths.get(fileName);
    return Files.readAllBytes(path);
  }
}