/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package zhang;

import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author hellochar
 */
public final class ZhangLogger {
    
    private static String name = "";

    private final static Logger LOGGER = Logger.getAnonymousLogger();

    public static Logger getLogger() {
        return LOGGER;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        ZhangLogger.name = name;
    }

    private static String nameString() {
        return name+": ";
    }

    /**
     * with name string. Prepend nameString() to the string
     * @param s
     * @return
     */
    private static String wns(String s) {
        return nameString()+s;
    }


    public static void warning(String message, Throwable t) {
        String msg = wns(message);
        LOGGER.log(Level.WARNING, msg, t);
    }

    public static void error(String message, Throwable t) {
        String msg = wns(message);
        LOGGER.log(Level.SEVERE, msg, t);
        if(!GraphicsEnvironment.isHeadless()) JOptionPane.showMessageDialog(null, msg);
        System.exit(2);
    }

    public static void log(String s, Level level) {
        LOGGER.log(level, wns(s));
    }

    public static void logMutexTry(String method, String mutexName) {
        log(method+" asking for mutex "+mutexName+"!", Level.FINEST);
    }
    public static void logMutexGet(String method, String mutexName) {
        log(method+" recieved mutex "+mutexName+"!", Level.FINEST);
    }
    public static void logMutexDone(String method, String mutexName) {
        log(method+" lost mutex "+mutexName+"!", Level.FINEST);
    }
    public static void logMutexTry(String method) {
        logMutexTry(method, "");
    }
    public static void logMutexGet(String method) {
        logMutexGet(method, "");
    }
    public static void logMutexDone(String method) {
        logMutexDone(method, "");
    }

    private static String getMethodCallerClassName() {
        String myName = ZhangLogger.class.getName();
        for(StackTraceElement s : new Throwable().fillInStackTrace().getStackTrace()) {
            if(!s.getClassName().equals(myName)) return s.getClassName();
        }
        return null;
    }
    
    public static void entering(String methodName) {
        LOGGER.logp(Level.FINER, getMethodCallerClassName(), methodName, wns("ENTRY"));
    }

    public static void exiting(String methodName) {
        LOGGER.logp(Level.FINER, getMethodCallerClassName(), methodName, wns("RETURN"));
    }

    public static void main(String[] args) {
        System.out.println(ZhangLogger.class.getName());
        for(StackTraceElement s : new Throwable().getStackTrace()) {
            System.out.println(s.getClassName());
        }
    }

}
