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

public class NoteOnEvent extends MidiEvent {
  private final int note;
  private final int velocity;

  public NoteOnEvent(int deltaTime, int absoluteTime, int note, int velocity) {
    super(deltaTime, absoluteTime);
    this.note = note;
    this.velocity = velocity;
  }

  public int getNote() {
    return note;
  }

  public int getVelocity() {
    return velocity;
  }
}
