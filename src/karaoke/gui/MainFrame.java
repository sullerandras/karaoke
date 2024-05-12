package karaoke.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

import karaoke.BetterJFileChooser;
import karaoke.Searcher;
import karaoke.midi.MetaEvent;
import karaoke.midi.Midi;
import karaoke.midi.Player;
import karaoke.midi.SoundfontManager;

public class MainFrame extends JFrame implements Player.PlayerListener {
  private LyricsRenderer label;
  private Lyrics lyrics;
  private Player player;
  private Thread midiThread = null;
  private int lineHeight;
  private boolean playerIsPlaying = false;
  private long usPosition;
  private float fontSize = 40.0f;
  private float lineHeightToFontSizeRatio = 1.2f;
  private int framesDrawn = 0;
  private long lastFpsTime = 0;
  private int fps = 0;
  private JTextField searchField;
  private SearchPopupManager searchPopupManager;
  private int lastPronouncedTick = 0;

  public MainFrame() throws FileNotFoundException, InvalidMidiDataException, IOException {
    super("Karaoke player");
    player = new Player();
    player.addPlayerListener(this);
    setSize(800, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    initComponents();
    setVisible(true);
    label.grabFocus();
    updateFontSize();
  }

  private void initComponents() {
    getContentPane().setLayout(new BorderLayout(0, 0));
    searchField = new JTextField();
    searchPopupManager = new SearchPopupManager(this);
    searchPopupManager.addSelectionListener(selectedFileName -> {
      label.grabFocus();
      playKar(new File(selectedFileName));
    });
    searchField.setToolTipText("Search to find a song");
    getContentPane().add(searchField, BorderLayout.NORTH);
    searchField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          searchPopupManager.hideSearchPopup();
          label.grabFocus();
          return;
        }
        searchPopupManager.updateSearchResults(searchField);
      }
    });

    label = new LyricsRenderer();
    label.setVerticalAlignment(JLabel.BOTTOM);
    label.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2));
    label.setFocusable(true);
    label.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
          player.seek(-5);
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
          player.seek(5);
        }
      }
    });

    getContentPane().add(label, BorderLayout.CENTER);
    getContentPane().setBackground(Color.BLACK);

    JMenuBar mb = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    mb.add(fileMenu);
    JMenuItem fileOpen = new JMenuItem("Open");
    fileMenu.add(fileOpen);
    fileOpen.addActionListener(e -> {
      JFileChooser fc = new BetterJFileChooser();
      fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Karaoke files (*.kar)", "kar"));
      if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        playKar(fc.getSelectedFile());
      }
    });

    JMenu sountfontMenu = new JMenu("Soundfont");
    mb.add(sountfontMenu);
    JMenuItem defaultSountfont = new JMenuItem("Default");
    sountfontMenu.add(defaultSountfont);
    defaultSountfont.addActionListener(ev -> {
      try {
        SoundfontManager.loadSoundFont(SoundfontManager.DEFAULT_SOUNDFONT);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    File[] files = new File(".").listFiles();
    java.util.Arrays.sort(files, (a, b) -> a.getName().compareTo(b.getName()));
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      if (file.getName().toLowerCase().endsWith(".sf2")) {
        JMenuItem sf2Item = new JMenuItem(file.getName());
        sountfontMenu.add(sf2Item);
        sf2Item.addActionListener(ev -> {
          try {
            SoundfontManager.loadSoundFont(file.getAbsolutePath());
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
      }
    }

    setJMenuBar(mb);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        calculateMaxFontSize();
        searchPopupManager.hideSearchPopup();
      }
    });
    addWindowFocusListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowLostFocus(WindowEvent e) {
        searchPopupManager.hideSearchPopup();
      }
    });
  }

  public void playKar(File karFile) {
    playerIsPlaying = false;
    usPosition = 0;
    framesDrawn = 0;
    lastFpsTime = System.currentTimeMillis();
    fps = 0;
    try {
      if (midiThread != null) {
        midiThread.interrupt();
      }

      Midi midi = new Midi(karFile.getAbsolutePath());
      setType1Events(midi.getTrack(1).getType1Events().stream()
          .filter(event -> event.getAbsoluteTime() > 0).toList());

      // System.out.println("Track #2 events: "
      //     + String.join("", type1Events.stream().map(MetaEvent::getDataAsCp874String).toList()).replaceAll("/", "\n"));

      midiThread = new Thread(() -> {
        try {
          player.play(karFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
      midiThread.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setType1Events(List<MetaEvent> type1Events) {
    this.lyrics = new Lyrics(type1Events);

    calculateMaxFontSize();
  }

  /** Calculate what is the maximum font size we can use */
  private void calculateMaxFontSize() {
    if (lyrics == null) {
      return;
    }

    label.setText(lyrics.getLongestLine());
    int minWidth = label.getPreferredSize().width;
    int minHeight = lineHeight * (1 + LyricsRenderer.PAST_LINES + 1 + LyricsRenderer.FUTURE_LINES);

    float widthRatio = getWidth() / (float) minWidth;
    float heightRatio = getHeight() / (float) minHeight;
    fontSize = Math.min(widthRatio, heightRatio) * fontSize;
    updateFontSize();
  }

  private void updateFontSize() {
    label.setFont(label.getFont().deriveFont(fontSize));
    lineHeight = label.getFontMetrics(label.getFont()).getHeight();
    lineHeightToFontSizeRatio = lineHeight / fontSize;
    System.out.println("Font size: " + fontSize + ", line height: " + lineHeight +
        " ratio: " + lineHeightToFontSizeRatio);
  }

  public void updateTick(int tick) throws InvocationTargetException, InterruptedException {
    if (lyrics == null) {
      return;
    }

    framesDrawn++;
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastFpsTime > 1000) {
      fps = framesDrawn;
      framesDrawn = 0;
      lastFpsTime = currentTime;
    }

    SwingUtilities.invokeAndWait(() -> {
      label.setData(lyrics, lineHeight, usPosition, fps, tick, lastPronouncedTick);
      label.repaint();
    });
  }

  @Override
  public void playerStarted() {
    playerIsPlaying = true;
    new Thread(() -> {
      while (playerIsPlaying) {
        usPosition = player.getUsPosition();
        try {
          updateTick(player.getTick());
          Thread.sleep(10);
        } catch (Exception ignore) {
          break;
        }
      }
    }).start();
  }

  @Override
  public void playerFinished() {
    playerIsPlaying = false;
  }

  @Override
  public void playerTick(long tick) {
    // updateTick((int) tick);
    lastPronouncedTick = (int) tick;
  }
}
