/*
 * Engine Alpha ist eine anfaengerorientierte 2D-Gaming Engine.
 * 
 * Copyright (C) 2011 Michael Andonie
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ea;

import ea.internal.util.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Der Manager ist eine Standartklasse und eine der wichtigsten der Engine Alpha, die zur Interaktion ausserhalb der engine benutzt werden kann.<br />
 * Neben einer Liste aller moeglichen Fonts handelt er auch das <b>Ticker-System</b>.
 * Dies ist ein relativ konsistentes System, das viele <b><code>Ticker</code></b>-Objekte - Interfaces mit einer Methode, die in immergleichen Abstaenden
 * immer wieder aufgerufen werden. <br />
 * <br />
 * 
 * Bewusst leitet sich diese Klasse nicht von <code>Thread</code> ab. Hierdurch kann ein Manager ohne grossen Ressourcenaufwand erstellt werden,
 * wobei der Thread (und damit Computerrechenzeit) erst mit dem aktiven Nutzen erstellt wird
 * 
 * @author Michael Andonie
 * @see Ticker
 */
public class Manager {
	/**
	 * Der Counter aller vorhandenen Manager-Tickerthreads
	 */
	private static int nummerCount = 0;
	
	/**
	 * Der Standard-Manager. Dieser wird nur innerhalb des "ea"-Paketes-verwendet!<br />
	 * Er ist der Manager, der verschiedene Ticker-Bedürfnisse von einzelnen internen Klassen deckt und seine Fassung ist
	 * exakt an der Anzahl der noetigen Ticker angeglichen. Dieser ist fuer:<br />
	 * - Die Fensterkontrollroutine<br />
	 * - Die Kollisionskontrollroutine der Klasse <code>Physik</code><br />
	 * - Die Figurenanimationsroutine<br />
	 * - Die Leuchtanimationsroutine
	 */
	public static final Manager standard = new Manager("Interner Routinenmanager");

	/**
	 * Die 'Liste' aller möglichen Fontnamen des Systems, auf dem man sich gerade befindet.<br />
	 * Hiernach werden ueberpruefungen gemacht, ob die gewuenschte Schriftart auch auf dem hiesigen PC vorhanden ist.
	 */
	public static final String[] fontNamen;

	static {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		fontNamen = ge.getAvailableFontFamilyNames();
	}

	/**
	 * Die Liste aller Aufträge.
	 */
	private volatile ArrayList<Auftrag> liste = new ArrayList<>();
	
	/**
	 * Der Name des Threads, ueber dem dieser Manager arbeitet
	 */
	private final String name;

	private final ScheduledExecutorService executor;
	
	/**
	 * Konstruktor eines Managers.<br />
	 * Bei einem normalen Spiel muss nicht extra ein Manager erstellt werden. Dafuer gibt es bereits eine Referenz<br />
	 * <br />
	 * <code>public final Manager manager;</code><br />
	 * <br />
	 * in der Klass <code>Game</code> und damit auch in jeder spielsteurnden Klasse.
	 * 
	 * @param name
	 *            Der Name, den der Thread haben wird, ueber den dieser Manager laeuft.<br />
	 *            Dieser Parameter kann auch einfach weggelassen werden; in diesem Fall erhaelt der Ticker einen standartisierten Namen.
	 * @see #Manager()
	 */
	public Manager(String name) {
		this.name = name;
		this.executor = Executors.newScheduledThreadPool(10);

		nummerCount++;
	}
	
	/**
	 * Vereinfachter Konstruktor ohne Parameter.<br />
	 * Bei einem normalen Spiel muss nicht extra ein Manager erstellt werden. Dafuer gibt es bereits eine Referenz<br />
	 * <br />
	 * <code>public final Manager manager;</code><br />
	 * <br />
	 * in der Klass <code>Game</code> und damit auch in jeder spielsteurnden Klasse.
	 */
	public Manager() {
		this("Tickerthread " + (nummerCount + 1));
	}
	
	/**
	 * Diese Methode prueft, ob zur Zeit <b>mindestens 1 Ticker</b> an diesem
	 * Manager ausgeführt wird.
	 * @return	<code>true</code>, wenn mindestens 1 Ticker an diesem Manager
	 * 			zur Zeit mit seiner <code>tick()</code>-Methode ausgeführt wird.
	 * 			Sonst <code>false</code>.
	 */
	public boolean hatAktiveTicker() {
		for(Auftrag a : liste) {
			if(a.aktiv) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Meldet einen Ticker am Manager an. Ab sofort laeuft er auf diesem Manager und damit wird auch dessen <code>tick()</code>-Methode immer wieder aufgerufen.
	 * 
	 * @param t
	 *            Der anzumeldende Ticker
	 * @see #anmelden(Ticker)
	 */
	public void anmelden(Ticker t, int intervall) {
		anmelden(t);
		
		starten(t, intervall);
	}
	
	/**
	 * Macht diesem Manager einen Ticker bekannt, <b>OHNE</b> ihn aufzurufen.
	 * 
	 * @param t
	 *            Der anzumeldende Ticker
	 * @see #anmelden(Ticker, int)
	 */
	public void anmelden(Ticker t) {
		if (istAngemeldet(t)) {
			Logger.warning("Der Ticker ist bereits an diesem Manager angemeldet und wird nicht erneut angemeldet.");
			return;
		}
		
		liste.add(new Auftrag(t, 1000, false));
	}
	
	/**
	 * Gibt den Auftrag zu einem bestimmten Ticker aus.
	 * 
	 * @param t
	 *            Der Ticker, zu dem der entsprechende Auftrag
	 * @return
	 */
	private Auftrag auftragZu(Ticker t) {
		for (Auftrag a : liste) {
			if (a.steuert(t)) {
				return a;
			}
		}
		
		return null;
	}
	
	/**
	 * Prueft, ob ein Ticker t bereits angemeldet ist.
	 * 
	 * @param t
	 *            Der zu pruefende Ticker.
	 * @return <code>true</code>, falls der Ticker bereits an diesem <code>Manager</code> angemeldet ist, sonst <code>false</code>.
	 */
	public boolean istAngemeldet(Ticker t) {
		for (Auftrag a : liste) {
			if (a.steuert(t)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Startet einen Ticker, der <b>bereits an diesem Manager angemeldet ist</b>.<br />
	 * Laueft der Ticker bereits, passiert gar nichts. War der Ticker nicht angemeldet, kommt eine Fehlermeldung.
	 * 
	 * @param t
	 *            Der zu startende, <b>bereits am Manager angemeldete</b> Ticker.
	 * @param intervall
	 *            Das Intervall, in dem dieser Ticker ab sofort immer wieder aufgerufen wird.
	 * @see #anhalten(Ticker)
	 */
	public void starten(final Ticker t, int intervall) {
		if (!istAngemeldet(t)) {
			Logger.error("Der Ticker ist noch nicht angemeldet.");
			return;
		}
		
		Auftrag a = auftragZu(t);
		
		if (a.aktiv) {
			Logger.error("Ticker ist bereits am Laufen!");
			return;
		}
		
		Runnable r = new Runnable() {
			@Override public void run() {
				t.tick();
			}
		};

		ScheduledFuture<?> future = executor.schedule(r, intervall, TimeUnit.MILLISECONDS);
		
		a.aktivSetzen(true);
		a.taskSetzen(future);
	}
	
	/**
	 * Haelt einen Ticker an, der <b>bereits an diesem Manager angemeldet ist</b>.<br />
	 * Ist der Ticker bereits angehalten, passiert gar nichts. War der Ticker nicht angemeldet, kommt eine Fehlermeldung.
	 * 
	 * @param t
	 *            Der anzuhaltende Ticker
	 * @see #starten(Ticker, int)
	 */
	public void anhalten(Ticker t) {
		if (!istAngemeldet(t)) {
			Logger.error("Der Ticker ist noch nicht angemeldet.");
			return;
		}
		
		Auftrag a = auftragZu(t);
		ScheduledFuture<?> future = a.task;
		
		a.aktivSetzen(false);

		if(!future.isCancelled()) {
			future.cancel(false);
		}
	}
	
	/**
	 * Diese Methode setzt das Intervall eines Tickers neu.
	 * 
	 * @param t
	 *            Der Ticker, dessen Intervall geaendert werden soll.<br />
	 *            Ist er nicht an dem Manager angemeldet, so wird eine Fehlermeldung ausgeloest!
	 * @param intervall
	 *            Das neue Intervall fuer den Ticker
	 */
	public void intervallSetzen(Ticker t, int intervall) {
		if (!istAngemeldet(t)) {
			Logger.error("Der Ticker ist noch nicht angemeldet.");
			return;
		}
		
		Auftrag a = auftragZu(t);
		
		if (a.aktiv) {
			// TODO Abmelden
			
			anmelden(t, intervall);
		}
		
		a.intervall = intervall;
	}
	
	/**
	 * Meldet einen Ticker ab.<br />
	 * War dieser Ticker nicht angemeldet, so passiert nichts, ausser einer Fehlermeldung.
	 * 
	 * @param t
	 *            Der abzumeldende Ticker
	 */
	public void abmelden(Ticker t) {
		if (!istAngemeldet(t)) {
			Logger.error("Der Ticker ist noch nicht angemeldet.");
			return;
		}
		
		Auftrag a = auftragZu(t);
		
		if (a.aktiv) {
			a.task.cancel(false);
		}

		liste.remove(a);
	}
	
	/**
	 * Macht diesen Manager frei von allen aktiven Tickern, jedoch ohne ihn selbst
	 * zu beenden. Neue Ticker koennen jederzeit wieder angemeldet werden.
	 */
	public void alleAbmelden() {
		for (Auftrag a : liste) {
			if (a.aktiv) {
				a.task.cancel(false);
			}
		}

		liste = new ArrayList<>();
	}
	
	/**
	 * Beendet den Thread, den dieser Manager verwendet und damit den Manager
	 * selbst. Sollte <b>nur</b> aufgerufen werden, wenn der Manager selbst
	 * geloescht werden soll.
	 */
	public void kill() {
		alleAbmelden();
	}
	
	/**
	 * Prueft, ob ein Font auf diesem Computer existiert.
	 * 
	 * @param name
	 *            Der Name des zu ueberpruefenden Fonts
	 * @return TRUE, wenn der Font auf dem PC existiert
	 */
	public static boolean fontExistiert(String name) {
		for (int i = 0; i < fontNamen.length; i++) {
			if (fontNamen[i].equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Diese Klasse beschreibt einen "Tick-Auftrag" und sammelt so alle eigenschaften:<br />
	 * Ticker, Intervall, Aktivitaet.
	 */
	private final class Auftrag {
		/**
		 * Der Ticker dieses Auftrages
		 */
		private final Ticker ticker;
		
		/**
		 * Das Intervall dieses Auftrages
		 */
		@SuppressWarnings("unused")
		private int intervall;
		
		/**
		 * Ob der Ticker momentan aktiv ist.
		 */
		private boolean aktiv;
		
		/**
		 * ScheduledFuture, das aufgerufen wird.
		 */
		private ScheduledFuture<?> task;
		
		/**
		 * Konstruktor.
		 * 
		 * @param ticker
		 *            Der Ticker dieses Auftrages.
		 * @param intervall
		 *            Das Aufrufintervall des Tickers in 1/ms.
		 * @param aktiv
		 *            Ob dieser Ticker aktiv ist
		 */
		public Auftrag(Ticker ticker, int intervall, boolean aktiv) {
			this.ticker = ticker;
			this.intervall = intervall;
			this.aktiv = aktiv;
		}
		
		/**
		 * Prueft, ob dieser Auftrag einen bestimmten Ticker swteuert.
		 * 
		 * @param t
		 *            Der Ticker, der auf Gleichheit mit dem angelegten zu pruefen ist.
		 * @return <code>true</code>, wenn beide Ticker identisch sind (Pruefung mit <code>equals</code>), sonst <code>false</code>.
		 */
		public boolean steuert(Ticker t) {
			return ticker.equals(t);
		}
		
		/**
		 * Setzt das Aufrufintervall neu.
		 * 
		 * @param intervall
		 *            Das neue Aufrufintervall
		 */
		@SuppressWarnings("unused")
		public void intervallSetzen(int intervall) {
			this.intervall = intervall;
		}
		
		/**
		 * Setzt, ob der anliegende Ticker momentan aktiv ist.
		 * 
		 * @param aktiv
		 *            Ob der anliegende Ticker aufgerufen werden soll, oder nicht.
		 */
		public void aktivSetzen(boolean aktiv) {
			this.aktiv = aktiv;
		}
		
		/**
		 * Setzt den Task neu.
		 * 
		 * @param task
		 *            Das neue tatsächliche ScheduledFuture, das ausgeführt wird.
		 */
		public void taskSetzen(ScheduledFuture<?> task) {
			this.task = task;
		}
	}
}
