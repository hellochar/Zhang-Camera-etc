/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zhang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * A deep hash contains a set of all the members that are of exactly class T,
 * and a map to deephashes that subtype T.
 * @author Owner
 */
public class DeepHash<T> {

    //Todo: possibly have interface maps?

    /**
     * Contains all members that are exactly type T.
     */
    HashSet<T> set;
    HashMap<Class<? extends T>, DeepHash<? extends T>> map;
    /**
     * The class of type T.
     */
    final Class<T> c;
//    float x, y;

    public static void main(String[] args) {
        Number n;
        DeepHash<Number> hash = new DeepHash(Number.class);
    }

    public DeepHash(/*float x, float y, */Class<T> c) {
        this.c = c;
        set = new HashSet();
        map = new HashMap();
    }

    /**
     * Returns true if this DeepHash represents exactly the members belonging to the given class.
     * @param o
     * @return
     */
    public boolean isSetFor(Class o) {
        return o == c;
    }

    /**
     * Returns true if this DeepHash is the direct superclass of the given class.
     * @param o
     * @return
     */
    public boolean isParentFor(Class o) {
        return o.getSuperclass() == c;
    }

    /**
     * If o is this one's, return this.
     * If o is a direct child of this one's, return the child.
     * Otherwise, create DeepHashes for every class that has not yet been created between this one's and o.
     * @param <K>
     * @param o
     * @return
     */
    protected <K extends T> DeepHash<K> getHashSetFor(final Class<K> o) {
        if (isSetFor(o)) {
            return (DeepHash<K>) this;
        }
        if (isParentFor(o)) {
            DeepHash<K> k;
            if (!map.containsKey(o)) {
                k = new DeepHash<K>(o);
                map.put(o, k);
            } else {
                k = (DeepHash<K>) map.get(o);
            }
            return k;
        }
        LinkedList<Class<? extends T>> list = new LinkedList<Class<? extends T>>();
        Class temp = o;
        while (!isSetFor(temp)) {
            list.add(temp);
            temp = (Class<K>) temp.getSuperclass();
        }
        Iterator<Class<? extends T>> it = list.descendingIterator(); //Terrier, Dog
        DeepHash cur = this; //start from this and go down the list
        try {
            for (Class cl = it.next();; cl = it.next()) {
                cur = cur.getHashSetFor(cl);
            }
        } catch (NoSuchElementException e) {
            //This will happen when it.next() throws an error, meaning that the list is ended.
        }
        return cur;
//        if(isParentFor(o)) {
//            DeepHash<K> k;
//            if(!map.containsKey(o)) {
//                k = new DeepHash<K>(o);
//                map.put(o, k);
//            } else {
//                k = (DeepHash<K>) map.get(o);
//            }
//            return k;
//        }
//        else if(isSetFor(o)) {
//            return (DeepHash<K>) this;
//        }
//        else {
//            return getHashFor(o.getSuperclass());
//        }
    }

    /**
     * Add the specified object to the DeepHash.
     * @param <K>
     * @param which
     */
    public <K extends T> void add(K which) {
        getSet((Class<K>) which.getClass()).add(which);
    }

    public HashSet<T> getSet() {
        return set;
    }

    /**
     * Returns all members of exactly type c.
     * @param <K>
     * @param c
     * @return
     */
    public <K extends T> HashSet<K> getSet(Class<K> c) {
        return getHashSetFor(c).set;
    }

    /**
     * Returns all members that are instancesof this type.
     * @return
     */
    public HashSet<? extends T> getAllSets() {
        HashSet<? extends T> s = new HashSet<T>(getSet());
        for (DeepHash h : map.values()) {
            s.addAll(h.getAllSets());
        }
        return s;
    }

    /**
     * Returns all instancesof the given class.
     * @param <K>
     * @param c
     * @return
     */
    public <K extends T> HashSet<? extends K> getAllSets(Class<K> c) {
        return getHashSetFor(c).getAllSets();
    }
}
