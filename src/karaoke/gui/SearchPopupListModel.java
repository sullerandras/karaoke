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
