/*
 * Created on 01.08.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.server;

import java.io.IOException;
import java.net.Socket;



import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import de.infomac.webserver.commons.Poolable;
import de.infomac.webserver.commons.Queue;
import de.infomac.webserver.commons.QueueFullException;
import de.infomac.webserver.commons.QueueImpl;
import de.infomac.webserver.http.BadRequest;
import de.infomac.webserver.http.HTTPRequest;

/**
 * @author matteo
 *
 * works, but is too complicated
 */
public class HTTPConnectionProcessorImpl implements Runnable , HTTPConnectionProcessor{

    private static Category logger = Logger.getInstance(HTTPConnectionProcessorImpl.class);

    private Socket socket;

    private boolean done = false;
    
    private long idle_since;

    //how many requests will be answered thru one connection
    private int keepAlive = 5;

    private int qlen = 10;


    private Context context;
    
    final RequestResponder reqresp = new RequestResponder();
    
    private static int instances = 0;
    public HTTPConnectionProcessorImpl() {
        logger.info("HTTPConnectionProcessorImpl() " + ++instances);  
    }

    public HTTPConnectionProcessorImpl(Socket socket,Context ctx) {
        this();
        init(socket,ctx);
    }
    
    public void init(Socket socket,Context ctx){
        this.socket = socket;
        this.context = ctx;
        this.keepAlive = Integer.parseInt(ctx.getProperties().getProperty("keep_alive"));
        this.reqresp.setContext(ctx);
        this.reqresp.setSocket(socket);
        this.idle_since = System.currentTimeMillis();
        this.queue.clear();
        this.done = false;
    }
    
    public void passivate(){
        this.socket = null;
        this.context = null;
        this.queue.clear();
        this.done = true;
        this.reqresp.close();
    }
    
    public void activate(){
       
    }
    

    /* (non-Javadoc)
     * @see org.ceruti.pool.Poolable#destroy()
     */
    public void destroy() {
        queue.clear();
    }
 
    
    private final Queue queue = new QueueImpl(qlen);
    private final Object lock = new Object();
    
    public String toString(){
        return "[HTTPConnectionProcessor-" + hashCode() + " " + socket.getInetAddress().getHostName() + ",idle=" + getIdleTime()/1000 + "s]";
    }
    
    private Runnable consumer = new Runnable() {
        public void run() {
         
            synchronized (lock) {
                while (!socket.isClosed() && !done) {
                    
                    HTTPRequest req = null;
                    synchronized (queue) {
                        while (queue.isEmpty() && !done) {
                            
                            
                            try {
                                queue.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                        
                        req = (HTTPRequest) queue.dequeue();
                        if(req != null)
                            logger.debug("dequeued: " + req.getRequestURI());
                        queue.notify();
                    }
                    
                    
                    
                    
                    if (req != null) {
                        
                        reqresp.process(req, false);
                        if(queue.isEmpty())
                            idle_since = System.currentTimeMillis();
                        
                    }
                    
                    
                    
                    
                }
                
                logger.debug("consumer done!");
            }
        }
    };
    
    public void run() {
        
        
        context.addHTTPConnection(this);
        
        
        int req_count = 0;
        
        //activate Consumer-Task
        context.getThreadPool().assign(consumer);

        //instantly fill the SynchronizedQueue
        while (!socket.isClosed() && !done) {
            try {
                HTTPRequest areq = RequestResponder.getRequest(socket);
                
                synchronized(queue){
                    queue.add(areq);
                    logger.debug("added: " + areq.getRequestURI());
                    req_count++;
                    queue.notify();
                    idle_since = 0;
                }
                
                
                if (req_count >= keepAlive){
                    done = true;
                    logger.info("keepAlive + (" +  keepAlive + ") exceeded");
                    synchronized(queue){
                        queue.notify();
                    }
                }

            } catch (IOException e) {
                if(!done){
                    logger.error("I/O-Exception", e);
                    done = true;
                }
                synchronized(queue){
                	queue.notify();
                }
                

            } catch (BadRequest e) {
                logger.info("Bad-Request", e);
                done = true;
                
                synchronized(queue){
                	queue.notify();
                }

            } catch (QueueFullException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 

        }

        //here we must wait until the consumer thread is done (the request currently beeing processed
        long time = System.currentTimeMillis();
        synchronized (lock) {
            logger.debug("waited for consumer "
                    + (System.currentTimeMillis() - time) + "msec");
        }

        try {
            logger.debug("closing socket");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if(!queue.isEmpty()){
            logger.error("queue is not empty!");
        }
        
        logger.debug("req_count: " + req_count);
        
        context.removeHTTPConnection(this);
        
        
        if(!context.getProcessorPool().put(this)){
            destroy();
        }
    }


    
    /* (non-Javadoc)
     * @see org.ceruti.server.HTTPConnectionProcessor#close()
     */
    public void close() {
        done = true;
        logger.debug(this + ".close()");
        synchronized(queue){
            queue.notify();
        }
        
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
     * @see org.ceruti.server.HTTPConnectionProcessor#getIdleTime()
     */
    public long getIdleTime() {
        
        return idle_since == 0 ? 0 : System.currentTimeMillis() - idle_since;
    }


}