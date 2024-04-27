package karaoke;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

public class Main {
  public static void main(String[] args) throws FileNotFoundException, InvalidMidiDataException, IOException {
    String soundfontFile = "fluidr3_gm.sf2";
    if (args.length > 0) {
      soundfontFile = args[0];
    }
    Player.loadSoundFont(soundfontFile);
    karaoke.gui.MainFrame mainFrame = new karaoke.gui.MainFrame();
  }
}
