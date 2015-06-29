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

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.lightless.heroscribe.Region;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class Read extends DefaultHandler {
  private List.Builder listBuilder;

  private boolean onBoard;
  private LObject.Builder pieceBuilder;
  private LBoard.Builder boardBuilder;
  private StringBuffer content;

  public Read(File file) throws Exception {
    super();

    listBuilder = List.newBuilder();

    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);

    SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(file, this);
  }

  public List getObjects() {
    return listBuilder.build();
  }

  /* --- */

  public InputSource resolveEntity(String publicId, String systemId) {
    if (publicId.equals("-//org.lightless//HeroScribe Object List 1.5//EN"))
      return new InputSource("DtdXsd/objectList-1.5.dtd");
    else
      return null;
  }

  public void error(SAXParseException e) throws SAXException {
    throw new SAXException(e);
  }

  public void startDocument() {
    content = new StringBuffer();
    onBoard = false;
  }

  public void startElement(String uri, String localName, String qName,
      Attributes attrs) throws SAXException {
    content = new StringBuffer();

    if (qName == "objectList") {
      String version = attrs.getValue("version");
      if (!version.equals(org.lightless.heroscribe.Constants.version))
        throw new SAXException(
            "HeroScribe's and Objects.xml's version numbers don't match.");

      listBuilder
          .setVersion(version)
          .setVectorPrefix(attrs.getValue("vectorPrefix"))
          .setVectorSuffix(attrs.getValue("vectorSuffix"))
          .setRasterPrefix(attrs.getValue("rasterPrefix"))
          .setRasterSuffix(attrs.getValue("rasterSuffix"))
          .setSamplePrefix(attrs.getValue("samplePrefix"))
          .setSampleSuffix(attrs.getValue("sampleSuffix"));
    } else if (qName == "kind") {
      listBuilder.addKind(new Kind(attrs.getValue("id"), attrs.getValue("name")));
    } else if (qName == "board") {
      boardBuilder = LBoard.newBuilder(Integer.parseInt(attrs.getValue("width")),
          Integer.parseInt(attrs.getValue("height")))
          .setBorderDoorsOffset(Float.parseFloat(attrs.getValue("borderDoorsOffset")))
          .setAdjacentBoardsOffset(Float.parseFloat(attrs.getValue("adjacentBoardsOffset")));

      onBoard = true;
    } else if (qName == "object") {
      pieceBuilder = LObject.newBuilder()
          .setId(attrs.getValue("id"))
          .setName(attrs.getValue("name"))
          .setKindId(attrs.getValue("kind"))
          .setDoor(Boolean.valueOf(attrs.getValue("door")).booleanValue())
          .setTrap(Boolean.valueOf(attrs.getValue("trap")).booleanValue())
          .setWidth(Integer.parseInt(attrs.getValue("width")))
          .setHeight(Integer.parseInt(attrs.getValue("height")))
          .setZOrder(Float.parseFloat(attrs.getValue("zorder")))
          .setNote(null);
    } else if (qName == "icon") {
      Icon.Builder iconBuilder = Icon.newBuilder()
          .setPath(attrs.getValue("path"))
          .setXoffset(Float.parseFloat(attrs.getValue("xoffset")))
          .setYoffset(Float.parseFloat(attrs.getValue("yoffset")))
          .setOriginal(Boolean.valueOf(attrs.getValue("original")).booleanValue());

      Region region = Region.parse(attrs.getValue("region"));
      if (onBoard)
        boardBuilder.putIcon(iconBuilder.build(), region);
      else
        pieceBuilder.putIcon(iconBuilder.build(), region);
    } else if (qName == "corridor") {
      if (onBoard) {
        int width, height;
        int left, top;

        width = Integer.parseInt(attrs.getValue("width"));
        height = Integer.parseInt(attrs.getValue("height"));
        left = Integer.parseInt(attrs.getValue("left"));
        top = Integer.parseInt(attrs.getValue("top"));

        if (left + width - 1 > boardBuilder.getWidth() || left < 1
            || top + height - 1 > boardBuilder.getHeight() || top < 1)
          throw new SAXException("Corridors: out of border");

        for (int i = 0; i < width; i++)
          for (int j = 0; j < height; j++)
            boardBuilder.setCorridor(i + left, j + top);
      }
    }
  }

  public void characters(char[] ch, int start, int length) {
    content.append(ch, start, length);
  }

  public void endElement(String uri, String localName, String qName)
      throws SAXException {
    if (qName == "board") {
      if (!boardBuilder.hasIconsForAllRegions())
        throw new SAXException("There should be both icons for each board.");

      listBuilder.setBoard(boardBuilder.build());

      onBoard = false;
    } else if (qName == "object") {
      if (!pieceBuilder.hasIconsForAllRegions())
        throw new SAXException("There should be both icons for each object.");

      LObject piece = pieceBuilder.build();
      listBuilder.addObject(piece);
    } else if (qName == "note") {
      pieceBuilder.setNote(new String(content));
    }
  }

  public void endDocument() {
    content = null;
    pieceBuilder = null;
  }
}
