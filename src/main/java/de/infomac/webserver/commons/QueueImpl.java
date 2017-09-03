/*
 * $Id: QueueImpl.java,v 1.1 2006/10/23 22:02:01 matteo Exp $
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


import java.util.LinkedList;

public class QueueImpl implements Queue {
    LinkedList queue = new LinkedList();

    private int capacity;

    public QueueImpl(int capacity) {
        this.capacity = capacity;
    }

    public void add(Object obj) throws QueueFullException{

        while (queue.size() >= capacity) {
            throw new QueueFullException();
        }

        queue.addLast(obj);
    }

    public void addImmediately(Object obj)
            throws QueueFullException {

        if (queue.size() >= capacity)
            throw new QueueFullException();
        queue.addLast(obj);
    }

    public Object dequeue() {
       
        if (isEmpty())
            return null;

        Object ret = queue.removeFirst();
        
        return ret;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /* (non-Javadoc)
     * @see org.ceruti.server.Queue#clear()
     */
    public void clear() {
       queue.clear();
    }

}