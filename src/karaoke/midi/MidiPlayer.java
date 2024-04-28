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