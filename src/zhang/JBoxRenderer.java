/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zhang;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.CircleShape;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.collision.PolygonShape;
import org.jbox2d.collision.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.Steppable;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * A renderer for JBox2D in Processing.
 * <p> Call <code>initWorld()</code> to initialize JBox2D.
 * <p> Call <code>step()</code> to move the model along. You should call <code>step</code>
 * once every draw loop (or every time you want it to update).
 * <p>Call <code>render()</code> to draw everything; optionally, you may also call <code>drawBounds()</code>
 * to draw the bounding AABB.
 * <p>Call <code>destroyIfFrozen(boolean)</code> to set whether to remove bodies that are frozen. If true, any
 * body that's frozen (e.g. outside the bounding box) will get removed in the next call to render().
 * <p>To destroy the model, call <code>destroy()</code>.
 * <p><b>Useful fields: </b>
 * <ul>
 * <li>world
 * <li>timeStep
 * <li>iterations
 * <li>camera
 * </ul></p>
 * <p><b>Useful methods: </b>
 * <ul>
 * <li>createBody
 * <li>createRect
 * <li>createCircle
 * <hr>
 * <li>setDensity
 * <li>setRestitution
 * <li>setFriction
 * </ul></p>
 * @author Xiaohan Zhang
 * @see initWorld(Vec2, Vec2, Vec2, float)
 * @see step()
 * @see render()
 * @see #scroll(float, float)
 * @see #destroy()
 * @see Camera
 */
public class JBoxRenderer extends PApplet {

    protected World world;
    public float timeStep = 1 / 60.0f;
    public int iterations = 10;
    public Camera camera;
    protected final BodyDef bodyDef = new BodyDef();
    protected CircleDef circleShapeDef;
    protected PolygonDef polyShapeDef;
//    public final static Vec2 ZERO = new Vec2(),  ONE = new Vec2(1, 1), X = new Vec2(1, 0), Y = new Vec2(0, 1);
    private final Steppable DESTROY_FROZEN_STEPPABLE = new Steppable() {
        public void step(float dt, int iterations) {
        for (Body b = world.getBodyList(); b != null; b = b.getNext()) {
                if (b.isFrozen() & destroyIfFrozen) {
                    world.destroyBody(b);
                }
            }
        }
    };
//
//    /**
//     * A convenience method for detecting whether a key is being held. If the key is a character,
//     * pass the (int) cast of the character. If the key is an action key, pass the KeyCode,
//     * as specified by KeyEvent.
//     * <p>For instance, if I wanted to see if 'k' was being held, I'd call
//     * <code> if( keyPressed( 'k' ) ) </code>.
//     * <p>If I wanted to see if shift was being held, I'd call <code>
//     * if( keyPressed( VK_SHIFT ) ) </code>.
//     * @param k
//     * @return
//     */
//    public boolean keyPressed(int k) {
//        return keysPressed.contains(new Integer(k));
//    }

    /**
     * Removes all bodies from the world; that is, calls destroyBody on each body inside the world.
     */
    public void destroy() {
        for (Body b = world.getBodyList(); b != null; b = b.getNext()) {
            world.destroyBody(b);
        }
    }

    /**
     * Call this method at your setup to initialize the model. The model will have an
     * AABB of low and high. The default camera will also be initialized with (0, 0) in
     * the center and a scale level of <code>defScale</code>. For best performance, pass a value
     * between 10 and 100.
     * <p>The camera will be initialized such that the exact center of the screen is at Vec2(0, 0).</p>
     * 
     * @param topLeft         The model coordinates of the top left boundary.
     * @param bottomRight     The model coordinates of the bottom right boundary.
     * @param grav            The model gravity.
     * @param defZoom         The default, initialized zoom amount of the camera.
     *
     * @see #model(Vec2)
     */
    public World initWorld(Vec2 topLeft, Vec2 bottomRight, Vec2 grav, float defZoom) {
        AABB boundary = new AABB(topLeft, bottomRight);
        world = new World(boundary, grav, true);
        camera = new Camera(this);
        camera.setScale(defZoom);
        camera.setCenter(0, 0);
//        camera.setTranslate(width / 2, height / 2);

        polyShapeDef = new PolygonDef();
        circleShapeDef = new CircleDef();
//        setDensity(1);
//        setRestitution(0);
//        setFriction(0);
        return world;
    }

    /**
     *
     * @see #initWorld(Vec2, Vec2, Vec2, float)
     */
    public World initWorld(float lx, float ly, float hx, float hy, float gx, float gy, float defScale) {
        return initWorld(new Vec2(lx, ly), new Vec2(hx, hy), new Vec2(gx, gy), defScale);
    }

    public World initWorld(float lx, float ly, float hx, float hy, float gy, float defScale) {
        return initWorld(lx, ly, hx, hy, 0, gy, defScale);
    }

    public void preStep() {
    }

    public void postStep() {
    }
    /**
     * Steps the model.
     */
    public void step() {
        step(timeStep, iterations);
    }

    public void step(float dt, int iter) {
        preStep();
        world.step(dt, iter);
        postStep();
    }

    /**
     * Create a body with the default bodydef at the specified location
     * @param loc
     * @return
     */
    private Body createBody(Vec2 loc) {
        bodyDef.position.set(loc);
        return createBody(bodyDef);
    }

    /**
     * Create a body with the given definition.
     * @param d
     * @return
     */
    public Body createBody(BodyDef d) {
        return world.createBody(d);
    }

    /**
     * Creates a new body and attaches a rect to it.
     * @param loc center of the rect
     * @param halfWidth World half-width
     * @param halfHeight World half-height
     * @return
     * @see setDensity(float)
     */
    public Body createRect(Vec2 loc, float halfWidth, float halfHeight) {
        polyShapeDef.setAsBox(halfWidth, halfHeight);
        Body b = createBody(loc);
        b.createShape(polyShapeDef);
        b.setMassFromShapes();

        return b;
    }

    /**
     *
     * @param loc Center of the circle
     * @param r World radius
     * @return
     */
    public Body createCircle(Vec2 loc, float r) {
        circleShapeDef.radius = r;
        Body b = createBody(loc);
        b.createShape(circleShapeDef);
        b.setMassFromShapes();
        return b;
    }

    public float density() {
        return polyShapeDef.density;
    }

    public float restitution() {
        return polyShapeDef.restitution;
    }

    public float friction() {
        return polyShapeDef.friction;
    }

    /**
     * Sets the density of all future createXXXX calls. Set the density to 0 to make static shapes (walls, etc.)
     * @param d
     */
    public void setDensity(float d) {
        polyShapeDef.density = d;
        circleShapeDef.density = d;
    }

    /**
     * Sets the restitution (elasticity) of all future createXXXX calls.
     * @param r
     */
    public void setRestitution(float r) {
        polyShapeDef.restitution = r;
        circleShapeDef.restitution = r;
    }

    public void setFriction(float f) {
        polyShapeDef.friction = f;
        circleShapeDef.friction = f;
    }
    boolean destroyIfFrozen = false;

    public void destroyIfFrozen(boolean destroy) {
        destroyIfFrozen = destroy;
    }

    /**
     * Converts screen coordinates to model coordinates.
     * @param screen
     * @return
     * @deprecated Use model(Vec2) instead
     */
    public Vec2 world(Vec2 screen) {
        //return Camera.toJBoxVec2(camera.model(Camera.toZhangVec2(screen)));
        PVector pvec = camera.model(new PVector(screen.x, screen.y));
        return new Vec2(pvec.x, pvec.y);
    }

    /**
     * @deprecated Use modelDist(Vec2) instead
     * @param scrnDist
     * @return
     */
    public Vec2 worldDist(Vec2 scrnDist) {
        return scrnDist.mul(1/camera.scale);
    }

    /**
     * @deprecated Use modelDist(float) instead
     * @param scrnDist
     * @return
     */
    public float worldDist(float scrnDist) {
        return camera.modelDist(scrnDist);
    }

    public Vec2 model(Vec2 s) {
        return world(s);
    }
    public Vec2 modelDist(Vec2 s) {
        return worldDist(s);
    }
    public float modelDist(float s) {
        return worldDist(s);
    }

    /**
     * Returns model coordinates to screen coordinates.
     * @param model
     * @return
     */
    public Vec2 screen(Vec2 world) {
        PVector pvec = camera.screen(new PVector(world.x, world.y));
        return new Vec2(pvec.x, pvec.y);
    }

    public Vec2 screenDist(Vec2 worldDist) {
        return worldDist.mul(camera.scale);
    }

    public float screenDist(float worldDist) {
        return camera.screenDist(worldDist);
    }

    /**
     * Returns screen mouse coordinates.
     * @return
     */
    public Vec2 mouseVec() {
        return new Vec2(mouseX, mouseY);
    }

    /**
     * Draw the boundary. 
     */
    public void drawBounds() {
        drawBounds(this, world);
    }

    public static void drawBounds(PApplet p, World w) {
        p.pushStyle();
        p.rectMode(CORNERS);
        AABB boundary = w.getWorldAABB();
        p.rect(boundary.lowerBound.x, boundary.lowerBound.y, boundary.upperBound.x, boundary.upperBound.y);
        p.popStyle();
    }

    /**
     * Renders this model once and destroys any frozen bodies if destoyIfFrozen() was called.
     */
    public void render() {
        pushStyle();
        ellipseMode(CENTER);
        for (Body b = world.getBodyList(); b != null; b = b.getNext()) {
//            Vec2 v = b.getWorldCenter();
            if (b.isFrozen() & destroyIfFrozen) {
                world.destroyBody(b);
            } //            else if(v.x < 0 | v.x > width | v.y < 0 | v.y > height) {
            //                continue;
            //            }
            else {
                drawBody(b);
            }
        }
        for(Joint j = world.getJointList(); j != null; j = j.getNext()) { //Draw all joints, probably taken from the demo
            drawJoint(j);
        }
        popStyle();
    }

    public void drawJoint(Joint j) {
        drawJoint(this, j, camera.scale);
    }
    public static void drawJoint(PApplet p, Joint j, float cameraScale) {
        //Draw all joints, probably taken from the demo
        if (j instanceof RevoluteJoint) {
            RevoluteJoint rj = (RevoluteJoint) j;
            Body b1 = rj.getBody1();
            p.pushMatrix();
            transformTo(p, b1);
            p.translate(rj.m_localAnchor1.x, rj.m_localAnchor1.y);
            p.ellipse(0, 0, .2f, .2f);
            if (rj.getMotorTorque() / rj.m_maxMotorTorque > .001f)
                p.arc(0, 0, 6 / cameraScale, 6 / cameraScale, 0,
                        TWO_PI * rj.getMotorTorque() / rj.m_maxMotorTorque);
            else
                p.arc(0, 0, 6 / cameraScale, 6 / cameraScale, 0, TWO_PI * rj.getMotorTorque() / 20);
            p.popMatrix();
        }
    }

    /**
     * Draw an individual body.
     * @param b
     */
    public void drawBody(Body b) {
        drawBody(this, b);
    }
    public static void drawBody(PApplet p, Body b) {
        if (b.getShapeList() == null) {
            return;
        }

        p.pushMatrix();
        transformTo(p, b);
//        println("Drawing at "+screenX(0, 0)+", "+screenY(0, 0));
        for (Shape s = b.getShapeList(); s != null; s = s.getNext()) {
            drawShape(p, s, true);
        }
        //  println("Drawing body "+b+" at "+b.getPosition()+"... ");
        p.popMatrix();
    }

    public void transformTo(Body b) {
        transformTo(this, b);
    }
    public static void transformTo(PApplet p, Body b) {
        p.translate(b.getPosition().x, b.getPosition().y);
        p.rotate(b.getAngle());
    }

    /**
     * Draw an individual shape.
     * @param s
     */
    public void drawShape(Shape s) {
        drawShape(this, s);
    }

    public static void drawShape(PApplet p, Shape s) {
        drawShape(p, s, false);
    }

    private static void drawShape(PApplet app, Shape s, boolean transformed) {
        if(!transformed && s.getBody() != null) {
            app.pushMatrix();
            transformTo(app, s.getBody());
        }
        if (s instanceof PolygonShape) {
            PolygonShape p = (PolygonShape) s;
            app.beginShape();
            for (int a = 0; a < p.getVertexCount(); a++) {
                Vec2 v = p.getVertices()[a];
                app.vertex(v.x, v.y);
            }
            app.endShape(CLOSE);
        } else if (s instanceof CircleShape) {
            CircleShape c = (CircleShape) s;
//            System.out.println("radius of "+c.getRadius());
            Vec2 pos = c.getLocalPosition();
            app.ellipse(pos.x, pos.y, c.getRadius() * 2, c.getRadius() * 2);
            if(s.getBody() != null) {
//                float ang = s.getBody().getAngle();
//                System.out.println("lines of len "+(float) (c.getRadius() * MathUtils.cos(ang))+","+
//                        c.getRadius() * MathUtils.sin(ang));
//                Vec2 endPoint = pos.add(new Vec2(c.getRadius()*cos(ang), c.getRadius()*sin(ang)));
//                line(pos.x, pos.y, endPoint.x, endPoint.y);

                //you don't have to account for the angle of the body because the transform already does that
                app.line(pos.x, pos.y, pos.x + c.getRadius(), pos.y);
            }
        }
        if(!transformed && s.getBody() != null) app.popMatrix();
    }

    public Camera getCamera() {
        return camera;
    }

    public int getIterations() {
        return iterations;
    }

    public float getTimeStep() {
        return timeStep;
    }

    public World getWorld() {
        return world;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PApplet.main(args);
    }
}
