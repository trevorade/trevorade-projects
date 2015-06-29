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

import java.awt.Image;
import java.security.InvalidParameterException;

public class Icon {
  public final String path;
  public final float xoffset, yoffset;
  public final boolean original;
  private Image image;  // Loads later. Will allow to be set once.

  private Icon(String path, float xoffset, float yoffset, boolean original) {
    this.path = path;
    this.xoffset = xoffset;
    this.yoffset = yoffset;
    this.original = original;
    this.image = null;
  }
  
  public void setImage(Image image) {
    if (this.image != null && this.image != image) {
      throw new InvalidParameterException(
          "The icon's image may only be set once.");
    }
    this.image = image;
  }
  
  public Image getImage() {
    return image;
  }

  public static Builder newBuilder() {
    return new Builder();
  }
  
  public static class Builder {
    private String path;
    private float xoffset, yoffset;
    private boolean original;

    private Builder() {}

    public Builder setPath(String path) {
      this.path = path;
      return this;
    }

    public Builder setXoffset(float xoffset) {
      this.xoffset = xoffset;
      return this;
    }

    public Builder setYoffset(float yoffset) {
      this.yoffset = yoffset;
      return this;
    }

    public Builder setOriginal(boolean original) {
      this.original = original;
      return this;
    }

    public Icon build() {
      return new Icon(path, xoffset, yoffset, original);
    }
  }
}