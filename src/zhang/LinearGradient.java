package zhang;

import java.util.Iterator;
import java.util.TreeSet;
import toxi.math.InterpolateStrategy;

public class LinearGradient {

    protected class GradientPoint implements Comparable<GradientPoint> {

        float pos;
        float value;

        public GradientPoint(float p, float v) {
            pos = p;
            value = v;
        }

        public int compareTo(GradientPoint p) {
            return new Float(pos).compareTo(p.pos);
        }
    }
    protected InterpolateStrategy strat;
    protected TreeSet<GradientPoint> points;

    public LinearGradient() {
        points = new TreeSet();
        strat = null;
//    strat = new LinearInterpolation();
    }

    public InterpolateStrategy getStrategy() {
        return strat;
    }

    public void setStrategy(InterpolateStrategy s) {
        strat = s;
    }

    public void addPoint(float pos, float value) {
        points.add(new GradientPoint(pos, value));
    }

    public boolean removePoint(float pos) {
        for (Iterator<GradientPoint> it = points.iterator(); it.hasNext();) {
            GradientPoint gp = it.next();
            if(gp.pos == pos) {
                it.remove();
                return true;
            }
            else if(pos < gp.pos) return false;
        }
        return false;
    }

    public float get(float pos) {
        if (points.size() < 2)
            throw new IllegalStateException("Need at least two points in linear gradient!");
        GradientPoint before = points.first(), after = points.higher(before);
        for (; after != null; before = after, after = points.higher(after)) { //find the two points that saddle the position
            if (before.pos <= pos && after.pos >= pos) {
                float norm = (pos - before.pos) / (after.pos - before.pos); //the amount to go towards either point
                if (strat == null) {
                    //do linear interpolation
                    return before.value + (after.value - before.value) * norm;
                } else
                    //use the InterpolateStrategy
                    return strat.interpolate(before.value, after.value, norm);
            }
        }
        throw new IllegalStateException(pos + ": out of bounds!");
    }
}
