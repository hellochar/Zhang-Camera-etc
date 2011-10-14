package zhang;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

/**
 * A generic camera class that keeps track of the current scale level and the location.
 * By default, this camera is automatically applied before each loop. Call noAuto() to turn this feature
 * off.
 * This class also has support for simple a simple move/zoom UI that uses
 * keyboard presses.
 * @see Camera(PApplet, float, float, boolean)
 * @see Camera(PApplet)
 * @see scale(float)
 * @see translate(float, float)
 * @see apply()
 * @see autoApply(), noAutoApply()
 */
public class Camera implements PConstants {

    /**
     * Current scale level. A higher scale means MORE zoom.
     * A scale of n means that all coordinates will be drawn to n times their original value.
     * Change through through the scale() or setScale() methods.
     */
    protected float scale = 1;
    /**
     * Current x/y coordinates of the top-left of the visible screen.
     * Change through through the translate() and setTranslate() methods. 
     */
    protected PVector corner;
    /**
     * The PApplet that this camera lives in. This affects the scale() and apply() methods.
     */
    private final PApplet applet;
    /**
     * The MultiKeyListener that holds what keys are being pressed.
     */
    private MultiKeyListener kl;
    /**
     * The MouseWheelListener to listen for mouse wheel scrolls.
     */
    private MouseWheelListener mwl;
    /**
     * These values should not be changed; they are public for easy reading.
     */
    public final PVector screen, halfScreen;


    //============================ BEGIN UI CONTROLS ====================================
    /**
     * UI options relating to how much the Camera should react to user input. May get deprecated and moved to a separate class later on.
     */
    protected float uiMoveSpeed, uiZoomAmount;
    /**
     * UI flags denoting whether the Camera should respond to various user inputs. 
     */
    protected boolean uiWasd = true, uiArrows = true, uiPlusMinus = true, uiScrollWheel = true;
    /**
     * The ui flag is as a catch-all for all UI input methods - if ui is false, no input will affect the Camera.
     */
    protected boolean ui = true;

    /**
     * The net units the scroll wheel has moved since the last uiHandle() call. This variable gets updated by a MouseWheelHandler and then read and reset by uiHandle.
     * This value is protected so that subclasses may control how the UI responds to scroll wheel zooming (e.g. by negating the number, the zoom directions are reversed)
     */
    protected float scrollAmount = 0;
    /**
     * A flag denoting whether the Camera should automatically apply its transform to the PApplet.
     */
    protected boolean applyAuto = true;


    /**
     * Creates a camera with the specified UI features. Use the arrow keys to move and +/(= or -) to zoom in/out.
     * @param applet
     * @param moveSpeed
     * @param zoomAmount
     */
    public Camera(PApplet applet, float moveSpeed, float zoomAmount) {
        this.applet = applet;
        uiMoveSpeed = moveSpeed;
        uiZoomAmount = zoomAmount;

        screen = new PVector(applet.width, applet.height);
        halfScreen = new PVector(applet.width/2, applet.height/2);

        mwl = new MouseAdapterImpl();
        applet.addMouseWheelListener(mwl);

        kl = new MultiKeyListener();
        applet.addKeyListener(kl);

        applet.registerPre(this);
        applet.registerSize(this);
        
        scale = 1;
        corner = new PVector();
    }

    /**
     * Creates a camera with a move speed of 15 and a zoom amount of 1.03.
     * @param applet
     */
    public Camera(PApplet applet) {
        this(applet, 15, 1.03f);
    }

    /**
     * Called by Processing before the draw() method occurs. This handles the UI movement and applies the camera transform.
     */
    public void pre() {
        if(ui) uiHandle();
        if(applyAuto) apply();
    }

    /**
     * Called by Processing when sketch resizes.
     * @param width
     * @param height
     */
    public void size(int width, int height) {
        screen.set(width, height, 0);
        halfScreen.set(width/2, height/2, 0);
    }

//======================================PROGRAMMATIC CONTROL OF THE CAMERA======================================================

    /**
     * Scales the viewport by specified amount towards the center of the view.
     * <br><b>scale - default = 1</b>
     * @param s amount to scale by.
     */
    public void scale(float s) {
//        if(scaleMode == CENTER) {
//            //todo: implement.
//
//        } else {
//            corner.add(PVector.mult(screen, (1 / s - 1) / (2 * scale)));
//        }
//        scale *= s;
        scale(s, model(halfScreen));
    }

    /**
     * Zooms in the view, focused at the specified model point. s > 1 means the camera will zoom in, 0 &lt; s &lt; 1 means the camera will zoom out.
     * s &lt; 0 is generally not recommended, as everything will be flipped.
     * @param s
     * @param modelPoint
     */
    public void scale(float s, PVector modelPoint) {
        scale *= s;
        //Using the rule that screen(modelPoint) = constant,
        //we get screen(point, corner, scale) = screen(point, corner', scale'), where
        //screen(p, c, s) = (p - c) * s.
        //So, (p - corner) * scale = (p - corner') * s * scale. Solving for corner' gives
        //corner' = p - (p - corner) / s.

        PVector sub = modelPoint.get(); sub.sub(corner); sub.div(s); //(p - corner) / s
        corner.set(PVector.sub(modelPoint, sub));

//        PVector offset = PVector.sub(corner, modelPoint);
//        //let d = sqrt(x*x + y*y), x' = x / s, y' = y / s, d' = sqrt(x'*x' + y'*y'). d/d' = ?
//        //d' = sqrt(x/s*x/s + y/s * y/s) = sqrt(x*x/s^2 + y*y/s^2) = sqrt((x*x+y*y)/s^2) = sqrt(x*x+y*y)/s = d/s
//        //so, d/d' = d/(d/s) = s.
//        offset.mult(1/s);
//        corner.set(PVector.add(modelPoint, offset));
    }

    /**
     * Sets the scale level to the specified parameter.
     * <br><b>scale - default = 1</b>
     * @param s scale level
     */
    public void setScale(float s) {
        scale(s / scale);
    }

    /**
     * Moves the camera view the specified model distance (which means it's affected by the current scale).
     * @param modelX
     * @param modelY
     */
    public void translate(float modelX, float modelY) {
        corner.x += modelDist(modelX);
        corner.y += modelDist(modelY);
    }

    /**
     * Moves the camera view the specified model distance (which means it's affected by the current scale).
     * @param model
     */
    public void translate(PVector model) {
        translate(model.x, model.y);
    }

    /**
     * Sets the top-left corner of the camera's view to the specified model coordinates.
     * <br><b>corner - default = (0, 0)</b>
     * @param modelX
     * @param modelY
     */
    public void setCorner(float modelX, float modelY) {
        corner.x = modelX;
        corner.y = modelY;
    }

    /**
     * Sets the top-left corner of the camera's view to the specified model coordinates.
     * <br><b>setCorner - default = (0, 0)</b>
     * @param model
     */
    public void setCorner(PVector model) {
        setCorner(model.x, model.y);
    }

    /**
     * Centers the camera's view on the specified model coordinates.
     * @param modelCenter
     */
    public void setCenter(PVector modelCenter) {
        setScreenToModel(halfScreen, modelCenter);
//        PVector halfScreenModel = modelDist(halfScreen);
////        PApplet.println("Setting camera corner to "+model+" - "+halfScreenModel);
//        setCorner(PVector.sub(modelCenter, halfScreenModel));
    }

    /**
     * Center's the camera's view on the specified model coordinate.
     * @param modelX
     * @param modelY
     */
    public void setCenter(float modelX, float modelY) {
        setCenter(new PVector(modelX, modelY));
    }

    /**
     * Scales from the center of the screen such that the resultant width of the camera's view is equal to modelWidth.
     * @param modelWidth
     */
    public void setViewportWidth(float modelWidth) {
        scale(modelDist(screen.x) / modelWidth);
    }

    /**
     * Scales from the center of the screen such that the resultant height of the camera's view is equal to modelHeight.
     * @param modelHeight
     */
    public void setViewportHeight(float modelHeight) {
        scale(modelDist(screen.y) / modelHeight);
    }

    /**
     * Pans the camera such that the given modelLoc will be "displayed" at the given screenLoc.
     * @param screenLoc
     * @param modelLoc
     */
    public void setScreenToModel(PVector screenLoc, PVector modelLoc) {
        PVector screenLocModel = modelDist(screenLoc);
        
        setCorner(PVector.sub(modelLoc, screenLocModel));
    }

//    /**
//     * Sets the scale mode. Acceptable modes are CORNER or CENTER.
//     * The CORNER mode will scale "towards" the top-left corner of the screen, while a CENTER mode will scale "towards" the center.
//     * @param mode
//     */
//    public void scaleMode(int mode) {
//        scaleMode = mode;
//    }
    
    /**
     * Apply transformations to the PApplet.
     * @see auto
     * @see scale(float)
     * @see translate(float, float)
     */
    public void apply() {
        applet.scale(scale);
        applet.translate(-corner.x, -corner.y);
    }


//===========================================GETTING CAMERA STATE=======================================================
    /**
     * Gets the current scale.
     * @return current scale
     */
    public float getScale() {
        return scale;
    }

    /**
     * Returns the top left corner of the screen, in model coordinates.
     * 
     * An alternative but equal definition is that this method returns how much this camera is translating
     * the view by.
     * @return
     */
    public PVector getCorner() {
        return corner.get();
    }

//TODO test and make sure this method works
    /**
     * Returns the center of this camera's viewport in model coordinates.
     * @return
     * @see setCenter(PVector), setCenter(float, float)
     */
    public PVector getCenter() {
        return model(halfScreen);
    }

    /**
     * Returns the current view in model coordinates.
     * @return
     */
    public Rectangle2D.Float getRect() {
        PVector tl = getCorner(), br = model(screen);
        return new Rectangle2D.Float(tl.x, tl.y, br.x-tl.x, br.y-tl.y);
    }

    /**
     * Returns the width, in model coordinates, of the camera's view.
     * @return
     */
    public float getViewportWidth() {
        return modelDist(screen.x);
    }

    /**
     * Returns the height, in model coordinates, of the camera's view.
     * @return
     */
    public float getViewportHeight() {
        return modelDist(screen.y);
    }
    
    /**
     * True if the camera is automatically translating the PApplet at the beginning of each frame.
     * @return
     */
    public boolean isApplyAuto() {
        return applyAuto;
    }

    /**
     * True if pressing arrow keys pans the camera.
     * @return
     */
    public boolean isUiArrows() {
        return uiArrows;
    }

    /**
     * True if Camera should change itself when the user presses sends input. If this value is false, all UI will be
     * stopped regardless of individual flags such as uiArrows, uiPlusMinus, etc.
     * @return
     */
    public boolean isUi() {
        return ui;
    }

    /**
     * True if pressing WASD pans the camera.
     * @return
     */
    public boolean isUiWasd() {
        return uiWasd;
    }

    /**
     * True if pressing (= or +)/- keys zooms the camera.
     * @return
     */
    public boolean isUiPlusMinus() {
        return uiPlusMinus;
    }

    /**
     * True if using the scroll wheel on the mouse zooms the camera.
     * @return
     */
    public boolean isUiScrollWheel() {
        return uiScrollWheel;
    }

    /**
     * Returns the model speed of the Camera. The Camera will translate this many units per frame in the direction(s)
     * pressed.
     * @return
     */
    public float getUiMoveSpeed() {
        return uiMoveSpeed;
    }

    /**
     * Returns the zoom amount of the Camera. The Camera will scale itself by this ratio each frame.
     * @return
     */
    public float getUiZoomAmount() {
        return uiZoomAmount;
    }

//============================CONTROLLING UI===================================

    /**
     * Automatically call the apply method before draw.
     * @see noAutoApply()
     */
    public void autoApply() {
        applyAuto = true;
    }

    /**
     * Don't call the apply method before draw.
     * @see autoApply()
     */
    public void noAutoApply() {
        applyAuto = false;
    }
    
    /**
     * Tells the camera to respond user inputs, as enabled/disabled by their respective flags.
     * <br><b>auto UI - default = true</b>
     */
    public void ui() {
        ui = true;
    }
    
    /**
     * Tells the camera to ignore all user inputs.
     * <br><b>auto UI - default = true</b>
     */
    public void noUi() {
        ui = false;
    }

    /**
     * Use arrow keys to move around.
     * <br><b>Use arrow keys - default = true</b>
     */
    public void arrows() {
        uiArrows = true;
    }

    /**
     * Don't use arrow keys.
     * <br><b>Use arrow keys - default = true</b>
     */
    public void noArrows() {
        uiArrows = false;
    }

    /**
     * Use WASD to move around.
     * <br><b>Use WASD - default = true</b>
     */
    public void wasd() {
        uiWasd = true;
    }

    /**
     * Don't use WASD.
     * <br><b>Use WASD - default = true</b>
     */
    public void noWasd() {
        uiWasd = false;
    }

    /**
     * Use the +/(= or -) keys to zoom in/out.
     * <br><b>Use plus/minus - default = true</b>
     */
    public void plusMinus() {
        uiPlusMinus = true;
    }

    /**
     * Don't use the +/(= or -) keys to zoom.
     * <br><b>Use plus/minus - default = true</b>
     */
    public void noPlusMinus() {
        uiPlusMinus = false;
    }

    /**
     * Use the mouse scroll wheel to zoom.
     * <br><b>Use scroll wheel - default = true</b>
     */
    public void scrollWheel() {
        uiScrollWheel = true;
    }

    /**
     * Don't use the mouse scroll wheel to zoom.
     * <br><b>Use scroll wheel - default = true</b>
     */
    public void noScrollWheel() {
        uiScrollWheel = false;
    }

    /**
     * Set how fast the camera moves in response to arrows and wasd (if they're turned on).
     * <br><b>Move speed - default = 15</b>
     * @param moveSpeed
     */
    public void ms(float moveSpeed) {
        uiMoveSpeed = moveSpeed;
    }


    /**
     * Same as ms.
     * @param ms
     * @see ms(float).
     */
    public void setMoveSpeed(float ms) { ms(ms); }

    /**
     * Sets how fast the camera zooms in response to +/- and scroll wheel (if it's turned on).
     * <br><b>Zoom amount - default = 1.03</b>
     * @param zoomAmount
     */
    public void za(float zoomAmount) {
        uiZoomAmount = zoomAmount;
    }

    /**
     * Same as za.
     * @param za
     * @see za(float).
     */
    public void setZoomAmount(float za) { za(za); }

    /**
     * Call this to mutate the camera state based on which buttons are currently being pressed.
     * You may choose to override this method if you want to do other UI stuff.
     */
    public void uiHandle() {
        float x = 0, y = 0;
        if (uiArrows) {
            if (isPressed(KeyEvent.VK_UP))
                y--;
            if (isPressed(KeyEvent.VK_LEFT))
                x--;
            if (isPressed(KeyEvent.VK_DOWN))
                y++;
            if (isPressed(KeyEvent.VK_RIGHT))
                x++;
        }
        if (uiWasd) {
            if (isPressed(KeyEvent.VK_W))
                y--;
            if (isPressed(KeyEvent.VK_A))
                x--;
            if (isPressed(KeyEvent.VK_S))
                y++;
            if (isPressed(KeyEvent.VK_D))
                x++;
        }
        translate(x * uiMoveSpeed, y * uiMoveSpeed);
        if(uiPlusMinus) {
            if (isPressed(KeyEvent.VK_MINUS)) {
                scale(1 / uiZoomAmount);
            }
            if (isPressed(KeyEvent.VK_EQUALS) | isPressed(KeyEvent.VK_PLUS)) {
                scale(uiZoomAmount);
            }
        }
        if(scrollAmount != 0)
            scale(PApplet.pow(uiZoomAmount, scrollAmount));
        scrollAmount = 0;
    }

    /**
     * A convenience method for detecting whether a key is being held. 
     * <p>If I wanted to see if 'k' was being held, I'd call
     * <code> if( keyPressed( 'k' ) ) </code>.
     * <p>If I wanted to see if shift was being held, I'd call <code>
     * if( keyPressed( VK_SHIFT ) ) </code>.
     * @param k
     * @return
     */
    protected boolean isPressed(int k) {
        return kl.isPressed(k);
    }

//==================================HELPER METHODS, CONVERSION============================

    /**
     * Returns the PApplet that this Camera is bound to.
     * @return
     */
    protected PApplet getApplet() {
        return applet;
    }
    
    /**
     * Reset the transforms in this camera. UI flags are not affected.
     */
    public void reset() {
        scale = 1;
        corner = new PVector();
    }

    /**
     * Get the model coordinates, given a screen coordinate.
     * @param screen
     * @return
     */
    public PVector model(PVector screen) {
//        return new PVector(modelX(screen.x), modelY(screen.y));
        return PVector.add(modelDist(screen), corner);
    }

    /**
     * Convert the given screen X coordinate into model coordinates.
     * @param screenX
     * @return
     */
    public float modelX(float screenX) {
        return corner.x + modelDist(screenX);
    }

    /**
     * Convert the given screen Y coordinate into model coordinates.
     * @param screenY
     * @return
     */
    public float modelY(float screenY) {
        return corner.y + modelDist(screenY);
    }

    /**
     * Get the model distance, given a screen distance.
     * @param scrnDist
     * @return
     */
    public PVector modelDist(PVector scrnDist) {
        return PVector.div(scrnDist, scale);
    }

    /**
     * Get the model distance, given a screen distance.
     * @param scrnDist
     * @return
     */
    public float modelDist(float scrnDist) {
        return scrnDist / scale;
    }

    /**
     * Get the screen coordinates, given a model coordinate.
     * @param model
     * @return
     */
    public PVector screen(PVector model) {
        return screenDist(PVector.sub(model, corner));
    }

    /**
     * Convert the given model X coordinate into screen coordinates.
     * @param modelX
     * @return
     */
    public float screenX(float modelX) {
        return screenDist(modelX - corner.x);
    }

    /**
     * Convert the given model Y coordinate into screen coordinates.
     * @param modelY
     * @return
     */
    public float screenY(float modelY) {
        return screenDist(modelY - corner.y);
    }

    /**
     * Get the screen distance, given a model distance.
     * @param modelDist
     * @return
     */
    public PVector screenDist(PVector modelDist) {
        return PVector.mult(modelDist, scale);
    }

    /**
     * Get the screen distance, given a model distance.
     * @param modelDist
     * @return
     */
    public float screenDist(float modelDist) {
        return modelDist * scale;
    }

    /**
     * Returns a new PVector object with the screen location of the mouse.
     * @return
     */
    public PVector mouseVec() {
        return new PVector(applet.mouseX, applet.mouseY);
    }

    /**
     * Returns true if the given model coordinate is visible on the screen.
     * @param model
     * @return
     */
    public boolean isVisible(PVector model) {
        PVector s = screen(model);
        return s.x > 0 & s.x < applet.width & s.y > 0 & s.y < applet.height;
    }

    /**
     * Returns true if the given model coordinate is visible on the screen.
     * @param modelX 
     * @param modelY 
     * @return
     */
    public boolean isVisible(float modelX, float modelY) {
        return isVisible(new PVector(modelX, modelY));
    }


    //Commented out to remove dependency on point2line
//    /**
//     * Returns true if the current camera's view is intersected by a line between points a and b. Does not take
//     * into account the width of the line.
//     * @param a one endpoint of the line
//     * @param b other endpoint of the line.
//     * @return
//     */
//    public boolean intersectedBy(PVector a, PVector b) {
//        if (isVisible(a) || isVisible(b))
//            return true;
//        Vect2 pa = new Vect2(a.x, a.y), pb = new Vect2(b.x, b.y);
//        Vect2[] ps = new Vect2[]{
//            new Vect2(0, 0), new Vect2(applet.width, 0),
//            new Vect2(applet.width, applet.height), new Vect2(0, applet.height)};
//        for (int k = 0; k < 4; k++) {
//            if (Space2.lineIntersection(pa, pb, ps[k], ps[(k + 1) % 4]) != null)
//                return true;
//        }
//        return false;
//    }

//======================BEGIN DEPRECATED=============================
    
    /**
     * Use scrollWheel() and za(float) instead
     * @param f
     * @deprecated
     * @see scrollWheel(), za(float)
     */
    public void registerScrollWheel(float f) {
        scrollWheel();
        za(f);
    }
    
    /**
     * Use noScrollWheel() instead
     * @deprecated
     */
    public void unregisterScrollWheel() {
        noScrollWheel();
    }

    /**
     * Use model(PVector) instead
     * @param scrn
     * @deprecated 
     * @return
     * @see model(PVector)
     */
    public PVector world(PVector scrn) {
        return model(scrn);
    }
    
    private class MouseAdapterImpl extends MouseAdapter {

        protected MouseAdapterImpl() {
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
//            if(ui)
//                scale(PApplet.pow(uiZoomAmount, -e.getWheelRotation()));
            scrollAmount += -e.getWheelRotation(); //todo: watch out for threading issues between this and uiHandle!
        }
    }
}
