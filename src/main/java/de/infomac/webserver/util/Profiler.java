/*
 * $Id: Profiler.java,v 1.2 2006/10/29 22:31:12 matteo Exp $
 *
 * http://www.ceruti.org/software/
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package de.infomac.webserver.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author matteo
 * 
 * TODO comment
 */
public class Profiler {
    private Map timingMap = new HashMap();

    public void startTiming(String name) {
        timingMap.put(name, new Long(System.currentTimeMillis()));
    }

    public long getTime(String name) {
        Long begin = (Long) timingMap.get(name);
        if (begin == null)
            return -1;
        return System.currentTimeMillis() - begin.longValue();
    }

    public long stopTiming(String name, java.io.PrintStream dest) {
        long t = getTime(name);
        if (dest!=null && t < 0) {
            dest.println("(" + name + " is no valid timing.)");
            return -1;
        }

        timingMap.remove(name);
        if(dest!=null)
        	dest.println("(" +name + " took approx. " + t + "ms.)");
        
        return t;
    }

    public long stopTiming(String name) {
        return stopTiming(name, null);
    }

}