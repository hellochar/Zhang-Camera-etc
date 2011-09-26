package zhang.grid;

import java.text.DecimalFormat;
import java.util.Collection;
import processing.core.PApplet;

/**
 * Some type of being that lives inside a <code>Grid</code>. 
 * @author hellochar
 * @see Grid
 */
abstract public class Entity {

    Grid grid;
    protected Collection curSet;
    private float x, y, nx, ny; //current x, y and new x, y
    int iterationsAlive = 0;
//    private final int createdIteration;
//    boolean canIntersect = true;
    private static final DecimalFormat fmt = new DecimalFormat("##00.000");

    public Entity(float x, float y, Grid grid) {
        this.x = nx = x;
        this.y = ny = y;
        grid.pollAdd(this);
//        createdIteration = grid.getSteps();
    }

    @Override
    public String toString() {
        return "["+getClass().getSimpleName()+"], [x, y="+fmt.format(x)+", "+fmt.format(y)+"]"+", [nx, ny="+fmt.format(nx)+", "+fmt.format(ny)+"], [iterationsAlive="+iterationsAlive+"], [grid="+grid+"]";
    }

    /**
     * Calculates the distance to another Entity.
     * @param e
     * @return
     */
    public float dist(Entity e) {
        return dist(e.x(), e.y());
    }

    public float dist(float x, float y) {
        return PApplet.dist(x, y, x(), y());
    }

    public float angleTo(Entity e) {
        return angleTo(e.x(), e.y());
    }
    
    public float angleTo(float x, float y) {
        return PApplet.atan2(y - y(), x - x());
    }

//    public void setIntersects(boolean intersects) {
//        this.canIntersect = intersects;
//    }
    public final float x() {
        return x;
    }

    public final float y() {
        return y;
    }

    public int getIterationsAlive() {
        return iterationsAlive;
    }

    /**
     * Remove this entity from the grid it currently belongs to.
     */
    public void remove() {
        if(grid != null)
            grid.pollRemove(this);
    }

    /**
     * Sets the location of this entity.
     * @param x
     * @param y
     */
    public void set(float x, float y) {
        nx = x;
        ny = y;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    /**
     * Implement with what your thing should do each iteration
     */
    public abstract void run();

    /**
     * How to draw this thing.
     */
    abstract public void show();

    /**
     * This method gets called when the Entity runs out of bounds. The default implementation is to do nothing.
     */
    public void outOfBounds() {
    }

    /**
     * Subclass to say whether a given location is inside this entity. This is useful for collisions. The
     * default implementation returns false.
     * @param x
     * @param y
     * @return
     */
    public boolean containsPoint(float x, float y) {
        return false;
    }

    /**
     * Sets the state of this Entity to the most recent one. You probably won't have to call this.
     */
    public void update() {
        x = nx;
        y = ny;
        iterationsAlive++;
        if (grid.isInBounds(this)) {
            Collection<Entity> newSet = grid.cellAt(nx, ny);
            //todo: this might be terribly buggy. Possibly comment out.
            if (newSet != curSet) {
                newSet.add(this);
                curSet.remove(this); //comment out if iterating through the blocks
                curSet = newSet;
            }
        }
        else {
            outOfBounds();
        }
    }

    public float nx() {
        return nx;
    }
    public float ny() {
        return ny;
    }

}
