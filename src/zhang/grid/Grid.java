/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package zhang.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Todo: Rewrite. This is pretty terrible. Lacking Vector capabilities, no control over position, no velocity or acceleration.
/** A 2D data structure that holds references to Entities a grid like pattern. This class works
 * with <code>Entity</code> to create an easy system for simulation. Subclass the
 * <code>Entity</code> class and make your own specific behavior. Then add entities to this Grid
 * with <code>pollAdd(E)</code>. Finally, call the <code>step()</code> and <code>show()</code>
 * methods. <br> <br>
 * Steps: <br>
 * a) Construct a UniformGrid of a RecursiveGrid<br>
 * b) Write subclasses of Entity<br>
 * c) Call pollAdd, pollRemove to add/remove your entities from the grid<br>
 * d) Call step() to move the simulation forward once, and call show() to render your Entities<br>
 * @author hellochar
 * @see Entity
 * @see UniformGrid
 * @see RecursiveGrid
 * @see pollAdd(E)
 */
public abstract class Grid<E extends Entity> implements Iterable<E> {
    
    private List<E> allEntities;
    private Set<E> toAdd, toRemove;
    private int iteratorCount = 0;
    protected int iteration = 0;
    public final float x, y, width, height;

    public Grid(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        allEntities = new ArrayList();
        toAdd = new HashSet();
        toRemove = new HashSet();
    }
    
    public Grid(float width, float height) {
        this(0, 0, width, height);
    }

    /**
     * If this grid is under iteration, the entity will get added to the queue of entities to be added.
     * Otherwise, this method will attempt to add the entity to the grid. Use this to add new Entities
     * inside a run() or update() method.
     * @param e
     * @return true if the Entity is now in the grid, false otherwise
     */
    public boolean pollAdd(E e) {
//        checkBounds(e);
        if(!isInBounds(e)) { e.outOfBounds(); return false; }
//        System.out.println(e+" wants to be added to "+this+"!");
        if (isBeingIterated()) {
            toAdd.add(e);
            return false;
        } else
            return add(e);
    }

    /**
     * If this grid is under iteration, the entity will get added to the queue of entities to be removed.
     * Otherwise, this method will attempt to remove entity from the grid.
     * @param e
     * @return true if the Entity is no longer in the grid, false otherwise
     */
    public boolean pollRemove(E e) {
//        checkBounds(e);
        if(!isInBounds(e)) { e.outOfBounds(); return false; }
        if (isBeingIterated()) {
            toRemove.add(e);
            return false;
        } else
            return remove(e);
    }

    /**
     * Move the simulation forward by one iteration.
     */
    public void step() {
        run();
        update();
        iteration++;
    }

    /**
     * Render all entities.
     */
    public void show() {
        iteratorCount++;
        for (Entity e : allEntities) {
            e.show();
        }
        iteratorCount--;
    }

    /**
     * Returns the first entity that occupies the specified world location
     * @param x
     * @param y
     * @return
     */
    public E entityAt(float x, float y) {
//        Collection<E> set = cellAt(x, y);
        iteratorCount++;
        for (E e : allEntities) {
            if (e.containsPoint(x, y))
                return e;
        }
        iteratorCount--;
        return null;
    }

    /**
     * Returns the number of times this Grid has been stepped.
     * @return
     */
    public int getSteps() {
        return iteration;
    }

    /**
     * Tests whether an Iterator object exists who is currently iterating over this Grid.
     * @return
     */
    public boolean isBeingIterated() {
        //todo: make this better and actually work.
        return iteratorCount != 0;
    }

    /**
     * Tests whether this Grid contains the given entity.
     * @param e
     * @return
     */
    public boolean contains(E e) {
        return allEntities.contains(e);
    }

    /**
     * Returns an unmodifiable copy of all the entities.
     * @return
     */
    public List<E> getAllEntities() {
        return java.util.Collections.unmodifiableList(allEntities);
    }
    
    /**
     * Remove all Entities from this Grid.
     */
    public void clear() {
        handleAdds();
        iteratorCount++;
        for (E e : allEntities)
            pollRemove(e);
        iteratorCount--;
        handleRemoves();
    }

    /**
     * Returns all entities that fall within the given rectangle.
     * @param x top left x coordinate
     * @param y top left y coordinate
     * @param w width
     * @param h height
     * @return
     */
    public abstract Collection<E> getInRect(float x, float y, float w, float h);

    /**
     * Returns all entities that fall within the given circle.
     * @param x center x coordinate
     * @param y center y coordinate
     * @param r radius of the circle
     * @return
     */
    public abstract Collection<E> getInCircle(float x, float y, float r);

    /**
     * Tests whether the given entity is within the bounds of this Grid.
     * @param e
     * @return
     */
    public boolean isInBounds(E e) {
        return isInBounds(e.x(), e.y());
    }

    /**
     * Tests whether the given point is within the bounds of this Grid.
     * @param xF
     * @param yF
     * @return
     */
    public boolean isInBounds(float xF, float yF) {
        return (xF > x && xF < x+width &&
               yF > y && yF < y+height);
    }

    public Iterator<E> iterator() {
        iteratorCount++;
        Iterator<E> e = new Iterator<E>() {
            Iterator<E> listI = allEntities.iterator();

            public E next() {       return listI.next();}
            public void remove() {  listI.remove();     }

            public boolean hasNext() {
                boolean k = listI.hasNext();
                if(!k) iteratorCount--;
                return k;
            }

        };
        return e;
    }

    /**
     * Returns the underlying grid cell at the given world location
     * @param x
     * @param y
     * @return
     */
    public abstract Collection<E> cellAt(float x, float y);
    
    @Override
    public String toString() {
        return "Grid: [" + x + ", " + y + ", " + width + ", " + height + "], [iteration="+iteration+"], [iteratingCount="+iteratorCount+"]";
    }

    /**
     * Throws an error if the entity is outside the scope of this grid.
     * @param e
     */
//    protected void checkBounds(E e) {
//        if(!isInBounds(e))
//             throw new IllegalArgumentException("Entity "+e+" is out of bounds!");
//    }

//======================BEGIN PROTECTED/PRIVATE DEFINITIONS=======================
    private void handleAdds() {
        for (Iterator<E> it = toAdd.iterator(); it.hasNext();) {
            E e = it.next();
            add(e);
            if (e.grid == this)
                it.remove();
        }
    }

    private void handleRemoves() {
        for (E e : toRemove) {
            remove(e);
        }
        toRemove.clear();
    }

    /**
     * Override this but CALL SUPER.
     * @param e
     * @return
     */
    protected boolean add(E e) {
        if(allEntities.add(e)) {
            e.setGrid(this);
            e.curSet = cellAt(e.x(), e.y());
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Override this but CALL SUPER.
     * @param e
     * @return
     */
    protected boolean remove(E e) {
        return allEntities.remove(e);
    }

    protected void run() {
        iteratorCount++;
        for (E e : allEntities)
            e.run();
        iteratorCount--;
    }

    protected void update() {
        iteratorCount++;
        for (Entity e : allEntities) {
            e.update();
        }
        iteratorCount--;
        handleAdds();
        handleRemoves();
    }

}