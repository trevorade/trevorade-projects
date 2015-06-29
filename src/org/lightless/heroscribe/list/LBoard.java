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

package org.lightless.heroscribe.list;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.lightless.heroscribe.Region;

public class LBoard {
  public final Map<Region, Icon> iconsByRegion;
  private final boolean[][] corridors;  // Arrays are always mutable so we can't give public access...
  public final int width, height;
  public final float borderDoorsOffset, adjacentBoardsOffset;

  private LBoard(Map<Region, Icon> iconsByRegion, boolean[][] corridors,
      int width, int height, float borderDoorsOffset, float adjacentBoardsOffset) {
    this.iconsByRegion = Collections.unmodifiableMap(iconsByRegion);
    this.corridors = corridors;
    this.width = width;
    this.height = height;
    this.borderDoorsOffset = borderDoorsOffset;
    this.adjacentBoardsOffset = adjacentBoardsOffset;
  }

  public Icon getIcon(Region region) {
    return iconsByRegion.get(region);
  }

  public boolean isCorridor(int col, int row) {
    return corridors[col][row];
  }

  public static Builder newBuilder(int width, int height) {
    return new Builder(width, height);
  }

  public static class Builder {
    private TreeMap<Region, Icon> iconsByRegion;
    private boolean[][] corridors;
    private final int width, height;
    private float borderDoorsOffset, adjacentBoardsOffset;

    private Builder(int width, int height) {
      iconsByRegion = new TreeMap<>();

      this.width = width;
      this.height = height;

      /* Summing 2 for the borders: not really necessary, but... */
      corridors = new boolean[width + 2][height + 2];
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }

    public boolean hasIconsForAllRegions() {
      for (Region region : Region.values()) {
        if (!iconsByRegion.containsKey(region)) {
          return false;
        }
      }
      return true;
    }

    public Builder putIcon(Icon icon, Region region) {
      this.iconsByRegion.put(region, icon);
      return this;
    }

    public Builder setCorridor(int col, int row) {
      corridors[col][row] = true;
      return this;
    }

    public Builder setBorderDoorsOffset(float borderDoorsOffset) {
      this.borderDoorsOffset = borderDoorsOffset;
      return this;
    }

    public Builder setAdjacentBoardsOffset(float adjacentBoardsOffset) {
      this.adjacentBoardsOffset = adjacentBoardsOffset;
      return this;
    }
    
    public LBoard build() {
      return new LBoard(iconsByRegion, corridors, getWidth(), getHeight(), borderDoorsOffset, adjacentBoardsOffset);
    }
  }
}
