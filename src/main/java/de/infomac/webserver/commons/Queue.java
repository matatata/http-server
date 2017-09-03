/*
 * $Id: Queue.java,v 1.1 2006/10/23 22:02:01 matteo Exp $
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
package de.infomac.webserver.commons;

/**
 * @author matteo
 *
 * TODO comment
 */
public interface Queue {
    void add(Object o) throws QueueFullException;
    Object dequeue();
    boolean isEmpty();
    
    void clear();
}
