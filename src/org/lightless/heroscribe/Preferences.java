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

package org.lightless.heroscribe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.lightless.heroscribe.helper.OS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public class Preferences extends DefaultHandler {
  private File ghostscriptExec;
  private Region region;
  private HashMap<String, Integer> numberOwnedByLObjectId;

  public Preferences() {
    super();

    ghostscriptExec = new File("");
    region = Region.EUROPE;
    numberOwnedByLObjectId = new HashMap<>();

    if (OS.isWindows()) {
      File base = new File("c:\\gs\\");

      if (base.isDirectory()) {
        File[] files = base.listFiles();

        for (int i = 0; i < files.length; i++) {
          if (files[i].isDirectory()
              && new File(files[i], "bin\\gswin32c.exe").isFile()) {
            ghostscriptExec = new File(files[i], "bin\\gswin32c.exe");

            break;
          }
        }
      }
    } else if (new File("/usr/bin/gs").isFile()) {
      ghostscriptExec = new File("/usr/bin/gs");
    }
  }

  public Preferences(File file) {
    this();

    if (file.isFile()) {
      try {
        SAXParserFactory factory = SAXParserFactory.newInstance();

        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(file, this);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public File getGhostscriptExec() {
    return ghostscriptExec;
  }

  public void setGhostscriptExec(File ghostscriptExec) {
    this.ghostscriptExec = ghostscriptExec;
  }

  public Region getRegion() {
    return region;
  }

  public void setRegion(Region region) {
    this.region = region;
  }

  public @Nullable Integer getNumOwned(@NotNull String lObjectId) {
    return numberOwnedByLObjectId.get(lObjectId);
  }

  public void setNumOwned(@NotNull String lObjectId, @Nullable Integer numOwned) {
    if (numOwned == null) {
      numberOwnedByLObjectId.remove(lObjectId);
    } else {
      numberOwnedByLObjectId.put(lObjectId, numOwned);
    }
  }

  /* Read XML */

  @Override
  public void startElement(String uri, String localName, String qName,
      Attributes attrs) throws SAXException {
    if (qName == "ghostscript") {
      File file = new File(attrs.getValue("path"));

      if (file.isFile()) {
        ghostscriptExec = file;
      }
    }
    if (qName == "region") {
      region = Region.parse(attrs.getValue("value"));
    }
    if (qName == "owned") {
      String id = attrs.getValue("id");
      int numOwned = Integer.parseInt(attrs.getValue("value"));
      numberOwnedByLObjectId.put(id, numOwned);
    }
  }

  /* Write XML */

  public void write() throws Exception {
    try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
        Constants.preferencesFile)))) {
      out.println("<?xml version=\"1.0\"?>");
      out.println("<preferences>");

      out.printf("<ghostscript path=\"%s\"/>\n\n", ghostscriptExec
          .getAbsoluteFile().toString().replaceAll("\"", "&quot;"));

      out.printf("<region value=\"%s\"/>\n\n", region.toString());

      for (String lObjectId : numberOwnedByLObjectId.keySet()) {
        out.printf("<owned id=\"%s\" value=\"%d\"/>\n", lObjectId,
            numberOwnedByLObjectId.get(lObjectId));
      }

      out.println("</preferences>");
    }
  }
}