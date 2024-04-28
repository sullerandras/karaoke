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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
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
import javax.swing.ToolTipManager;

import karaoke.BetterJFileChooser;
import karaoke.MetaEvent;
import karaoke.Midi;
import karaoke.Player;
import karaoke.Searcher;

public class MainFrame extends JFrame implements Player.PlayerListener {
  private static final int PAST_LINES = 3;
  private static final int FUTURE_LINES = 3;

  private JLabel label;
  private List<MetaEvent> type1Events;
  private TreeSet<Integer> slashTicks;
  private Player player;
  private Thread midiThread = null;
  private int lineHeight;
  private boolean playerIsPlaying = false;
  private long usPosition;
  private long usTotalLength;
  private float fontSize = 40.0f;
  private int framesDrawn = 0;
  private long lastFpsTime = 0;
  private int fps = 0;
  private Popup popup = null;

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
    JTextField searchField = new JTextField();
    searchField.setToolTipText("Search to find a song");
    getContentPane().add(searchField, BorderLayout.NORTH);
    searchField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          if (popup != null) {
            popup.hide();
          }
          label.grabFocus();
          return;
        }
        List<String> results = Searcher.search(searchField.getText());
        JList<String> jList = new JList<>(new Vector<>(results));
        jList.addMouseListener(new java.awt.event.MouseAdapter() {
          @Override
          public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getClickCount() == 2) {
              String selected = jList.getSelectedValue();
              if (selected != null) {
                popup.hide();
                playKar(new File(selected));
                label.grabFocus();
              }
            }
          }
        });
        jList.addKeyListener(new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              String selected = jList.getSelectedValue();
              if (selected != null) {
                popup.hide();
                playKar(new File(selected));
                label.grabFocus();
              }
            }
          }
        });
        if (popup != null) {
          popup.hide();
        }
        popup = PopupFactory.getSharedInstance().getPopup(MainFrame.this, jList,
          searchField.getLocationOnScreen().x, searchField.getLocationOnScreen().y + searchField.getHeight());
        popup.show();
      }
    });

    label = new JLabel("") {
      @Override
      public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, getHeight() - lineHeight, getWidth(), lineHeight);
        g.fillRect(0, 0, getWidth(), getHeight() - lineHeight * (PAST_LINES + FUTURE_LINES + 1));
        g.setColor(Color.GRAY);
        g.setFont(label.getFont());
        g.drawString(formatUs(usPosition) + " / " + formatUs(usTotalLength), 10, lineHeight);
        g.setFont(label.getFont().deriveFont(10.0f));
        g.drawString("FPS: " + fps, 10, getHeight() - 10);
      }
    };
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

    setJMenuBar(mb);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        calculateMaxFontSize();
        if (popup != null) {
          popup.hide();
        }
      }
    });
    addWindowFocusListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowLostFocus(WindowEvent e) {
        if (popup != null) {
          popup.hide();
        }
      }
    });
  }

  public void playKar(File karFile) {
    playerIsPlaying = false;
    usPosition = 0;
    usTotalLength = 0;
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
    this.type1Events = new ArrayList<>(type1Events);
    for (int i = 0; i < PAST_LINES + 1; i++) {
      this.type1Events.add(new MetaEvent(0, 2000000000 + i, 1, new byte[] { '/' }));
    }

    prepareSlashTicks();
    calculateMaxFontSize();
  }

  /** Calculate what is the maximum font size we can use */
  private void calculateMaxFontSize() {
    if (type1Events == null) {
      return;
    }

    String[] lines = String.join("", type1Events.stream().map(MetaEvent::getDataAsCp874String).toList()).split("/");
    while (true) {
      int maxLineWidth = 0;
      int maxLineHeight = 0;
      for (int i = 0; i < lines.length; i++) {
        label.setText(lines[i]);
        maxLineWidth = Math.max(maxLineWidth, label.getPreferredSize().width);
        maxLineHeight = Math.max(maxLineHeight, label.getPreferredSize().height);
      }
      int minHeight = maxLineHeight * (1 + PAST_LINES + 1 + FUTURE_LINES + 1);
      if (maxLineWidth >= getWidth() || minHeight >= getHeight()) {
        fontSize *= 0.95;
        updateFontSize();
      } else if (maxLineWidth * 1.1 < getWidth() && minHeight * 1.1 < getHeight()) {
        fontSize /= 0.95;
        updateFontSize();
      } else {
        break;
      }
    }
  }

  private void updateFontSize() {
    label.setFont(label.getFont().deriveFont(fontSize));
    lineHeight = label.getFontMetrics(label.getFont()).getHeight();
    System.out.println("Font size: " + fontSize + ", line height: " + lineHeight);
  }

  private void prepareSlashTicks() {
    slashTicks = new TreeSet<>();
    for (MetaEvent event : type1Events) {
      if (event.getDataAsCp874String().contains("/")) {
        slashTicks.add(event.getAbsoluteTime());
      }
    }
  }

  public void updateTick(int tick) throws InvocationTargetException, InterruptedException {
    if (type1Events == null) {
      return;
    }

    framesDrawn++;
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastFpsTime > 1000) {
      fps = framesDrawn;
      framesDrawn = 0;
      lastFpsTime = currentTime;
    }

    StringBuilder sbPast = new StringBuilder("///////////");
    StringBuilder sbFuture = new StringBuilder();
    boolean futureStarted = false;
    for (MetaEvent event : type1Events) {
      if (event.getAbsoluteTime() < tick) {
        sbPast.append(event.getDataAsCp874String());
      } else if (!futureStarted && isVowel(event.getDataAsCp874String())) {
        sbPast.append(event.getDataAsCp874String());
      } else {
        futureStarted = true;
        sbFuture.append(event.getDataAsCp874String());
      }
    }
    String past = sbPast.toString();
    String future = sbFuture.toString();

    Integer previousSlashTick = slashTicks.lower(tick);
    if (previousSlashTick == null) {
      previousSlashTick = 0;
    }
    Integer nextSlashTick = slashTicks.ceiling(tick);
    if (nextSlashTick == null) {
      nextSlashTick = type1Events.get(type1Events.size() - 1).getAbsoluteTime();
    }
    int currentLineLength = nextSlashTick - previousSlashTick;
    int currentLineProgressInPercent = currentLineLength == 0 ? 100
        : (tick - previousSlashTick) * 100 / currentLineLength;
    if (currentLineProgressInPercent < 0) {
      currentLineProgressInPercent = 0;
    } else if (currentLineProgressInPercent > 100) {
      currentLineProgressInPercent = 100;
    }

    char[] pastChars = past.toCharArray();
    int newLinesToFind = PAST_LINES + 1;
    for (int i = pastChars.length - 1; i >= 0; i--) {
      if (pastChars[i] == '/') {
        newLinesToFind--;
        if (newLinesToFind == 0) {
          past = past.substring(i + 1);
          break;
        }
      }
    }

    char[] futureChars = future.toCharArray();
    newLinesToFind = FUTURE_LINES + 1;
    for (int i = 0; i < futureChars.length; i++) {
      if (futureChars[i] == '/') {
        newLinesToFind--;
        if (newLinesToFind == 0) {
          future = future.substring(0, i);
          break;
        }
      }
    }
    int marginBottom = lineHeight * currentLineProgressInPercent / 100;
    // System.out.println("currentLineProgressInPercent: " + currentLineProgressInPercent+ ", marginBottom: " + marginBottom);
    final String newText = "<html><body style=\"margin-bottom: " + marginBottom + "pt\">" +
        "<font color=yellow>" + past.replaceAll("/", "<br>") +
        "</font>" +
        "<font color=gray>" + future.replaceAll("/", "<br>") +
        "</font>" +
        "</body></html>";

    SwingUtilities.invokeAndWait(() -> {
      label.setText(newText);
    });
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

  @Override
  public void playerStarted() {
    usTotalLength = player.getUsTotalLength();
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
  }

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
}
