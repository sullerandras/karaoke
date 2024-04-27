package karaoke;

public abstract class MidiEvent {
  private final int deltaTime;
  private final int absoluteTime;

  public MidiEvent(int deltaTime, int absoluteTime) {
    this.deltaTime = deltaTime;
    this.absoluteTime = absoluteTime;
  }

  public int getDeltaTime() {
    return deltaTime;
  }

  public int getAbsoluteTime() {
    return absoluteTime;
  }
}
