/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package zhang;
/*
 *  Copyright(c)1998 Forward Computing and Control Pty. Ltd.
 *  ACN 003 669 994, NSW, Australia     All rights Reserved
 *
 *   Written by Dr. M.P. Ford
 */

import java.io.*;
import java.util.*;

/**
 *  Redirects System.Err and optionally System.Out to an output file This
 *  catches any uncaught exception traces. A heading can be written to the top
 *  of the file. Autoflush is set so ensure last error saved to disk Each
 *  application invocation appends to the log file.
 *
 *  To initialize this class add the following code to your main application class

 static {
    // set up logging for errors etc
    LogStdStreams.initializeErrorLogging("applicationLog.log",
                                         "Log File for Application "+ new Date(),
                                         true, true);
                                         // redirect System.out as well as err and append to existing log
 }

 *
 * Then in the body of the code any statements like
 *

 System.err.println(" Error message ..");
 System.out.println(" Output ... ");

 *
 * will be sent to the log file.
 *
 * @author   Dr. M.P. Ford
 * @created    October 21, 2001
 * @version    1 5th Oct 2001
 */

public class LogStdStreams extends PrintStream {

  /**
   *  The name of the output log file
   */
  private static String logFileName;

  /**
   *  The only instance of this class
   */
  private static LogStdStreams logStream = null;

  /**
   *  True if System.Out is being redirected
   */
  private static boolean redirectSystemOut = true;


  /**
   *  Private constructor to create PrintStream an redirect standard streams
   *
   * @param  logFile    the file output stream to use
   * @param  logStdOut  true if the System.out to be redirected also
   */
  private LogStdStreams(FileOutputStream logFile, boolean logStdOut) {
    super(logFile, true);
    // autoflush
    System.setErr(this);
    redirectSystemOut = logStdOut;
    if (redirectSystemOut) {
      // redirect stdOut as well
      System.setOut(this);
    }
  }


  /**
   *  Get the PrintStream being used for this logfile
   *
   * @return    the PrintStream System.err is being redirected to
   * @throws    RuntimeException if initializeErrorLogging() has not been called
   *      yet
   */
  public static PrintStream getLogStream() {
    if (logStream == null) {
      throw new RuntimeException("initializeErrorLogging() has not been called yet.");
    }
    return logStream;
  }


  /**
   *  Is the System.Out being redirected to this logfile
   *
   * @return    true if System.Out is being redirected
   * @throws    RuntimeException if initializeErrorLogging() has not been called
   *      yet
   */
  public static boolean isSystemOutRedirected() {
    if (logStream == null) {
      throw new RuntimeException("initializeErrorLogging() has not been called yet.");
    }
    return redirectSystemOut;
  }


  /**
   *  Get the name of the logfile
   *
   * @return    true if System.Out is being redirected
   * @throws    RuntimeException if initializeErrorLogging() has not been called
   *      yet
   */
  public static String getLogFileName() {
    if (logStream == null) {
      throw new RuntimeException("initializeErrorLogging() has not been called yet.");
    }
    return logFileName;
  }


  /**
   *  The static initialization method <br>
   *  Also redirects System.out, rewrites output file
   *
   * @param  fileName  the name of the log file
   * @throws           RuntimeException if initializeErrorLogging() has already
   *      been called yet
   */

  public static void initializeErrorLogging(String fileName) {
    initializeErrorLogging(fileName, null, true, false);
  }


  /**
   *  The static initialization method which writes heading <br>
   *  Also redirects System.out, rewrites output file
   *
   * @param  fileName    the name of the log file
   * @param  initialStr  the heading string to write to the log file when opened
   * @throws             RuntimeException if initializeErrorLogging() has already
   *      been called yet
   */
  public static void initializeErrorLogging(String fileName, String initialStr) {
    initializeErrorLogging(fileName, initialStr, true, false);
  }


  /**
   *  The static initialization method which writes heading <br>
   *  Rewrites output file
   *
   * @param  fileName    the name of the log file
   * @param  initialStr  the heading string to write to the log file when opened
   * @param  logStdOut   true if System.Out to redirected to log file also
   * @throws             RuntimeException if initializeErrorLogging() has already
   *      been called yet
   */
  public static void initializeErrorLogging(String fileName, String initialStr,
      boolean logStdOut) {
    initializeErrorLogging(fileName, initialStr, true, false);
  }


  /**
   *  The static initialization method
   *
   * @param  fileName    the name of the log file
   * @param  initialStr  the heading string to write to the log file when opened
   * @param  logStdOut   true if System.Out to redirected to log file also
   * @param  append      true if to append to existing log, false to rewrite new
   *      log.
   * @throws             RuntimeException if initializeErrorLogging() has already
   *      been called yet
   */
  public static void initializeErrorLogging(String fileName, String initialStr,
      boolean logStdOut, boolean append) {
    if (logStream != null) {
      throw new RuntimeException("initializeErrorLogging() has already been called.");
    }

    logFileName = fileName;
    try {
      logStream = new LogStdStreams(new FileOutputStream(logFileName, append),
          logStdOut);
      // append
    } catch (IOException ex) {
      System.err.println("Could not open output file " + logFileName);
      System.exit(1);
    }
    if ((initialStr != null) && (initialStr.length() != 0)) {
      // write heading
      System.err.println(initialStr);
    }
  }


  /**
   *  A test main()
   *
   * @param  args  Description of Parameter
   */
  public static void main(String args[]) {
    initializeErrorLogging("test.log", "Test Log: " + new Date(), true);
    // test call to initializeErrorLogging again
    try {
      initializeErrorLogging("test.log");
    } catch (Exception ex) {
      // test redirect of System.out
      System.out.println(ex.toString());
    }
    // check logfile and isSystemOutRedirected()
    System.out.println("Log file name is " + getLogFileName());
    System.out.println("is System.Out redirected? " + isSystemOutRedirected());
  }
}
