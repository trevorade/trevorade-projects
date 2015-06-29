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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.lightless.heroscribe.Region;
import org.lightless.heroscribe.helper.OS;

public class List {
  public final LBoard board;

  public final Map<String, LObject> list;
  public final Set<Kind> kinds;

  public final String version;
  public final String vectorPrefix, vectorSuffix;
  public final String rasterPrefix, rasterSuffix;
  public final String samplePrefix, sampleSuffix;

  private List(LBoard board, Map<String, LObject> list, Set<Kind> kinds,
      String version, String vectorPrefix, String vectorSuffix,
      String rasterPrefix, String rasterSuffix, String samplePrefix,
      String sampleSuffix) {
    this.board = board;
    this.list = Collections.unmodifiableMap(list);
    this.kinds = Collections.unmodifiableSet(kinds);
    this.version = version;
    this.vectorPrefix = vectorPrefix;
    this.vectorSuffix = vectorSuffix;
    this.rasterPrefix = rasterPrefix;
    this.rasterSuffix = rasterSuffix;
    this.samplePrefix = samplePrefix;
    this.sampleSuffix = sampleSuffix;
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
    return list.get(id);
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

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private LBoard board;

    private Map<String, LObject> list;
    private Set<Kind> kinds;

    private String version;
    private String vectorPrefix, vectorSuffix;
    private String rasterPrefix, rasterSuffix;
    private String samplePrefix, sampleSuffix;

    private Builder() {
      list = new TreeMap<>();
      kinds = new TreeSet<>();
    }

    public Builder setBoard(LBoard board) {
      this.board = board;
      return this;
    }
    
    public Builder addObject(LObject object) {
      list.put(object.id, object);
      return this;
    }

    public Builder addKind(Kind kind) {
      kinds.add(kind);
      return this;
    }

    public Builder setVersion(String version) {
      this.version = version;
      return this;
    }

    public Builder setVectorPrefix(String vectorPrefix) {
      this.vectorPrefix = vectorPrefix;
      return this;
    }

    public Builder setVectorSuffix(String vectorSuffix) {
      this.vectorSuffix = vectorSuffix;
      return this;
    }

    public Builder setRasterPrefix(String rasterPrefix) {
      this.rasterPrefix = rasterPrefix;
      return this;
    }

    public Builder setRasterSuffix(String rasterSuffix) {
      this.rasterSuffix = rasterSuffix;
      return this;
    }

    public Builder setSamplePrefix(String samplePrefix) {
      this.samplePrefix = samplePrefix;
      return this;
    }

    public Builder setSampleSuffix(String sampleSuffix) {
      this.sampleSuffix = sampleSuffix;
      return this;
    }

    public List build() {
      return new List(board, list, kinds, version, vectorPrefix, vectorSuffix,
          rasterPrefix, rasterSuffix, samplePrefix, sampleSuffix);
    }
  }
}
