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

public class LObject implements Comparable<LObject> {
  public final Map<Region, Icon> iconsByRegion;
  public final String id, name, kindId, note;
  public final int height, width;
  public final float zorder;
  public final boolean door, trap;

  private LObject(Map<Region, Icon> iconsByRegion, String id, String name,
      String kindId, String note, int height, int width, float zorder,
      boolean door, boolean trap) {
    this.iconsByRegion = Collections.unmodifiableMap(iconsByRegion);
    this.id = id;
    this.name = name;
    this.kindId = kindId;
    this.note = note;
    this.height = height;
    this.width = width;
    this.zorder = zorder;
    this.door = door;
    this.trap = trap;
  }

  public Icon getIcon(Region region) {
    return this.iconsByRegion.get(region);
  }

  @Override
  public int compareTo(LObject that) {
    return name.compareTo(that.name);
  }

  @Override
  public String toString() {
    return name;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Map<Region, Icon> iconsByRegion;
    private String id, name, kindId, note;
    private int height, width;
    private float zorder;
    private boolean door, trap;

    private Builder() {
      iconsByRegion = new TreeMap<>();
    }

    public Builder putIcon(Icon icon, Region region) {
      iconsByRegion.put(region, icon);
      return this;
    }

    public boolean hasIconsForAllRegions() {
      for (Region region : Region.values()) {
        if (!iconsByRegion.containsKey(region)) {
          return false;
        }
      }
      return true;
    }

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setKindId(String kindId) {
      this.kindId = kindId;
      return this;
    }

    public Builder setNote(String note) {
      this.note = note;
      return this;
    }

    public Builder setHeight(int height) {
      this.height = height;
      return this;
    }

    public Builder setWidth(int width) {
      this.width = width;
      return this;
    }

    public Builder setZOrder(float zorder) {
      this.zorder = zorder;
      return this;
    }

    public Builder setDoor(boolean door) {
      this.door = door;
      return this;
    }

    public Builder setTrap(boolean trap) {
      this.trap = trap;
      return this;
    }

    public LObject build() {
      return new LObject(iconsByRegion, id, name, kindId, note, height, width,
          zorder, door, trap);
    }
  }
}