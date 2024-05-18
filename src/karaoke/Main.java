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

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import karaoke.midi.SoundfontManager;

public class Main {
  public static void main(String[] args) throws FileNotFoundException, InvalidMidiDataException, IOException, MidiUnavailableException {
    SoundfontManager.initialize();
    karaoke.gui.MainFrame mainFrame = new karaoke.gui.MainFrame();
  }
}
