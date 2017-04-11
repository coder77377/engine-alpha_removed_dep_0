/*
 * Engine Alpha ist eine anfängerorientierte 2D-Gaming Engine.
 *
 * Copyright (c) 2011 - 2017 Michael Andonie and contributors.
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

package ea.internal.io;

import ea.internal.util.Optimizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageLoader {
	private ImageLoader () {
		// keine Objekte erlaubt!
	}

	/**
	 * Lädt ein Bild und optimiert es für das aktuelle System.
	 *
	 * @param path
	 * 		Pfad des Bildes.
	 *
	 * @return geladenes Bild
	 */
	public static BufferedImage load(String path) {
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(ResourceLoader.load(path)));
			return Optimizer.toCompatibleImage(img);
		} catch (IOException e) {
			throw new RuntimeException("Das Bild konnte nicht geladen werden: " + path);
		}
	}
}