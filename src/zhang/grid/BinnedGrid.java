
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zhang.grid;

import java.util.ArrayList;
import java.util.Collection;
import processing.core.PApplet;

/** A <code>Grid</b> employing a binning technique; it is backed by a two dimensional array of ArrayLists. Each block is of equal size,
 * specified either in the constructor or defaulting to 32. 
 *
 * @author hellochar
 */
public class BinnedGrid<E extends Entity> extends Grid<E> {

    private final Collection<E>[][] blocks;
    public final int blockSize;

    public BinnedGrid(int gridsX, int gridsY, int blockSize) {
        super(0, 0, gridsX*blockSize, gridsY*blockSize);
        this.blockSize = blockSize;
        blocks = new ArrayList[gridsX][gridsY];
    }

    public BinnedGrid(int width, int height) {
        this(width, height, 32);
    }

    @Override
    public String toString() {
        return "UniformGrid: " +
               "[MAXX, MAXY="+blocks.length+", "+blocks[0].length+"], " +
               "[blockSize="+blockSize+"], " +
               super.toString();
    }

    @Override
    protected boolean add(E e) {
        boolean free = true;
        if (e.getGrid() != null)
            free = e.getGrid().pollRemove(e);

        //If the entity was successfully removed, then we can add him to us.
        if(free) {
            super.add(e);
            e.curSet.add(e);
            return true;
        } else { //If the entity wasn't removed, then fuck. //Todo: figure out something better to do than putting him in a state of limbo
            e.setGrid(null);
            e.curSet = null;
            return false;
        }
    }

    @Override
    protected boolean remove(E e) {
        if (e.grid != this)
            return false;
        else {
            e.curSet.remove(e);
            e.grid = null;
            super.remove(e);
            return true;
        }
    }

    public Collection<E> getInRect(float x, float y, float w, float h) {
        Collection<E> ret = new ArrayList<E>();
        int tlx = grid(x - w),
                tly = grid(y - h),
                brx = grid(x + w),
                bry = grid(y + h);
        for (int i = tlx; i <= brx; i++) {
            for (int j = tly; j <= bry; j++) {
                if (isValidGridCoord(i, j)) {
                    Collection<E> set = cellAtGrid(i, j);
                    if (i == tlx || i == brx || j == tly || j == bry) { //what to do if listAt only marginally inside
                        for (E e : set) {                         //go through each entity and check if it's in the rect
                            if (e.x() >= x - w &&
                                    e.y() >= y - h &&
                                    e.x() < x + w &&
                                    e.y() < y + h)
                                ret.add(e);
                        }//end of for loop
                    } else { //what to do if fully inside range
                        ret.addAll(set);
                    }
                }
            }
        }
        return ret;
    }

    //Todo: this is absolutely ridiculous. Find a better way to implement this.
    public Collection<E> getInCircle(float x, float y, float r) {
//        Collection<Collection<E>> ret = new ArrayList<Collection<E>>();
        //Todo: this is ridiculous. There's got to be a better way to do this.
        Collection<E> in = new ArrayList();
        int minX = grid(x - r), maxX = grid(x + r);
        //"flattening" problem: if there's an even number of x to go through, there's a problem where the very top or bottom of the circle isn't accounted for. this problem is caused
        //by iterating through the x, and is minor if the grid size is small.

        //test for if the circle is completely contained in one horizontal
        if (minX == maxX) {
            int minY = grid(y);
            addEdges(in, minX, minY, x, y, r);
            //flattening problem in one circle
            if (r > (y - blockSize * minY)) {
                addEdges(in, minX, minY - 1, x, y, r);
            } else if (r > ((minY + 1) * blockSize - y)) {
                addEdges(in, minX, minY + 1, x, y, r);
            }
        } else {
            boolean check = (maxX - minX) % 2 == 0;
            int prevYTop = grid(y), prevYBottom = prevYTop;
            for (int k = minX; k <= maxX; k++) {
                float nextX = (k + 1) * blockSize;
                int nextYTop = grid(y - PApplet.sqrt(r * r - PApplet.sq(nextX - x))),
                        nextYBottom = grid(y + PApplet.sqrt(r * r - PApplet.sq(nextX - x)));
                if (k == minX) {
                    for (int i = nextYTop; i <= nextYBottom; i++) {
                        addEdges(in, k, i, x, y, r);
                    }
                } else if (k == maxX) {
                    for (int i = prevYTop; i <= prevYBottom; i++) {
                        addEdges(in, k, i, x, y, r);
                    }
                } else {
                    //account for flattening problem
                    if (check && k == (maxX + minX) / 2) {
                        if (r > (y - blockSize * nextYTop)) {
                            //problem has occured at the top. account for it.
                            addEdges(in, k, nextYTop - 1, x, y, r);
                        }
                        if (r > ((nextYBottom + 1) * blockSize - y)) {
                            //problem has occured at the bottom. account for it.
                            addEdges(in, k, nextYBottom + 1, x, y, r);
                        }
                    }

                    //add the edges of the top of the circle
                    int dir = (int) Math.signum(nextYTop - prevYTop), i = prevYTop;
                    do {
                        addEdges(in, k, i, x, y, r);
                        i += dir;
                    } while (i != nextYTop);
                    addEdges(in, k, i, x, y, r);

                    //all in middle
                    for (i = PApplet.max(prevYTop, nextYTop) + 1; i < PApplet.min(prevYBottom, nextYBottom); i++)
                        if (isValidGridCoord(k, i))
                            in.addAll(cellAtGrid(k, i));

                    //add the edges of the bottom of the circle
                    dir = (int) Math.signum(nextYBottom - prevYBottom);
                    i = prevYBottom;
                    do {
                        addEdges(in, k, i, x, y, r);
                        i += dir;
                    } while (i != nextYBottom);
                    addEdges(in, k, i, x, y, r);
                }
                prevYTop = nextYTop;
                prevYBottom = nextYBottom;
            }
        }
        //previous method
//        Set<IVec2> outer = getInCircleChild(x, y, radius-blockSize, getInCircleChild(x, y, radius, new HashSet<IVec2>()));
//        //outer is guaranteed to contain the outer edge and then some.
//        for (IVec2 v : outer) {
//            for(E e : cellAtGrid(v)) {
//                if(PApplet.dist(e.x(), e.y(), x, y) < radius) {
//                    set.add(e);
//                }
//            }
//        }
//        HashSet<IVec2> inner = new HashSet<IVec2>();
//        for(float rad = radius-blockSize*2; rad > 0; rad -= blockSize)
//            getInCircleChild(x, y, rad, inner);
//        //these are all completely inside the circle, so you can just add all of them
//        for(IVec2 v : inner) {
//            set.addAll(cellAtGrid(v));
//        }
//        ret.add(in);
        return in;
    }

    private void addEdges(Collection<E> set, int gx, int gy, float x, float y, float r) {
        if (isValidGridCoord(gx, gy)) {
            for (E e : cellAtGrid(gx, gy)) {
                if (e.dist(x, y) < r) {
                    set.add(e);
                }
            }
        }
    }

    private boolean isValidGridCoord(int x, int y) {
        return x > 0 && y > 0 && x < blocks.length && y < blocks[0].length;
    }

    /**
     * Converts the given world coordinate to its grid coordinate.
     * @param val
     * @return
     */
    public int grid(float val) {
        if (val >= 0)
            return (int) val / blockSize;
        else
            return (int) val / blockSize - 1;
    }

    public int world(int grid) {
        return grid * blockSize;
    }

    /**
     * Returns the grid at the given grid coordinate
     * @param loc
     * @return
     */
    public Collection<E> cellAtGrid(int x, int y) {
        return (blocks[x][y] == null ? blocks[x][y] = new ArrayList() : blocks[x][y]);
    }

    /**
     * Returns the grid at the given world coordinate
     * @param loc
     * @return
     */
    @Override
    public Collection<E> cellAt(float x, float y) {
        return cellAtGrid(grid(x), grid(y));
    }

}
