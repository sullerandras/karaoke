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
