/*
 * Created on 13.08.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.server;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import de.infomac.webserver.http.BadRequest;
import de.infomac.webserver.http.HTTPRequest;

/**
 * @author matteo
 *
 */
public class FastConnectionProcessor implements HTTPConnectionProcessor {

    private static Category logger = Logger.getInstance(FastConnectionProcessor.class);

    private Socket socket;
    private Context context;
    
    
    public String toString(){
        return "[HTTPConnectionProcessor-" + hashCode() + " " + socket.getInetAddress().getHostName() + ",idle=" + getIdleTime()/1000 + "s]";
    }
    
    public FastConnectionProcessor(){
        
    }
    
    /* (non-Javadoc)
     * @see org.ceruti.server.HTTPConnectionProcessor#getIdleTime()
     */
    public long getIdleTime() {
        return idle_since == 0 ? 0 : System.currentTimeMillis() - idle_since;
    }

    /* (non-Javadoc)
     * @see org.ceruti.server.HTTPConnectionProcessor#close()
     */
    public void close() {
        done = true;
        
        
        synchronized(lock){
            
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.ceruti.server.HTTPConnectionProcessor#init(java.net.Socket, org.ceruti.server.ServerContext)
     */
    public void init(Socket connectionSocket, Context context) {
        this.socket = connectionSocket;
        this.context = context;
        
        
        reqresp.setContext(context);
        reqresp.setSocket(socket);
    }

    /* (non-Javadoc)
     * @see org.ceruti.pool.Poolable#activate()
     */
    public void activate() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.ceruti.pool.Poolable#passivate()
     */
    public void passivate() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.ceruti.pool.Poolable#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub

    }

    private final Object lock = new Object();
    
    private final RequestResponder reqresp = new RequestResponder();
    
    private boolean done = false;
    
    long idle_since = System.currentTimeMillis();
    
    /*private class Consumer implements Runnable {
        
        public HTTPRequest request;
        public long idle_since = System.currentTimeMillis();
        
        public void run() {
            synchronized(lock){
                reqresp.process(request,false);
                
                idle_since = System.currentTimeMillis();
            }
        }
        
    }
    
    private Consumer consumer;
    
    
    private Queue queue;
    */
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        context.addHTTPConnection(this);
        
        
//      instantly fill the SynchronizedQueue
        while (!socket.isClosed() && !done) {
           
                try {
                    HTTPRequest areq = RequestResponder.getRequest(socket);
                    idle_since = 0;
                    reqresp.process(areq,false);
                    idle_since = System.currentTimeMillis();
                    
                    
                } catch (IOException e) {
                    logger.error(e);
                    done = true;
                } catch (BadRequest e) {
                    logger.error(e);
                    done = true;
                }
         
        }
        
        //do not close, while answering a request
        synchronized (lock){
	        try {
	            socket.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }
        
        context.removeHTTPConnection(this);
    }

}
