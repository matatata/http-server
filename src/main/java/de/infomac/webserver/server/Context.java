/*
 * Created on 07.08.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import de.infomac.webserver.commons.Pool;
import de.infomac.webserver.threads.ThreadPool;

/**
 * @author matteo
 *
 * TODO comment
 */
public abstract class Context {
    public abstract Properties getProperties();
    public abstract String getServerRoot();
    
    /**
     * 
     * @param strlowercase
     * @return server_root for hostname or null
     */
    protected abstract String getServerRootForHost(String strlowercase);
    
    public void init(){
        
    }
    
    public abstract ThreadPool getThreadPool();
    public abstract Pool getProcessorPool();
    
    
    /**
     * 
     * @param hostname
     * @return serverroot of virtual if configured host or default server root
     */
    public final String getHostRoot(String hostname){
        String vh = getServerRootForHost(hostname.toLowerCase());
        if(vh==null)
            vh =  getServerRoot();
        
        return vh;
    }
    
    private Set conns = Collections.synchronizedSet(new HashSet());
    
    public void addHTTPConnection(HTTPConnectionProcessor c){
        conns.add(c);
    }
    
    public boolean removeHTTPConnection(HTTPConnectionProcessor c){
        return conns.remove(c);
    }
    /**
     * @return
     */
    public Set getHTTPConnections() {
        return conns;
    }
}
