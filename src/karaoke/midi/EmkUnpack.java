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
package karaoke.midi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Unpacks a .emk file into a .mid, .lyr, .cur file.
 * Based on the javascript version found here: https://medium.com/@crkza1134/%E0%B8%A7%E0%B8%B4%E0%B8%98%E0%B8%B5%E0%B8%AD%E0%B9%88%E0%B8%B2%E0%B8%99%E0%B9%84%E0%B8%9F%E0%B8%A5%E0%B9%8C-emk-extreme-karaoke-%E0%B9%81%E0%B8%9A%E0%B8%9A%E0%B8%9A%E0%B9%89%E0%B8%B2%E0%B8%99%E0%B9%86-d684c5a0859d
 */
public class EmkUnpack {
  static class InvalidMagicException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidMagicException(byte[] data, int offset, int length, byte[] expected) {
      super("Invalid magic at offset " + offset + " in data of length " + length + ". Expected: "
          + Arrays.toString(expected)
          + ", got: " + Arrays.toString(Arrays.copyOfRange(data, offset, offset + expected.length)));
    }
  }

  private static int off;

  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: java -cp classes karaoke.midi.EmkUnpack <input.emk>");
      System.exit(1);
    }

    String emkFileName = args[0];
    unpackEmk(emkFileName);
  }

  public static void unpackEmk(String emkFileName) {
    try {
      byte[] data = Files.readAllBytes(Paths.get(emkFileName));

      byte[] xorKey = new byte[] { (byte) 0xAF, (byte) 0xF2, (byte) 0x4C, (byte) 0x9C, (byte) 0xE9, (byte) 0xEA,
          (byte) 0x99, (byte) 0x43 };
      for (int i = 0; i < data.length; i++) {
        data[i] ^= xorKey[i % xorKey.length];
      }

      byte[] magicHeader = new byte[] { (byte) 0x2E, (byte) 0x53, (byte) 0x46, (byte) 0x44, (byte) 0x53 };
      if (!arraysEqual(Arrays.copyOfRange(data, 0, magicHeader.length), magicHeader)) {
        throw new InvalidMagicException(data, 0, magicHeader.length, magicHeader);
      }

      int headerPos = readBigUInt64LE(data, 0x22);
      int headerEnd = readBigUInt64LE(data, 0x2a);

      byte[] header = Arrays.copyOfRange(data, headerPos, headerEnd);

      off = 0;

      byte[] magic = new byte[] { (byte) 0x53, (byte) 0x46, (byte) 0x44, (byte) 0x53 }; // SFDS

      while (off < header.length) {
        // System.out.println("---------------------------");
        checkMagic(header, magic);
        Object tag = readTag(header);
        int uncompressedSize = ((Number) readTag(header)).intValue();
        Object unk2 = readTag(header);
        int dataBegin = ((Number) readTag(header)).intValue();
        int dataEnd = ((Number) readTag(header)).intValue();
        Object unk5 = readTag(header); // this might be "whether the data is compressed" flag, but every file I've seen has it set to 1
        Object unk6 = readTag(header);
        skipBytes(0x10); // no idea what this is, possibly MD-5 hash?
        Object unk7 = readTag(header);
        Object unk8 = readTag(header);

        // the data is deflate compressed, with zlib header
        byte[] compressedData = Arrays.copyOfRange(data, dataBegin, dataEnd);

        Inflater inflater = new Inflater();
        inflater.setInput(compressedData);
        byte[] rawData = new byte[uncompressedSize];
        int inflatedSize = inflater.inflate(rawData);
        if (inflatedSize != uncompressedSize) {
          throw new RuntimeException("Invalid uncompressed size");
        }

        // switch ((String) tag) {
        //   case "HEADER": {
        //     System.out.println("--- HEADER ---");
        //     System.out.println(new String(rawData, Charset.forName("UTF-8")));
        //     System.out.println("--- END HEADER ---");
        //     break;
        //   }
        // }

        String ext = null;
        switch ((String) tag) {
          case "HEADER":
            ext = "txt";
            break;
          case "MIDI_DATA":
            ext = "mid";
            break;
          case "LYRIC_DATA":
            ext = "txt";
            // rawData = new String(rawData, Charset.forName("cp874")).getBytes();
            break;
          case "CURSOR_DATA":
            ext = "bin";
            break;
        }

        String filename = tag + "." + (ext != null ? ext : "bin");
        Files.write(Paths.get(filename), rawData);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (DataFormatException e) {
      e.printStackTrace();
    }
  }

  // const skipBytes = (n) => (off += n);
  private static void skipBytes(int n) {
    off += n;
  }

  // const readByte = () => header[off++];
  private static byte readByte(byte[] header) {
    return header[off++];
  }

  private static int readUShort(byte[] header) {
    int v = (header[off] & 0xFF) | ((header[off + 1] & 0xFF) << 8);
    off += 2;
    return v;
  }

  private static int readUInt(byte[] header) {
    int v = (header[off] & 0xFF) | ((header[off + 1] & 0xFF) << 8) | ((header[off + 2] & 0xFF) << 16)
        | ((header[off + 3] & 0xFF) << 24);
    off += 4;
    return v;
  }

  private static String readString(byte[] header) {
    int len = readByte(header);
    String str = new String(Arrays.copyOfRange(header, off, off + len), Charset.forName("UTF-8"));
    off += len;
    return str;
  }

  private static void checkMagic(byte[] header, byte[] magic) {
    byte[] data = Arrays.copyOfRange(header, off, off + magic.length);
    if (!arraysEqual(data, magic)) {
      throw new InvalidMagicException(data, 0, magic.length, magic);
    }
    off += magic.length;
  }

  private static Object readTag(byte[] header) {
    byte tag = readByte(header);
    switch (tag) {
      case 2: {
        byte v = readByte(header);
        // System.out.println("BYTE: " + v);
        return v;
      }
      case 3: {
        int v = readUShort(header);
        // System.out.println("USHORT: " + v);
        return v;
      }
      case 4: {
        int v = readUInt(header);
        // System.out.println("UINT: " + v);
        return v;
      }
      case 6: {
        String v = readString(header);
        // System.out.println("STRING: " + v);
        return v;
      }
      default:
        throw new RuntimeException("Unknown tag: 0x" + Integer.toHexString(tag));
    }
  }

  /**
   * Read a Little-Endian 64 bit unsigned integer from the data at the given offset.
   * Since we work with arrays of bytes, we assume the result can fit into a signed 32 bit integer.
   */
  public static int readBigUInt64LE(byte[] data, int offset) {
    return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) | ((data[offset + 2] & 0xFF) << 16)
        | ((data[offset + 3] & 0xFF) << 24) | ((data[offset + 4] & 0xFF) << 32) | ((data[offset + 5] & 0xFF) << 40)
        | ((data[offset + 6] & 0xFF) << 48) | ((data[offset + 7] & 0xFF) << 56);
  }

  public static boolean arraysEqual(byte[] a, byte[] b) {
    if (a.length != b.length) {
      return false;
    }
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) {
        return false;
      }
    }
    return true;
  }
}
