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
package karaoke.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;

import javax.swing.JLabel;

/**
 * Renderer to show the lyrics on the screen.
 * It shows the current position in the song in the first line.
 * The rest of the lines are the lyrics.
 * The lyrics are scrolled smoothly.
 */
public class LyricsRenderer extends JLabel {
  public static final int PAST_LINES = 3;
  public static final int FUTURE_LINES = 3;
  private Lyrics lyrics;
  private int lineHeight;
  private long usPosition;
  private int fps;
  private int tick;
  private int lastPronouncedTick;

  @Override
  public void paintComponent(java.awt.Graphics g) {
    super.paintComponent(g);
    if (lyrics == null) {
      return;
    }

    FontMetrics fontMetrics = g.getFontMetrics();
    int leftMargin = 10;
    int lineIndex = lyrics.getLineIndexAtTick(tick);

    // Draw time on the top
    g.setColor(Color.darkGray);
    g.fillRect(0, 0, getWidth(), lineHeight);
    g.setColor(Color.GRAY);
    Font font = getFont();
    g.setFont(font);
    String txt = formatUs(usPosition);
    g.drawString(txt, leftMargin, fontMetrics.getAscent());

    // Set clip to draw only the lyrics area, so we can smooth scroll the lyrics
    Shape originalClip = g.getClip();
    g.setClip(0, lineHeight, getWidth(), getHeight() - lineHeight);
    int translateY = lineHeight * currentLineProgressInPercent() / 100;

    // Draw the "past" lines in yellow
    g.setColor(Color.YELLOW);
    int y = lineHeight - translateY;
    for (int i = lineIndex - PAST_LINES; i < lineIndex; i++) {
      txt = lyrics.getLine(i);
      g.drawString(txt, leftMargin, y + fontMetrics.getAscent());
      y += lineHeight;
    }

    // Draw first part of current line in yellow
    PairOfStrings currentLine = lyrics.getLineSplitAtTick(lineIndex, lastPronouncedTick);
    txt = currentLine.getFirst();
    g.drawString(txt, leftMargin, y + fontMetrics.getAscent());

    // Draw remaining part of current line in gray
    g.setColor(Color.GRAY);
    txt = currentLine.getSecond();
    g.drawString(txt, leftMargin + fontMetrics.stringWidth(currentLine.getFirst()), y + fontMetrics.getAscent());

    // Draw the "future" lines in gray. We draw until reaching the bottom of the screen
    y += lineHeight;
    int i = lineIndex + 1;
    while (y < getHeight()) {
      txt = lyrics.getLine(i++);
      g.drawString(txt, leftMargin, y + fontMetrics.getAscent());
      y += lineHeight;
    }

    // Restore clip and translate
    g.setClip(originalClip);

    // Draw fps in bottom right corner
    g.setFont(font.deriveFont(10.0f));
    txt = "FPS: " + fps;
    int textWidth = g.getFontMetrics().stringWidth(txt);
    g.drawString(txt, getWidth() - textWidth - 10, getHeight() - 10);
  }

  /**
   * Set the data to be rendered.
   * @param lyrics the lyrics to render
   * @param lineHeight the height of a line in pixels
   * @param usPosition the current position in the song in microseconds
   * @param fps the frames per second
   * @param tick the current tick in the song
   * @param lastPronouncedTick the last tick where a word was pronounced
   */
  public void setData(Lyrics lyrics, int lineHeight, long usPosition, int fps, int tick, int lastPronouncedTick) {
    this.lyrics = lyrics;
    this.lineHeight = lineHeight;
    this.usPosition = usPosition;
    this.fps = fps;
    this.tick = tick;
    this.lastPronouncedTick = lastPronouncedTick;
  }

  /**
   * Format a time in microseconds to a string in the format "MM:SS.T" or "HH:MM:SS.T".
   * @param time the time in microseconds
   * @return the formatted time
   */
  public static String formatUs(long time) {
    long secs = time / 1000000;
    int hours = (int) (secs / 3600);
    int minutes = (int) ((secs % 3600) / 60);
    int seconds = (int) (secs % 60);
    int tenths = (int) ((time / 100000) % 10);

    if (hours == 0) {
      return String.format("%02d:%02d.%01d", minutes, seconds, tenths);
    }

    return String.format("%02d:%02d:%02d.%01d", hours, minutes, seconds, tenths);
  }

  /**
   * Calculate the progress of the current line in percent.
   * This is used to calculate the position of the smooth scrolling.
   */
  private int currentLineProgressInPercent() {
    int previousSlashTick = lyrics.previousLineTick(tick);
    int nextSlashTick = lyrics.nextLineTick(tick);
    int currentLineLength = nextSlashTick - previousSlashTick;
    int currentLineProgressInPercent = currentLineLength == 0 ? 100
        : (tick - previousSlashTick) * 100 / currentLineLength;
    if (currentLineProgressInPercent < 0) {
      currentLineProgressInPercent = 0;
    } else if (currentLineProgressInPercent > 100) {
      currentLineProgressInPercent = 100;
    }
    return currentLineProgressInPercent;
  }
}
