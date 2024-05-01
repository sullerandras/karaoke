package karaoke.gui;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.JLabel;

import karaoke.midi.MetaEvent;

/**
 * Represents the lyrics of a song.
 * It has helper methods to make it easier to use the lyrics in the renderer.
 */
public class Lyrics {
  /**
   * The lines of the lyrics.
   * It has the same number of elements as {@link #metaEventsForLines}.
   */
  private final List<String> lines = new ArrayList<>();
  /**
   * The meta events for each line.
   * It has the same number of elements as {@link #lines}.
   */
  private final List<List<MetaEvent>> metaEventsForLines = new ArrayList<>();
  /**
   * The renderer to find the line with the biggest width.
   */
  private final LyricsRenderer renderer = new LyricsRenderer();
  /**
   * Maps the tick to the line index.
   */
  private final TreeMap<Integer, Integer> tickToLineIndex = new TreeMap<>();
  /**
   * The index of the line that has the biggest width when displayed in the renderer.
   */
  private int longestLineIndex = -1;
  /**
   * The maximum tick in the lyrics.
   */
  private int maxTick = 0;

  public Lyrics(List<MetaEvent> type1Events) {
    renderer.setFont(renderer.getFont().deriveFont(40f));
    int lineIndex = 0;
    maxTick = type1Events.get(type1Events.size() - 1).getAbsoluteTime();

    tickToLineIndex.put(0, 0); // the first line starts at tick 0
    StringBuilder line = new StringBuilder();
    List<MetaEvent> metaEventsForLine = new ArrayList<>();
    for (MetaEvent event : type1Events) {
      String ch = event.getDataAsCp874String();
      if (ch.indexOf('/') != ch.lastIndexOf('/')) { // if there are more than one '/'
        ch = ch.substring(0, ch.indexOf('/') + 1); // trim the string to (and including) the first '/'
      }
      boolean isLineBreak = ch.contains("/");
      ch = ch.replace("/", "");
      line.append(ch);
      metaEventsForLine.add(new MetaEvent(event.getDeltaTime(), event.getAbsoluteTime(), event.getType(),
          ch.getBytes(Charset.forName("Cp874"))));
      if (isLineBreak) {
        lines.add(line.toString());
        metaEventsForLines.add(metaEventsForLine);

        lineIndex++;
        tickToLineIndex.put(event.getAbsoluteTime(), lineIndex); // this is the end of the prev line and the beginning of the next line
        line = new StringBuilder();
        metaEventsForLine = new ArrayList<>();
      }
    }
  }

  /**
   * Returns the line index that has the biggest width when displayed in a JLabel.
   */
  public int getLongestLineIndex() {
    if (longestLineIndex >= 0) {
      return longestLineIndex;
    }

    int longestLinePixels = 0;
    for (int i = 0; i < lines.size(); i++) {
      renderer.setText(lines.get(i));
      int lineWidth = renderer.getPreferredSize().width;
      if (lineWidth > longestLinePixels) {
        longestLinePixels = lineWidth;
        longestLineIndex = i;
      }
    }

    return longestLineIndex;
  }

  /**
   * Returns the line that has the biggest width when displayed in a JLabel.
   * @see #getLongestLineIndex()
   */
  public String getLongestLine() {
    return lines.get(getLongestLineIndex());
  }

  /**
   * Returns the line index that contains the given tick.
   */
  public int getLineIndexAtTick(int tick) {
    Entry<Integer, Integer> e = tickToLineIndex.lowerEntry(tick);
    if (e == null) {
      return 0;
    }
    Integer lineIndex = e.getValue();
    return lineIndex != null ? lineIndex : 0;
  }

  /**
   * Returns the line that contains the given tick.
   * @see #getLineIndexAtTick(int)
   * @see #getLine(int)
   */
  public String getLineAtTick(int tick) {
    return getLine(getLineIndexAtTick(tick));
  }

  /**
   * Returns the line at the given index.
   */
  public String getLine(int index) {
    if (index < 0 || index >= lines.size()) {
      return "";
    }
    return lines.get(index);
  }

  /**
   * Returns the requested line split at the given tick.
   * The first string contains all characters before the tick.
   * The second string contains all characters after the tick including the tick.
   * It also moves the Thai vowels at the beginning of the second string to the to end of the first.
   * This is needed to make the text rendering look better: if there's no consonant then java renders the vowel
   * above a fake circle representing a missing character, resulting in flickering during song plays.
   * @param index the index of the line that we want to split. This should be the current line.
   * @param tick the tick where we want to split the line.
   * @return a pair of strings where the first string is the first part of the line and the second string is the second part of the line.
   */
  public PairOfStrings getLineSplitAtTick(int index, int tick) {
    List<MetaEvent> metaEvents = metaEventsForLines.get(index);
    StringBuilder first = new StringBuilder();
    int endIndex = 0;
    for (; endIndex < metaEvents.size(); endIndex++) {
      MetaEvent event = metaEvents.get(endIndex);
      if (event.getAbsoluteTime() >= tick) {
        break;
      }
      first.append(event.getDataAsCp874String());
    }
    while (endIndex < metaEvents.size() && isVowel(metaEvents.get(endIndex).getDataAsCp874String())) {
      first.append(metaEvents.get(endIndex).getDataAsCp874String());
      endIndex++;
    }

    StringBuilder second = new StringBuilder();
    for (; endIndex < metaEvents.size(); endIndex++) {
      second.append(metaEvents.get(endIndex).getDataAsCp874String());
    }
    return new PairOfStrings(first.toString(), second.toString());
  }

  /**
   * Returns the tick of the previous newline character.
   * Which is the greatest tick belonging to a newline character which is strictly less than the given tick.
   */
  public int previousLineTick(int tick) {
    Integer previousLineTick = tickToLineIndex.lowerKey(tick);
    if (previousLineTick == null) {
      return 0;
    }
    return previousLineTick;
  }

  /**
   * Returns the tick of the next newline character.
   * Which is the smallest tick belonging to a newline character which is greater than or equal to the given tick.
   */
  public int nextLineTick(int tick) {
    Integer nextLineTick = tickToLineIndex.ceilingKey(tick);
    if (nextLineTick == null) {
      return maxTick;
    }
    return nextLineTick;
  }

  private boolean isVowel(String s) {
    if (s.length() == 0) {
      return false;
    }
    char ch = s.charAt(0);
    return ch == '\u0E31'
        || ch == '\u0E33'
        || ch == '\u0E34'
        || ch == '\u0E35'
        || ch == '\u0E36'
        || ch == '\u0E37'
        || ch == '\u0E38'
        || ch == '\u0E39'
        || ch == '\u0E3A'
        || ch == '\u0E47'
        || ch == '\u0E48'
        || ch == '\u0E49'
        || ch == '\u0E4A'
        || ch == '\u0E4B'
        || ch == '\u0E4C'
        || ch == '\u0E4D'
        || ch == '\u0E4E';
  }
}
