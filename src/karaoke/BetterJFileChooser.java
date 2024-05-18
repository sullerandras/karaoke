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
