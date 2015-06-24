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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
  private TreeMap<String, JList<LObject>> kindList;

  private String selectedObject;
  private int objectRotation;

  public ObjectSelector(Gui gui) {
    super();

    this.gui = gui;

    objectsPanel = new JPanel();
    cardLayout = new CardLayout();
    kindsComboBox = new JComboBox<>();
    kindList = new TreeMap<>();

    selectedObject = null;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    objectsPanel.setLayout(cardLayout);

    add(kindsComboBox);
    add(objectsPanel);

    for (Kind kind : gui.getObjects().kindsIterable()) {
      JList<LObject> list = new JList<>(new DefaultListModel<>());

      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      kindList.put(kind.id, list);

      kindsComboBox.addItem(kind);

      objectsPanel.add(new JScrollPane(list), kind.id);

      list.addListSelectionListener(this);
    }

    for (LObject obj : gui.getObjects().objectsIterable()) {
      JList<LObject> list = kindList.get(obj.kind);
      DefaultListModel<LObject> listModel = (DefaultListModel<LObject>) list.getModel();

      listModel.addElement(obj);
    }

    kindsComboBox.addItemListener(this);
  }

  public String getSelectedObject() {
    return selectedObject;
  }

  public int getSelectedObjectRotation() {
    return objectRotation;
  }

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
      JList<LObject> list = kindList.get(selected.id);

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
}
