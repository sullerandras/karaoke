package karaoke.midi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public class Player {
  public interface PlayerListener {
    void playerStarted();

    void playerFinished();

    void playerTick(long tick);
  }

  private final List<PlayerListener> playerListeners = new java.util.ArrayList<>();

  private List<MetaEvent> type1Events;
  private Sequencer sequencer;
  // private double us_per_tick;

  public Player() throws FileNotFoundException, InvalidMidiDataException, IOException {
  }

  public void play(String file) throws IOException {
    play(new File(file));
  }

  public void play(File midiFile) throws IOException {
    System.out.println("Start playing " + midiFile);

    type1Events = null;
    sequencer = null;

    if (!midiFile.exists() || midiFile.isDirectory() || !midiFile.canRead()) {
      System.out.println("Invalid file: " + midiFile);
      return;
    }

    Midi midi = new Midi(midiFile.getAbsolutePath());
    // System.out.println("Format: " + midi.getFormat());
    // System.out.println("Track Count: " + midi.getTrackCount());
    // System.out.println("Division: " + midi.getDivision());
    // System.out.println("Tempo: " + midi.getTempo());
    // long ticks_per_quarter = midi.getDivision();
    // System.out.println("ticks_per_quarter: " + ticks_per_quarter);
    // long us_per_quarter = midi.getTempo();
    // System.out.println("us_per_quarter: " + us_per_quarter);
    // us_per_tick = us_per_quarter / (double) ticks_per_quarter;
    // System.out.println("us_per_tick: " + us_per_tick);
    // double seconds_per_tick = us_per_tick / 1000000;
    // System.out.println("seconds_per_tick: " + seconds_per_tick);
    // seconds = ticks * seconds_per_tick
    type1Events = midi.getTrack(1).getType1Events().stream()
        .filter(event -> event.getAbsoluteTime() > 0).toList();
    // System.out.println("Track #2 events: " + String.join("", type1Events.stream().map(MetaEvent::getDataAsCp874String).toList()).replaceAll("/", "\n"));

    // Play once
    try {
      // MidiFileFormat midiFileFormat = MidiSystem.getMidiFileFormat(midiFile);
      // System.out.println("======> DivisionType: " + midiFileFormat.getDivisionType());
      // System.out.println("======> MicrosecondLength: " + midiFileFormat.getMicrosecondLength());
      // System.out.println("======> Resolution: " + midiFileFormat.getResolution());
      // System.out.println("======> Type: " + midiFileFormat.getType());

      sequencer = MidiSystem.getSequencer(false);

      sequencer.open();
      SoundfontManager.synthesizer.open();

      sequencer.getTransmitter().setReceiver(SoundfontManager.synthesizer.getReceiver());

      sequencer.setSequence(MidiSystem.getSequence(midiFile));
      // sequencer.setMicrosecondPosition(60 * 1000000);
      // System.out.println("======> MicrosecondLength: " + sequencer.getMicrosecondLength());
      sequencer.addMetaEventListener(new MetaEventListener() {
        public void meta(MetaMessage event) {
          if (event.getType() == 1) {
            // System.out.println("Meta event type: " + event.getType() + ", data: "
            //     + byteArrayToString(event.getData()) + " at " + sequencer.getMicrosecondPosition()
            //     + ", ticks: " + sequencer.getMicrosecondPosition() / us_per_tick+", tickPosition: "+sequencer.getTickPosition());
            playerListeners.forEach(l -> l.playerTick(sequencer.getTickPosition()));
          }
        }
      });
      sequencer.start();

      playerListeners.forEach(PlayerListener::playerStarted);

      while (true) {
        if (sequencer.isRunning()) {
          try {
            Thread.sleep(300); // Check every second
            // System.out.print(String.format("======> MicrosecondPosition: %.6f       \r", sequencer.getMicrosecondPosition()*0.000001));
            // int tick = (int) Math.round((sequencer.getMicrosecondPosition() / us_per_tick));
          } catch (InterruptedException ignore) {
            break;
          }
        } else {
          break;
        }
      }

      // Close the MidiDevice & free resources
      sequencer.stop();
    } catch (MidiUnavailableException e) {
      e.printStackTrace();
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      // Close the MidiDevice & free resources
      if (sequencer != null) {
        sequencer.close();
      }
      SoundfontManager.synthesizer.close();
    }

    playerListeners.forEach(PlayerListener::playerFinished);

    System.out.println("Done playing " + midiFile);
  }

  public List<MetaEvent> getType1Events() {
    return type1Events;
  }

  public long getUsPosition() {
    if (sequencer == null) {
      return 0;
    }

    return sequencer.getMicrosecondPosition();
  }

  public long getUsTotalLength() {
    if (sequencer == null) {
      return 0;
    }

    return sequencer.getMicrosecondLength();
  }

  public int getTick() {
    if (sequencer == null) {
      return 0;
    }

    return (int) sequencer.getTickPosition();
  }

  /** Converts a byte array to a string */
  private static String byteArrayToString(byte[] buffer) {
    return new String(buffer, Charset.forName("Cp874"));
  }

  public void addPlayerListener(PlayerListener listener) {
    playerListeners.add(listener);
  }

  public void removePlayerListener(PlayerListener listener) {
    playerListeners.remove(listener);
  }

  public void seek(int seconds) {
    if (sequencer == null) {
      return;
    }

    long newPosition = sequencer.getMicrosecondPosition() + seconds * 1000000;
    if (newPosition < 0) {
      newPosition = 0;
    }
    sequencer.setMicrosecondPosition(newPosition);
  }
}
