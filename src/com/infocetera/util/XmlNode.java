/*
 *
 * 
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/**
 * (C) 1999-2002  WTS Systems, L.L.C.
 *   All rights reserved
 */



package com.infocetera.util;


import java.awt.Color;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * Class XmlNode _more_
 *
 *
 * @author IDV Development Team
 * @version $Revision: 1.3 $
 */
public class XmlNode {

    /** _more_ */
    public static final int TYPE_DOCUMENT = 0;

    /** _more_ */
    public static final int TYPE_ELEMENT = 1;

    /** _more_ */
    public static final int TYPE_TEXT = 2;

    /** _more_ */
    XmlNode parent;

    /** _more_ */
    int type;

    /** _more_ */
    String tag;

    /** _more_ */
    Hashtable attributes;

    /** _more_ */
    Vector children = new Vector();

    /** _more_ */
    String value;

    /**
     * _more_
     *
     * @param value _more_
     */
    public XmlNode(String value) {
        type       = TYPE_TEXT;
        this.value = value;
        this.tag   = "";
    }

    /**
     * _more_
     *
     * @param tag _more_
     * @param attributes _more_
     */
    public XmlNode(String tag, Hashtable attributes) {
        this(TYPE_ELEMENT, tag, attributes);
    }

    /**
     * _more_
     *
     * @param type _more_
     * @param tag _more_
     */
    public XmlNode(int type, String tag) {
        this(type, tag, null);
    }

    /**
     * _more_
     *
     * @param type _more_
     * @param tag _more_
     * @param attributes _more_
     */
    public XmlNode(int type, String tag, Hashtable attributes) {
        this.type       = type;
        this.attributes = ((attributes == null)
                           ? new Hashtable()
                           : attributes);
        this.tag        = tag;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public XmlNode copy() {
        XmlNode newNode = new XmlNode(type, tag,
                                      (Hashtable) attributes.clone());
        Vector newChildren = new Vector();
        for (int i = 0; i < size(); i++) {
            XmlNode newChild = get(i).copy();
            newChild.parent = newNode;
            newChildren.addElement(newChild);
        }
        newNode.children = newChildren;
        return newNode;
    }

    /**
     * _more_
     *
     * @param t _more_
     *
     * @return _more_
     */
    public boolean tagEquals(String t) {
        if (tag == null) {
            return false;
        }
        return tag.equals(t);
    }

    /**
     * _more_
     *
     * @param parent _more_
     */
    public void setParent(XmlNode parent) {
        this.parent = parent;
    }

    /**
     * _more_
     *
     * @param xml _more_
     *
     * @return _more_
     */
    public static XmlNode parse(String xml) {

        XmlNode   root                    = new XmlNode(TYPE_DOCUMENT,
                                                "root");

        final int STATE_LOOKINGFORTAG     = 0;
        final int STATE_LOOKINGFORTAGNAME = 1;
        final int STATE_INTAG             = 2;


        XmlNode   currentParent           = root;

        char[]    chars                   = xml.toCharArray();
        int       state                   = STATE_LOOKINGFORTAG;
        boolean   lastCharSlash           = false;
        String    attrs                   = "";
        String    tag                     = null;
        boolean   gotTagName              = false;
        boolean   inQuote                 = false;

        String    content                 = "";
        boolean   contentNonWhite         = false;
        int       currentChar             = 0;
        String    errorMsg                = null;
        try {
            for (currentChar = 0;
                    (errorMsg == null) && (currentChar < chars.length);
                    currentChar++) {
                char c = chars[currentChar];
                switch (state) {

                  case STATE_LOOKINGFORTAG : {
                      //We are either at the close tag of the currentParent
                      //or an open tag of the next child
                      if (c != '<') {
                          if ( !Character.isWhitespace(c)) {
                              contentNonWhite = true;
                          }
                          content += c;
                          break;
                      }

                      //Add  a text node if we have non-whitespace content
                      if ((content.length() > 0) && contentNonWhite) {
                          XmlNode textNode = new XmlNode(content);
                          currentParent.appendChild(textNode);
                      }
                      contentNonWhite = false;
                      content         = "";
                      //Take care of spaces after the <
                      currentChar = nextNonWhitespace(chars, currentChar);

                      //Are we at the close tag of the current parent
                      if (peek(chars, currentChar + 1) != '/') {
                          state = STATE_LOOKINGFORTAGNAME;
                          tag   = null;
                          break;
                      }

                      currentChar = incrBy(chars, currentChar, 2);  //Point to the  char after the '/'
                      currentChar = nextNonWhitespace(chars, currentChar);  //Eat whitespace
                      //Make sure this close tag matches the open tag
                      String parentTag = currentParent.tag;
                      int    tagLength = parentTag.length();
                      if (tagLength > (chars.length - currentChar)) {
                          errorMsg =
                              "Error: Unexpected end of input. Could not find close tag for: "
                              + parentTag;
                          break;
                      }
                      String closeTag = "";
                      for (int j = 0; j < tagLength; j++) {
                          closeTag += chars[currentChar];
                          if (parentTag.charAt(j) != chars[currentChar]) {
                              currentChar++;
                              while (peek(chars, currentChar) != '>') {
                                  closeTag += chars[currentChar];
                                  currentChar++;
                              }
                              errorMsg = "Error: unexpected close tag:"
                                         + closeTag + " for tag:" + parentTag;
                              break;
                          }
                          currentChar++;
                      }
                      if (errorMsg != null) {
                          break;
                      }
                      currentChar = nextNonWhitespace(chars, currentChar);

                      if (chars[currentChar] != '>') {
                          while (peek(chars, currentChar) != '>') {
                              closeTag += chars[currentChar];
                              currentChar++;
                          }
                          errorMsg = "Error: unexpected close tag:"
                                     + closeTag + " for tag:" + parentTag;
                      }
                      if (errorMsg != null) {
                          break;
                      }
                      currentParent = currentParent.parent;
                      state         = STATE_LOOKINGFORTAG;
                      break;
                  }

                  case STATE_LOOKINGFORTAGNAME : {
                      if (Character.isWhitespace(c)) {
                          break;
                      }
                      if (c == '>') {
                          errorMsg = "found \">\" when looking for tag name";
                          break;
                      }
                      if ((c == '!') && (chars.length > (currentChar + 3))
                              && ((chars[currentChar + 1] == '-')
                                  && (chars[currentChar + 2] == '-'))) {
                          boolean foundClose = false;
                          for (int j = currentChar + 1; j < chars.length;
                                  j++) {
                              if ((chars.length > (j + 3))
                                      && ((chars[j] == '-')
                                          && (chars[j + 1] == '-')
                                          && (chars[j + 2] == '>'))) {
                                  currentChar = j + 3;
                                  foundClose  = true;
                                  state       = STATE_LOOKINGFORTAG;
                                  break;
                              }
                          }
                          if ( !foundClose) {
                              errorMsg = "Failed to find close comment tag";
                          }
                          break;
                      }

                      state      = STATE_INTAG;
                      gotTagName = false;
                      tag        = "" + c;
                      attrs      = "";
                      inQuote    = false;
                      break;
                  }

                  case STATE_INTAG : {
                      if ( !inQuote
                              && ((c == '>')
                                  || ((c == '/')
                                      && (peek(chars, currentChar + 1)
                                          == '>')))) {
                          state = STATE_LOOKINGFORTAG;
                          XmlNode newNode =
                              new XmlNode(TYPE_ELEMENT, tag,
                                          XmlNode.parseAttributes(attrs));
                          currentParent.appendChild(newNode);
                          if (c == '>') {
                              currentParent = newNode;
                          } else {
                              currentChar++;
                          }
                          break;
                      }
                      if (gotTagName && (c == '"')) {
                          inQuote = !inQuote;
                      }
                      if (Character.isWhitespace(c)) {
                          if (gotTagName) {
                              attrs += c;
                          } else {
                              gotTagName = true;
                          }
                      } else {
                          if (gotTagName) {
                              attrs += c;
                          } else {
                              tag += c;
                          }
                      }
                      break;
                  }
                }
            }
        } catch (IllegalStateException ise) {
            errorMsg = ise.getMessage();
        }

        if (state != STATE_LOOKINGFORTAG) {
            if (inQuote) {
                errorMsg =
                    "Error: Unexpected end of input. Looking for close quote. tag:"
                    + tag + "\nattributes:" + attrs;
            } else {
                errorMsg = "Error: Unexpected end of input. Looking at:"
                           + content;
            }
        }

        if (errorMsg != null) {
            currentChar -= 100;
            if (currentChar < 0) {
                currentChar = 0;
            }
            errorMsg = errorMsg + "\n" + "Xml:" + xml.substring(currentChar);
            error(errorMsg);
        }


        return root;

    }


    /**
     * _more_
     *
     * @param chars _more_
     * @param currentIdx _more_
     *
     * @return _more_
     */
    public static int nextNonWhitespace(char[] chars, int currentIdx) {
        for (int i = currentIdx; i < chars.length; i++) {
            if ( !Character.isWhitespace(chars[i])) {
                return i;
            }
        }
        error("Error: Read to end of input - all whitespace");
        return -1;
    }

    /**
     * _more_
     *
     * @param chars _more_
     * @param idx _more_
     * @param delta _more_
     *
     * @return _more_
     */
    public static int incrBy(char[] chars, int idx, int delta) {
        if ((idx + delta) >= chars.length) {
            error("Error: End of input reached");
        }
        return idx + delta;

    }

    /**
     * _more_
     *
     * @param chars _more_
     * @param idx _more_
     *
     * @return _more_
     */
    public static char peek(char[] chars, int idx) {
        if (idx >= chars.length) {
            error("Error: End of input reached");
        }
        return chars[idx];
    }

    /**
     * _more_
     *
     * @param msg _more_
     */
    public static void error(String msg) {
        throw new IllegalStateException(msg);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getTag() {
        return tag;
    }

    /** _more_ */
    private static String[] raw = { "<", ">", "\"", "&" };

    /** _more_ */
    private static String[] encoded = { "&lt;", "&gt;", "&quot;", "&amp;" };

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public static String encode(String s) {
        if (s == null) {
            return null;
        }

        for (int i = 0; i < raw.length; i++) {
            s = GuiUtils.replace(s, raw[i], encoded[i]);
        }
        return s;
    }

    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    public static String decode(String s) {
        for (int i = 0; i < raw.length; i++) {
            s = GuiUtils.replace(s, encoded[i], raw[i]);
        }
        return s;
    }

    /**
     * _more_
     *
     * @param otherNode _more_
     */
    public void mergeAttributes(XmlNode otherNode) {
        if (otherNode == null) {
            return;
        }
        for (Enumeration keys = otherNode.attributes.keys();
                keys.hasMoreElements(); ) {
            Object k = keys.nextElement();
            attributes.put(k, otherNode.attributes.get(k));
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getAttributeString() {
        if (attributes == null) {
            return "";
        }
        String s = "";
        for (Enumeration keys = attributes.keys(); keys.hasMoreElements(); ) {
            String name  = (String) keys.nextElement();
            String value = (String) attributes.get(name);
            s += " " + name + "=\"" + encode(value) + "\"";
        }
        return s;
    }

    /**
     * _more_
     *
     * @param attrs _more_
     *
     * @return _more_
     */
    public static Hashtable parseAttributes(String attrs) {
        String    attrName              = "";
        String    attrValue             = "";
        Hashtable ht                    = new Hashtable();
        final int STATE_LOOKINGFORNAME  = 0;
        final int STATE_INNAME          = 1;
        final int STATE_LOOKINGFORVALUE = 2;
        final int STATE_INVALUE         = 2;
        int       state                 = STATE_LOOKINGFORNAME;
        attrs = attrs + " ";
        char[]  chars          = attrs.toCharArray();
        boolean gotDblQuote    = false;
        boolean gotSingleQuote = false;
        boolean gotEquals      = false;


        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (state) {

              case STATE_LOOKINGFORNAME : {
                  if ((c == ' ') || (c == '\t')) {
                      break;
                  }
                  attrName  = "" + c;
                  state     = STATE_INNAME;
                  gotEquals = false;
                  break;
              }

              case STATE_INNAME : {
                  //Are we at the end of the name?
                  if ((c == ' ') || (c == '\t') || (c == '=')) {
                      if ( !gotEquals) {
                          gotEquals = (c == '=');
                      }
                      break;
                  }
                  if ((c == '\"') || (c == '\'')) {
                      gotDblQuote    = (c == '\"');
                      gotSingleQuote = (c == '\'');
                      state          = STATE_INVALUE;
                      break;
                  }
                  if (gotEquals) {
                      attrValue += c;
                      state     = STATE_INVALUE;
                      break;
                  }

                  attrName += c;
                  break;
              }

              case STATE_INVALUE : {
                  if ((gotDblQuote && (c == '\"'))
                          || (gotSingleQuote && (c == '\''))
                          || ( !gotDblQuote && !gotSingleQuote
                               && (c == ' '))) {
                      ht.put(attrName.toLowerCase().trim(),
                             decode(attrValue));
                      state     = STATE_LOOKINGFORNAME;
                      attrName  = "";
                      attrValue = "";
                      break;
                  }
                  attrValue += c;
                  break;
              }
            }
        }
        return ht;
    }



    /**
     * _more_
     *
     * @return _more_
     */
    public int size() {
        return children.size();
    }

    /**
     * _more_
     *
     * @param i _more_
     *
     * @return _more_
     */
    public XmlNode get(int i) {
        return (XmlNode) children.elementAt(i);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Vector getChildren() {
        return children;
    }

    /**
     * _more_
     *
     * @param child _more_
     */
    public void appendChild(XmlNode child) {
        children.addElement(child);
        child.setParent(this);
    }

    /**
     * _more_
     *
     * @param child _more_
     */
    public void removeChild(XmlNode child) {
        children.removeElement(child);
    }

    /**
     *  After an XmlNode has been parsed and created you can add attributes
     *
     * @param name _more_
     * @param value _more_
     */
    public void addAttribute(String name, String value) {
        if (attributes == null) {
            attributes = new Hashtable();
        }
        attributes.put(name.trim(), value);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Enumeration getAttributes() {
        return attributes.keys();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public Hashtable getAttributeTable() {
        return attributes;
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getAttributeFromTree(String name) {
        if (attributes == null) {
            return null;
        }
        String attr = (String) attributes.get(name);
        if ((attr == null) && (parent != null)) {
            return parent.getAttributeFromTree(name);
        }
        return attr;
    }


    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     */
    public void setAttribute(String name, String value) {
        if (attributes == null) {
            attributes = new Hashtable();
        }
        attributes.put(name, value);
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public String getAttribute(String name) {
        if (attributes == null) {
            return null;
        }
        return (String) attributes.get(name);
    }




    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getAttribute(String name, boolean dflt) {
        String value = getAttribute(name);
        if (value == null) {
            return dflt;
        }
        return new Boolean(value).booleanValue();
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public Color getAttribute(String name, Color dflt) {
        String value = getAttribute(name);
        if (value == null) {
            return dflt;
        }
        return GuiUtils.getColor(value);
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public int getAttribute(String name, int dflt) {
        String value = getAttribute(name);
        if (value == null) {
            return dflt;
        }
        return new Integer(value).intValue();
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public long getAttribute(String name, long dflt) {
        String value = getAttribute(name);
        if (value == null) {
            return dflt;
        }
        return new Long(value).longValue();
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public double getAttribute(String name, double dflt) {
        String value = getAttribute(name);
        if (value == null) {
            return dflt;
        }
        return new Double(value).doubleValue();
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getAttribute(String name, String dflt) {
        String value = getAttribute(name);
        if (value == null) {
            return dflt;
        }
        return value;
    }





    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public boolean getAttributeFromTree(String name, boolean dflt) {
        String value = getAttributeFromTree(name);
        if (value == null) {
            return dflt;
        }
        return new Boolean(value).booleanValue();
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public Color getAttributeFromTree(String name, Color dflt) {
        String value = getAttributeFromTree(name);
        if (value == null) {
            return dflt;
        }
        return GuiUtils.getColor(value);
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public int getAttributeFromTree(String name, int dflt) {
        String value = getAttributeFromTree(name);
        if (value == null) {
            return dflt;
        }
        return new Integer(value).intValue();
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param dflt _more_
     *
     * @return _more_
     */
    public String getAttributeFromTree(String name, String dflt) {
        String value = getAttributeFromTree(name);
        if (value == null) {
            return dflt;
        }
        return value;
    }


    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isText() {
        return type == TYPE_TEXT;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isElement() {
        return type == TYPE_ELEMENT;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getChildValue() {
        if ( !hasSingleTextNode()) {
            return null;
        }
        return ((XmlNode) children.elementAt(0)).value;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean hasSingleTextNode() {
        if (children.size() != 1) {
            return false;
        }
        return ((XmlNode) children.elementAt(0)).isText();
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        toString(buff, "");
        return buff.toString();
    }

    /**
     * _more_
     *
     * @param tag _more_
     *
     * @return _more_
     */
    public XmlNode getChild(String tag) {
        for (int i = 0; i < children.size(); i++) {
            XmlNode child = (XmlNode) children.elementAt(i);
            if (child.getTag().equals(tag)) {
                return child;
            }
        }
        return null;
    }

    /**
     * _more_
     *
     * @param tag _more_
     * @param copy _more_
     *
     * @return _more_
     */
    public Vector getChildren(String tag, boolean copy) {
        Vector v = new Vector();
        for (int i = 0; i < children.size(); i++) {
            XmlNode child = (XmlNode) children.elementAt(i);
            if (child.getTag().equals(tag)) {
                v.addElement((copy
                              ? child.copy()
                              : child));
            }
        }
        return v;
    }

    /**
     * _more_
     *
     * @param buff _more_
     * @param tab _more_
     */
    public void toString(StringBuffer buff, String tab) {
        if (type == TYPE_DOCUMENT) {
            for (int i = 0; i < children.size(); i++) {
                ((XmlNode) children.elementAt(i)).toString(buff, "");
            }
        } else if (type == TYPE_ELEMENT) {
            buff.append(tab + "<" + tag + getAttributeString());
            if (children.size() == 0) {
                buff.append("/>\n");
            } else {
                if (hasSingleTextNode()) {
                    buff.append(">");
                } else {
                    buff.append(">\n");
                }
                for (int i = 0; i < children.size(); i++) {
                    ((XmlNode) children.elementAt(i)).toString(buff,
                            tab + "  ");
                }
                if ( !hasSingleTextNode()) {
                    buff.append(tab);
                }
                buff.append("</" + tag + ">\n");
            }
        } else if (type == TYPE_TEXT) {
            buff.append(value);
        }
    }

    /**
     * _more_
     *
     * @param name _more_
     *
     * @return _more_
     */
    public static String quote(String name) {
        return "\"" + name + "\"";
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param value _more_
     *
     * @return _more_
     */
    public static String attr(String name, String value) {
        return " " + name + "=" + quote(encode(value));
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param attrs _more_
     * @param body _more_
     *
     * @return _more_
     */
    public static String tag(String name, String attrs, String body) {
        return "<" + name + " " + attrs + ">" + body + "</" + name + ">";
    }

    /**
     * _more_
     *
     * @param name _more_
     * @param attrs _more_
     *
     * @return _more_
     */
    public static String tag(String name, String attrs) {
        return "<" + name + " " + attrs + "/>";
    }


    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        String xml =
            "<t1 foo=\"bar\" name=\"jeff\"><t2 bar=\"foo\"/><t3><t4>some text</t4></t3></t1>";
        StringBuffer buff = new StringBuffer();
        parse(xml).toString(buff, "");
        System.err.println(buff.toString());

    }


}

