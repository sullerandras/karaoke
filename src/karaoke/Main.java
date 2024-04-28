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
