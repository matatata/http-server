package de.infomac.webserver.http.io;

/**
 * @author matteo
 * This class extends java.io.BufferedInputStream,
 * with a readLine()-Method wich copes with the Line-Separator-Issue.
 * It reads bytes until one of the common Line-Separators is encountered.
 * 
 * UNIX:	LF
 * Windows: CRLF
 * Mac (Classic): CR
 * 
 * If you for example send some lines from an old Macintosh-App,
 * it might happen that it uses only CR as line-separator.
 * So recieving on a socket by
 * 	String line = reader.readLine();
 * might cause problems, because the code probably will be expecting also the LF.
 
 * 
 * The readLine()-Method was taken from Elliotte Rusty Harold's
 * SafeBufferedReader.java (see included File)
 * I applied it to a java.io.BufferedInputStream and needed to
 * override the several read()-Methods to preserve the original behaviour of
 * the java.io.BufferedInputStream. I do not know if this is true in every case,
 * but yet it seems to do its job properly.
 */


import java.io.IOException;
import java.io.InputStream;

public class BufferedInputStream extends java.io.BufferedInputStream{

	/**
	 * @param arg0
	 */
	public BufferedInputStream(InputStream arg0) {
		super(arg0);
	}

	private boolean lookingForLineFeed = false;

	private boolean dirty = false;

	/**
	 * UNIX: if LF is encountered first, but no CR was last read in the previous readLine-Call.
	 * MAC or Win: if CR is encountered first
	 * 	In this case it returns the data read so far, but remembers to skip a possibly following LF
	 * 	the next time.
	 */
	public String readLine() throws IOException {
		StringBuffer sb = new StringBuffer("");
		while (true) {
			int c = super.read();
			if (c == -1) { // end of stream
				if (sb.length()==0)
					return null;
				return sb.toString();
			} else if (c == '\n') {
				if (lookingForLineFeed) {
					lookingForLineFeed = false;
					continue;
				} else {
					return sb.toString();
				}
			} else if (c == '\r') {
				lookingForLineFeed = true;
				dirty = true;
				return sb.toString();
			} else {
				lookingForLineFeed = false;
				sb.append((char) c);
			}
		}
	}
	
	//Must override read() because if previously readLine() was called,
	//we micht have to consume a LineFeed if expected.

	public int read() throws IOException {
		int c = super.read();
		if (dirty && lookingForLineFeed) {
			if (c == '\n') {
				dirty = false;
				lookingForLineFeed = false;
				return super.read();
			}
		}

		return c;

	}

	//Must read first byte manually to make it consume the LineFeed if necessary
	
	public int read(byte[] b) throws IOException {
		if(b.length == 0)
			return 0;
		int c = this.read();
		if(c == -1)
			return -1;
		else
			b[0] = (byte) c;
		
		return super.read(b,1,b.length - 1) + 1;
	}
	
	//	Must read first byte manually to make it consume the LineFeed if necessary
	
	public int read(byte[] b,int off,int len) throws IOException {
		if(off < 0)
			throw new IndexOutOfBoundsException();
		if(off + len > b.length)
			throw new IndexOutOfBoundsException();
		
		if(len <= 0)
			return 0;
		int c = this.read();
		if(c==-1)
			return -1;
		else
			b[off] = (byte) c;
		
		if(len>0)
			return super.read(b,off + 1,len - 1) + 1;
		
		return 1;
	}
	
	
	

}