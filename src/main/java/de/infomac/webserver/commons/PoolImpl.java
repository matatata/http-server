/*
 * $Id: PoolImpl.java,v 1.1 2006/10/23 22:02:01 matteo Exp $
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
public class PoolImpl implements Pool{

    
    private Queue pool;
    
    public PoolImpl(int capacity){
        pool = new QueueImpl(capacity);
    }
    
	/**
	 * Gets an Object from the pool.
	 * @return A pooled Object or null if no Object is available.
	 */
    public Poolable get(){
        Poolable thing = (Poolable) pool.dequeue();
        if(thing!=null)
            thing.activate();
        
        return thing;
    }
    
    
	/**
	 * Adds an Object to the pool. 
	 * @return True if the object could be added to the pool, otherwise false. 
	 * If the object isn't added to the pool, clients have to dispose and clean it up manually.
	 */
    public boolean put(Poolable obj){
        try {
            pool.add(obj);
            obj.passivate();
            return true;
        } catch (QueueFullException e) {
           
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see org.ceruti.pool.Pool#clear()
     */
    public void clear() {
        Poolable pp;
        while((pp = (Poolable) pool.dequeue()) != null){
            pp.destroy();
        }
        
        pool.clear();
    }
}
