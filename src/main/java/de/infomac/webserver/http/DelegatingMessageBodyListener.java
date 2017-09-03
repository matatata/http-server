/*
 * Created on 19.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;

/**
 * @author matteo
 *
 * TODO comment
 */
public class DelegatingMessageBodyListener implements HTTPMessageBodyListener {
    private HTTPMessageBodyListener delegatee;
    
    public DelegatingMessageBodyListener(HTTPMessageBodyListener del){
        delegatee = del;
    }
    
    /**
     * @param body
     */
    public void afterCopyToOutputStream(MessageBody body) {
        delegatee.afterCopyToOutputStream(body);
    }
    
    /* (non-Javadoc)
     * @see org.ceruti.http.HTTPMessageBodyListener#afterExceptionDuringTransfer(org.ceruti.http.MessageBody, java.lang.Exception)
     */
    public void afterExceptionDuringTransfer(MessageBody body, Exception e) {
        delegatee.afterExceptionDuringTransfer(body,e);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        return delegatee.equals(arg0);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return delegatee.hashCode();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return delegatee.toString();
    }

  
}
