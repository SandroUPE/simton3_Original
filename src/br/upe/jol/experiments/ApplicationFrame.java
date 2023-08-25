/**
 * 
 */
package br.upe.jol.experiments;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * @author Danilo
 *
 */
public class ApplicationFrame extends JFrame {	
	private static final long serialVersionUID = 1L;

	public ApplicationFrame() { this(""); }
	
	public ApplicationFrame(String title) {
		super(title);
		createUI();
	}

	protected void createUI() {
		setSize(new Dimension(900, 560));
		setPreferredSize(new Dimension(900, 560));
		center();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
		setNativeLookAndFeel();
	}

	public void center() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		int x = (screenSize.width - frameSize.width) / 2;
		int y = (screenSize.height - frameSize.height) / 2;
		setLocation(x, y);
	}

	public static void setNativeLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			setJavaLookAndFeel();
			System.out.println("Error setting native LAF: " + e);
		}
	}

	public static void setJavaLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Error setting Java LAF: " + e);
		}
	}

	public static void setMotifLookAndFeel() {
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
		} catch (Exception e) {
			System.out.println("Error setting Motif LAF: " + e);
		}
	}
}