/*
  HeroScribe
  Copyright (C) 2002-2004 Flavio Chierichetti and Valerio Chierichetti

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2 (not
  later versions) as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.lightless.heroscribe.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import org.lightless.heroscribe.Command;
import org.lightless.heroscribe.Preferences;
import org.lightless.heroscribe.Region;
import org.lightless.heroscribe.helper.BoardPainter;
import org.lightless.heroscribe.helper.OS;
import org.lightless.heroscribe.list.List;
import org.lightless.heroscribe.quest.Quest;

public class Gui extends JFrame implements WindowListener, ItemListener,
    ActionListener {
  private static final long serialVersionUID = 1027540516394507664L;

  private List objects;
  private Quest quest;
  private Preferences prefs;

  ToolsPanel tools;
  Board board;

  BoardPainter boardPainter;

  JFileChooser fileChooser, ghostscriptChooser;

  TreeMap<String, ActualFileFilter> filters;

  ButtonGroup regionGroup;
  JMenuItem newKey, openKey, saveKey, saveAsKey, exportPdfKey, exportEpsKey,
      exportPngKey, ghostscriptKey, quitKey, listKey, aboutKey;

  JSplitPane contentSplitPane;

  Vector<SpecialQuestMenuItem> newSpecialKeys;

  JLabel hint, status;

  public Gui(Preferences preferences, List objects, Quest quest)
      throws Exception {
    super();

    this.prefs = preferences;
    this.objects = objects;
    this.quest = quest;

    filters = new TreeMap<>();

    ghostscriptChooser = new JFileChooser();
    ghostscriptChooser.setFileFilter(new GhostScriptFileFilter());

    fileChooser = new JFileChooser();
    filters.put("pdf", new ActualFileFilter("pdf", "PDF files (*.pdf)"));
    filters.put("eps", new ActualFileFilter("eps", "EPS files (*.eps)"));
    filters.put("png", new ActualFileFilter("png", "PNG files (*.png)"));
    filters
        .put("xml", new ActualFileFilter("xml", "HeroScribe Quests (*.xml)"));

    boardPainter = new BoardPainter(this);

    newSpecialKeys = new Vector<>();
    newSpecialKeys.add(new SpecialQuestMenuItem(1, 2));
    newSpecialKeys.add(new SpecialQuestMenuItem(2, 1));
    newSpecialKeys.add(new SpecialQuestMenuItem(2, 2));
    newSpecialKeys.add(new SpecialQuestMenuItem(2, 3));
    newSpecialKeys.add(new SpecialQuestMenuItem(3, 2));
    newSpecialKeys.add(new SpecialQuestMenuItem(3, 3));

    populateFrame();

    setMenuRegion();
    updateHint();
    updateTitle();

    addWindowListener(this);
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    setLocation(preferences.getWindowLocation());
    setSize(preferences.getWindowSize());

    this.setVisible(true);
  }

  public void updateTitle() {
    StringBuffer sb;

    sb = new StringBuffer(org.lightless.heroscribe.Constants.applicationName
        + " " + org.lightless.heroscribe.Constants.version
        + org.lightless.heroscribe.Constants.applicationVersionSuffix + " - ");

    if (quest.getFile() == null)
      sb.append("Untitled");
    else
      sb.append(quest.getFile().getName());

    if (quest.isModified())
      sb.append("*");

    setTitle(new String(sb));
  }

  private void populateFrame() {
    Container content;
    JMenuBar menu = new JMenuBar();
    JMenu file = new JMenu("File");
    JMenu region = new JMenu("Region");
    JMenu help = new JMenu("Help");

    JMenu newMenu = new JMenu("New");
    JMenu exportMenu = new JMenu("Export");
    JMenu prefsMenu = new JMenu("Preferences");

    JPanel bottom = new JPanel();

    /* New Menu */
    newKey = new JMenuItem("Quest");
    newKey.addActionListener(this);
    newMenu.add(newKey);

    newMenu.addSeparator();

    for (int i = 0; i < newSpecialKeys.size(); i++) {
      SpecialQuestMenuItem menuItem = (SpecialQuestMenuItem) newSpecialKeys
          .get(i);

      menuItem.addActionListener(this);
      newMenu.add(menuItem);
    }

    /* Export Menu */
    exportPdfKey = new JMenuItem("PDF ( high quality ) ...");
    exportPdfKey.addActionListener(this);
    exportMenu.add(exportPdfKey);

    exportEpsKey = new JMenuItem("EPS ( high quality ) ...");
    exportEpsKey.addActionListener(this);
    exportMenu.add(exportEpsKey);
    exportMenu.addSeparator();

    exportPngKey = new JMenuItem("PNG ( low quality ) ...");
    exportPngKey.addActionListener(this);
    exportMenu.add(exportPngKey);

    /* Prefs Menu */
    ghostscriptKey = new JMenuItem("GhostScript path...");
    ghostscriptKey.addActionListener(this);
    prefsMenu.add(ghostscriptKey);

    /* File Menu */
    file.add(newMenu);

    openKey = new JMenuItem("Open Quest...");
    openKey.addActionListener(this);
    file.add(openKey);
    file.addSeparator();

    saveKey = new JMenuItem("Save Quest");
    saveKey.addActionListener(this);
    file.add(saveKey);
    saveAsKey = new JMenuItem("Save Quest as...");
    saveAsKey.addActionListener(this);
    file.add(saveAsKey);
    file.addSeparator();

    file.add(exportMenu);
    file.addSeparator();

    file.add(prefsMenu);
    file.addSeparator();

    quitKey = new JMenuItem("Quit");
    quitKey.addActionListener(this);
    file.add(quitKey);

    menu.add(file);

    /* Region menu */
    regionGroup = new ButtonGroup();  // Makes menu items act as Radio selector.

    JRadioButtonMenuItem europeItem = new JRadioButtonMenuItem("Europe layout");
    europeItem.setModel(new RegionButtonModel(Region.EUROPE));
    europeItem.addItemListener(this);
    regionGroup.add(europeItem);
    region.add(europeItem);

    JRadioButtonMenuItem usaItem = new JRadioButtonMenuItem("USA layout");
    usaItem.setModel(new RegionButtonModel(Region.USA));
    usaItem.addItemListener(this);
    regionGroup.add(usaItem);
    region.add(usaItem);

    menu.add(region);

    /* Help menu */
    listKey = new JMenuItem("Objects...");
    listKey.addActionListener(this);
    help.add(listKey);
    help.addSeparator();

    aboutKey = new JMenuItem("About");
    aboutKey.addActionListener(this);
    help.add(aboutKey);

    menu.add(help);

    menu.setBorderPainted(false);

    setJMenuBar(menu);

    content = getContentPane();

    content.setLayout(new BorderLayout());

    tools = new ToolsPanel(this, prefs);
    board = new Board(this);

    contentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, tools, new JScrollPane(board));
    Integer dividerLocation = prefs.getDividerLocation();
    if (dividerLocation != null) {
      contentSplitPane.setDividerLocation(dividerLocation);
    }
    content.add(contentSplitPane);

    bottom.setLayout(new BorderLayout());

    hint = new JLabel();
    hint.setHorizontalAlignment(SwingConstants.LEFT);
    bottom.add(hint, BorderLayout.WEST);

    status = new JLabel();
    status.setHorizontalAlignment(SwingConstants.RIGHT);
    bottom.add(status, BorderLayout.EAST);

    bottom.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    /* MacOSX: bar on top; other os: bar on bottom */
    if (OS.isMacOsX()) {
      content.add(bottom, BorderLayout.NORTH);
    } else {
      content.add(bottom, BorderLayout.SOUTH);
    }
  }

  public Region getMenuRegion() {
    for (AbstractButton menuItem : Collections.list(regionGroup.getElements())) {
      if (menuItem.isSelected()) {
        RegionButtonModel model = (RegionButtonModel) menuItem.getModel();
        return model.getRegion();
      }
    }
    return Region.EUROPE;
  }

  private void setMenuRegion() {
    for (AbstractButton menuItem : Collections.list(regionGroup.getElements())) {
      RegionButtonModel model = (RegionButtonModel) menuItem.getModel();
      if (model.getRegion() == quest.getRegion()) {
        menuItem.setSelected(true);
        break;
      }
    }
  }

  public List getObjects() {
    return objects;
  }

  public Quest getQuest() {
    return quest;
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      if (e.getSource() instanceof AbstractButton) {
        AbstractButton source = (AbstractButton) e.getSource();
        if (source.getModel() instanceof RegionButtonModel) {
          RegionButtonModel model = (RegionButtonModel) source.getModel();
          quest.setRegion(model.getRegion());
          prefs.setRegion(model.getRegion());
        }
      }

      updateTitle();
      board.repaint();
    }
  }

  public void updateHint() {
    switch (tools.getCommand()) {
    case ADD:
      if (tools.selectorPanel.getSelectedObject() == null) {
        hint.setText("Select an object.");
      } else {
        hint.setText("Click on a square to add. Right Click or CTRL Click to turn.");
      }
      break;

    case SELECT:
      hint.setText("Click on a square to select it.");
      break;

    case DARKEN:
      hint.setText("Click to darken a square or to add a bridge. Right Click or CTRL Click to clear.");
      break;

    case NONE:
      hint.setText("Select a command.");
      break;

    default:
      hint.setText("!! COMMAND WITHOUT HINTS !!");
      break;
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JMenuItem source = (JMenuItem) e.getSource();

    if (source == newKey) {
      if (!quest.isModified()
          || JOptionPane.showConfirmDialog(this,
              "The current quest has not been saved.\n"
                  + "Do you really want to create a new one?", "New Quest",
              JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

        Quest newQuest = new Quest(1, 1, objects.getBoard(), null, getMenuRegion());

        tools.none.doClick();
        quest = newQuest;

        updateHint();
        updateTitle();

        boardPainter.init();

        board.setSize();
        board.repaint();
      }
    } else if (newSpecialKeys.contains(source)) {
      SpecialQuestMenuItem menuItem = (SpecialQuestMenuItem) source;

      if (!quest.isModified()
          || JOptionPane.showConfirmDialog(this,
              "The current quest has not been saved.\n"
                  + "Do you really want to create a new one?", "New Quest",
              JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

        Quest newQuest = new Quest(menuItem.getQuestWidth(),
            menuItem.getQuestHeight(), objects.getBoard(), null, getMenuRegion());

        tools.none.doClick();
        quest = newQuest;

        updateHint();
        updateTitle();

        boardPainter.init();

        board.setSize();
        board.repaint();
      }

    } else if (source == openKey) {
      if (!quest.isModified()
          || JOptionPane.showConfirmDialog(this,
              "The current quest has not been saved.\n"
                  + "Do you really want to open a new one?", "Open Quest",
              JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

        fileChooser.resetChoosableFileFilters();

        if (fileChooser.getSelectedFile() != null) {
          String path = fileChooser.getSelectedFile().getAbsolutePath();

          path = path.replaceFirst("[.][^.]*$", ".xml");

          fileChooser.setSelectedFile(new File(path));
        }

        fileChooser.setFileFilter((FileFilter) filters.get("xml"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
          try {
            Quest newQuest = new org.lightless.heroscribe.quest.Read(
                fileChooser.getSelectedFile(), objects).getQuest();

            tools.none.doClick();
            quest = newQuest;
            setMenuRegion();

            updateHint();
            updateTitle();

            boardPainter.init();

            board.setSize();
            board.repaint();
          } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Can't open file.", "Error",
                JOptionPane.ERROR_MESSAGE);

            ex.printStackTrace();
          }
        }
      }
    } else if (source == saveKey) {
      File file = null;
      if (quest.getFile() != null || (file = askPath("xml")) != null) {
        try {
          if (file != null)
            quest.setFile(file);

          quest.save();
          updateTitle();
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, "Can't save file.", "Error",
              JOptionPane.ERROR_MESSAGE);
          ex.printStackTrace();
        }
      }
    } else if (source == saveAsKey) {
      File file;
      if ((file = askPath("xml")) != null) {
        try {
          quest.setFile(file);
          quest.save();
          updateTitle();
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, "Can't save file.", "Error",
              JOptionPane.ERROR_MESSAGE);
          ex.printStackTrace();
        }
      }
    } else if (source == exportPdfKey) {
      File file;
      if ((file = askPath("pdf")) != null) {
        try {
          org.lightless.heroscribe.export.ExportPDF.write(
              prefs.getGhostscriptExec(), file, quest, objects);
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this,
              "Can't save file. Please set the right ghostscript path.",
              "Error", JOptionPane.ERROR_MESSAGE);
          ex.printStackTrace();
        }
      }
    } else if (source == exportEpsKey) {
      File file;
      if ((file = askPath("eps")) != null) {
        try {
          org.lightless.heroscribe.export.ExportEPS.write(file, quest, objects);
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, "Can't save file.", "Error",
              JOptionPane.ERROR_MESSAGE);
          ex.printStackTrace();
        }
      }
    } else if (source == exportPngKey) {
      File file;
      if ((file = askPath("png")) != null) {
        try {
          org.lightless.heroscribe.export.ExportRaster.write(file, "png",
              boardPainter);
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, "Can't save file.", "Error",
              JOptionPane.ERROR_MESSAGE);
          ex.printStackTrace();
        }
      }
    } else if (source == ghostscriptKey) {
      ghostscriptChooser.setSelectedFile(prefs.getGhostscriptExec());

      if (ghostscriptChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        prefs.setGhostscriptExec(ghostscriptChooser.getSelectedFile());

        try {
          prefs.write();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    } else if (source == quitKey) {
      windowClosing(null);
    } else if (source == listKey) {
      String object = tools.selectorPanel.getSelectedObject();

      if (tools.getCommand() == Command.ADD && object != null)
        org.lightless.heroscribe.helper.OS.openURL(new File("Objects.html"),
            "object_" + object);
      else
        org.lightless.heroscribe.helper.OS.openURL(new File("Objects.html"),
            null);
    } else if (source == aboutKey) {
      JOptionPane
          .showMessageDialog(
              this,
              org.lightless.heroscribe.Constants.applicationName
                  + " "
                  + org.lightless.heroscribe.Constants.version
                  + org.lightless.heroscribe.Constants.applicationVersionSuffix
                  + "\n"
                  + org.lightless.heroscribe.Constants.applicationName
                  + " is (C) 2003-2004 Flavio Chierichetti and Valerio Chierichetti.\n"
                  + org.lightless.heroscribe.Constants.applicationName
                  + " is free software, distributed under the terms of the GNU GPL 2.\n"
                  + "HeroQuest and its icons are (C) of Milton Bradley Co.\n",
              "About", JOptionPane.PLAIN_MESSAGE);
    }
  }

  private File askPath(String extension) {
    fileChooser.resetChoosableFileFilters();

    if (fileChooser.getSelectedFile() != null) {
      String path = fileChooser.getSelectedFile().getAbsolutePath();

      path = path.replaceFirst("[.][^.]*$", "." + extension);

      fileChooser.setSelectedFile(new File(path));
    }

    fileChooser.setFileFilter((FileFilter) filters.get(extension));

    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
      File saveFile = fileChooser.getSelectedFile();

      if (!saveFile.getName().toLowerCase().endsWith("." + extension)) {
        saveFile = new File(saveFile.toString() + "." + extension);
        fileChooser.setSelectedFile(saveFile);
      }

      return saveFile;
    } else
      return null;
  }

  @Override
  public void windowClosing(WindowEvent e) {
    if (!quest.isModified()
        || JOptionPane.showConfirmDialog(this,
            "The current quest has not been saved.\n"
                + "Do you really want to quit?", "Quit",
            JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

      try {
        prefs.setWindowDimensions(getLocation(), getSize());
        prefs.setDividerLocation(contentSplitPane.getDividerLocation());
        prefs.write();
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      System.exit(0);

    }
  }

  @Override
  public void windowActivated(WindowEvent e) {
  }

  @Override
  public void windowClosed(WindowEvent e) {
  }

  @Override
  public void windowDeactivated(WindowEvent e) {
  }

  @Override
  public void windowDeiconified(WindowEvent e) {
  }

  @Override
  public void windowIconified(WindowEvent e) {
  }

  @Override
  public void windowOpened(WindowEvent e) {
  }
}

class GhostScriptFileFilter extends FileFilter {
  public GhostScriptFileFilter() {
    super();
  }

  @Override
  public boolean accept(File f) {
    if (f.isDirectory())
      return true;

    if (OS.isWindows() && f.getName().toLowerCase().equals("gswin32c.exe"))
      return true;

    if (!OS.isWindows() && f.getName().toLowerCase().equals("gs"))
      return true;

    return false;
  }

  @Override
  public String getDescription() {
    if (OS.isWindows())
      return "Ghostscript Shell (gswin32c.exe)";
    else
      return "Ghostscript Shell (gs)";
  }
}

class ActualFileFilter extends FileFilter {
  String extension, description;

  public ActualFileFilter(String extension, String description) {
    super();
    this.extension = extension;
    this.description = description;
  }

  @Override
  public boolean accept(File f) {
    return f.isDirectory()
        || f.getName().toLowerCase().endsWith("." + extension);
  }

  @Override
  public String getDescription() {
    return description;
  }
}

class SpecialQuestMenuItem extends JMenuItem {
  private static final long serialVersionUID = -8853825228925019854L;

  private int questWidth, questHeight;

  public SpecialQuestMenuItem(int questWidth, int questHeight) {
    super("Quest " + questWidth + "x" + questHeight);

    this.questWidth = questWidth;
    this.questHeight = questHeight;
  }

  public int getQuestWidth() {
    return questWidth;
  }

  public int getQuestHeight() {
    return questHeight;
  }
}

class RegionButtonModel extends JToggleButton.ToggleButtonModel {
  private static final long serialVersionUID = 7864691023556683961L;

  private final Region region;

  public RegionButtonModel(Region region) {
    super();
    this.region = region;
  }

  public Region getRegion() {
    return region;
  }
}
