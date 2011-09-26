/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package zhang;

import java.util.Random;
import org.jbox2d.common.Vec2;

/**
 *
 * @author hellochar
 */
public class MyRandom extends Random {

    public MyRandom(long seed) {
        super(seed);
    }

    public MyRandom() {
    }

    public Vec2 nextVec2() {
        return new Vec2(nextFloat(), nextFloat());
    }

    public float random(float low, float high) {
        return nextFloat() * (high - low) + low;
    }

    public float random(float high) {
        return random(0, high);
    }

    public float random() {
        return random(1);
    }
}
