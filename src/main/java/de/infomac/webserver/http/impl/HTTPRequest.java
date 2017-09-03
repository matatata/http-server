/*
 * Created on 22.06.2004 by matteo Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.infomac.webserver.commons.Assert;
import de.infomac.webserver.http.HTTPHeaders;
import de.infomac.webserver.http.HTTPParseError;
import de.infomac.webserver.http.HTTPVersion;
import de.infomac.webserver.http.MalformedHTTPVersion;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.MessageBodyException;
import de.infomac.webserver.http.ParsingHelper;
import de.infomac.webserver.http.io.BufferedInputStream;


public class HTTPRequest extends de.infomac.webserver.http.impl.HTTPMessage implements
		de.infomac.webserver.http.HTTPRequest {
	private static Category logger = Logger.getInstance(HTTPRequest.class);

	private String method;

	private String requestURI;

	/**
	 * @param method
	 * @param request_uri
	 * @param version
	 * @param headers
	 */
	public HTTPRequest(String method, String request_uri, HTTPVersion version,
			HTTPHeaders headers) {

		super(version, headers);
		this.method = method;
		this.requestURI = request_uri;

	}

	/**
	 * @param method
	 * @param request_uri
	 * @param version
	 */
	public HTTPRequest(String method, String request_uri, HTTPVersion version) {
		super(version, new de.infomac.webserver.http.impl.HTTPHeaders());

		this.method = method;
		this.requestURI = request_uri;

	}

	/**
	 * @return Returns the method.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method
	 *            The method to set.
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return Returns the request_uri.
	 */
	public String getRequestURI() {
		return requestURI;
	}

	/**
	 * @param request_uri
	 *            The request_uri to set.
	 */
	public void setRequestURI(String request_uri) {
		this.requestURI = request_uri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cwprx.org.ceruti.http.HTTPRequest#parseHTTPRequest(java.io.InputStream)
	 */
	public static HTTPRequest parseHTTPRequest(java.io.InputStream is)
			throws IOException, HTTPParseError {
		final BufferedInputStream sbr = new BufferedInputStream(is);
		String line = sbr.readLine();
		if(line==null)
		    throw new IOException("EOF");
		Object[] req_line = parseRequestLine(line);
		HTTPHeaders headers = de.infomac.webserver.http.impl.HTTPHeaders.parseHeaders(sbr);

		final HTTPRequest req = new HTTPRequest((String) req_line[0],
				(String) req_line[1], (HTTPVersion) req_line[2], headers);

		/*
		 * The presence of a message-body [...] is signaled by the inclusion of
		 * a Content-Length or Transfer-Encoding Header-Field
		 */
		try {

			if (req.getVersion().isVersion1_1() && req.hasNonEmptyHeader("Transfer-Encoding")) {
				if (req.getHeader("Transfer-Encoding").equals("chunked"))
					req.setMessageBody(new ChunkedMessageBody(sbr,(String) req.getHeader("Content-Encoding")));
				else
					throw new HTTPParseError("Unknown Transfer-Encoding "+req.getHeader("Transfer-Encoding"));
			} else if (req.getHeaders().getContentLength() != -1) {
				//This will be the normal case

				req.setMessageBody(new DefaultMessageBody(sbr,
						(String) req.getHeader("Content-Encoding"), req
								.getHeaders().getContentLength()));
				
				
				
			} else {
				req.setMessageBody(null);
			}
		} catch (MessageBodyException e) {
			throw new HTTPParseError(e);
		}
		
	    //NOTE: the default behaviour of a MessageBody is to close the stream/socket,
        //after beeing sent. So if this requests contains a MessageBody (e.g. POST request)
        //we must make sure it does not close the stream (and socket) , because we expect a response.
	    MessageBody body = req.getMessageBody();
	    if(body!=null)
	        body.removeDefaultListener();
        

		return req;
	}

	public static HTTPRequest GET(String req_uri, HTTPVersion v) {
		HTTPRequest req = new HTTPRequest("GET", req_uri, v);

		return req;
	}

	/**
	 * Method = "OPTIONS" ; Section 9.2 | "GET" ; Section 9.3 | "HEAD" ; Section
	 * 9.4 | "POST" ; Section 9.5 | "PUT" ; Section 9.6 | "DELETE" ; Section 9.7 |
	 * "TRACE" ; Section 9.8 | "CONNECT" ; Section 9.9 | extension-method
	 * extension-method = token
	 *  
	 */
	private static Pattern methodpat = Pattern
			.compile("(OPTIONS|GET|HEAD|POST|PUT|DELETE|TRACE|CONNECT)");

	private static Object[] parseRequestLine(String line) throws HTTPParseError {

		String parts[] = line.split(String.valueOf(ParsingHelper.SP));
		if (parts.length < 2)
			throw new HTTPParseError("Error in '" + line + "'");

		String meth = parts[0];
		
		String uri = "";
		
		if(parts.length==3)
			uri = parts[1];

		

		HTTPVersion vers = null;
		try {
			vers = HTTPVersion.parseHTTPVersion((parts.length == 3) ? parts[2] : parts[1]);
		} catch (MalformedHTTPVersion e) {
			throw new HTTPParseError(e);
		}
		
		//Check URI
		//look for leading '/' in relative-Paths
		//Note: Some older lynx version (2.8.3dev.9 (13 Sep 1999)) does not send the leading '/'
		//rfc sais: if none is present in the original URI, it MUST be given as �/�
		if(uri.length()==0)
			uri="/";
		else if(!uri.equals("*") && !uri.toLowerCase().startsWith("org.ceruti.http://") && !uri.startsWith("/"))
			throw new HTTPParseError("Illegal RequestURI '" + uri + "'");
		

		if (!methodpat.matcher(meth).matches()) {
			throw new HTTPParseError("Illegal Method '" + meth + "'");
		}

		return new Object[] { meth, uri, vers };

	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		//      METHOD SP URI SP CRLF
		buf.append(getMethod()).append(ParsingHelper.SP)
				.append(getRequestURI()).append(ParsingHelper.SP).append(
						getVersion()).append(ParsingHelper.CRLF);

		buf.append(getHeaders());
		buf.append(ParsingHelper.CRLF);

		return buf.toString();
	}

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");

		try {
			//HTTPRequest req = HTTPRequest.parseHTTPRequest(System.in);
			HTTPRequest req = HTTPRequest.GET("org.ceruti.http://wwwc.eruti.de",
					HTTPVersion.VERSION_1_1);
			req.setHeaderNonDestructive("Age", new Integer(1233));
			HTTPRequestValidatorBase val = new HTTPRequestValidatorBase() {
				public void validateMethod(de.infomac.webserver.http.HTTPRequest req) {
				}

			};
			val.validate(req);

			req.copyToOutputStream(System.out);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return null if impossible (will likely happen in HTTP/1.0)
	 */
	public URL getURL() throws MalformedURLException {
		if (isAbsoluteURI()) {
			URL u = new URL(getRequestURI());
			Assert.condition(u.getProtocol().startsWith("http"));
			return u;
		}
		String host = (String) getHeader("Host");
		if (getVersion().isVersion1_0()) {
			// v1.0 does not require Host if the uri is not absolute
			// in this case we cannot construct a URL
			if (host == null)
				return null;
		}

		Assert.condition(host); // Here we must assert to have a Host
		
		
		String url = "http://" + host + getRequestURI();

		return new URL(url);
	}

	public boolean isAbsoluteURI() {
		return getRequestURI().startsWith("http://");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cwprx.org.ceruti.http.HTTPRequest#isMethodGET()
	 */
	public boolean isMethodGET() {
		return getMethod().equals("GET");
	}

	public boolean isMethodPOST() {
		return getMethod().equals("POST");
	}

	public boolean isMethodHEAD() {
		return getMethod().equals("HEAD");
	}



	/* (non-Javadoc)
	 * @see org.ceruti.http.HTTPRequest#hasAcceptEncoding(java.lang.String)
	 */
	public boolean hasAcceptEncoding(String coding) {
		if(!hasNonEmptyHeader("Accept-Encoding")){
			return false;
		}
		
		//x-gzip == gzip and x-defalte == deflate (those are identically)
		if(coding.equals("x-gzip") || coding.equals("x-deflate") )
		    coding = coding.replaceFirst("x-","");
		
		return ((String)getHeader("Accept-Encoding")).indexOf(coding) != -1;
		

	}

	/* (non-Javadoc)
	 * @see org.ceruti.http.HTTPRequest#getAcceptEncodings()
	 */
	public String[] getAcceptEncodings() {
		if(getHeader("Accept-Encoding")==null){
			return null;
		}
		String []acc = ((String)getHeader("Accept-Encoding")).split("\\s,\\s");
		for(int i = 0;i<acc.length;i++){
			acc[i] = acc[i].replaceFirst(";.*", "");
		}
		
		return acc;
	}

  

}