/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ramadda.repository.client;

/**
 *
 * @author xuqing
 */
public class EntryFile {

    public String entryName;
    public String entryDescription;
    public String parent;
    public String filePath;
    public String north="";
    public String south="";
    public String west="";
    public String east="";

    public EntryFile(String entryName, String entryDescription,
            String parent, String filePath) {
        this.entryName = entryName;
        this.entryDescription = entryDescription;
        this.parent = parent;
        this.filePath = filePath;
    }

    public EntryFile(String entryName, String entryDescription,
            String parent, String filePath, String north, String south, String west, String east) {
        this.entryName = entryName;
        this.entryDescription = entryDescription;
        this.parent = parent;
        this.filePath = filePath;
        this.east = east;
        this.west = west;
        this.north = north;
        this.south = south;
    }

    public void setRange(String north, String south, String west, String east) {
        this.east = east;
        this.west = west;
        this.north = north;
        this.south = south;
    }
}
