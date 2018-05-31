package ea.actor;

import ea.Point;
import ea.internal.ano.API;
import ea.internal.ano.NoExternalUse;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;

import java.awt.*;

/**
 * Beschreibt eine beliebige polygonale geometrische Form.
 *
 * @author Michael Andonie
 */
@API
public class Polygon extends Geometry {
    /**
     * Die Punkte
     */
    private final Point[] points;

    /**
     * Die Punkte, die das Polygon beschreiben
     */
    private final int[] px, py;

    /**
     * Erstellt ein neues Polygon. Seine Position ist der <b>Ursprung</b>.
     *
     * @param points Der Streckenzug an Punkten, der das Polygon beschreibt. Alle
     */
    @API
    public Polygon(Point... points) {
        if (points.length < 3) {
            throw new RuntimeException("Der Streckenzug muss mindestens aus 3 Punkten bestehen, um ein gültiges Polygon zu beschreiben.");
        }

        this.points = points.clone();

        this.px = new int[points.length];
        this.py = new int[points.length];

        for (int i = 0; i < points.length; i++) {
            px[i] = Math.round(points[i].getRealX());
            py[i] = Math.round(points[i].getRealY());
        }
    }

    /**
     * {@inheritDoc}
     */
    @NoExternalUse
    @Override
    public void render(Graphics2D g) {
        g.setColor(getColor());
        g.fillPolygon(px, py, px.length);
    }

    /**
     * {@inheritDoc}
     */
    @NoExternalUse
    @Override
    public Shape createShape(float pixelProMeter) {
        Vec2[] vectors = new Vec2[points.length];

        for (int i = 0; i < points.length; i++) {
            vectors[i] = points[i].toVec2().mul(1 / pixelProMeter);
        }

        PolygonShape shape = new PolygonShape();
        shape.set(vectors, points.length);

        return shape;
    }
}