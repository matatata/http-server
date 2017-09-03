/*
 * Created on 23.06.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http;

import java.util.Iterator;




public abstract class HTTPHeaders  {
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";

    
    
   /**
    * @param field_name
    * @return field-value of the Field identified by field_name
    */
    public abstract Object get(String field_name);
    
	public String []getList(String key){
	    String s = (String) get(key);
	    if(s==null)
	        return null;
	    
	    String []sp = s.split(",| ,");
	    
	    return sp;
	}
    
    /**
     * 
     * @param field_name
     * @param field_value
     * @param replaceIfPresent if true previous values will be overridden otherwise update as a ',' separated list
     * @return field-value that was previously associated or null
     */
    public abstract Object put(String field_name,Object field_value,boolean replaceIfPresent);
    
    public abstract Object remove(String key);
    
    /**
     * 
     * @return Iterator that iterates through instances of HTTPHeader in the order in wich they were parsed or added
     */
    public abstract Iterator iterator();
    
    public abstract boolean containsKey(String k);
    
    /**
     * 
     * @return -1 if not present
     * @throws NumberFormatException
     */
    public long getContentLength() throws NumberFormatException{
        Object o = get(HTTPHeaders.CONTENT_LENGTH);
        if(o==null)
            return -1;
        return Long.parseLong((String)o);
    }
    
    /**
     * @param field_name
     * @return
     */
    public boolean hasNonEmptyField(String field_name) {
        Object o = get(field_name);
        if (o == null)
            return false;

        if (o instanceof String) {
            String fv = (String) o;
            return (fv.length() != 0);
        }
        
        return true;
    }
    
    public boolean hasHeader(String field_name,Object value) {
        if(! hasNonEmptyField(field_name))
        		return false;
        
        return get(field_name).equals(value);
    }

    public boolean hasHeaderIgnoreCase(String field_name,String value) {
        if(! hasNonEmptyField(field_name))
        		return false;
        
        return ((String)get(field_name)).equalsIgnoreCase(value);
    }
    /**
     * 
     * @return
     */
    public HTTPHeaders copy() {
        HTTPHeaders ret = newInstance();
        
        for(Iterator it = iterator();it.hasNext();){
            HTTPHeader head = (HTTPHeader)it.next();
            
            ret.put(head.name,head.value,true);
        }
        
        return ret;
    }
    
    public abstract HTTPHeaders newInstance();
    
}
