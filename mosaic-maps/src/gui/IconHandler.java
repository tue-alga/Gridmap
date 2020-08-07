package gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import main.Hexastuff;

/**
 * This class contains a method to fetch icons.
 */
public class IconHandler {
	
	/**
	 * Retrieves an icon.
	 * 
	 * @param path The path to the icon.
	 * @return The {@link ImageIcon} constructed, or <code>null</code> if the image
	 * couldn't be found or read.
	 */
	public static ImageIcon getIcon(String path) {
		
		URL f = Hexastuff.class.getResource("res" + System.getProperty("file.separator") + path + ".png");
		
		if (f == null) {
			System.out.println("Couldn't find resource " + "res" + System.getProperty("file.separator") + path + ".png");
			return null;
		}

                try {
			return new ImageIcon(ImageIO.read(f));
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Retrieves an icon, and combines it with an emblem.
	 * 
	 * @param path The path to the icon.
	 * @param emblemPath The path to the emblem.
	 * @return The {@link ImageIcon} constructed, or <code>null</code> if one of the images
	 * couldn't be found or read.
	 */
	public static ImageIcon getIconEmblemed(String path, String emblemPath) {
		
		URL f = Hexastuff.class.getResource("/res" + System.getProperty("file.separator") + path + ".png");
		
		if (f == null) {
			System.out.println("Couldn't find resource " + "/res" + System.getProperty("file.separator") + path + ".png");
			return null;
		}
		
		BufferedImage icon;
		try {
			icon = ImageIO.read(f);
		} catch (IOException e) {
			return null;
		}
		
		f = Hexastuff.class.getResource("/res" + System.getProperty("file.separator") + emblemPath + ".png");
		
		if (f == null) {
			System.out.println("Couldn't find resource " + "/res" + System.getProperty("file.separator") + emblemPath + ".png");
			return null;
		}
		
		BufferedImage emblem;
		try {
			emblem = ImageIO.read(f);
		} catch (IOException e) {
			return null;
		}
		
		icon.getGraphics().drawImage(emblem, 0, icon.getHeight() - emblem.getHeight(), null);
		
		return new ImageIcon(icon);
	}
}
