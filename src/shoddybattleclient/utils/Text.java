/* Text.java
 *
 * Created April 22, 2009
 *
 * This file is a part of Shoddy Battle.
 * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, visit the Free Software Foundation, Inc.
 * online at http://gnu.org.
 */

package shoddybattleclient.utils;
import java.io.*;
import java.util.*;
import shoddybattleclient.BattleField;
import shoddybattleclient.Preference.HealthDisplay;

/**
 * Reads and allows for retrieval of text from a .lang file
 * @author ben
 */
public class Text {
    private static List<HashMap<Integer, String>> m_text = new ArrayList<HashMap<Integer, String>>();

    static {
        // TODO: remove this (it is temporary)
        loadText("english.lang");
    }

    public static String getText(int cat, int id, String[] args, BattleField field) {
        Map<Integer, String> map = m_text.get(cat);
        if ((map == null) || !map.containsKey(id)) {
            System.out.println("Missing message: " + cat + "," + id);
            return "";
        }
        String text = map.get(id);
        for (int i = 0; i < args.length; i++) {
            String match = "$" + (i + 1);
            text = text.replace(match, args[i]);
        }

        text = parse(text, field);

        return text;
    }

    public static String parse(String text, BattleField field) {
        int pos;
        String match = "${";
        while ((pos  = text.indexOf(match)) >= 0) {
            int pos2 = text.indexOf("}", pos);
            if (pos2 < 0) break;
            String sub = text.substring(pos + match.length(), pos2);
            String[] parts = sub.split(",");
            String replacement = getText(Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]));
            text = text.substring(0, pos) + replacement + text.substring(pos2 + 1);
        }

        match = "$p{";
        if (field != null) {
            while ((pos = text.indexOf(match)) >= 0) {
                int pos2 = text.indexOf("}", pos);
                if (pos2 < 0) break;
                String sub = text.substring(pos + match.length(), pos2);
                String[] parts = sub.split(",");
                int party = Integer.parseInt(parts[0]);
                String name = field.getName(party,
                        Integer.parseInt(parts[1]));
                name = formatName(name, field.getParty() == party);
                text = text.substring(0, pos) + name + text.substring(pos2 + 1);
            }
        }
        return text;
    }
    
    public static String formatName(String name, boolean ally) {
        String style = ally ? "ally" : "enemy";
        name = "<font class='pokemon-" + style + "'>" + name + "</font>";
        return name;
    }

    public static String formatTrainer(String name, int us, int party) {
        String style;
        if (party == us) {
            style = "self";
        } else if (party == (1 - us)) {
            style = "opponent";
        } else {
            style = "others";
        }
        return "<font class='" + style + "'>" + name + "</font>";
    }

    //retarded html 3.2
    public static String addClass(String txt, String cls) {
        return "<b class='" + cls + "'>" + txt + "</b>";
    }

    public static String formatHealthChange(int delta, int denom, HealthDisplay disp) {
        String str;
        if (disp.equals(HealthDisplay.EXACT)) {
            str = delta + "/" + denom;
        } else if (disp.equals(HealthDisplay.PERCENT)) {
            str = (delta * 100 / denom) + "%";
        } else {
            str = delta + "/" + denom + " (" + (delta * 100 / denom) + "%)";
        }
        return "<font class='health-change'>" + str + "</font>";
    }

    //strips HTML tags from a string
    public static String stripTags(String s) {
        return s.replaceAll("\\<.*?\\>", "");
    }

    public static String getText(int cat, int id, String[] args) {
        return getText(cat, id, args, null);
    }

    public static String getText(int cat, int id) {
        return getText(cat, id, new String[0], null);
    }

    public static void loadText(String file) {
        Scanner s = null;
        InputStream is = Text.class.getResourceAsStream("/shoddybattleclient/languages/" + file);

        try {
            s = new Scanner(is);
        } catch (Exception e) {
            System.err.println("Failed to load language file");
            return;
        }
        int category =  -1;
        int lineNumber = 0;
        while (s.hasNextLine()) {
            lineNumber++;
            String line = s.nextLine();
            line = line.trim();
            if ("".equals(line)) continue;
            
            int pos = line.indexOf("//");
            if (pos == 0) continue;
            if (pos != -1) {
                line = line.substring(0, pos - 1);
            }
            if (line.charAt(0) == '[') {
                if (line.indexOf(']') == -1) {
                    informError(lineNumber);
                    continue;
                }
                category++;
                m_text.add(new HashMap<Integer, String>());
                continue;
            }
            pos = line.indexOf(':');
            if (pos == -1) {
                informError(lineNumber);
                continue;
            }
            int id;
            try {
                id = Integer.parseInt(line.substring(0, pos));
            } catch (NumberFormatException e) {
                informError(lineNumber);
                continue;
            }
            String str = line.substring(pos + 1).trim();
            m_text.get(category).put(id, str);
        }
    }

    private static void informError(int line) {
        System.out.println("Syntax error on line " + line);
    }

    public static String formatDateDifference(int date) {
        long longNow = System.currentTimeMillis();
        int now = (int)(longNow / 1000);
        int diff = date - now;
        int years = diff / 31536000;
        diff -= years * 31536000;
        int days = diff / 86400;
        diff -= days * 86400;
        int hours = diff / 3600;
        diff -= hours * 3600;
        int minutes = diff / 60;
        StringBuilder sb = new StringBuilder();
        boolean y, d, h, m;
        y = d = h = m = false;
        if (years > 0) {
            sb.append(years).append(" year");
            if (years > 1) sb.append("s");
            sb.append(" ");
            y = true;
        }
        if (days > 0) {
            sb.append(days).append(" day");
            if (days > 1) sb.append("s");
            sb.append(" ");
            d = true;
        }
        if ((hours > 0) && !y) {
            sb.append(hours).append(" hour");
            if (hours > 1) sb.append("s");
            sb.append(" ");
        }
        if (!y && !d && !h && (minutes == 0)) minutes = 1;
        if ((minutes > 0) && !y && !d) {
            sb.append(minutes).append(" minute");
            if (minutes > 1) sb.append("s");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(Text.formatDateDifference(1315949113));
    }

}
