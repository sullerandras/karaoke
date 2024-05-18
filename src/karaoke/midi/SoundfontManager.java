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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public class SoundfontManager {
  public static String DEFAULT_SOUNDFONT = "default";
  static Soundbank soundfont = null;
  static Synthesizer synthesizer = null;
  static boolean usingDefaultSoundfont = true;

  public static void initialize()
      throws FileNotFoundException, InvalidMidiDataException, IOException, MidiUnavailableException {
    synthesizer = MidiSystem.getSynthesizer();

    String lastSoundfont = getPreferences().get("LAST_USED_SOUNDFONT", DEFAULT_SOUNDFONT);
    if (!lastSoundfont.isEmpty()) {
      loadSoundFont(lastSoundfont);
    }
  }

  public static void loadSoundFont(String soundfontFile)
      throws FileNotFoundException, InvalidMidiDataException, IOException {
    if (usingDefaultSoundfont) {
      usingDefaultSoundfont = false;
      synthesizer.unloadAllInstruments(synthesizer.getDefaultSoundbank());
    }
    if (soundfont != null) {
      synthesizer.unloadAllInstruments(soundfont);
      soundfont = null;
    }
    if (soundfontFile.equals(DEFAULT_SOUNDFONT)) {
      soundfont = null;
      usingDefaultSoundfont = true;
      synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
    } else {
      soundfont = MidiSystem.getSoundbank(new BufferedInputStream(new FileInputStream(soundfontFile)));
      if (soundfont != null) {
        synthesizer.loadAllInstruments(soundfont);
      }
      usingDefaultSoundfont = false;
    }
    getPreferences().put("LAST_USED_SOUNDFONT", soundfontFile);
    System.out.println("Soundfont loaded: " + soundfontFile);
  }

  private static Preferences getPreferences() {
    return Preferences.userRoot().node(SoundfontManager.class.getName());
  }
}
