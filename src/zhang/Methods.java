 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zhang;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;
import processing.core.PVector;
import sun.misc.Launcher;

/**
 *  All the various misc. Methods I use.
 * @author Han-Han
 */
@SuppressWarnings({"UnusedDeclaration"})
public class Methods {
    /**
     * Represents PI in float precision. For convenience.
     */
    /**
     * Represents TWO_PI in float precision. For convenience.
     */
    public static final float PI = (float) Math.PI,
                              TWO_PI = PI*2;

    static private String printHierarchy(Component c, StringBuilder input, String prefix) {
//        input.append(prefix).append(c.getClass().getName());
        input.append(prefix).append(c);
        if (c instanceof Container) {
            Container cont = (Container) c;
            input.append(", which contains: \n");
            for (Component inner : cont.getComponents()) {
                printHierarchy(inner, input, prefix + "\t");
            }
        }
        else {
            input.append("\n");
        }
        return input.toString();
    }

    /**
     * Prints a readable hierarchy of the given component. For some component c,
     * this method will print out: <p><p>
     * c.toString(), which contains:<ul>
     *          printHierarchy(first child)<br>
     *          printHierarchy(second child)<br> </ul>
     *          ...
     * @param c
     * @return
     */
    public static String printHierarchy(Component c) {
        return printHierarchy(c, new StringBuilder(), "");
    }

    /**
     * Returns all subclasses, direct and indirect, of the given class cl inside the package denoted by pckgname.
     * @param pckgname name of the package to search
     * @param cl superclass to look for.
     * @return a set of all subclasses found in pckgname.
     * @see classesInPckg(String)
     */
    public static <E> Set<Class<? extends E>> getAllSubclasses(String pckgname, Class<E> cl) {
        Set<Class> classes = classesInPckg(pckgname);
        Set<Class<? extends E>> set = new HashSet<Class<? extends E>>();
        for (Class c : classes) {
            try {
                if (c != cl && cl.isAssignableFrom(c)) {
                    set.add((Class<? extends E>) c);
                }
            } catch (Exception e) {
//                        System.out.println(" -- "+s+"threw an exception -- "+e);
                e.printStackTrace();
            }
        }
        return set;
    }

    /**
     * Returns all direct subclasses of the given class cl inside the package denoted by pckgname.
     * @param pckg name of the package to search
     * @param cl superclass to look for
     * @return a set of all direct subclasses found in pckg.
     */
    public static <E> Set<Class<? extends E>> getDirectSubclasses(String pckg, Class<E> cl) {
        Set<Class> classes = classesInPckg(pckg);
        Set<Class<? extends E>> set = new HashSet<Class<? extends E>>();
        for (Class c : classes) {
            if (c.getSuperclass() == cl)
                set.add(c);
        }
        return set;
    }

    /**
     * Returns all classes in a given package.
     * @param pckgname name of the package to search
     * @return a set of all classes found in pckg.
     */
    public static Set<Class> classesInPckg(String pckgname) {
        // Code from JWhich
        // ======
        // Translate the package name into an absolute path
        String name = pckgname;
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        name = name.replace('.', '/');
//        System.out.print("zhang.Methods.getAllSubclasses debug: name is " + name);

        // Get a File object for the package
        URL url = Launcher.class.getResource(name);
        System.out.print(", url is: " + url);
        File directory = new File(url.getFile());
        if (!directory.exists())
            return null;
        String[] list = directory.list();
        Set<Class> cl = new HashSet<Class>();
        for (String s : list) {
            if (s.endsWith(".class")) {
                String className = s.substring(s.length() - 5); //remove the "class" extension from the end of the filename
                try {
                    Class c = Class.forName(pckgname + className);
                    cl.add(c);
                } catch (ClassNotFoundException cnf) {
                    cnf.printStackTrace();
                }
            }
        }
        return cl;
    }

    /** Finds a constructor that takes one argument of type <code>outer.getClass()</code>
     * for the given inner class and instantiates a new instance of inner class using the supplied outer
     * argument as an argument to the constructor. Returns this new instance. This method is useful for creating
     * new instances of inner classes.
     * @param outer enclosing object
     * @param inner the inner class
     * @return A new instance of the inner class, instantiated to refer to outer as its parent.
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    static public <E> E createInstance(Object outer, Class<E> inner) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Constructor<E> cons = inner.getConstructor(outer.getClass());
        return cons.newInstance(outer);
    }

    /**
     * Creates a deep copy of a serializable object by serializing and then deserializing the given object.
     * @param orig original object
     * @return a serialized and then deserialized version of the argument.
     */
    public static <T extends Serializable> T copy(T orig) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            FastByteArrayOutputStream fbos =
                    new FastByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Retrieve an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in =
                    new ObjectInputStream(fbos.getInputStream());
            obj = in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return (T) obj;
    }

    /**
     * Returns the number of bytes that the given object takes up
     * @param orig
     * @return
     */
    public static int objectSerializationSize(Serializable orig) {
        try {
            // Write the object out to a byte array
            FastByteArrayOutputStream fbos =
                    new FastByteArrayOutputStream(1024);
            ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(orig);
            out.flush();
            out.close();
            // Retrieve an input stream from the byte array and read
            // a copy of the object back in.
            InputStream in = fbos.getInputStream();
            int k = 0;
            for( ; in.read() != -1; k++) {}
            return k;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    //http://www.java2s.com/Tutorial/Java/0040__Data-Type/Getthedifferencebetweentwostrings.htm
    public static String diff(String str1, String str2) {
        int index = str1.lastIndexOf(str2);
        if (index > -1) {
            return str1.substring(str2.length());
        }
        return str1;
    }

    /**
     * Performs half-up rounding on <code>val</code> to the nearest multiple of <code>multiple</code>. <p>
     * (2, 7) rounds to 0.
     * <br>(5, 7) rounds to 7.
     * <br>(19, 7) rounds to 21.
     * <br>(29, 7) rounds to 28.
     * @param val number to round
     * @param multiple multiple to round to
     * @return
     */
    public static float roundToNearest(float val, float multiple) {
        return Math.round(val / multiple) * multiple;
    }

    /**
     * Floors <code>val</code> to the nearest multiple of <code>multiple</code>. <p>
     * (2, 7) rounds to 0.
     * <br>(5, 7) rounds to 0.
     * <br>(19, 7) rounds to 14.
     * <br>(29, 7) rounds to 28.
     * @param val number to floor
     * @param multiple multiple to floor to
     * @return
     */
    public static float floorToNearest(float val, float multiple) {
        return (float) (Math.floor(val / multiple) * multiple);
    }

    /**
     * Ceil's <code>val</code> to the nearest multiple of <code>multiple</code>. <p>
     * (2, 7) rounds to 7.
     * <br>(5, 7) rounds to 7.
     * <br>(19, 7) rounds to 21.
     * <br>(29, 7) rounds to 35.
     * @param val number to ceil
     * @param multiple multiple to ceil to
     * @return
     */
    public static float ceilToNearest(float val, float multiple) {
        return (float) (Math.ceil(val / multiple) * multiple);
    }
//
//    public static double roundToNearest(double val, double multiple) {
//        return Math.round(val / multiple) * multiple;
//    }
//
//    public static double floorToNearest(double val, double multiple) {
//        return (Math.floor(val / multiple) * multiple);
//    }
//
//    public static double ceilToNearest(double val, double multiple) {
//        return (Math.ceil(val / multiple) * multiple);
//    }

    /**
     * Rounds the given value to the given number of digits past the decimal. Use negative values for rounding to 10's, 100's, etc.
     * @param val number to round
     * @param digit number of digits past the decimal to round to
     * @return
     */
    public static double round(double val, int digit) {
        double inc = Math.pow(10, digit);
        //Todo - optimize?
        return Math.round(val / inc) * inc;
    }

    /**
     * Rounds the given value to the given number of digits past the decimal. Use negative values for rounding to 10's, 100's, etc.
     * @param val number to round
     * @param digit number of digits past the decimal to round to
     * @return
     */
    public static float round(float val, int digit) {
        float inc = (float) Math.pow(10, digit);
        //Todo - optimize?
        return Math.round(val / inc) * inc;
    }

    /**
     * Calls <code>Math.round(val)</code>.
     * @param val
     * @return
     * @deprecated Use <code>Math.round()</code>; it does the exact same thing.
     */
    @Deprecated
    public static int round(float val) {
        return Math.round(val);
    }

    /**
     * Calls <code>(int)Math.round(val)</code>.
     * @param val
     * @return
     */
    public static int round(double val) {
        return (int) Math.round(val);
    }

    /**
     * Checks if val is between low and high, inclusive
     * @param val number to check
     * @param low lower bound
     * @param high upper bound
     * @return true if <code>val >= low && val <= high</code>.
     */
    public static boolean isInRange(float val, float low, float high) {
        return val >= low && val <= high;
    }

    /**
     * Checks for if the given x/y coordinates are inside a window of size width/height. The top corner of the
     * window is assumed to be at (0, 0).
     * @param x x coordinate of point
     * @param y y coordinate of point
     * @param width
     * @param height
     * @return true if <code>isInRange(x, 0, width) && isInRange(y, 0, height)</code>
     * @see isInRange(float, float, float)
     */
    public static boolean isInWindow(float x, float y, float width, float height) {
        return isInRange(x, 0, width) && isInRange(y, 0, height);
    }

    /**
     * Checks for if the given x/y coordinates are inside of the given PApplet.
     * @param x x coordinate
     * @param y y coordinate
     * @param c PApplet to check.
     * @return
     * @deprecated Use <code>contains(float, float, Component)</code> instead.
     */
    public static boolean isInWindow(float x, float y, Component c) {
        return contains(x, y, c);
    }

    public static boolean contains(float x, float y, Component c) {
        return isInWindow(x, y, c.getWidth(), c.getHeight());
    }

    /**
     * Checks for if the given coordinates are inside of the given PGraphics range.
     * @param x
     * @param y
     * @param g
     * @return true if <code>isInWindow(x, y, g.width, g.height)</code>
     */
    public static boolean isInWindow(float x, float y, PGraphics g) {
        return isInWindow(x, y, g.width, g.height);
    }

    /**
     * Checks for if the given vector is completely "inside" the min and max.
     * @param v
     * @param min
     * @param max
     * @return true if <code>isInRange(v.x, min.x, max.x) &&
               isInRange(v.y, min.y, max.y) &&
               isInRange(v.z, min.z, max.z)</code>
     */
    public static boolean isInRange(PVector v, PVector min, PVector max) {
        return isInRange(v.x, min.x, max.x) &&
               isInRange(v.y, min.y, max.y) &&
               isInRange(v.z, min.z, max.z);
    }

    /**
     * Returns the least residue equivalent of <code>x</code> (mod <code>wrap</code>). The difference between
     * this number and the normal modulo (%) operator is that negative values of <code>x</code> will be converted
     * to their positive equivalents; so wrap(-1, 6) will return 5.
     * @param x
     * @param wrap
     * @return Least residue equivalent of <code>x</code> in modulo </code>wrap</code> space
     */
    public static float wrap(float x, float wrap) {
        float mod = x % wrap;
        if(mod < 0) return mod + wrap;
        else        return mod;
    }

    public static double wrap(double x, double wrap) {
        double mod = x % wrap;
        if(mod < 0) return mod + wrap;
        else        return mod;
    }

//
//    public static double wrap(double x, double wrap) {
//        if (x >= 0)
//            return x % wrap;
//        else
//            return x % wrap + wrap;
//    }

    /**
     * Computes the closest offset between two coordinates in mod wrap space. For instance, in mod 100 space,
     * the "distance" from 10 to 90 is -20, since moving backwards from 10 will get you to 90 after 20 units.
     * The distance from 90 to 10 is +20.
     * @param x1 First coordinate
     * @param x2 Second coordinate
     * @param wrap modulo space
     * @return The "closest" distance from <code>x</code> to <code>target</code>.
     */
    public static float distance(float x1, float x2, float wrap) {
        x1 = wrap(x1, wrap);
        x2 = wrap(x2, wrap);

        if(x1 == x2) return 0;

        if(x1 > x2) {
            if(x1 - x2 > wrap/2) return (x2 + wrap) - x1;
            else return -(x1 - x2);
        }
        else {
            if(x2 - x1 > wrap/2) return x2 - (x1 + wrap);
            else return x2 - x1;
        }
    }

//    public static double constrain(double a, double min, double max) {
//        return Math.min(Math.max(a, min), max);
//    }

    //todo: this has a bug in it! FIX NAO.
    /**
     * Returns the closest radian offset between ang1 and ang2. Note that this is anti-commutative (I think);
     * that is, angleDistance(ang1, ang2) == -angleDistance(ang2, ang1).
     * @param ang1 first angle
     * @param ang2 second angle
     * @return Closest radian offset between the given angles.
     */
    public static float angleDistance(float ang1, float ang2) {
        return distance(ang1, ang2, PApplet.TWO_PI);
    }

    /**
     * Returns an angle that is <code>cap</code> radians closer to <code>wantAng</code>;
     * or just returns <code>wantAng</code> if <code>cap</code> would overshoot.
     * @param angle Starting angle
     * @param wantAng Target angle
     * @param cap Maximum change
     * @return <code>angle</code> turned <code>cap</code> radians closer to wantAng, or wantAng itself if it can reach.
     */
    public static float turnTowardsAngle(float angle, float wantAng, float cap) {
        angle = wrap(angle, PApplet.TWO_PI);
        return angle + PApplet.constrain(angleDistance(angle, wantAng), -cap, cap);
    }

    /**
     * Returns a new angle that is oriented amount% closer to wantAng. amount at .5 returns the angle halfway.
     * amount at 2 returns angle reflected about wantAng.
     * @param angle
     * @param wantAng
     * @param amount
     * @return
     */
    public static float turnTowardsAngleAmount(float angle, float wantAng, float amount) {
        angle = wrap(angle, PApplet.TWO_PI);
        return angle + angleDistance(angle, wantAng)*amount;
    }

    /**
     * Prints out an array in the form<p>
     * [0=s[0].toString()]<br>
     * [1=s[1].toString()]<br>
     * [2....]<br>
     * @param s Array to display
     * @return
     */
    public static String display(Object[] s) {
        String ret = "";
        for (int i = 0; i < s.length; i++) {
            ret += "["+i+"="+s[i]+"]\n";
        }
        return ret;
    }

    /**
     * Sleeps for the given time and then calls System.exit(exitCode). A new thread is created and started
     * immediately.
     * @param seconds
     * @param exitCode
     * @return The thread that will run System.exit
     */
    public static Thread killAfterSeconds(final float seconds, final int exitCode) {
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep((int)(seconds*1000));
                    System.exit(exitCode);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

        };
        t.start();
        return t;
    }

    /**
     * Builds a new string of a given length, comprised of the given character repeated.
     * @param c char to repeat
     * @param len length of string
     * @return
     */
    public static String makeStr(char c, int len) {
        StringBuilder b = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            b.append(c);
        }
        return b.toString();
    }

    /**
     * Checks if the two given vectors are mathematically equivalent; that is, if their coordinates are all equal.
     * @param a
     * @param b
     * @return true if <code>a.x == b.x && a.y == b.y && a.z == b.z</code>
     */
    public static boolean equals(PVector a, PVector b) {
        return a.x == b.x && a.y == b.y && a.z == b.z;
    }

    /**
     * Returns a new PVector that contains the minimum entry in each dimension of the given vectors.
     * @param a
     * @param b
     * @return <code>new PVector(PApplet.min(a.x, b.x), PApplet.min(a.y, b.y), PApplet.min(a.z, b.z))</code>
     */
    public static PVector min(PVector a, PVector b) {
        return new PVector(PApplet.min(a.x, b.x), PApplet.min(a.y, b.y), PApplet.min(a.z, b.z));
    }

    /**
     * Returns a new PVector that contains the maximum entry in each dimension of the given vectors.
     * @param a
     * @param b
     * @return <code>new PVector(PApplet.max(a.x, b.x), PApplet.max(a.y, b.y), PApplet.max(a.z, b.z))</code>
     */
    public static PVector max(PVector a, PVector b) {
        return new PVector(PApplet.max(a.x, b.x), PApplet.max(a.y, b.y), PApplet.max(a.z, b.z));
    }

    /**
     * Returns <code>vec</code> "constrained" by the min and max values. For each dimension (x, y, z), if the value
     * for that dimension is outside the boundary given the by the corresponding entries in the <code>min</code> and
     * <code>max</code> PVectors, then that value will be set to the min/max.
     * @param vec
     * @param min
     * @param max
     * @return
     */
    public static PVector constrain(PVector vec, PVector min, PVector max) {
        return min(max(vec, min), max);
    }

    /**
     * Computes a PVector with the following coordinates:<br>
     * x = mag*cos(phi)*cos(theta)<br>
     * y = mag*cos(phi)*sin(theta)<br>
     * z = mag*sin(phi)<br><br>
     *
     * Note that this is the same as fromPolar(float, float), with a third parameter phi
     * specifying the elevation/azumith/latitude, where 0 is 0, PI/2 is straight up, and -PI/2 is
     * straight down.
     * @param r
     * @param theta
     * @param phi
     * @return
     * @see http://en.wikipedia.org/wiki/Spherical_coordinate_system#Cartesian_coordinates
     */
    public static PVector fromPolar(float r, float theta, float phi) {
        return new PVector(r*PApplet.cos(phi)*PApplet.cos(theta),
                           r*PApplet.cos(phi)*PApplet.sin(theta),
                           r*PApplet.sin(phi));
    }

    /**
     * Returns a new two-dimensional vector with the given magnitude and direction
     * @param r magnitude
     * @param theta direction
     * @return
     */
    public static PVector fromPolar(float r, float theta) {
        return new PVector(r * PApplet.cos(theta), r * PApplet.sin(theta));
    }

    /**
     * Creates a random PVector within the box described by min on one end and max on the other.
     * @param min lower bound for values
     * @param max upper bound for values
     * @return
     */
    public static PVector random(PVector min, PVector max) {
        return new PVector(random(min.x, max.x), random(min.y, max.y), random(min.z, max.z));
    }

    /**
     * Creates a random PVector within the box described by min on one end and max on the other.
     * @param min lower bound for values
     * @param max upper bound for values
     * @return
     */
    public static Vec2 random(Vec2 min, Vec2 max) {
        return new Vec2(random(min.x, max.x), random(min.y, max.y));
    }

    public static Vec2 randomVec2(World w) {
        AABB bounds = w.getWorldAABB();
        return random(bounds.lowerBound, bounds.upperBound);
    }

    /**
     * Returns a new PVector having the same angle as <code>pVector</code>, of magnitude 1.
     * @param pVector
     * @return
     */
    public static PVector normalize(PVector pVector) {
        return PVector.div(pVector, pVector.mag());
    }

    /**
     * Appends the given character <code>c</code> to the end of String <code>s</code> until it is of length
     * <code>wantLen</code>.
     * @param s base string
     * @param c character to append
     * @param wantLen wanted length
     * @return
     */
    public static String lpad(String s, char c, int wantLen) {
        return makeStr(c, wantLen - s.length()) + s;
    }

    /**
     * Prepends the given character <code>c</code> to the end of String <code>s</code> until it is of length
     * <code>wantLen</code>.
     * @param s base string
     * @param c character to append
     * @param wantLen wanted length
     * @return
     */
    public static String rpad(String s, char c, int wantLen) {
        return s + makeStr(c, wantLen - s.length());
    }
//
//    public static int color(double r, double g, double b) {
//        return 255 << 24
//                | (int) Methods.constrain(r, 0, 255) << 16
//                | (int) Methods.constrain(g, 0, 255) << 8
//                | (int) Methods.constrain(b, 0, 255) << 0;
//    }

    /**
     * Returns an integer representing an RGB color having the given components.
     * @param r red component (between 0 and 255)
     * @param g green component (between 0 and 255)
     * @param b blue component (between 0 and 255)
     * @return an int with the components packed into it's bits.
     */
    public static int color(float r, float g, float b) {
        return 255 << 24
                | (int) PApplet.constrain(r, 0, 255) << 16
                | (int) PApplet.constrain(g, 0, 255) << 8
                | (int) PApplet.constrain(b, 0, 255) << 0;
    }

    /**
     * Extracts the red from the given RGB color.
     * @param c
     * @return
     */
    public static int red(int c) {
        return c >> 16 & 0xFF;
    }

    /**
     * Extracts the green from the given RGB color.
     * @param c
     * @return
     */
    public static int green(int c) {
        return c >> 8 & 0xFF;
    }

    /**
     * Extracts the blue from the given RGB color.
     * @param c
     * @return
     */
    public static int blue(int c) {
        return c & 0xFF;
    }

    /**
     * Extracts the red, green, and blue color components from the given RGB color, and returns a new int[] array
     * with the information.
     * @param c
     * @return
     */
    public static int[] rgb(int c) {
        return new int[] {red(c), green(c), blue(c)};
    }

    /**
     * Draws a vector with the given length and label. The <code>headLength</code> variable specifies the length
     * of the triangle to be drawn at the end of the vector as a percentage of the length.
     * @param g a graphics object to draw on
     * @param vec the PVector to draw
     * @param len length of the vector
     * @param label text to draw on the vector
     * @param headLength size of the vector point as a percentage of the length.
     */
    public static void drawVector(PGraphics g, PVector vec, float len, String label, float headLength) {
//        System.out.println("For vector "+label+" of color "+g.red(color)+", "+g.green(color)+", "+g.blue(color)+
//                ": ");
        vec = normalize(vec);
        vec.mult(len);
        drawArrow(g, vec.x, vec.y, vec.z, headLength);
        if (label != null)
            g.text(label, g.screenX(vec.x, vec.y, vec.z), g.screenY(vec.x, vec.y, vec.z));
    }

    /**
     * Draws a vector with the given length and label, and a headLength of .1
     * @param g
     * @param vec
     * @param len
     * @param label
     * @see drawVector(PGraphics, PVector, float, String, float)
     */
    @Deprecated
    public static void drawVector(PGraphics g, PVector vec, float len, String label) {
        drawVector(g, vec, len, label, .1f);
    }

    /**
     * Draws an arrow on the given PGraphics object that goes from (0, 0, 0) to (x, y, z). The headLength parameter
     * determines the size of the arrowhead to be drawn as a percentage of the length of the arrow.
     * @param g
     * @param x
     * @param y
     * @param z
     * @param headLength
     */
    public static void drawArrow(PGraphics g, float x, float y, float z, float headLength) {
//        g.line(0, 0, 0, x, y, z);
        float mag = (float) Math.sqrt(x * x + y * y + z * z);
        g.pushMatrix();
        //TODO: THIS IS HELLA FAULTY. You want to "rotateAtoB(Vec3(0, 0, 1), Vec3(x, y, z))"
        //instead of these two rotates.

        //def rotateAtoB(a:Vec3, b:Vec3) { val c = (a cross b).normalize; val ang = a angleBetween b; rotate(ang, c.x, c.y, c.z); }
        g.rotateY((float) Math.atan2(-z, x));
        g.rotateZ((float) Math.atan2(y, x));
        g.line(0, 0, 0, mag, 0, 0);
        float oneMinus = 1 - headLength;
        g.line(mag, 0, 0, mag * oneMinus, 0, mag * -headLength);
        g.line(mag, 0, 0, mag * oneMinus, 0, mag * +headLength);
        g.line(mag, 0, 0, mag * oneMinus, mag * -headLength, 0);
        g.line(mag, 0, 0, mag * oneMinus, mag * +headLength, 0);
        g.popMatrix();
    }

    /**
     * Draws an arrow with a headLength of .1.
     * @param g
     * @param x
     * @param y
     * @param z
     */
    public static void drawArrow(PGraphics g, float x, float y, float z) {
        drawArrow(g, x, y, z, .1f);
    }

    /**
     * Draws an arrow from (0, 0, 0) to the given vector.
     * @param g
     * @param v
     */
    public static void drawArrow(PGraphics g, PVector v) {
        drawArrow(g, v.x, v.y, v.z);
    }

    /**
     * Draws an arrow from (0, 0, 0) to the given vector of length <code>len</code>.
     * @param g
     * @param v
     * @param len
     */
    public static void drawArrow(PGraphics g, PVector v, float len) {
        v = v.get();
        v.normalize();
        v.mult(len);
        drawArrow(g, v.x, v.y, v.z);
    }

    /**
     * Draws coordinate axes of length len. <p>
     * The X direction will be red and point from (0, 0, 0) to (len, 0, 0).<br>
     * The Y direction will be green and point from (0, 0, 0) to (0 ,len, 0).<br>
     * The Z direction will be blue and point from (0, 0, 0) to (0, 0, len).<br>
     * @param g a graphics object to draw on
     * @param len length of the axes
     */
    public static void drawAxes(PGraphics g, float len) {
        g.pushStyle();
        g.stroke(255, 0, 0);
        drawArrow(g, len, 0, 0);
        g.stroke(0, 255, 0);
        drawArrow(g, 0, len, 0);
        g.stroke(0, 0, 255);
        drawArrow(g, 0, 0, len);
        g.popStyle();
    }

    /**
     * Draws coordinate axes of length 1.
     * @param g a graphics object to draw on
     * @see drawAxes(PGraphics, float)
     */
    public static void drawAxes(PGraphics g) {
        drawAxes(g, 1);
    }

    /**
     * Returns a random object from the given array.
     * @param <E>
     * @param objects
     * @return
     */
    public static <E> E randomElement(E[] objects) {
        return objects[(int) random(objects.length)];
    }

    /**
     * Returns a random float between low (inclusive) and high (exclusive).
     * @param low
     * @param high
     * @return
     */
    public static float random(float low, float high) {
        return (float) Math.random() * (high - low) + low;
    }

    /**
     * Returns a random float between 0 and high.
     * @param high
     * @return
     */
    public static float random(float high) {
        return random(0, high);
    }

    /**
     * Returns a random float between 0 and 1.
     * @return
     */
    public static float random() {
        return random(1);
    }

    /**
     * Tests to see if the argument can be represented as an integer without any loss of precision.
     * @param num number to test
     * @return true if there is no fractional component to <code>num</code>
     */
    public static boolean isInt(float num) {
        return num % 1 == 0;
    }

    /**
     * Maps the <code>value</code> variable, ranged between <code>istart<code> and <code>istop</code>, to it's
     * corresponding value in the range from <code>ostart</code> to <code>ostop</code>.
     * @param value number to interpolate
     * @param istart lower bound of input range
     * @param istop upper bound of input range
     * @param ostart lower bound of output range
     * @param ostop upper bound of output range
     * @return the corresponding number of <code>value</code> in the output range.
     */
    public static final double map(double value, double istart, double istop, double ostart, double ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }

    /**
     * Maps the <code>value</code> variable, ranged between <code>istart<code> and <code>istop</code>, to it's
     * corresponding value in the range from <code>ostart</code> to <code>ostop</code>.
     * @param value number to interpolate
     * @param istart lower bound of input range
     * @param istop upper bound of input range
     * @param ostart lower bound of output range
     * @param ostop upper bound of output range
     * @return the corresponding number of <code>value</code> in the output range.
     */
    public static final float map(float value, float istart, float istop, float ostart, float ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }

    /**
     * Maps the <code>value</code> variable, ranged between <code>istart<code> and <code>istop</code>, to it's
     * corresponding value in the range from <code>ostart</code> to <code>ostop</code>.
     * @param value number to interpolate
     * @param istart lower bound of input range
     * @param istop upper bound of input range
     * @param ostart lower bound of output range
     * @param ostop upper bound of output range
     * @return the corresponding number of <code>value</code> in the output range.
     */
    public static final int map(int value, int istart, int istop, int ostart, int ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }

    private static boolean cacheReady = false;
    private static float[] sinCache, cosCache;

    /**
     * Returns a fast, cached approximation of sin for the given radian angle. The approximaion will be accurate
     * within 1 degree.
     * @param ang angle
     * @return approximation of <code>sin(ang)</code>
     */
    public static float sinf(float ang) {
        if (cacheReady)
            return sinCache[(int) wrap(ang * 180 / PI, 360)];
        else {
            makeCache();
            return sinf(ang);
        }
    }

    /**
     * Returns a fast, cached approximation of cos for the given radian angle. The approximaion will be accurate
     * within 1 degree.
     * @param ang angle
     * @return approximation of <code>cos(ang)</code>
     */
    public static float cosf(float ang) {
        if (cacheReady)
            return cosCache[(int) wrap(ang * 180 / PI, 360)];
        else {
            makeCache();
            return cosf(ang);
        }
    }

    private static void makeCache() {
        cacheReady = true;
        sinCache = new float[360];
        cosCache = new float[sinCache.length];
        for (int i = 0; i < sinCache.length; i++) {
            sinCache[i] = PApplet.sin(i * PI / 180);
            cosCache[i] = PApplet.cos(i * PI / 180);
        }
    }

    /**
     * Returns the sign of f.<p>
     * For f < 0, this method returns -1.<br>
     * For f > 0, this method returns 1.<br>
     * For f = 0, this method returns 0.<br>
     * @param f
     * @return
     */
    public static float signum(float f) {
        if(f < 0) return -1;
        else if(f > 0) return 1;
        return 0;
    }

    public static int randomColor() {
        return 255 << 24 | (int)random(256) << 16 | (int)random(256) << 8 | (int)random(256);
    }

    public static float constrain(float val, float min, float max) {
        return Math.max(Math.min(val, max), min);
    }

    public static <T> Set<T> toSet(T[] aArr) {
        return new HashSet(Arrays.asList(aArr));
    }

    /**
 *  A useful toString() method
 *  that handles both null's and Throwable's
 *
 * @author     Dr. M.P. Ford
 * @created    October 21, 2001
 * @version    0.1 15th Jan 1998
 */
  public static String toString(Object obj) {
        if (obj == null) {
            return "NULL";
        }

        Throwable e;
        if (obj instanceof Throwable) {
            e = (Throwable) obj;
            StringWriter strWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(strWriter);
            e.printStackTrace(printWriter);
            return strWriter.toString();
        } //else {
        try {
            return obj.toString();
        } catch (Throwable t) {
            return (toString(t));
        }
    }


    /**
     *
     * @param applet
     * @param modelWeight
     * @param scale
     * @see setStrokeWeightScreen(PApplet, float, Camera)
     */
    public static void strokeWeightScreen(PApplet applet, float screenWeight, float scale) {
        applet.strokeWeight(screenWeight / scale);
    }

    /**
     * Sets the stroke weight of the given PApplet to be the given float in screen coordinates after the
     * Applet has undergone the Camera's transformation. Call this method after <code>c.apply()</code> in
     * order for your strokes to have <code>screenWeight</code> weight in screen coordinates.
     * @param applet
     * @param screenWeight
     * @param c
     */
    public static void strokeWeightScreen(PApplet applet, float screenWeight, Camera c) {
        strokeWeightScreen(applet, screenWeight, c.scale);
    }

    public static void strokeWeightModel(PApplet applet, float modelWeight, float scale) {
        applet.strokeWeight(modelWeight * scale);
    }

    /**
     * Sets the stroke weight of the given PApplet to be the given float in model coordinates after the
     * Applet has undergone the Camera's transformation. Call this method after <code>c.apply()</code> in
     * order for your strokes to have <code>screenWeight</code> weight in model coordinates.
     * @param applet
     * @param screenWeight
     * @param c
     */
    public static void strokeWeightModel(PApplet applet, float modelWeight, Camera c) {
        strokeWeightScreen(applet, modelWeight, c.scale);
    }

    /**
     * Returns a Vec2 with the same x and y coordinates as the given PVector.
     * @param pv
     * @return
     */
    public static Vec2 pVector2Vec2(PVector pv) {
        return new Vec2(pv.x, pv.y);
    }

    /**
     * Returns a PVector with the same x/y coordinates as the given Vec2, and a z coordinate of zero.
     */
    public static PVector vec22PVector(Vec2 v2) {
        return new PVector(v2.x, v2.y);
    }

    public static Iterator<Body> bodyIterator(final World w) {
        return new Iterator<Body>() {

            Body next = w.getBodyList(); //The next call to "next()" will return this body.
            Body last = null;

            public boolean hasNext() {
                return next != null;
            }

            public Body next() {
                last = next;
                next = next.getNext();

                return last;
            }

            public void remove() {
                w.destroyBody(last);
            }
        };
    }

    public static Iterator<Shape> shapeIterator(final Body b) {
        return new Iterator<Shape>() {

            Shape next = b.getShapeList(), last = null;

            public boolean hasNext() {
                return next != null;
            }

            public Shape next() {
                last = next;
                next = next.getNext();

                return last;
            }

            public void remove() {
                b.destroyShape(last);
            }
        };
    }

    public static float getTranslateX(PApplet p) {
        return (float) getGraphics2D(p).getTransform().getTranslateX();
    }

    public static float getTranslateY(PApplet p) {
        return (float) getGraphics2D(p).getTransform().getTranslateY();

    }

    public static float getScaleX(PApplet p) {
        return (float) getGraphics2D(p).getTransform().getScaleX();
    }

    public static float getScaleY(PApplet p) {
        return (float) getGraphics2D(p).getTransform().getScaleY();

    }

    public static Graphics2D getGraphics2D(PApplet p) {
        return getGraphics2D(p.g);
    }

    public static Graphics2D getGraphics2D(PGraphics g) {
        if(!(g instanceof PGraphicsJava2D))
            throw new RuntimeException("Renderer is not JAVA2D!");
        return ((PGraphicsJava2D)g).g2;
    }

    public static float getAngle(Vec2 dir) {
        return (float) Math.atan2(dir.y, dir.x);
    }

    public static boolean isZero(Vec2 dir) {
        return dir.x == 0 && dir.y == 0;
    }

    public static long checkSum(World w) {
        long sum = 2;
        for(Body b = w.getBodyList(); b != null; b = b.getNext()) {
            final long cs1 = (long) checkSumHelper(b.getAngle(), b.getAngularVelocity());
            final long cs2 = (long) checkSumHelper(b.getPosition().x, b.getPosition().y);
            final long cs3 = (long) checkSumHelper(b.getLinearVelocity().x, b.getLinearVelocity().y);
            sum = sum * 13 + (cs1 << 24)
                           + (cs2 << 12)
                           + cs3;
        }
        return sum;
    }

    public static String vec2ToString(Vec2 v) {
        return v.x+" "+v.y;
    }
    public static Vec2 stringToVec2(String s) {
        String elms[] = s.split(" ");
        return new Vec2(Float.parseFloat(elms[0]), Float.parseFloat(elms[1]));
    }

    public static int checkSumHelper(float a, float b) {
        return Float.floatToIntBits(a) ^ Float.floatToIntBits(b);
    }

    public static void writeDocumentToFile(Document d, File f) {
        try {
            writeDocument(d, new FileOutputStream(f));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    public static void writeDocument(Document d, OutputStream out) {
        try {
            OutputFormat fmt = new OutputFormat(d);
            fmt.setIndent(1);
            fmt.setIndenting(true);
//            XMLSerializer serializer = new XMLSerializer(out, fmt);
            XMLSerializer serializer = new XMLSerializer(out, null);
            serializer.serialize(d);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static Document readDocumentFromFile(File f) {
        try {
            //        InputSource in = new InputSource(f.toURI().toASCIIString()); //copied from line 207 of javax.xml.parsers.DocumentBuilder
            return readDocument(new FileInputStream(f));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static Document readDocument(InputStream in) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);
            return doc;
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /*public static float[][] generatePlasmaFractal(float low, float high, float[][] array) {
    int width = array.length,
    height = array[0].length;
    if(!isInt((float)(Math.log(array.length)/Math.log(2))) | !isInt((float)(Math.log(array[0].length)/Math.log(2)))) {
    throw new IllegalArgumentException("array must have dimensions that are powers of two");
    }
    //        int stepsX = (int)(Math.log(array.length)/Math.log(2)),
    //            stepsY = (int)(Math.log(array[0].length)/Math.log(2));
    //set the initial corners; this counts as a step
    array[0][0] = random(low, high);
    array[width][0] = random(low, high);
    array[width][height] = random(low, high);
    array[0][height] = random(low, high);
    int steps = (int)(Math.log(array.length)/Math.log(2)) - 1;
    for(int a = 0; a < steps; a++) {

    }
    return array;
    }*/
//    public static float[][] generatePlasmaFractal(float low, float high, int size) {
////        float[][] array = new float[size][size];
////        float[][] points = new float[size + 1][size + 1];
////        points[0][0] = random(low, high);
////        points[size][0] = random(low, high);
////        points[size][size] = random(low, high);
////        points[0][size] = random(low, high);
////        int steps = (int) (log(size) / log(2));
////        for (int a = 1; a < steps; a++) {
////            int dist = size / (int) pow(2, a);
////            for (int x = 0; x < size; x += dist) {
////            }
////        }
////        Plasma[]
//    }
//    public static float[][] generatePlasmaFractal(float mid, float rough, int size) {
//        Plasma[][] plas = new Plasma[size][size];
//        Plasma first = new Plasma(new Point(0, 0, mid), new Point(size, 0, mid), new Point(size, size, mid), new Point(0, size, mid));
//        Plasma[] genned = first.generate(rough);
//        plas[0][0] = genned[0];
//        plas[0][size] = genned[1];
//        plas[size][size] = genned[2];
//        plas[0][size] = genned[3];
//
//    }
}
