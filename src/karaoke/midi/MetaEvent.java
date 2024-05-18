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

import java.nio.charset.Charset;

public class MetaEvent extends MidiEvent {
  private final int type;
  private final byte[] data;
  private final String dataAsCp874String;

  public MetaEvent(int deltaTime, int absoluteTime, int type, byte[] data) {
    super(deltaTime, absoluteTime);
    this.type = type;
    this.data = data;
    this.dataAsCp874String = new String(data, Charset.forName("Cp874")).replaceAll("�", " ");
  }

  public int getType() {
    return type;
  }

  public byte[] getData() {
    return data;
  }

  public String getDataAsCp874String() {
    return dataAsCp874String;
  }

  @Override
  public String toString() {
    return "MetaEvent(" + getDeltaTime() + ", " + type + ", " + data.length + " bytes: "
        + getDataAsCp874String() + ")" +
        (data.length == 1 && data[0] == '/' ? "\n" : "");
  }
}
