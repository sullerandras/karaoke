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
/* http://blog.taragana.com/index.php/archive/how-to-play-a-midi-file-from-a-java-application/ */

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

/** Plays a midi file provided on command line */
public class MidiPlayer {
  public static void main(String args[]) throws IOException, InvalidMidiDataException, MidiUnavailableException {
    SoundfontManager.initialize();
    karaoke.gui.MainFrame mainFrame = new karaoke.gui.MainFrame();
    // Argument check
    if (args.length == 0) {
      helpAndExit();
    }
    mainFrame.playKar(new File(args[0]));
  }

  /** Provides help message and exits the program */
  private static void helpAndExit() {
    System.out.println("Usage: java MidiPlayer midifile.mid");
    System.exit(1);
  }
}