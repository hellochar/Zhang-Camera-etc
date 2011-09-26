/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zhang;

/**
 *
 * @author hellochar
 */
public class PlasmaFractal {

    Point p1, p2, p3, p4;
    float x, y;

    PlasmaFractal(Point p1, Point p2, Point p3, Point p4) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
        x = (p1.x + p3.x) / 2;
        y = (p1.y + p3.y) / 2;
        /*    float[] xVals = {p1.x, p2.x, p3.x, p4.x};
        w = max(xVals);
        float[] yVals = {p1.y, p2.y, p3.y, p4.y};
        l = max(yVals);*/
    }

    public float value() {
        return (p1.val + p2.val + p3.val + p4.val) / 4;
    }

    PlasmaFractal[] generate(float roughness) {
        Point mid = new Point(p1, p2, p3, p4);
        mid.randomize(roughness);
        Point up = new Point(p1, p2), right = new Point(p2, p3), down = new Point(p3, p4), left = new Point(p4,
                p1);
        /*    up.randomize(roughness);
        right.randomize(roughness);
        down.randomize(roughness);
        left.randomize(roughness);*/
        return new PlasmaFractal[]{
                    new PlasmaFractal(p1, up, mid, left), new PlasmaFractal(up, p2, right, mid), new PlasmaFractal(mid, right, p3,
                    down), new PlasmaFractal(left, mid, down, p4)};
    }

    private static class Point {

        float val;
        float x, y;

        Point(float x, float y, float val) {
            this.x = x;
            this.y = y;
            this.val = val;
        }

        Point(Point p1, Point p2) {
            this((p1.x + p2.x) / 2, (p1.y + p2.y) / 2, (p1.val + p2.val) / 2);
        }

        Point(Point p1, Point p2, Point p3, Point p4) {
            this((p1.x + p3.x) / 2, (p1.y + p3.y) / 2, (p1.val + p2.val + p3.val + p4.val) / 4);
        }

        void randomize(float chng) {
            val += zhang.Methods.random(-chng, chng);
            if (val < 0) {
                val = 0;
            } else if (val > 1) {
                val = 1;
            }
        }
    }
}