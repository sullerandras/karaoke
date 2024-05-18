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

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 * Represents the list model for the search popup.
 * I created this so we don't have to recreate it on every character typed, although it does not seem to improve performance.
 */
public class SearchPopupListModel implements ListModel<String> {
  private List<String> results;
  private List<ListDataListener> listeners = new ArrayList<>();

  @Override
  public void addListDataListener(ListDataListener l) {
    listeners.add(l);
  }

  @Override
  public void removeListDataListener(ListDataListener l) {
    listeners.remove(l);
  }

  @Override
  public String getElementAt(int index) {
    return results.get(index);
  }

  @Override
  public int getSize() {
    return results.size();
  }

  /**
   * Sets the search results in the model.
   */
  public void setResults(List<String> results) {
    this.results = results;
  }
}
