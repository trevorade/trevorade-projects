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

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lightless.heroscribe.list.Kind;
import org.lightless.heroscribe.list.LObject;

class ObjectSelector extends JPanel implements ItemListener,
    ListSelectionListener {
  private static final long serialVersionUID = 6632957726118414665L;

  private Gui gui;

  private JPanel objectsPanel;
  private CardLayout cardLayout;
  private JComboBox<Kind> kindsComboBox;
  private TreeMap<String, JList<LObject>> kindLists;

  private String selectedObject;
  private int objectRotation;

  public ObjectSelector(Gui gui) {
    super();

    this.gui = gui;

    objectsPanel = new JPanel();
    cardLayout = new CardLayout();
    kindsComboBox = new JComboBox<>();
    kindLists = new TreeMap<>();

    selectedObject = null;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    objectsPanel.setLayout(cardLayout);

    add(kindsComboBox);
    add(objectsPanel);

    for (Kind kind : gui.getObjects().kindsIterable()) {
      JList<LObject> list = new JList<>(new DefaultListModel<LObject>());
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.addListSelectionListener(this);
      objectsPanel.add(new JScrollPane(list), kind.id);

      kindLists.put(kind.id, list);
      kindsComboBox.addItem(kind);
    }

    for (LObject obj : gui.getObjects().objectsIterable()) {
      JList<LObject> list = kindLists.get(obj.kindId);
      DefaultListModel<LObject> listModel = (DefaultListModel<LObject>) list.getModel();

      listModel.addElement(obj);
    }

    for (Kind kind : gui.getObjects().kindsIterable()) {
      JList<LObject> list = kindLists.get(kind.id);
      DefaultListModel<LObject> listModel = (DefaultListModel<LObject>) list.getModel();
      list.setCellRenderer(new ObjectListCellRenderer(Collections.list(listModel.elements())));
    }

    kindsComboBox.addItemListener(this);
  }

  public String getSelectedObject() {
    return selectedObject;
  }

//  public int getSelectedObjectRotation() {
//    return objectRotation;
//  }

  private void setSelectedObject(LObject obj) {
    if (obj != null) {
      selectedObject = obj.id;
    } else {
      selectedObject = null;
    }

    gui.board.resetRotation();
  }

  /* -- */

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

  public void valueChanged(ListSelectionEvent e) {
    @SuppressWarnings("unchecked")
    JList<LObject> list = (JList<LObject>) e.getSource();

    setSelectedObject((LObject) list.getSelectedValue());

    gui.updateHint();
  }

  class ObjectListCellRenderer implements ListCellRenderer<LObject> {
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
        maxRemainingWidth = Math.max(maxRemainingWidth, metrics.stringWidth("10/10"));
      }

      this.maxIconWidth = maxIconWidth;
      this.maxLabelWidth = maxLabelWidth;

      preferredSize = new Dimension(maxIconWidth + padding + maxLabelWidth
          + padding + maxRemainingWidth, maxHeight);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends LObject> list,
        LObject value, int index, boolean isSelected, boolean cellHasFocus) {
      return new ObjectListCell(value,
          isSelected ? list.getSelectionBackground() : list.getBackground(),
          isSelected ? list.getSelectionForeground() : list.getForeground());
    }

    class ObjectListCell extends Component {
      private static final long serialVersionUID = -4296037614762334039L;

      private final LObject obj;
      private final Color background;
      private final Color foreground;

      public ObjectListCell(LObject obj, Color background, Color foreground) {
        this.obj = obj;
        this.background = background;
        this.foreground = foreground;

        this.setPreferredSize(preferredSize);
      }
      
      @Override
      public void paint(Graphics g) {
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
        g2d.drawImage(image, x + (maxIconWidth - imageWidth) / 2, (height - imageHeight) / 2, null);

        x += maxIconWidth + padding;
        g2d.setColor(foreground);
        int textY = (metrics.getAscent() + height) / 2;
        g2d.drawString(obj.name, x, textY);

        x += maxLabelWidth + padding;
        g2d.drawString("10/10", x, textY);
      }
    }
  }
}
