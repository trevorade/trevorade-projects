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

package org.lightless.heroscribe.quest;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.lightless.heroscribe.Region;
import org.lightless.heroscribe.list.LBoard;

public class Quest {
  private File file;

  private int width, height;
  private String name, speech;
  private Region region;
  private Vector<String> notes;

  private QBoard[][] boards;
  private boolean[][][] horizontalBridges, verticalBridges;

  private boolean modified;

  private final Map<String, Integer> objectCountById;
  private final Collection<ObjectCountListener> objectCountListeners;

  public Quest(int width, int height, LBoard board, File file, Region region) {
    this.width = width;
    this.height = height;

    boards = new QBoard[width][height];

    for (int i = 0; i < width; i++)
      for (int j = 0; j < height; j++)
        boards[i][j] = new QBoard(board.width, board.height, this);

    horizontalBridges = new boolean[width - 1][height][board.height];
    verticalBridges = new boolean[width][height - 1][board.width];

    notes = new Vector<>();

    this.region = region;

    name = "";
    speech = "";

    this.file = file;
    modified = false;

    objectCountById = new HashMap<>();
    objectCountListeners = new HashSet<>();
  }

  public void setHorizontalBridge(boolean value, int column, int row, int top) {
    if (0 <= column && column < width - 1)
      horizontalBridges[column][row][top - 1] = value;
  }

  public void setVerticalBridge(boolean value, int column, int row, int left) {
    if (0 <= row && row < height - 1)
      verticalBridges[column][row][left - 1] = value;
  }

  public boolean getHorizontalBridge(int column, int row, int top) {
    return horizontalBridges[column][row][top - 1];
  }

  public boolean getVerticalBridge(int column, int row, int left) {
    return verticalBridges[column][row][left - 1];
  }

  public QBoard getBoard(int column, int row) {
    return boards[column][row];
  }

  public void setBoard(QBoard board, int column, int row) {
    boards[column][row] = board;
  }

  public boolean isModified() {
    return modified;
  }

  public void setModified(boolean mod) {
    modified = mod;
  }

  public String getName() {
    return name;
  }

  public void setName(String newName) {
    name = newName;

    modified = true;
  }

  public Iterable<QObject> objectsIterable() {
    return new Iterable<QObject>() {
      @Override
      public Iterator<QObject> iterator() {
        return new ObjectsIterator(boards);
      }
    };
  }

  public Iterable<String> notesIterable() {
    return notes;
  }

  public void addNote(String newNote) {
    notes.add(newNote);

    modified = true;
  }

  public String getSpeech() {
    return speech;
  }

  public void setSpeech(String newSpeech) {
    speech = newSpeech;

    modified = true;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File newFile) {
    file = newFile;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public Region getRegion() {
    return region;
  }

  public void setRegion(Region newRegion) {
    if (!region.equals(newRegion)) {
      region = newRegion;

      modified = true;
    }
  }

  public void save() throws Exception {
    Write.write(this);

    modified = false;
  }

  // Called by QBoard.
  void objectAdded(String objId) {
    if (objectCountById.containsKey(objId)) {
      objectCountById.put(objId, 1 + objectCountById.get(objId));
    } else {
      objectCountById.put(objId, 1);
    }
    notifyObjectCountListeners();
  }

  // Called by QBoard.
  void objectRemoved(String objId) {
    objectCountById.put(objId, -1 + objectCountById.get(objId));
    notifyObjectCountListeners();
  }

  public void addListener(ObjectCountListener listener) {
    objectCountListeners.add(listener);
  }

  public void removeListener(ObjectCountListener listener) {
    objectCountListeners.remove(listener);
  }

  public int getNumInQuest(String objId) {
    if (!objectCountById.containsKey(objId))
      return 0;
    return objectCountById.get(objId);
  }

  private void notifyObjectCountListeners() {
    for (ObjectCountListener listener : objectCountListeners) {
      listener.objectCountChanged();
    }
  }

  public interface ObjectCountListener {
    void objectCountChanged();
  }
}

class ObjectsIterator implements java.util.Iterator<QObject> {
  private QBoard boards[][];

  private int i, j;
  private boolean hasEnded;

  private Iterator<QObject> currentBoardIterator;

  ObjectsIterator(QBoard[][] boards) {
    this.boards = boards;

    currentBoardIterator = null;

    gotoNext();
  }

  private void gotoNext() {
    if (currentBoardIterator == null) {
      i = j = 0;
      hasEnded = false;
    } else {
      if (currentBoardIterator.hasNext())
        return;
      j++;
    }

    while (i < boards.length) {
      while (j < boards[i].length) {
        if (boards[i][j] != null) {
          currentBoardIterator = boards[i][j].objectsIterable().iterator();

          if (currentBoardIterator.hasNext())
            return;
        }

        j++;
      }

      i++;
      j = 0;
    }

    hasEnded = true;
  }

  @Override
  public boolean hasNext() {
    return !hasEnded;
  }

  @Override
  public QObject next() throws NoSuchElementException {
    if (hasNext()) {
      QObject obj = currentBoardIterator.next();
      gotoNext();
      return obj;
    } else
      throw new NoSuchElementException();
  }

  @Override
  public void remove() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
}