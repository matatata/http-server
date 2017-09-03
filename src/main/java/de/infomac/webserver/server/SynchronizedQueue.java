/*
 * Created on 10.08.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.server;

import java.util.LinkedList;

import de.infomac.webserver.commons.Queue;
import de.infomac.webserver.commons.QueueFullException;

class SynchronizedQueue implements Queue {
    LinkedList queue = new LinkedList();

    private int capacity;

    public SynchronizedQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void add(Object obj) throws QueueFullException{

        while (queue.size() >= capacity) {
            try {
                wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        queue.addLast(obj);
        notifyAll();
    }

    public synchronized void addImmediately(Object obj)
            throws QueueFullException {

        if (queue.size() >= capacity)
            throw new QueueFullException();
        queue.addLast(obj);
        notifyAll();
    }

    public synchronized Object dequeue() {
        while (queue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (isEmpty())
            return null;

        Object ret = queue.removeFirst();
        notifyAll();
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