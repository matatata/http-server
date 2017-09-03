/*
 * Created on 15.07.2004 by matteo
 * Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl.byterange;

/**
 * @author matteo
 *
 * TODO comment
 */
public class ByteRangesSpecifier {
    public String bytes_unit;
    public ByteRangeSpecBase [] byte_range_set;
    
    public ByteRangesSpecifier(String u,ByteRangeSpecBase []arr){
        byte_range_set = arr;
        bytes_unit = u;
    }
    
    public String toString(){
        StringBuffer buf = new StringBuffer(bytes_unit + "=");
        
        for(int i = 0; i < byte_range_set.length; i++){
            buf.append(byte_range_set[i]);
            
            if(i < byte_range_set.length - 1)
                buf.append(", ");
        }
        
        return buf.toString();
    }
}