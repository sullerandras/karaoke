package karaoke;

import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;

/**
 * A better JFileChooser that remembers the last directory.
 */
public class BetterJFileChooser extends JFileChooser {
  private static final long serialVersionUID = 1L;

  private String lastFolder;

  public BetterJFileChooser() {
    Preferences prefs = Preferences.userRoot().node(getClass().getName());
    lastFolder = prefs.get("LAST_USED_FOLDER", new File(".").getAbsolutePath());
  }

  @Override
  public int showOpenDialog(java.awt.Component parent) {
    setCurrentDirectory(new File(lastFolder));
    int result = super.showOpenDialog(parent);
    if (result == JFileChooser.APPROVE_OPTION) {
      lastFolder = getSelectedFile().getParent();
      Preferences prefs = Preferences.userRoot().node(getClass().getName());
      prefs.put("LAST_USED_FOLDER", lastFolder);
    }
    return result;
  }
}
