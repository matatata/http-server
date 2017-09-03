/*
 * Created on 26.06.2004 by matteo Mat. Nr. 953982
 */
package de.infomac.webserver.http.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import de.infomac.webserver.http.HTTPMessage;
import de.infomac.webserver.http.HTTPMessageBodyListener;
import de.infomac.webserver.http.HTTPVersion;
import de.infomac.webserver.http.MessageBody;
import de.infomac.webserver.http.MessageBodyException;
import de.infomac.webserver.http.Unimplemented;
import de.infomac.webserver.http.io.Utils;
import de.infomac.webserver.server.Server;

/**
 * This is a base-Implementaion wich actually is capable of quit-nothing. But you can use it if you instanciate Adapters
 * or sub-classes
 */
public abstract class MessageBodyBase implements MessageBody {

  private static Category logger = Logger.getInstance(MessageBodyBase.class);

  protected Set listeners = new HashSet();

  public Iterator getListeners() {
    return listeners.iterator();
  }

  public void addListener(HTTPMessageBodyListener l) {
    listeners.add(l);
  }

  private HTTPMessageBodyListener default_listener;

  /**
   * 
   * @return null if not available
   */
  public final HTTPMessageBodyListener getDefaultListener() {
    if (default_listener == null) {
      default_listener = newDefaultListener();
    }

    return default_listener;
  }

  public HTTPMessageBodyListener newDefaultListener() {
    return new HTTPMessageBodyListener() {

      public void afterCopyToOutputStream(MessageBody body) {
        logger.debug("default: afterCopyToOutputStream()");
        cleanup();
      }

      public void afterExceptionDuringTransfer(MessageBody body, Exception e) {
        logger.debug("default: afterExceptionDuringTransfer()");
      }

    };
  }

  public final void removeDefaultListener() {
    if (removeListener(getDefaultListener())) {
      logger.debug("removed default-listener");
    }
  }

  public void addDefaultListener() {
    if (getDefaultListener() != null) {
      addListener(getDefaultListener());
    }
  }

  public boolean removeListener(HTTPMessageBodyListener l) {
    if (l != null) {
      return listeners.remove(l);
    }
    return false;
  }

  public void fireAfterCopyToOutputStream() {
    for (java.util.Iterator it = getListeners(); it.hasNext();) {
      HTTPMessageBodyListener l = (HTTPMessageBodyListener) it.next();

      l.afterCopyToOutputStream(this);
    }
  }

  public void fireAfterExceptionDuringTransfer(Exception e) {
    for (java.util.Iterator it = getListeners(); it.hasNext();) {
      HTTPMessageBodyListener l = (HTTPMessageBodyListener) it.next();

      l.afterExceptionDuringTransfer(this, e);
    }
  }

  protected static int prefetch_size = 5000000;
  static {
    try {
      if (Server.getContext()
          .getProperties()
          .getProperty("prefetch_size") != null) {
        prefetch_size = Integer.parseInt(Server.getContext()
            .getProperties()
            .getProperty("prefetch_size"));
      }
    } catch (NumberFormatException e1) {
      logger.error("prefetch_size must be an Integer", e1);
    }

  }

  public boolean hasInputStream() {
    return false;
  }

  public InputStream getInputStream() {
    return null;
  }

  public boolean isReadable() {
    return false;
  }

  public boolean lengthAvailable() {
    return false;
  }

  public long length() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see cwprx.io.Readable#read()
   */
  public int read() throws IOException {
    // TODO Auto-generated method stub
    return -1;
  }

  /**
   * when buffering data the maximum-parameter is useful
   */
  public void copyToOutputStream(java.io.OutputStream out, int maximum) throws IOException, MessageBodyException {
    long bytes = -1;
    try {

      if (hasInputStream()) {
        if (lengthAvailable()) {
          bytes = Utils.transferBytes(getInputStream(), out, maximum < length() ? maximum : length());
        } else {
          bytes = Utils.transferBytes(getInputStream(), out, maximum);
        }
      } else if (isReadable()) {
        if (lengthAvailable()) {
          bytes = Utils.transferBytes(this, out, maximum < length() ? maximum : length());
        } else {
          bytes = Utils.transferBytes(this, out, maximum);
        }
      } else {
        throw new MessageBodyException("MessageBody can not be transferred");
      }
    } catch (IOException e) {
      fireAfterExceptionDuringTransfer(e);
      throw e;
    } catch (MessageBodyException e) {
      fireAfterExceptionDuringTransfer(e);
      throw e;
    } finally {

      fireAfterCopyToOutputStream();
      if (bytes >= maximum) {
        throw new MessageBodyException("Too many bytes in message");
      }
    }
  }

  /**
   * Transfers its content to out. After that calls afterCopyToOutputStream()
   */
  public void copyToOutputStream(java.io.OutputStream out) throws IOException, MessageBodyException {
    try {

      if (hasInputStream()) {
        if (lengthAvailable()) {
          Utils.transferBytes(getInputStream(), out, length());
        } else {
          Utils.transferBytes(getInputStream(), out);
        }
      } else if (isReadable()) {
        if (lengthAvailable()) {
          Utils.transferBytes(this, out, length());
        } else {
          Utils.transferBytes(this, out);
        }
      } else {
        throw new MessageBodyException("MessageBody can not be transferred");
      }
    } catch (IOException e) {
      fireAfterExceptionDuringTransfer(e);
      throw e;
    } catch (MessageBodyException e) {
      fireAfterExceptionDuringTransfer(e);
      throw e;
    } finally {
      fireAfterCopyToOutputStream();
    }
  }

  public String getTransferEncoding() {
    return null;
  }

  /**
   * This must me called when a MessageBody gets attached to a Message. It sets up some standard Header-Fields
   * 
   * @throws MessageBodyException
   */

  public void attachedTo(HTTPMessage dest) throws MessageBodyException {

    if (getTransferEncoding() != null) {
      dest.setHeader("Transfer-Encoding", getTransferEncoding());
    } else {
      dest.getHeaders()
          .remove("Transfer-Encoding");
    }

    if (lengthAvailable()) {
      dest.setHeader(HTTPHeaders.CONTENT_LENGTH, String.valueOf(length()));
    } else if (getTransferEncoding() == null) {
      // we do not know the length and there is no Transfer-encoding, so tell the client we will close the connection
      // when
      // there are no more bytes.
      dest.setHeader("Connection", "close");

      // remove any Content-Length-Header because we really do not know the size
      dest.getHeaders()
          .remove("Content-Length");
    }

    if (ETagAvailable()) {
      dest.setHeader("ETag", getETag());
    }

    if (getContentCoding() != null && !getContentCoding().equals("identity")) {
      dest.setHeader("Content-Encoding", getContentCoding());
    } else {
      dest.getHeaders()
          .remove("Content-Encoding"); // make sure to remove any existing encoding if "identity" or null
    }

    if (getContentType() != null) {
      dest.setHeader(HTTPHeaders.CONTENT_TYPE, getContentType());
    }

  }

  public boolean resetAvailable() {
    return false;
  }

  public void reset() throws IOException {

  }

  public String getContentType() {
    return null;
  }

  public MessageBody decode(HTTPVersion version, boolean content_length_required)
      throws Unimplemented, MessageBodyException, IOException {

    throw new Unimplemented("cannot decode " + getContentCoding());
  }

  public MessageBody decode(HTTPVersion version) throws Unimplemented, MessageBodyException, IOException {
    return decode(version, false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ceruti.http.MessageBody#preFetchData()
   */
  public ByteArrayMessageBody preFetchData() throws MessageBodyException, IOException {
    ByteArrayOutputStream data = new ByteArrayOutputStream();

    logger.info("prefetching (max=" + prefetch_size + " bytes)");

    copyToOutputStream(data, prefetch_size);
    return new ByteArrayMessageBody(data.toByteArray(), getContentCoding());
  }

  public String getContentCoding() {
    return null;
  }

  public boolean ETagAvailable() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ceruti.http.MessageBody#getETag()
   */
  public String getETag() {
    return null;
  }

}
