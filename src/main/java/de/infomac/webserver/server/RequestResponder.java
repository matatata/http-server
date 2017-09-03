/*
 * Created on 26.06.2004 by matteo Mat. Nr. 953982
 */
package de.infomac.webserver.server;




import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;




import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import de.infomac.webserver.commons.Assert;
import de.infomac.webserver.commons.AssertionFailed;
import de.infomac.webserver.http.BadRequest;
import de.infomac.webserver.http.HTTPParseError;
import de.infomac.webserver.http.HTTPRequest;
import de.infomac.webserver.http.HTTPResponse;
import de.infomac.webserver.http.HTTPVersion;
import de.infomac.webserver.http.InvalidMessage;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.MessageBodyException;
import de.infomac.webserver.http.Unimplemented;
import de.infomac.webserver.http.UnsupportedMethod;
import de.infomac.webserver.http.impl.FileMessageBody;
import de.infomac.webserver.http.impl.HTTPRequestValidatorBase;
import de.infomac.webserver.http.impl.PartialContentMessageBodyBase;


public class RequestResponder implements Runnable {

    private static Category logger = Logger.getInstance(RequestResponder.class);


    
    private Context context;
    
    
    private java.net.Socket socket; //the connection's sockets

    public RequestResponder() {
      logger.info("RequestResponder()");  
    }
    
    /**
     * @param connectionSocket
     */
    public RequestResponder(Socket connectionSocket,Context context) {
        this();
        socket = connectionSocket;
        this.context = context;        
    }

    
    public void close(){
        socket = null;
        context = null;
    }
    
    public void setSocket(Socket sock){
        this.socket = sock;
    }
    
    public void setContext(Context ctx){
        this.context = ctx;
    }
    
    public boolean isConnected(){
        return socket != null && !socket.isClosed();
    }
    
    
    public static HTTPRequest parseRequest(Socket socket) throws IOException, HTTPParseError{
        return de.infomac.webserver.http.impl.HTTPRequest.parseHTTPRequest(socket
                .getInputStream());
    }

    /**
     * Parses a Request from the socket. On failure it sends the necessary
     * Server-Messages. but you'll have to close the connection if necessary
     * 
     * @return null on Failure
     * @throws IOException
     * @throws HTTPParseError
     * @throws BadRequest
     */
    public static HTTPRequest getRequest(Socket socket) throws IOException, BadRequest  {
       try {
            return parseRequest(socket);
        } catch (HTTPParseError e) {
           
                try {
                    de.infomac.webserver.http.impl.HTTPResponse.get400(HTTPVersion.VERSION_1_0,
                            e.getMessage()).copyToOutputStream(
                            socket.getOutputStream());
                } catch (IOException e1) {
                    logger.debug(e1);
                   
                  } catch (MessageBodyException e1) {
                    logger.debug(e1);
                    
                  }
                
                  throw new BadRequest(e); //throw the original ex
       
        } catch (Unimplemented ui) {
            logger.error(ui);

            try {
                de.infomac.webserver.http.impl.HTTPResponse.get501(HTTPVersion.VERSION_1_0,
                        ui.getMessage()).copyToOutputStream(
                        socket.getOutputStream());
            } catch (IOException e1) {
                logger.debug(e1);
                
              } catch (MessageBodyException e1) {
                logger.debug(e1);
                
              }
            
              throw ui; //throw the original ex
        }

    }
    
    public void process(HTTPRequest request,boolean close_connection){
        
        logger.debug("processing " + request.getRequestURI());
        try {
            HTTPResponse response = null;
            

            try {
                response = respond(request);

            } catch (AssertionFailed a) { //an assertions failed: this is a booboo
                logger.fatal(a);
                a.printStackTrace();
                response = de.infomac.webserver.http.impl.HTTPResponse.get500(request.getVersion(),a);
            } catch (Exception e) { //Catch other Exceptions we do not expect
                logger.fatal(e);
                e.printStackTrace();
                response = de.infomac.webserver.http.impl.HTTPResponse.get500(request.getVersion(),e);
            }
            
            if (request.getVersion().isVersion1_1()){
                
                if (request.hasNonEmptyHeader("Connection")
                        && ((String) request.getHeader("Connection"))
                                .equalsIgnoreCase("close"))
                    close_connection = true;
            }
            else { //close connections by default for non 1.1 HTTP-versions
                close_connection = true;
            }
             
            
            
               
            if (request.getVersion().isVersion1_1()) {

                if (close_connection && request.getVersion().isVersion1_1())
                    response.setHeader("Connection", "close");
                
                else if (!response.hasContentLength()
                        && !response.hasTransferEncoding()) {
                    logger.debug("Needed to close connection, although not preferred");
                }
            }
            
            
//
//            try {
//                Thread.sleep((int) (Math.random() * 20000));
//            } catch (InterruptedException e) {
//            }
            
            response.copyToOutputStream(socket.getOutputStream());

            logger.debug("transfer complete " + response.getVersion() + " "
                    + response.getStatusCode() + " " + request.getRequestURI());
            
            

        } catch (IOException e) { //I/O-Error likely a broken pipe
            logger.error("transfer incomplete or failed (" + e.getMessage()
                    + ") " + request.getRequestURI());
            
        } catch (MessageBodyException e) { // The Message-body was not successfully sent/forwarded. This will
            								 // be a problem of the MessageBody-Implementation,
            logger.error("transfer incomplete or failed (" + e.getMessage()
                    + ") " + request.getRequestURI());
            
        } finally {
            if(close_connection){
            		shutdown();
            }
            
            
        }
    }
    
    public void process(boolean close_connection) throws IOException, BadRequest{
        HTTPRequest request = null;

        try {
            request = getRequest(socket);
            
        } catch (IOException e) {
            //If we had I/O problems during receiving a request,
            //we are sorry, but we can't help.
            
            logger.error(e);
            throw e;
        
        } catch (BadRequest e) {
            throw e;
        }

        process(request,close_connection);
        
    }

    /**
     * 
     */
    private void shutdown() {
        try {
            if (socket.getOutputStream() != null)
                socket.getOutputStream().close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e1) {
            }
        }
    }

    public void run() {
        try {
            process(true);
        } catch (IOException e) {
            logger.debug(e);
            shutdown();
        } catch (BadRequest e) {
            logger.debug(e);
            shutdown();
        }
    }

 
    
   
    
    private HTTPResponse respond(HTTPRequest request) {

        
        HTTPRequestValidatorBase val = new HTTPRequestValidatorBase() {
            public void validateMethod(de.infomac.webserver.http.HTTPRequest req)
                    throws UnsupportedMethod {
                if (req.isMethodGET() || req.isMethodPOST())
                    return;

                throw new UnsupportedMethod(req.getMethod()
                        + " is not supported");
            }

        };
        try {
            val.validate(request);
        } catch (InvalidMessage e) {
            return de.infomac.webserver.http.impl.HTTPResponse.get400(request.getVersion(), e
                    .getMessage());
        } catch (UnsupportedMethod e) {
            return de.infomac.webserver.http.impl.HTTPResponse.get501(request.getVersion(), e
                    .getMessage());
        }

        // respond

        HTTPResponse response = null;

        
        response = serve(request);


        //now let's see if the client requests a specific byte-Range (only
        // HTTP/1.1) and check for If-Range
        if (response.getStatusCode() == 200
                && request.getVersion().isVersion1_1()
                && response.getVersion().isVersion1_1()
                && request.getHeader("Range") != null) {

            if (request.getHeader("If-Range") != null
                    && response.hasNonEmptyHeader("ETag")) {
                String etag = (String) response.getHeader("ETag");
                if (!etag.equals(request.getHeader("If-Range"))) {
                    return de.infomac.webserver.http.impl.HTTPResponse.getResponse(request
                            .getVersion(), 412, "Precondition failed");
                }

            }
            //try to satisfy the range-request and modify the response
            response = PartialContentMessageBodyBase.processRangeRequest(
                    (String) request.getHeader("Range"), response);

        }

        return response;
    }

    

 

    /**
     * serve local files, just like a normal HTTP-Server
     * 
     * @param request
     * @return
     */
    private HTTPResponse serve(HTTPRequest request) {
        HTTPResponse response = null;
        try {
            URL url = request.getURL();
            String file = request.getRequestURI();
            if (url == null) {
                //cannot determine a unique url.
                //This means that we do not have a Host-Header and no
                //absoluteURI, so this must be a Good ol' no-proxy HTTP/1.0
                //request wich requests something on localhost.

                //we can assert HTTP/1.0 otherwise the request would not have
                // passed the HTTP-Validator
                Assert.condition(request.getVersion().isVersion1_0());
                Assert.condition(request.getHeader("Host") == null);
            } else { //We have a URL
                //logger.debug("Was able to construct a URL:" + url);
            }

            //for now we serve it locally in both cases
            //Could do some virtual-host stuff
            
            String decoded_file = java.net.URLDecoder.decode(file,"UTF-8");

            //get the default server root
            String server_root = context.getServerRoot();
            
            //override the server_root
            if(request.getHeader("Host") != null){
                server_root = context.getHostRoot((String) request.getHeader("Host"));
            }
            
            MessageBody fbody = new FileMessageBody(server_root
                    + decoded_file);

            //checl If-Match now that we know the file seems to be available
            if (fbody.ETagAvailable() && request.hasNonEmptyHeader("If-Match")
                    && !request.getHeader("If-Match").equals("*")) {
                boolean found = false;
                String[] etags = request.getHeaders().getList("If-Match");
                if (etags != null) {
                    for (int i = 0; i < etags.length; i++) {
                        if (fbody.getETag().equals(etags[i])) {
                            found = true; //got a match.. break
                            break;
                        }
                    }
                }

                if (found == false) {//no match at all!!!
                    response = de.infomac.webserver.http.impl.HTTPResponse.getResponse(request
                            .getVersion(), 412, "Precondition failed");
                }
            }

            //Now we have a (matching) a FileMessageBody ... generate a response
            // if not already present
            if (response == null)
                response = de.infomac.webserver.http.impl.HTTPResponse.getResponse(request
                        .getVersion(), 200, "OK", fbody);

            //TODO is OK only with NON-persistent Connections
            //response.setHeader("Connection", "close");

        } catch (MalformedURLException e) {
            //means the request-uri (possibly together with 'Host')
            //is not a valid url. But this
            //should have been checked during parsing
            Assert.condition(false);
        } catch (FileNotFoundException e1) {
            logger.info(e1);
            response = de.infomac.webserver.http.impl.HTTPResponse.get404(request.getVersion(),
                    request.getRequestURI());
        } catch (MessageBodyException e) {
            response = de.infomac.webserver.http.impl.HTTPResponse.get500(request.getVersion(), e);
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
            Assert.condition(false);
        }

        //we accept byte-ranges:
        response.setHeader("Accept-Ranges", "bytes");

        logger.info("served: "
                + request.getVersion()
                + " "
                + response.getStatusCode()
                + " "
                + request.getRequestURI()
                + " "
                + (request.hasNonEmptyHeader("User-Agent") ? request
                        .getHeader("User-Agent") : ""));

        return response;
    }


    

}