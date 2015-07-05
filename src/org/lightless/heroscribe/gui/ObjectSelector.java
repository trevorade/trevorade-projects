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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.lightless.heroscribe.Preferences;
import org.lightless.heroscribe.list.Kind;
import org.lightless.heroscribe.list.LObject;
import org.lightless.heroscribe.quest.Quest.ObjectCountListener;

class ObjectSelector extends JPanel implements ItemListener,
    ListSelectionListener {
  private static final long serialVersionUID = 6632957726118414665L;

  private final Gui gui;
  private final Preferences prefs;

  private JComboBox<Kind> kindsComboBox;
  private JPanel objectsPanel;
  private CardLayout cardLayout;
  private JPanel ownedPanel;
  private JTextField numberOwnedField;

  private TreeMap<String, JList<LObject>> kindLists;

  private String selectedObject;
  private final int preferredWidth;

  public ObjectSelector(Gui gui, Preferences prefs) {
    super();

    this.gui = gui;
    this.prefs = prefs;

    kindsComboBox = new JComboBox<>();
    objectsPanel = new JPanel();
    cardLayout = new CardLayout();
    ownedPanel = new JPanel();

    ownedPanel.setLayout(new BorderLayout());
    ownedPanel.add(new JLabel("Number Owned:"), BorderLayout.WEST);
    numberOwnedField = new JTextField();
    ((AbstractDocument)numberOwnedField.getDocument()).setDocumentFilter(new NumberFilter());
    numberOwnedField.getDocument().addDocumentListener(new NumberOwnedListener());
    numberOwnedField.setEnabled(false);
    ownedPanel.add(numberOwnedField);

    kindLists = new TreeMap<>();

    selectedObject = null;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    objectsPanel.setLayout(cardLayout);

    // Add a checkbox to limit display to owned items (non-zero or greater than 1).
    setLayout(new BorderLayout());
    add(kindsComboBox, BorderLayout.NORTH);
    add(objectsPanel, BorderLayout.CENTER);
    add(ownedPanel, BorderLayout.SOUTH);

    for (Kind kind : gui.getObjects().kindsIterable()) {
      JList<LObject> list = new JList<>(new DefaultListModel<LObject>());
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.addListSelectionListener(this);
      objectsPanel.add(new JScrollPane(list), kind.id);

      kindLists.put(kind.id, list);
      kindsComboBox.addItem(kind);
    }
    kindsComboBox.setMaximumRowCount(kindsComboBox.getItemCount());

    for (LObject obj : gui.getObjects().objectsIterable()) {
      JList<LObject> list = kindLists.get(obj.kindId);
      DefaultListModel<LObject> listModel = (DefaultListModel<LObject>) list.getModel();

      listModel.addElement(obj);
    }

    int preferredWidth = 0;
    for (Kind kind : gui.getObjects().kindsIterable()) {
      JList<LObject> list = kindLists.get(kind.id);
      DefaultListModel<LObject> listModel = (DefaultListModel<LObject>) list.getModel();
      list.setCellRenderer(new ObjectListCellRenderer(Collections.list(listModel.elements())));
      preferredWidth = Math.max(preferredWidth, (int) list.getPreferredSize().getWidth());
    }
    this.preferredWidth = preferredWidth;

    kindsComboBox.addItemListener(this);

    gui.getQuest().addListener(new ObjectCountListener() {
      @Override public void objectCountChanged() {
        objectsPanel.repaint();
      }
    });
  }

  public String getSelectedObject() {
    return selectedObject;
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(preferredWidth, Integer.MAX_VALUE);
  }

  private void setSelectedObject(LObject obj) {
    if (obj != null) {
      selectedObject = obj.id;
      numberOwnedField.setEnabled(true);

      Integer numOwned = prefs.getNumOwned(obj.id);
      numberOwnedField.setText(numOwned == null ? "" : numOwned.toString());
    } else {
      selectedObject = null;
      numberOwnedField.setEnabled(false);
      numberOwnedField.setText("");
    }

    gui.board.resetRotation();
  }

  /* -- */

  @Override
  public void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      @SuppressWarnings("unchecked")
      Kind selected = (Kind) ((JComboBox<Kind>) e.getSource()).getSelectedItem();
      cardLayout.show(objectsPanel, selected.id);
      JList<LObject> list = kindLists.get(selected.id);

      setSelectedObject((LObject) list.getSelectedValue());

      gui.updateHint();
    }
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    @SuppressWarnings("unchecked")
    JList<LObject> list = (JList<LObject>) e.getSource();

    setSelectedObject((LObject) list.getSelectedValue());

    gui.updateHint();
  }

  private void numOwnedTextChanged(String newValue) {
    if (newValue.trim().isEmpty()) {
      prefs.setNumOwned(selectedObject, null);
    } else {
      prefs.setNumOwned(selectedObject, Integer.parseInt(newValue));
    }
    objectsPanel.repaint();
  }

  private final class NumberFilter extends DocumentFilter {
    final Pattern numbers = Pattern.compile("[0-9]*");

    @Override
    public void insertString(FilterBypass fb, int offset, String string,
        AttributeSet attr) throws BadLocationException {
      if (numbers.matcher(string).matches()) {
        super.insertString(fb, offset, string, attr);
      }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text,
        AttributeSet attrs) throws BadLocationException {
      if (numbers.matcher(text).matches()) {
        super.replace(fb, offset, length, text, attrs);
      }
    }
  }

  private final class NumberOwnedListener implements
      DocumentListener {
    private boolean inEvent = false;

    private void textChanged() {
      if (inEvent) return;
      inEvent = true;
      numOwnedTextChanged(numberOwnedField.getText());
      inEvent = false;
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      textChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      textChanged();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {}
  }

  private static final int MAX_ICON_WIDTH = 64;
  private static final int MAX_ICON_HEIGHT = 64;

  private final class ObjectListCellRenderer implements ListCellRenderer<LObject> {
    private final int maxIconWidth;
    private final int maxLabelWidth;
    private final int padding = 10;

    private final FontMetrics metrics;
    private final Dimension preferredSize;

    public ObjectListCellRenderer(Iterable<LObject> listObjects) {
      metrics = ObjectSelector.this.getFontMetrics(ObjectSelector.this.getFont());
      int maxIconWidth = 0;
      int maxLabelWidth = 0;
      int maxRemainingWidth = 0;
      int maxHeight = 30;

      for (LObject obj : listObjects) {
        Image objImage = obj.getIcon(gui.getMenuRegion()).getImage();
        maxIconWidth = Math.max(maxIconWidth, objImage.getWidth(null));
        maxHeight = Math.max(maxHeight, objImage.getHeight(null));
        maxLabelWidth = Math.max(maxLabelWidth, metrics.stringWidth(obj.name));
        maxRemainingWidth = Math.max(maxRemainingWidth, metrics.stringWidth("100/100"));
       // People who own more than 100 of some object are crazy.
      }

      maxIconWidth = Math.min(maxIconWidth, MAX_ICON_WIDTH);
      maxHeight = Math.min(maxHeight, MAX_ICON_HEIGHT);

      this.maxIconWidth = maxIconWidth;
      this.maxLabelWidth = maxLabelWidth;

      preferredSize = new Dimension(maxIconWidth + padding + maxLabelWidth
          + padding + maxRemainingWidth, maxHeight);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends LObject> list,
        LObject value, int index, boolean isSelected, boolean cellHasFocus) {
      return new ObjectListCell(value, list, isSelected);
    }

    class ObjectListCell extends Component {
      private static final long serialVersionUID = -4296037614762334039L;

      private final LObject obj;
      private final JList<? extends LObject> parentList;
      private final boolean isSelected;

      public ObjectListCell(LObject obj, JList<? extends LObject> parentList, boolean isSelected) {
        this.obj = obj;
        this.parentList = parentList;
        this.isSelected = isSelected;

        this.setPreferredSize(preferredSize);
      }

      private Color getBackground(boolean isSelected, boolean haveNone, boolean tooManyInQuest) {
        if (isSelected) {
            if (tooManyInQuest) {
              return new Color(255, 51, 51);
            } else if (haveNone) {
              return new Color(90, 90, 90);
            } else {
              return parentList.getSelectionBackground();
            }
        } else {
            if (tooManyInQuest) {
              return new Color(255, 207, 207);
            } else if (haveNone) {
              return new Color(210, 210, 210);
            } else {
              return parentList.getBackground();
            }
        }
      }

      private Color getForeground(boolean isSelected, boolean haveNone, boolean tooManyInQuest) {
        if (isSelected) {
          if (haveNone) {
            return new Color(210, 210, 210);
          } else {
            return parentList.getSelectionForeground();
          }
        } else {
          if (tooManyInQuest) {
            return new Color(79, 0, 0);
          } else if (haveNone) {
            return new Color(50, 50, 50);
          } else {
            return parentList.getForeground();
          }
        }
      }

      @Override
      public void paint(Graphics g) {
        final Integer numOwned = prefs.getNumOwned(obj.id);
        final int numInQuest = gui.getQuest().getNumInQuest(obj.id);
        final boolean haveNone = numOwned != null && numOwned < 1;
        final boolean tooManyInQuest = numOwned != null && numInQuest > numOwned;

        final Color foreground = getForeground(isSelected, haveNone, tooManyInQuest);
        final Color background = getBackground(isSelected, haveNone, tooManyInQuest);

        final int width = getSize().width;
        final int height = getSize().height;
        int x = 0;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(background);
        g2d.fillRect(0, 0, width, height);

        Image image = gui.getObjects().getObject(obj.id).getIcon(gui.getMenuRegion()).getImage();
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        if (imageWidth > MAX_ICON_WIDTH || imageHeight > MAX_ICON_HEIGHT) {
          imageWidth = Math.min(imageWidth, MAX_ICON_WIDTH);
          imageHeight = Math.min(imageHeight, MAX_ICON_HEIGHT);

          // Destination rectangle
          final int dx1 = x + (maxIconWidth - imageWidth) / 2;
          final int dy1 = (height - imageHeight) / 2;
          final int dx2 = dx1 + imageWidth;
          final int dy2 = dy1 + imageHeight;

          // Source rectangle
          final int sx1 = 0;
          final int sy1 = 0;
          final int sx2 = sx1 + imageWidth;
          final int sy2 = sy1 + imageHeight;

          g2d.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
        } else {
          g2d.drawImage(image, x + (maxIconWidth - imageWidth) / 2, (height - imageHeight) / 2, null);
        }

        x += maxIconWidth + padding;
        g2d.setColor(foreground);
        int textY = (metrics.getAscent() + height) / 2;
        g2d.drawString(obj.name, x, textY);

        x += maxLabelWidth + padding;
        String text = Integer.toString(gui.getQuest().getNumInQuest(obj.id));
        if (numOwned != null) {
          text += "/" + numOwned;
        }
        g2d.drawString(text, x, textY);
      }
    }
  }
}
