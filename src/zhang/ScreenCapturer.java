/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package zhang;

import com.sun.awt.AWTUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Captures the screen at a given location, using its 
 * @author hellochar
 */
public class ScreenCapturer {

    JFrame frame;
    JPanel panel;
    JLabel label;
    public static final Color transparent = new Color(255, 255, 255, 50);
    boolean closed = false;
    
    public ScreenCapturer(String frameName) {
        frame = new JFrame(frameName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new JPanel(new BorderLayout()) {

            protected void paintComponent(Graphics g) {
                if (g instanceof Graphics2D) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(transparent);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    super.paintComponent(g);
                }
            }
        };
        panel.setOpaque(false);
        frame.setContentPane(panel);
        
        label = new JLabel(); label.setBackground(Color.red); label.setOpaque(true); label.setFont(new Font("Serif", Font.BOLD, 25));
        frame.add(label, BorderLayout.NORTH);
        frame.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setUndecorated(true); frame.setAlwaysOnTop(true);
        AWTUtilities.setWindowOpaque(frame, false);
        frame.pack();
        frame.setVisible(true);
        
        frame.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    frame.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                }
            }
            
        });
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                closed = true;
            }
            
        });
    }
    
    /**
     * Will capture the next mouse press and return the point.
     * @return 
     */
    public Point captureMouse() {
        final Thread curThread = Thread.currentThread();
        final Point p = new Point();
        MouseAdapter listener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                p.setLocation(e.getLocationOnScreen());
                frame.removeMouseListener(this);
                curThread.interrupt(); //this should be the only thing to proc position A
            }
        };
        frame.addMouseListener(listener);
        try {
            curThread.join(); //this is position A.
        } catch (InterruptedException ex) {
        } finally {
            return p;
        }
    }
    
//    /**
//     * Captures the next key press and returns the keyCode.
//     * @return 
//     */
//    public int captureKey() {
//        
//    }
    
    public void waitUntilKeyPressed(int keyCode) {
        final Thread curThread = Thread.currentThread();
        KeyAdapter listener = new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_Z) {
                    frame.removeKeyListener(this);
                    curThread.interrupt(); //this should be the only thing to proc position A
                }
            }
        };
        frame.addKeyListener(listener);
        try {
            curThread.join(); //this is position A.
        } catch (InterruptedException ex) {
        }
    }
}
