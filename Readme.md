# Karaoke player for everyone!

KARAOKE is a java application that can play .kar and .emk files.

## How to run?

You must install Java JRE or Java JDK on your computer. I use OpenJDK: https://openjdk.org/install/

Download the latest release from the releases page, then run it with `java -jar karaoke.jar`.

## How to use?

You can use the File/Open menu to load a .kar file.

Or you can use input box at the top of the window to search for .kar or .emk files.
It scans the current directory for karaoke files. It checks all subfolders recursively.
Note that scanning all folders could take minutes, and there's no progress bar at the moment.
However it creates a `kars.cache` file that lists all karaoke files, and next time it will just load the cache file instead of scanning all files.
This means you need to delete the `kars.cache` file if you added or removed karaoke files from the current folder, and then restart the KARAOKE app.

## What is the license of this software?

GPL Version 1.

```
KAROKE - Karaoke player for everyone! It can play .kar and .emk files.
Copyright (C) 2024  Andras Suller

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License Version 1 as
published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA  02110-1301 USA.
```

## Disclaimer

At this moment this software assumes that all lyrics in .kar and .emk files are encoded using `windows-874` or `Cp874` character set.
This character set is used to display Thai characters.

At this moment this is not configurable. Let me know if you need to support other languages, or implement the change yourself. Pull requests are welcome!

## How to get the source code?

The source code is included in the released .jar files. Or you can visit https://github.com/sullerandras/karaoke

## What is a .kar file?

It is a MIDI file that has a special MIDI track that contains the lyrics of the song.

## What is an .emk file?

It is a compressed file format containing a MIDI file, a lyrics file and and a third file that contains the timing information to synchronise the lyrics to the song.

## How to contribute?

1. Fork this repo.
2. Implement your change.
3. Create a pull request.

Note that all derivative work of this software must use the GPLv1 license.

## How to make it sound better?

The midi player in java comes with some basic sounds. If you don't like it, then you can use your own Soundfont files!

The app will add all `*.sf2` files from the current folder to the "Soundfont" menu, and you can select a different Soundfont any time (even during playing a song).
The app remembers the last selected Soundfont file and automatically loads it when the app starts.
