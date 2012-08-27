/*
* Copyright 2008-2012 Jeff McWhirter/ramadda.org
*                     Don Murray/CU-CIRES
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of this 
* software and associated documentation files (the "Software"), to deal in the Software 
* without restriction, including without limitation the rights to use, copy, modify, 
* merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
* permit persons to whom the Software is furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies 
* or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
* PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
* OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
* DEALINGS IN THE SOFTWARE.
*/

package org.ramadda.plugins.scheduler;


import org.ramadda.repository.*;
import org.ramadda.repository.auth.*;
import org.ramadda.repository.harvester.*;
import org.ramadda.repository.search.*;
import org.ramadda.repository.type.*;

import org.ramadda.util.HtmlUtils;
import org.ramadda.util.HtmlUtils;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;


import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;
import ucar.unidata.xml.XmlUtil;


import java.io.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.List;
import java.util.regex.*;


/**
 * Provides a top-level API
 *
 */
public class Scheduler extends RepositoryManager implements RequestHandler {
    /**
     *     ctor
     *    
     *     @param repository the repository
     *     @param node xml from api.xml
     *     @param props propertiesn
     *    
     *     @throws Exception on badness
     */
    public Scheduler(Repository repository) throws Exception {
        super(repository);
    }




    /**
     * handle the request
     *
     * @param request request
     *
     * @return result
     *
     * @throws Exception on badness
     */
    public Result processScheduleRequest(Request request) throws Exception {
        StringBuffer sb = new StringBuffer();
        String       base   = getRepository().getUrlBase();
        sb.append(msgHeader("Scheduler"));
        sb.append(HtmlUtils.form(base+"/scheduler/schedule"));
        sb.append(HtmlUtils.formTable());
        sb.append(HtmlUtils.formEntry(msgLabel("# Weeks"),HtmlUtils.input("weeks",request.getString("weeks","12"), HtmlUtils.SIZE_6)));
        sb.append(HtmlUtils.formEntry(msgLabel("# Players"),HtmlUtils.input("numplayers",request.getString("numplayers", "8"), HtmlUtils.SIZE_6)));
        sb.append(HtmlUtils.formEntry("",HtmlUtils.submit("Submit")));
        sb.append(HtmlUtils.formTableClose());
        sb.append(HtmlUtils.formClose());


        if(request.defined("weeks")) {
            schedule(sb, request.get("numplayers",8), request.get("weeks", 12));
        }
        return new Result("Scheduler", sb);
    }


    public void schedule(StringBuffer sb, int numPlayers, int weeks) {
        sb.append("<pre>");
        List<Player> players = new ArrayList<Player>();
        for(int i=0;i<numPlayers;i++) {
            players.add(new Player("Player " + (i+1)));
        }

        for(int week=0;week<weeks;week++) {
            sb.append("Week " + (week+1));
            sb.append("\n");
            schedule(week, players);
            for(Player player: players) {
                if(player.playing) {
                    for(Player otherPlayer: players) {
                        if(!otherPlayer.equals(player)) {
                            otherPlayer.yourPlayingWith(player);
                            player.yourPlayingWith(otherPlayer);
                        }
                    }
                    sb.append("\t"+ player);
                    sb.append("\n");
                }
            }
        }


        for(Player player: players) {
            if(player.playedWith.size()!= numPlayers-1) {
                sb.append ("Player:" + player +" not played with all players:" + player.playedWith);
                sb.append("\n");
            }
        }
        sb.append("\nSummary\n");
        sb.append("Player\t#Games\n");
        for(Player player: players) {
            sb.append(player +"\t" + player.numGamesPlayed);
            sb.append("\n");
        }
        sb.append("</pre>");

    }




    public  void schedule(int week, List<Player> players) {
        List<Player> myPlayers = new ArrayList<Player>(players);
        int maxNumberOfGamesPlayed = 0;
        for(Player player: myPlayers) { 
            player.playing = false;
            maxNumberOfGamesPlayed = Math.max(player.numGamesPlayed, maxNumberOfGamesPlayed);
        }
        int selectedCnt = 0;
        //        System.err.println("week:" + week);
        for(Player player: players) { 
            int weeksSincePlayed = week-player.lastPlayed;
            if(weeksSincePlayed>2) {
                //                System.err.println("  adding:" + player +" " + player.lastPlayed);
                player.setPlaying(week, true);
                myPlayers.remove(player);
                if(selectedCnt++>=4) break;
            }
        }
        if(selectedCnt>=4) return;

        List<Player> playersToPickFrom = new ArrayList<Player>();
        for(Player player: myPlayers) { 
            int weeksSincePlayed = week-player.lastPlayed;
            playersToPickFrom.add(player);
            if(weeksSincePlayed==1) continue;
            playersToPickFrom.add(player);
            if(weeksSincePlayed==2) continue;
            playersToPickFrom.add(player);
        }

        for(Player player: myPlayers) { 
            int diff = maxNumberOfGamesPlayed - player.numGamesPlayed;
            while(diff>0) {
                //System.err.println("weighting " + player + " max=" +maxNumberOfGamesPlayed +"   played:" +player.numGamesPlayed);
                playersToPickFrom.add(player);
                playersToPickFrom.add(player);
                diff--;
            }
        }

        //        System.err.println("pick:" + playersToPickFrom);
        while(selectedCnt<4) {
            int item = new Random().nextInt(playersToPickFrom.size());
            Player player = playersToPickFrom.get(item);
            while(playersToPickFrom.remove(player)) {
            }
            player.setPlaying(week, true);
            selectedCnt++;
        }
    }



    public static class Player {
        
        String name;
        boolean playing = false;
        int lastPlayed=-1;
        HashSet<Player> playedWith = new HashSet<Player>();
        int numGamesPlayed = 0;

        public Player(String name) {
            this.name = name;
        }


        public void setPlaying(int week, boolean playing) {
            lastPlayed = week;
            this.playing  = playing;
            numGamesPlayed ++;
        }

        public void yourPlayingWith(Player player) {
            playedWith.add(player);
        }

        public int hashCode() {
            return name.hashCode();
        }


        public boolean equals(Object o) {
            if(!(o instanceof Player)) return false;
            Player that = (Player) o;
            return that.name.equals(this.name);
        }

        public String toString() {
            return name;
        }

    }



}
