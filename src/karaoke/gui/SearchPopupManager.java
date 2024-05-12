package karaoke.gui;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;

import karaoke.Searcher;

/**
 * Manages the search popup.
 */
public class SearchPopupManager {
  private Component parentWindow;
  private Popup searchPopup = null;
  private JList<String> searchPopupResults;
  private SearchPopupListModel searchPopupModel;

  private List<SelectionListener> selectionListeners = new java.util.ArrayList<>();

  /**
   * Creates a new search popup manager.
   */
  public SearchPopupManager(Component parentWindow) {
    this.parentWindow = parentWindow;
    searchPopupModel = new SearchPopupListModel();
  }

  /**
   * Hides the search popup.
   */
  public void hideSearchPopup() {
    if (searchPopup != null) {
      searchPopup.hide();
    }
  }

  /**
   * Updates the search results based on the search field text.
   * @param searchField The search field. We need this so we can position the search popup correctly.
   */
  public void updateSearchResults(JTextField searchField) {
    List<String> results = Searcher.search(searchField.getText());
    if (searchPopupResults == null) {
      searchPopupResults = new JList<>();
      searchPopupModel = new SearchPopupListModel();
      searchPopupResults.setModel(searchPopupModel);
      searchPopupResults.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
          if (e.getClickCount() == 2) {
            notifyListenersAboutSelection();
          }
        }
      });
      searchPopupResults.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            notifyListenersAboutSelection();
          }
        }
      });
    }
    searchPopupModel.setResults(results);
    Popup newSearchPopup = PopupFactory.getSharedInstance().getPopup(parentWindow, searchPopupResults,
        searchField.getLocationOnScreen().x, searchField.getLocationOnScreen().y + searchField.getHeight());
    newSearchPopup.show();
    if (searchPopup != null) {
      searchPopup.hide();
    }
    searchPopup = newSearchPopup;
  }

  /**
   * Adds a selection listener.
   * Listeners will be notified when the user selects a search result.
   * The selected search result will be passed down to the listener.
   */
  public void addSelectionListener(SelectionListener listener) {
    selectionListeners.add(listener);
  }

  /**
   * Removes the selection listener from the search popup.
   */
  public void removeSelectionListener(SelectionListener listener) {
    selectionListeners.remove(listener);
  }

  /**
   * Hides the popup and notifies the listeners about the selection.
   */
  private void notifyListenersAboutSelection() {
    String selected = searchPopupResults.getSelectedValue();
    hideSearchPopup();
    selectionListeners.forEach(l -> l.selectionChanged(selected));
  }

  /**
   * Represents a selection listener.
   * Listeners will be notified when the user selects a search result.
   * The only parameter is the selected search result which should never ne `null`.
   */
  public interface SelectionListener {
    public void selectionChanged(String selectedFileName);
  }
}
