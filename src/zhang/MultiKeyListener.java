/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package zhang;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author hellochar
 */
public class MultiKeyListener extends KeyAdapter implements Serializable {

    static final public long serialVersionUID = 15721943294129L;
    Set<Integer> keys, keyView;

    public MultiKeyListener() {
        keys = Collections.synchronizedSet(new HashSet(4));
        keyView = java.util.Collections.unmodifiableSet(keys);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
//        if(e.isActionKey()) {
//        synchronized (keys)
            keys.add(e.getKeyCode());
//        }
//        else {
//            keys.add((int)e.getKeyChar());
//        }
    }


    @Override
    public void keyReleased(KeyEvent e) {
//        if(e.isActionKey()) {
//        synchronized (keys)
            keys.remove(e.getKeyCode());
//        }
//        else {
//            keys.remove((int)e.getKeyChar());
//        }
    }

    /**
     * Returns true if a specified keyCode is pressed, as defined in java.awt.event.KeyEvent.
     * @param key
     * @return
     * @see KeyEvent
     */
    public boolean isPressed(int keyCode) {
        return keys.contains(keyCode);
    }

    public Set<Integer> allKeys() {
        return keyView;
    }
    
    public void clear() {
//        synchronized (keys)
            keys.clear();
    }

}
