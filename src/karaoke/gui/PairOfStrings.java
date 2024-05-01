package karaoke.gui;

public class PairOfStrings {
  private final String first;
  private final String second;

  public PairOfStrings(String first, String second) {
    this.first = first;
    this.second = second;
  }

  public String getFirst() {
    return first;
  }

  public String getSecond() {
    return second;
  }

  @Override
  public String toString() {
    return "PairOfStrings [first=" + first + ", second=" + second + "]";
  }
}
