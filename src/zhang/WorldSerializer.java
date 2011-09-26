/**
 * Created at 7:56:11 AM Jan 14, 2011
 */
package zhang;

import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.CircleShape;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.collision.PolygonShape;
import org.jbox2d.collision.Shape;
import org.jbox2d.collision.ShapeDef;
import org.jbox2d.collision.ShapeType;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class WorldSerializer {

    public static final int VERSION = 1;

    protected static Element singleEntry(Document doc, String name, Object value) {
        Element ele = doc.createElement(name);
        ele.appendChild(doc.createTextNode(String.valueOf(value)));
        return ele;
    }

    /**
     * Returns a "Shape" element to be used in the specified document. The Shape
     * element has one attribute, TYPE, one of CIRCLE_SHAPE, POLYGON_SHAPE, SHAPE_TYPE_COUNT, or UNKNOWN_SHAPE.
     * Then come the elements "Restitution", "Friction", and "Density", each of which has a single text node child
     * containing the value of the variable in the shape. After that, there is either a "Radius" and "Position" element
     * (if TYPE is CIRCLE_SHAPE), or there is a "Vertices" element, which contains a list of "Vertex" nodes, each of which
     * contains one text node holding the vertex data (if TYPE is POLYGON_SHAPE). No implementation has yet been made
     * for SHAPE_TYPE_COUNT or UNKNOWN_SHAPE.
     * @param doc
     * @param s
     * @return
     */
    public Element serializeShape(Document doc, Shape s) {
        Element ele = doc.createElement("Shape");
        ShapeType type = s.getType();
        ele.setAttribute("TYPE", type.toString());
        //done: restitution, friction, density
        ele.appendChild(singleEntry(doc, "Restitution", s.m_restitution));
        ele.appendChild(singleEntry(doc, "Friction", s.m_friction));
        ele.appendChild(singleEntry(doc, "Density", s.m_density));
        if(type.equals(ShapeType.CIRCLE_SHAPE)) {
            CircleShape cs = (CircleShape) s;
            ele.appendChild(singleEntry(doc, "Radius", cs.getRadius()));
            ele.appendChild(serializeVec2(doc, "Position", cs.getLocalPosition()));
        }
        else if(type.equals(ShapeType.POLYGON_SHAPE)) {
            PolygonShape ps = (PolygonShape) s;
            Element vertices = doc.createElement("Vertices");
            for(Vec2 v : ps.getVertices()) {
                vertices.appendChild(serializeVec2(doc, "Vertex", v));
            }
            ele.appendChild(vertices);
            //todo: maybe add centroid
//            ele.appendChild(singleEntry(doc, "name", ps.))
        }
        else {
            //todo: add support for other shapes
            throw new RuntimeException("Unsupported shape type "+type+"!");
        }
        //todo: save userdata
        return ele;
    }

    /**
     * Returns a body element with the given name, to be used in the given document.
     * The body element begins with two elements: "Angle", "AngularVelocity", each of which
     * holds one text node whose text is the body's respective angle and angular velocity.
     * The next two element are "Position" and "LinearVelocity", each of which holds one
     * text node whose text is the position and linear velocity vectors (to be deserialized with
     * <code>deserializeVec2</code>. The next entry, "Bullet", holds a boolean value specifying
     * if the body is a bullet. Then comes a "Shapes" element, which holds any number of Shape
     * elements, as specified by <code>serializeShape</code>.
     * @param doc
     * @param name
     * @param b
     * @return
     */
    public Element serializeBody(Document doc, String name, Body b) {
        Element body = doc.createElement(name);
        body.appendChild(singleEntry(doc, "Angle", b.getAngle()));
        body.appendChild(singleEntry(doc, "AngularVelocity", b.getAngularVelocity()));
        body.appendChild(singleEntry(doc, "AngularDamping", b.m_angularDamping));
        body.appendChild(serializeVec2(doc, "Position", b.getPosition()));
        body.appendChild(serializeVec2(doc, "LinearVelocity", b.getLinearVelocity()));
        body.appendChild(singleEntry(doc, "LinearDamping", b.m_linearDamping));
        //todo: maybe save the XForm instead of the position and angle
        //todo: angular damping, linear damping
        //todo: jointlist, contactlist, force, torque
        body.appendChild(singleEntry(doc, "Bullet", b.isBullet()));
        Element shapes = doc.createElement("Shapes");
        for(Shape s = b.getShapeList(); s != null; s = s.getNext()) {
            shapes.appendChild(serializeShape(doc, s));
        }
        body.appendChild(shapes);
        //todo: maybe save the massdata
        //todo: maybe save userdata
        //todo: fixedrotation, fixtures?
        //todo: flags, force?
        return body;
    }
    /**
     * Returns an element with the given name, holding one text node that contains the vector's x and y coordinates
     * separated by a single space.
     * @param argDoc
     * @param argName
     * @param argVec
     * @return
     */
    public Element serializeVec2(Document argDoc, String argName, Vec2 argVec) {
        Element vec = argDoc.createElement(argName);
        Text t = argDoc.createTextNode(argVec.x + " " + argVec.y);
        vec.appendChild(t);
        return vec;
    }
    /**
     * Takes the given node, which should be the element as returned by <code>serializeVec2</code>, and returns
     * the Vec2 it was serialized from.
     * @param node
     * @return
     */
    public Vec2 deserializeVec2(Node node) {
        String s = node.getTextContent();
        String elms[] = s.split(" ");
        return new Vec2(Float.parseFloat(elms[0]), Float.parseFloat(elms[1]));
    }

    /**
     * Returns a World element to be used with the specified document. A World element has an attribute
     * "Version" that holds the current serialization version. The first child is a Vec2 element named "Gravity".
     * After that is a "Bodies" element, which holds some number of Body elements, serialized in the order
     * in which <code>world.getBodyList()</code> returns them. Each Body element has the name "Body" and is serialized
     * according to <code>serializeBody(Document, String, Body)</code>.
     * <b>Note</b>: This method does not actually append the World element to the document. You must call doc.appendChild
     * on the returned Element to actually include the serialized World in the document.
     * @param doc
     * @param world
     * @return
     */
    public Element serializeWorld(Document doc, World world) {
        Element root = doc.createElement("World");
        root.setAttribute("Version", VERSION + "");

        Element gravity = serializeVec2(doc, "Gravity", world.getGravity());
        root.appendChild(gravity);

        Element bodies = doc.createElement("Bodies");
        for(Body b = world.getBodyList(); b != null; b = b.getNext()) {
            bodies.appendChild(serializeBody(doc, "Body", b));
        }
        root.appendChild(bodies);
        //todo: contacts, joints
        //broad phase, contact manager, body list, contact list, joint list
        return root;


//        if (argWorld.getGravity().isValid() && !argWorld.getGravity().equals(new Vec2())) {
//            Element gravity = Methods.serializeVec2(argDocument, "Gravity", argWorld.getGravity());
//            root.appendChild(gravity);
//        }

    }

    /**
     * Given a World element as specified by <code>serializeWorld</code>, this method will populate the argWorld
     * with the same information that was serialized in the element. Note that this method does not instantiate a new
     * world, since the application specific data tied with a World (Boundary/ContactListener, DebugDraw, warm-starting, etc.)
     * cannot be serialized with the World. Clients should manually construct a new world with application specific information
     * and then call deserializeWorld, passing the new world as an argument.
     * @param e
     * @param argWorld
     * @return
     */
    public void deserializeWorld(Element e, World argWorld) {
        //TODO: Two bugs.
        //a) serializeWorld also serializes the initial dummy Body that exists at the tail of the World. This method also deserializes it.
        //b) This method recovers bodies and shapes in the OPPOSITE order in which they were sent.
        //So, if world A has a body list B1->B2->B3->DUMMY, the deserialized world looks like DUMMY->B3->B2->B1->DUMMY
        //Within each body, if it has a shape list S1->S2->S3, the deserialized body has a shape list S3->S2->S1

        //solutions:
        //a) don't serialize the dummy body (or figure out a way to remove it from the world to begin with)
        //b) Iterate through the XML document backwards

        
        //Gravity
            //vec2text
        Vec2 gravity = deserializeVec2(e.getFirstChild());
        argWorld.setGravity(gravity);
        //Bodies
        Element bodiesEle = (Element) e.getFirstChild().getNextSibling();
        NodeList bodies = bodiesEle.getElementsByTagName("Body");
        for(int i = bodies.getLength() - 1; i >= 0; i--) {
            deserializeAndAddBody(argWorld, (Element) bodies.item(i)); //todo: is this safe?
        }
    }

    /**
     * Given a Node that was created by <code>serializeBody</code>, this method will reconstruct the saved body and add it to
     * the specified World.
     * @param argWorld
     * @param bodyEle
     * @throws NumberFormatException
     * @throws DOMException
     */
    public Body deserializeAndAddBody(World argWorld, Element bodyEle) throws NumberFormatException, DOMException {
        BodyDef bodyDef = new BodyDef();
        //Body
        Node n = bodyEle.getFirstChild();
        //Angle
        bodyDef.angle = Float.parseFloat(n.getTextContent());
        //float
        n = n.getNextSibling();
        //AngularVelocity
        float angularVelocity = Float.parseFloat(n.getTextContent());
        //float
        n = n.getNextSibling();
        //AngularDamping
        bodyDef.angularDamping = Float.parseFloat(n.getTextContent());
        //float
        n = n.getNextSibling();
        //Position
        bodyDef.position = deserializeVec2(n);
        //vec2text
        n = n.getNextSibling();
        //LinearVelocity
        Vec2 linearVelocity = deserializeVec2(n);
        //vec2text
        n = n.getNextSibling();
        //LinearDamping
        bodyDef.linearDamping = Float.parseFloat(n.getTextContent());
        //float
        n = n.getNextSibling();
        //Bullet
        bodyDef.isBullet = Boolean.parseBoolean(n.getTextContent());
        //boolean
        Body body = argWorld.createBody(bodyDef);
        n = n.getNextSibling();
        //Shapes
        for (Node shapeNode = n.getFirstChild(); shapeNode != null; shapeNode = shapeNode.getNextSibling()) {
            deserializeAndAttachShape(argWorld, body, (Element) shapeNode);
        }
        body.setAngularVelocity(angularVelocity);
        body.setLinearVelocity(linearVelocity);
        //Body
        //Body
        //...
        body.setMassFromShapes();
        return body;
    }

    /**
     * Given an Element created by <code>serializeShape</code>, this method will reconstruct the saved shape and add it to
     * the specified Body.
     * @param argWorld
     * @param b
     * @param shapeEle
     * @return
     */
    public Shape deserializeAndAttachShape(World argWorld, Body b, Element shapeEle) {
        ShapeType type = ShapeType.valueOf(shapeEle.getAttribute("TYPE"));
        ShapeDef def;
        float restitution, friction, density;
        Node shapeIter = shapeEle.getFirstChild();
        restitution = Float.parseFloat(shapeIter.getTextContent());
        shapeIter = shapeIter.getNextSibling();
        friction = Float.parseFloat(shapeIter.getTextContent());
        shapeIter = shapeIter.getNextSibling();
        density = Float.parseFloat(shapeIter.getTextContent());

        switch(type) {
            case CIRCLE_SHAPE:
            //Shape TYPE = "CIRCLE_SHAPE"
                CircleDef defC = new CircleDef();
                shapeIter = shapeIter.getNextSibling();
                //Radius
                defC.radius = Float.parseFloat(shapeIter.getTextContent());
                    //float
                shapeIter = shapeIter.getNextSibling();
                //Position
                defC.localPosition = deserializeVec2(shapeIter);
                    //vec2text

                def = defC;
                break;
            case POLYGON_SHAPE:
            //Shape TYPE = "POLYGON_SHAPE"
                PolygonDef defP = new PolygonDef();
                NodeList vertices = shapeEle.getElementsByTagName("Vertex");
                for(int i = 0; i < vertices.getLength(); i++) {
                    Node vertexNode = vertices.item(i);
                    Vec2 vertex = deserializeVec2(vertexNode);
                    defP.addVertex(vertex);
                }
                //Vertices
                    //Vertex
                        //vec2text
                    //Vertex
                    //Vertex
                    //...
            //...

                def = defP;
                break;
            default:
                throw new RuntimeException("WTF");
        }
        def.restitution = restitution; 
        def.friction = friction; 
        def.density = density;
        Shape shape = b.createShape(def);
        return shape;
    }
}
