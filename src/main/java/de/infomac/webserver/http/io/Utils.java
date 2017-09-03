/*
 * Created on 24.06.2004 by matteo Mat. Nr. 953982
 */
package de.infomac.webserver.http.io;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import de.infomac.webserver.commons.Assert;


public class Utils {

	
	/**
	 * @param inputStream
	 * @param out
	 * @param l
	 * @throws IOException
	 */
	public static long transferBytes(InputStream is, OutputStream os, long maxBytes) throws IOException {
		byte[] buffer = new byte[1024];
		int bytes = buffer.length;

		long i = 0;
		long total = 0;
		
		
		if(total + bytes > maxBytes){
			bytes = (int) (maxBytes - total);
		}
		
		// Copy bytes into the output stream.
		while ((bytes = is.read(buffer,0,bytes)) != -1) {
			
			
			
			os.write(buffer, 0, bytes);
			total += bytes;
			
			if(total == maxBytes)
				return total;
			Assert.condition(total <= maxBytes);
			
			
			if(total + bytes > maxBytes){
				bytes = (int) (maxBytes - total);
			}
		}
		
		return total;
		
	}
	
	
	
	public static long transferBytes(InputStream is, OutputStream os)
			throws IOException {
		byte[] buffer = new byte[1024];
		int bytes = 0;
		long total = 0;

		long i = 0;
		// Copy bytes into the output stream.
		while ((bytes = is.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
			total+=bytes;
		}
		
		return total;
	}

	
	
	/**
	 * @param reader
	 * @param out
	 * @param maxBytes
	 * @throws IOException
	 */
	
	
	public static void transferBytes(Reader reader, OutputStream os,
			long maxBytes) throws IOException {
		int c;
		long i = 0;

		while ((i++ < maxBytes) && ((c = reader.read()) != -1)) {
			os.write(c);
		}
	}

	/**
	 * @param reader
	 * @param out
	 * @param maxBytes
	 * @throws IOException
	 */
	public static long transferBytes(Readable reader, OutputStream os,
			long maxBytes) throws IOException {
		int c;
		long i = 0;
		long total = 0;

		while ((i++ < maxBytes) && ((c = reader.read()) != -1)) {
			os.write(c);
			total++;
		}
		
		return total;
	}

	public static long transferBytes(Readable reader, OutputStream os)
			throws IOException {
		int c;
		long total = 0;
		while ((c = reader.read()) != -1) {
			os.write(c);
			total++;
		}
		return total;
	}

	public static void transferBytes(Reader reader, OutputStream os)
			throws IOException {
		int c;

		while ((c = reader.read()) != -1) {
			os.write(c);
		}
	}


	
	public static void main(String []args){
	
		
		try {
			transferBytes(System.in,System.out,14);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



   /**
    * 
    * @param in
    * @param orig if not null, any skipped byte is written to orig
    * @param n
    * @return
    * @throws IOException
    */
    public static long skippppp(InputStream in, OutputStream orig, long n) throws IOException {
        //do not use InputStream since it does not really work with large n
        int c=0;
        long i = 0;
        for(i=0;i < n && c !=-1; i++){
            c = in.read();
            if(orig!=null)
                orig.write(c);
        }
            
        return i;
    }
    
    /**
     * 
     * @param in
     * @param orig if not null, any skipped byte is written to orig
     * @param n
     * @return
     * @throws IOException
     */
    public static long skipppp(Readable in, OutputStream orig,long n) throws IOException {
        //do not use InputStream since it does not really work with large n
        int c=0;
        long i = 0;
        for(i=0;i < n && c !=-1; i++){
            c = in.read();
            if(orig!=null)
                orig.write(c);
        }
            
        return i - 1;
    }
}