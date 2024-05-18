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

public class Keywords {
  private String[] keywords;

  public Keywords(String searchString) {
    this.keywords = searchString.trim().toLowerCase().split("\\s+");
  }

  public boolean matches(String text) {
    String lowerText = text.toLowerCase();
    for (String keyword : keywords) {
      if (!lowerText.contains(keyword)) {
        return false;
      }
    }
    return true;
  }
}
