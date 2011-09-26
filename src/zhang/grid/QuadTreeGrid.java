///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package zhang.grid;
//
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.Set;
//import java.util.HashSet;
//import toxi.geom.Rect;
//import toxi.geom.Vec2D;
//
///**
// * A Quadtree implementation of Grid.
// * @author hellochar
// * @see Grid
// */
//public class QuadTreeGrid<E extends Entity> extends Grid<E> {
//
//    QTNode main;
//
//    public QuadTreeGrid(float x, float y, float width, float height, int capacity) {
//        super(x, y, width, height);
//        main = new QTNode(this, x, y, width, height, capacity);
//    }
//
//    public QuadTreeGrid(float x, float y, float width, float height) {
//        this(x, y, width, height, 10);
//    }
//
//    public QuadTreeGrid(float width, float height) {
//        this(0, 0, width, height);
//    }
//
//    @Override
//    protected boolean add(E e) {
//        if(super.add(e))
//            return main.add(e);
//        else return false;
//    }
//
//    @Override
//    protected boolean remove(E e) {
//        if(super.remove(e))
//            return main.remove(e);
//        else return false;
//    }
//
//    @Override
//    public boolean contains(E e) {
//        return main.contains(e);
//    }
//
//    @Override
//    public Collection<E> getInRect(float x, float y, float w, float h) {
//        return main.getInRect(x, y, w, h);
//    }
//
//    @Override
//    public Collection<E> getInCircle(float x, float y, float r) {
//        return main.getInCircle(x, y, r);
//    }
//
//    @Override
//    public void clear() {
//        main.clear();
//        super.clear();
//    }
//
//    @Override
//    public Collection<E> cellAt(float x, float y) {
//        if(!isInBounds(x, y)) return null;
//        return main.nodeFor(x, y);
//    }
//
//    public String toString() {
//        String s = main.toString()+super.toString();
//        return s;
//    }
//
//
//    private static class QTNode<E extends Entity> extends Rect implements Collection<E> {
//
//        QuadTreeGrid owner;
//        QTNode parent;
//        QTNode[] children;
//        //todo: change object array into a list and a set and see how those work, performance wise.
//        Set<E> members = new HashSet<E>(); //a HashSet won't be able to handle duplicate vectors.
//        private final int capacity;
////        Rect thisRect;
//
//        public QTNode(QuadTreeGrid owner, float x, float y, float width, float height, int capacity) {
//            super(x, y, width, height);
//            this.owner = owner;
////            thisRect = new Rect(x, y, width, height);
//            this.capacity = capacity;
//        }
//
//        protected QTNode(QTNode parent, int index) {
//            this(parent.owner, parent.x + (index % 2) * parent.width / 2,
//                    parent.y + (index / 2) * parent.height / 2,
//                    parent.width / 2, parent.height / 2, parent.capacity);
//            this.parent = parent;
//        }
//
//        public QTNode(QuadTreeGrid owner, float width, float height, int capacity) {
//            this(owner, 0, 0, width, height, capacity);
//        }
//
//        @Override
//        public String toString() {
//            return "QTNode: [size="+size()+"], [isRoot="+isRoot()+"], [isBranch="+isBranch()+"], [isEmpty="+isEmpty()+"]";
//        }
//
//
//        /**
//         *
//         * @param e
//         * @return true if the operation was successful.
//         */
////        @Override
//        public boolean add(E e) {
//            if (contains(e))
//                return false;
//            nodeFor(e).addHelper(e);
//            return true;
//        }
//
//        private void addHelper(E e) {
//            members.add(e);
//            e.curSet = this;
//            if (members.size() == capacity) {
//                children = new QTNode[] {new QTNode(this, 0), new QTNode(this, 1),
//                            new QTNode(this, 2), new QTNode(this, 3)};
////            System.out.println("Split!");
//                for (E v : members) {
//                    getDirectChild(v).addHelper(v);
//                }
//                members = null;
//            }
//        }
//
//        /**
//         *
//         * @param e
//         * @return true if the operation was successful.
//         */
//        public boolean remove(E e) {
//            QTNode n = nodeFor(e);
//            return n.members.remove(e);
//            //todo: leaf pruning
//        }
//
//        /**
//         * Returns true if this QuadTreeGrid contains the element.
//         * @param e
//         * @return
//         */
//        public boolean contains(E e) {
//            QTNode n = nodeFor(e);
//            return n.members.contains(e);
//        }
//
//        /**
//         * Returns the number of elements in this QuadTreeGrid.
//         * @return
//         */
//        public int size() {
//            if (isBranch()) {
//                return children[0].size() + children[1].size() + children[2].size() + children[3].size();
//            } else
//                return members.size();
//        }
//
//        /**
//         * Remove all of the members, but don't do any pruning.
//         */
//        public void clear() {
//            if (isBranch()) {
//                for (QTNode q : children) {
//                    q.clear();
//                }
//            } else {
//                members.clear();
//            }
//        }
//
//        /**
//         * Remove references to empty leafs/branches.
//         */
//        public void prune() {
//            throw new UnsupportedOperationException();
//            //todo: finish, think about it more
////        if(isBranch()) {
////            boolean empty = true;
////            for (QuadTreeVec2D q : children) {
////                q.prune();
////                if(!q.isEmpty()) empty = false;
////            }
////            if(empty) children = null;
////        }
//        }
//
//        /**
//         * Returns true if this is the root node of a QuadTreeGrid.
//         * @return
//         */
//        public boolean isRoot() {
//            return parent == null;
//        }
//
//        /**
//         * Returns true if this node is a branch.
//         * @return
//         */
//        public boolean isBranch() {
//            return members == null;
//        }
//
//        /**
//         * Returns true if there are no items in this QuadTreeVec2D (including all of its children).
//         * @return
//         */
//        public boolean isEmpty() {
//            if (isBranch())
//                return children[0].isEmpty() && children[1].isEmpty() && children[2].isEmpty() && children[3].
//                        isEmpty();
//            else
//                return members.size() == 0;
//        }
//
//        protected QTNode[] getChildren() {
//            return children;
//        }
//
//        /**
//         * Returns all members in this QuadTreeVec2D.
//         * @return
//         */
//
//        public Collection<E> getAllMembers() {
//            if(isRoot()) return owner.getAllEntities();
//            Collection<E> s = addAllMembers(new HashSet());
////        System.out.println("Added all members of size "+s.size());
//            return s;
//        }
//        private Collection<E> addAllMembers(Collection<E> placement) {
//            if (isBranch()) {
//                for (QTNode q : children) {
//                    q.addAllMembers(placement);
//                }
//            } else {
//                placement.addAll(members);
//            }
//            return placement;
//        }
//
//        /**
//         * Returns a set of all members that are within the given rect. Returns null if there aren't any members.
//         * @param r rect
//         * @return all members in the given rect, or null
//         */
//        public Collection<E> getInRect(Rect r) {
//            /* Find all quadtrees that intersect with the rect. For each tree, do this:
//             * 1) if the tree is fully enclosed, then add all of its members.
//             * 2) if the tree is somewhat enclosed,
//             *      a) if the tree is a leaf, check each of its members with the normal test
//             *      b) if the tree is a branch, check each of its leaves. you can automatically count out leaves that aren't
//             *         intersecting.
//             */
//
////          Tests to see if this QuadTreeEntity is completely enclosed in the given rect.
//            if (r.containsPoint(getTopLeft()) && r.containsPoint(getBottomRight())) {
////            System.out.println("Getting all members!");
//                return getAllMembers();
//            } else if (intersects(r)) {
////            System.out.println("At least it intersects");
//                Set<E> arr = new HashSet<E>();
//                if (isBranch()) {
//                    for (QTNode q : children) {
//                        Collection<E> s = q.getInRect(r);
//                        if (s != null)
//                            arr.addAll(s);
//                    }
//                } else {
//                    for (E e : members) {
//                        if (e.x() > r.x && e.x() < r.x + r.width
//                                && e.y() > r.y && e.y() < r.y + r.height)
//                            arr.add(e);
//                    }
//                }
//                return arr;
//            } else {
//                return null;
//            }
//        }
//        /**
//         * Returns a set of all members that are within the given rect. Returns null if there aren't any members
//         * @param x
//         * @param y
//         * @param w
//         * @param h
//         * @return all members of the given rect, or null
//         */
//        public Collection<E> getInRect(float x, float y, float w, float h) {
//            return getInRect(new Rect(x, y, w, h));
//        }
//
//        protected void getInCircleAddBranches(Vec2D loc, float rad,
//                                              Set<E> arr) {
//            Collection<E> s = children[0].getInCircle(loc, rad);
//            if(s != null) arr.addAll(s);
//            s = children[1].getInCircle(loc, rad);
//            if(s != null) arr.addAll(s);
//            s = children[2].getInCircle(loc, rad);
//            if(s != null) arr.addAll(s);
//            s = children[3].getInCircle(loc, rad);
//            if(s != null) arr.addAll(s);
//        }
//
//        protected void getInCircleCheckMembers(Vec2D loc, float rad2,
//                                            Set<E> arr) {
//            for (E e : members) {
//                float dx = e.x() - loc.x;
//                float dy = e.y() - loc.y;
//                if ((dx * dx + dy * dy) < rad2)
//                    arr.add(e);
//            }
//        }
//
//        /**
//         * Returns a set of all members that are within the given circle. Returns null if there aren't any members.
//         * @param loc circle's origin
//         * @param rad circle's radius
//         * @return all members in the given circle, or null
//         */
//        public Collection<E> getInCircle(Vec2D loc, float rad) {
//            boolean b1 = getTopLeft().isInCircle(loc, rad),
//                    b2 = new Vec2D(x + width, y).isInCircle(loc, rad),
//                    b3 = getBottomRight().isInCircle(loc, rad),
//                    b4 = new Vec2D(x, y + height).isInCircle(loc, rad);
//
//            if (b1 && b2 && b3 && b4) { //the circle completely encloses this rectangle
//                return getAllMembers();
//            } else if (containsPoint(loc) || b1 || b2 || b3 || b4
//                    || (x - loc.x < rad) || (loc.x - x + width < rad)
//                    || (y - loc.y < rad) || (loc.y - y + height < rad)) { //there is still some intersection.
//                Set<E> arr = new HashSet<E>();
//                if (isBranch()) {
//                    getInCircleAddBranches(loc, rad, arr);
//                } else {
//                    float rad2 = rad * rad;
//                    getInCircleCheckMembers(loc, rad2, arr);
//                }
//                return arr;
//            } else {
//                return null;
//            }
//        }
//        /**
//         * Returns a set of all members that are within the given circle. Returns null if there aren't any members.
//         * @param x x location of circle's origin
//         * @param y y location of circle's radius
//         * @param rad circle's radius
//         * @return all members in the given circle, or null
//         */
//        public Collection<E> getInCircle(float x, float y, float rad) {
//            return getInCircle(new Vec2D(x, y), rad);
//        }
//
//        /**
//         * Returns true if this QuadTreeVec2D intersects with the given rect.
//         * @param r2
//         * @return
//         */
//        public boolean intersects(Rect r2) {
//            //shamelessly stolen from java.awt.geom.Rectangle2D
//            return (r2.x + r2.width > x
//                    && r2.y + r2.height > y
//                    && r2.x < x + width
//                    && r2.y < y + height);
//        }
//
//        public Iterator<E> iterator() {
//            if (isBranch()) {
//                return new Iterator<E>() {
//
//                    Iterator<E>[] iterators = new Iterator[]{
//                        children[0].iterator(), children[1].iterator(),
//                        children[2].iterator(), children[3].iterator()
//                    };
//                    int tempCount = 0;
//
//                    public boolean hasNext() {
//                        updateTempCount();
//                        return tempCount < iterators.length; //it's always 4 but this is easier to understand
//                    }
//
//                    private void updateTempCount() {
//                        //tempCount gets updated to the next iterator that holds something.
//                        //if none of the remaining iterators have anything, then tempCount will be iterators.length.
//                        while (tempCount < iterators.length && !iterators[tempCount].hasNext()) {
//                            tempCount++;
//                        }
//                    }
//
//                    public E next() {
//                        updateTempCount();
//                        return iterators[tempCount].next();
//                    }
//
//                    public void remove() {
//                        updateTempCount();
//                        iterators[tempCount].remove();
//                    }
//                };
//            } else {
//                return members.iterator();
//            }
//        }
//
//        /**
//         * If this is the leaf, then stop here. Otherwise, go to the next node and check.
//         * @param e
//         * @return
//         */
//        protected QTNode nodeFor(E e) {
//            return nodeFor(e.x(), e.y());
//        }
//        protected QTNode nodeFor(float x, float y) {
//            //todo: better traversal algorithm?
//            //there is no check bounds because all calls to this method have already been checked.
//            if (!isBranch())
//                return this;
//            else {
//                return getDirectChild(x, y).nodeFor(x, y);
//            }
//        }
//
//        protected QTNode getDirectChild(E e) {
//            return getDirectChild(e.x(), e.y());
//        }
//        protected QTNode getDirectChild(float xF, float yF) {
//            return children[((xF < x + width / 2) ? 0 : 1)
//                    + ((yF < y + height / 2) ? 0 : 2)];
//        }
//
//        public boolean contains(Object o) {
//            if(o instanceof Entity) return contains((E) o);
//            else return false;
//        }
//
//        public Object[] toArray() {
//            return getAllMembers().toArray();
//        }
//
//        public <T> T[] toArray(T[] a) {
//            return getAllMembers().toArray(a);
//        }
//
//        public boolean remove(Object o) {
//            if(o instanceof Entity) return remove((E) o);
//            throw new IllegalArgumentException("Cannot remove objects of type "+o.getClass()+" from this QuadTree!");
//        }
//
//        public boolean containsAll(Collection<?> c) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean addAll(Collection<? extends E> c) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean removeAll(Collection<?> c) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public boolean retainAll(Collection<?> c) {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }
//}
