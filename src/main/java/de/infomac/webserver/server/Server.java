/*
 * 
 * Matteo Ceruti@rbg.informatik.tu-darmstadt.de
 * 
 * 953982
 * 
 */

package de.infomac.webserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import de.infomac.webserver.commons.Pool;
import de.infomac.webserver.commons.PoolImpl;
import de.infomac.webserver.threads.ThreadPool;

public class Server {

  private static Category logger = Logger.getInstance(Server.class);

  private boolean running = false;

  private ServerSocket listenSocket = null;

  private long keepAliveTimeout;
  private int max_threads;

  public static Context getContext() {
    return context;
  }

  private final static Context context = new Context() {

    private Properties properties;

    private ThreadPool pool = null;

    private Pool conn_pool = null;

    private long keepAliveTimeout;
    private int max_threads;

    @Override
    public void init() {
      try {
        Properties defaults = new Properties();
        defaults.load(ClassLoader.getSystemClassLoader().getResourceAsStream("server.defaults"));
        properties = new Properties(defaults);
        properties.load(ClassLoader.getSystemClassLoader()
            .getResourceAsStream("server.properties"));
      } catch (Exception e) {
        logger.fatal("Could not load server.defaults/properties " + e.getMessage());
        throw new Error(e);
      }

      keepAliveTimeout = Integer.parseInt(properties.getProperty("keep_alive_timeout"));
      max_threads = Integer.parseInt(properties.getProperty("max_threads"));

      pool = new ThreadPool(max_threads);
      conn_pool = new PoolImpl(max_threads);
    }

    @Override
    public Properties getProperties() {
      return properties;
    }

    @Override
    public String getServerRoot() {
      return properties.getProperty("server_root");
    }

    @Override
    protected String getServerRootForHost(String strlowercase) {
      strlowercase = strlowercase.replaceAll(":", "_");
      return properties.getProperty(strlowercase + "_server_root");
    }

    public void assignTask(Runnable task) {
      pool.assign(task);
    }

    @Override
    public ThreadPool getThreadPool() {
      return pool;
    }

    @Override
    public Pool getProcessorPool() {
      return conn_pool;
    }

  };

  public Server() {
    context.init();

    keepAliveTimeout = Integer.parseInt(context.getProperties()
        .getProperty("keep_alive_timeout"));
    max_threads = Integer.parseInt(context.getProperties()
        .getProperty("max_threads"));

    installMonitorTask();

  }

  private void quit() throws IOException {
    running = false;

    // we need to close the listenSocket, because it might still be
    // listening and blocking.
    if (!listenSocket.isClosed()) {
      listenSocket.close();
    }

    /*
     * Object [] conns = context.getHTTPConnections().toArray(); for(int i = 0; i < conns.length ;i++){
     * ((HTTPConnectionProcessor)conns[i]).close(); }
     */

    // Wait for work to complete

    System.out.println("Waiting for Threads to complete");
    context.getThreadPool()
        .complete();

    context.getThreadPool()
        .shutdown();
    monitorTask.cancel();
    context.getProcessorPool()
        .clear();

    System.gc();
    System.out.println("Shutdown complete");
  }

  private class UserInterface implements Runnable {

    private final static String helptext =
        "Commands:\nquit - gracefully quits the server.\nhelp - displays this text.\ngc - run garbage-collector\nstatus - status info";

    /**
     * 
     * @param cmd
     * @return true if command is supported
     */
    private boolean handleCmd(String cmd) throws IOException {

      if (cmd.equals("quit")) {
        System.out.println("quitting");

        quit();

        return true;
      } else if (cmd.equals("help")) {
        System.out.println(helptext);
        return true;
      }
      if (cmd.equals("gc")) {
        logger.info("gc");
        System.gc();
        return true;
      }
      if (cmd.equals("status")) {

        System.out.println("OK " + context.getHTTPConnections()
            .size() + " connections active.");
        System.out.println(context.getHTTPConnections());
        return true;
      }

      return false;
    }

    public void run() {
      // Read from stdin
      InputStreamReader isr = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isr);

      try {

        System.out.println("HTTP/1.x Server");
        System.out.println();
        System.out.println(helptext);

        while (running) {
          // prompt
          System.out.print("?>");

          String cmd = br.readLine();
          cmd = cmd.toLowerCase();
          cmd.trim();
          if (cmd.length() > 0) {
            // command was unknown
            if (!handleCmd(cmd)) {
              System.out.println("Unknown command. Type 'help' for help.");
            }
          }
        }
        // close BufferedReader (stdin)
        br.close();

      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }

  }

  public static void main(String argv[]) throws IOException {
    // PropertyConfigurator.configure("log4j.properties");

    String portArg = "8080";
    // check parameters
    if (argv.length == 1) {
      portArg = argv[0];
    } else if (argv.length > 1) {
      System.out.println("Usage: Server [port]");
      return;
    }

    // Get the port number and message from the command line.
    int port = Integer.parseInt(portArg);
    System.out.println("Will try to listen on port " + port);
    new Server().doService(port);
  }

  public void doService(int port) throws IOException {
    try {
      // Establish the listen socket.
      listenSocket = new ServerSocket(port);

    } catch (IOException e) {
      logger.fatal("Could not create socket on port " + port + " ... giving up", e);
      return;
    }

    // Now socket is established. enter the run-loop

    // Process HTTP service requests in an infinite loop.
    try {
      running = true;

      // start User-Input Thread
      Thread ui = new Thread(new UserInterface());
      ui.setName("UserInterface");
      ui.start();

      while (running) {
        // Listen for a TCP connection request.
        Socket connectionSocket = listenSocket.accept();

        // Create a new thread to process the request.
        // Thread thread = new Thread(
        // new HTTPConnectionProcessorImpl(connectionSocket,context));
        // thread.setName(connectionSocket.getInetAddress().getHostName());
        // Start the thread.
        // thread.start();

        HTTPConnectionProcessor prcs = (HTTPConnectionProcessor) context.getProcessorPool()
            .get();
        if (prcs == null) {
          prcs = AFactory.createHTTPConnectionProcessor();
        }

        prcs.init(connectionSocket, context);

        context.getThreadPool()
            .assign(prcs);

      }
    } catch (IOException e) {
      logger.debug("exiting Listen-loop, reason: " + e.getMessage());
      running = false;
    }

    // close the socket if not already closed
    if (!listenSocket.isClosed()) {
      listenSocket.close();
    }

    logger.info("HTTP service stopped");

  }

  private long monitorTaskInterval() {
    return keepAliveTimeout / max_threads + 1000;
  }

  private Timer monitorTask = new Timer();

  private void installMonitorTask() {

    monitorTask.schedule(new TimerTask() {

      @Override
      public void run() {

        Object[] conns = context.getHTTPConnections()
            .toArray();
        if (conns.length > 0) {
          logger.info(conns.length + " active connections");
        }
        for (Object conn2 : conns) {
          HTTPConnectionProcessor conn = (HTTPConnectionProcessor) conn2;
          logger.debug("Connection: " + conn);
          if (conn.getIdleTime() >= keepAliveTimeout) {
            logger.info("timeout (" + keepAliveTimeout / 1000 + "s) exceeded for " + conn);
            conn.close();
          }
        }

      }

    }, 500, monitorTaskInterval());
  }

}
