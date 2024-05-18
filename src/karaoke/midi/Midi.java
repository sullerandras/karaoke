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

/**
 * Represents a Midi file.
 * Can be used for reading Midi files in a convenient way.
 */
public class Midi {
  private final byte[] data;

  public Midi(String fileName) throws IOException {
    data = Makekar.readFile(fileName);
    if (data.length < 14) {
      throw new IOException("Invalid Midi file: too short");
    }
    if (data[0] != 'M' || data[1] != 'T' || data[2] != 'h' || data[3] != 'd') {
      throw new IOException("Invalid Midi file: missing MThd header");
    }
    if (data[4] != 0 || data[5] != 0 || data[6] != 0 || data[7] != 6) {
      throw new IOException("Invalid Midi file: invalid MThd header size");
    }
  }

  public int getFormat() {
    return Makekar.getBigEndianWord(data, 8);
  }

  public int getTrackCount() {
    return Makekar.getBigEndianWord(data, 10);
  }

  public int getDivision() {
    return Makekar.getBigEndianWord(data, 12);
  }

  public MidiTrack getTrack(int index) {
    int offset = 14;
    for (int i = 0; i < index; i++) {
      int length = Makekar.getBigEndianDWord(data, offset + 4);
      offset += 8 + length;
    }
    return new MidiTrack(data, offset, 8 + Makekar.getBigEndianDWord(data, offset + 4));
  }

  /** Tempo is the last FF 51 03 tt tt tt event in the first track */
  public int getTempo() {
    return getTrack(0).getTempo();
  }
}
