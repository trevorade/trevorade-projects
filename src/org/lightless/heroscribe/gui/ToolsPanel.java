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
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;

import org.lightless.heroscribe.Command;
import org.lightless.heroscribe.Preferences;

class ToolsPanel extends JPanel implements ItemListener {
  private static final long serialVersionUID = -3651474373588612642L;

  Gui gui;
  ObjectSelector selectorPanel;
  SquareDisplayer displayerPanel;

  ButtonGroup commands;
  ItemListener listener;
  JToggleButton add, select, dark, none;

  JPanel extraPanel;

  private Command selected;

  public ToolsPanel(Gui gui, Preferences prefs) {
    this.gui = gui;

    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    selected = Command.NONE;

    extraPanel = new JPanel();

    commands = new ButtonGroup();

    add = new JToggleButton("Add object");
    select = new JToggleButton("Select/Remove object");
    dark = new JToggleButton("Dark/Bridge");
    none = new JToggleButton();

    commands.add(add);
    commands.add(select);
    commands.add(dark);
    commands.add(none);

    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    modePanel.setLayout(new GridLayout(3, 1));

    modePanel.add(add);
    modePanel.add(select);
    modePanel.add(dark);

    selectorPanel = new ObjectSelector(gui, prefs);
    selectorPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

    displayerPanel = new SquareDisplayer(gui);
    displayerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

    this.add(modePanel, BorderLayout.NORTH);
    this.add(extraPanel);

    extraPanel.setLayout(new CardLayout());

    extraPanel.add(new JPanel(), "empty");
    extraPanel.add(selectorPanel, Command.ADD.toString());
    extraPanel.add(displayerPanel, Command.SELECT.toString());

    add.addItemListener(this);
    select.addItemListener(this);
    dark.addItemListener(this);
  }

  public void deselectAll() {
    add.setSelected(false);
    select.setSelected(false);
    dark.setSelected(false);
  }

  public Command getCommand() {
    return selected;
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    JToggleButton source = (JToggleButton) e.getSource();

    if (e.getStateChange() == ItemEvent.SELECTED) {
      if (source == add) {
        selected = Command.ADD;
        ((CardLayout) extraPanel.getLayout()).show(extraPanel, selected.toString());
      } else if (source == select) {
        selected = Command.SELECT;
        displayerPanel.clearList();
        ((CardLayout) extraPanel.getLayout()).show(extraPanel, selected.toString());
      } else if (source == dark) {
        selected = Command.DARKEN;
      } else if (source == none) {
        selected = Command.NONE;
      }

      gui.updateHint();
    } else {
      selected = Command.NONE;
      ((CardLayout) extraPanel.getLayout()).show(extraPanel, "empty");
    }
  }
}
