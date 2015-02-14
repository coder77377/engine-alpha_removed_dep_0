package ea.internal.frame;

import org.jbox2d.dynamics.World;

/**
 * Dieser Thread kümmert sich um die frameweise Weiterentwicklung der physikalischen Game-World.
 * Created by andonie on 14.02.15.
 */
public class WorldThread
extends FrameSubthread {

    /**
     * globaler WT-Counter
     */
    private static int wtcnt = 1;

    /**
     * Die Physik-World, die alle (komplexen) physikalischen Berechnungen wrappt.
     */
    private final World world;

    /**
     * Das deltaT für die kommende Berechnung. Wird vom Parent-Thread upgedated.
     */
    private float deltaT;

    /**
     * Die Anzahl an Iterationen für die Neuberechnung der Geschwindigkeitsvektoren.
     * Empfohlen: 6.
     */
    private int velocityIterations = 6;

    /**
     * Die Anzahl an Iterationen für die Neuberechnung der Positionsvektoren.
     * Empfohlen: 3.
     */
    private int positionIterations = 3;

    /**
     * Setzt den DeltaT-Wert für die kommende Brechnung. Wird vom Parent-Thread aufgerufen,
     * bevor die nächste Berechnung gestartet wird.
     * @param millis    Die Zeit in Millisekunden, die voraus gerechnet werden soll.
     */
    public void setDT(long millis) {
        deltaT = ((float)millis) / 1000f;
    }

    /**
     * Erstellt
     * @param world
     */
    public WorldThread(World world) {
        super("Physics-Thread #" + wtcnt++);
        this.world = world;
    }

    /**
     * Die Run-Methode; führt einen DeltaT-Schritt aus.
     */
    @Override
    public void frameLogic() {
        world.step(deltaT, velocityIterations, positionIterations);
    }
}
