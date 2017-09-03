package de.infomac.webserver.http.io;

import java.io.IOException;
import java.io.OutputStream;


public class MultiOutputStream extends OutputStream{
	private OutputStream [] os;
	/**
	 * @throws java.io.IOException
	 */
	
	public MultiOutputStream(OutputStream [] streams){
		os = streams;
	}
	
	public void close() throws IOException {
		IOException ex = null;
		for(int i = 0;i<os.length;i++){
			try {
				os[i].close();
			}catch(IOException e){
				ex = e;
			}
		}
		
		if(ex!=null)
			throw ex;
		
	}

	/**
	 * @throws java.io.IOException
	 */
	public void flush() throws IOException {
		IOException ex = null;
		for(int i = 0;i<os.length;i++){
			try {
				os[i].flush();
			}catch(IOException e){
				ex = e;
			}
		}
		
		if(ex!=null)
			throw ex;
	}

	
	/**
	 * @param arg0
	 * @throws java.io.IOException
	 */
	public void write(byte[] arg0) throws IOException {
		IOException ex = null;
		for(int i = 0;i<os.length;i++){
			try {
				os[i].write(arg0);
			}catch(IOException e){
				ex = e;
			}
		}
		
		if(ex!=null)
			throw ex;
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @throws java.io.IOException
	 */
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		IOException ex = null;
		for(int i = 0;i<os.length;i++){
			try {
				os[i].write(arg0, arg1, arg2);
			}catch(IOException e){
				ex = e;
			}
		}
		
		if(ex!=null)
			throw ex;

	}
	/**
	 * @param arg0
	 * @throws java.io.IOException
	 */
	public void write(int arg0) throws IOException {
		IOException ex = null;
		for(int i = 0;i<os.length;i++){
			try {
				os[i].write(arg0);
			}catch(IOException e){
				ex = e;
			}
		}
		
		if(ex!=null)
			throw ex;
	}
}
