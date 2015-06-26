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

import java.util.TreeMap;
import java.util.TreeSet;

import org.lightless.heroscribe.Region;
import org.lightless.heroscribe.helper.OS;

public class List {
  public LBoard board;

  public TreeMap<String, LObject> list;
  public TreeSet<Kind> kinds;

  public String version;
  public String vectorPrefix, vectorSuffix;
  public String rasterPrefix, rasterSuffix;
  public String samplePrefix, sampleSuffix;

  public List() {
    list = new TreeMap<>();
    kinds = new TreeSet<>();
  }

  public Iterable<LObject> objectsIterable() {
    /*
     * I know it's inefficient, but I need the objects ordered by value, not key
     * (i.e. by name, not id)
     */
    return new TreeSet<>(list.values());
  }

  public Iterable<Kind> kindsIterable() {
    return kinds;
  }

  public LObject getObject(String id) {
    return (LObject) list.get(id);
  }

  public LBoard getBoard() {
    return board;
  }

  public Kind getKind(String id) {
    for (Kind kind : kinds) {
      if (id.equals(kind.id)) {
        return kind;
      }
    }
    return null;
  }

  public String getVectorPath(String id, Region region) {
    return OS.getAbsolutePath(vectorPrefix + getObject(id).getIcon(region).path
        + vectorSuffix);
  }

  public String getRasterPath(String id, Region region) {
    return OS.getAbsolutePath(rasterPrefix + getObject(id).getIcon(region).path
        + rasterSuffix);
  }

  public String getSamplePath(String id, Region region) {
    return OS.getAbsolutePath(samplePrefix + getObject(id).getIcon(region).path
        + sampleSuffix);
  }

  public String getVectorPath(Region region) {
    return OS.getAbsolutePath(vectorPrefix + getBoard().getIcon(region).path
        + vectorSuffix);
  }

  public String getRasterPath(Region region) {
    return OS.getAbsolutePath(rasterPrefix + getBoard().getIcon(region).path
        + rasterSuffix);
  }

  public String getSamplePath(Region region) {
    return OS.getAbsolutePath(samplePrefix + getBoard().getIcon(region).path
        + sampleSuffix);
  }

}
