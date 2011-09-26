/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zhang;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Locale;
import processing.core.PApplet;

/**
 * A class for writing data in CSV format to an output file. Pass the name of a file plus a variable number of names
 * for the data in the corresponding column. Call record(Object...) with the actual objects to save another line.
 * @author hellochar
 */
public class Recorder {

    File f;
    BufferedWriter out;

    public Recorder(File f, Object... dataNames) {
        this.f = f;
        String path = f.getAbsolutePath();
        if (!path.contains(".csv"))
            f = new File(path + ".csv");
        try {
            f.createNewFile();
            out = new BufferedWriter(new FileWriter(f));
            record(dataNames);
            newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Recorder(String absolutePath, Object... dataNames) {
        this(new File(absolutePath), dataNames);
    }

    public Recorder(String absolutePath, PApplet owner, boolean addDate, Object... dataNames) {
        this(owner.sketchPath + File.separator + appendDateIfTrue(absolutePath, addDate), dataNames);
    }

    private static String appendDateIfTrue(String s, boolean append) {
        if (append) {
            GregorianCalendar gc = new GregorianCalendar();
            s += "_" +
                    gc.getDisplayName(gc.MONTH, gc.SHORT, Locale.getDefault()) +
                    gc.get(gc.DAY_OF_MONTH) + "_" +
                    (int) (1 + Methods.wrap(Methods.wrap(gc.get(gc.HOUR_OF_DAY) - 1, 24), 12)) + "." +
                    gc.get(gc.MINUTE) + "." +
                    gc.get(gc.SECOND) +
                    gc.getDisplayName(gc.AM_PM, gc.SHORT, Locale.getDefault());
        }
        return s;
    }

    public final void record(Object... data) {
        if (out == null)
            return;
        try {
            for (Object o : data) {
                out.write(o + ",");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
     public final void newLine() {
        try {
            out.newLine();
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
     }

    public final void close() throws IOException {
        out.close();
    }
}
