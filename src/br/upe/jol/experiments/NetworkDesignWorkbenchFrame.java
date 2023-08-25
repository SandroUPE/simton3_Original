package br.upe.jol.experiments;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

/**
 * Classe do frame principal para análise da otimização de projeto de redes
 * 
 * @author Danilo Araújo
 * @since 28/12/2010
 */
public class NetworkDesignWorkbenchFrame extends ApplicationFrame{
	private static final long serialVersionUID = 1L;
	
	private JMenuBar menuBar;
	private JMenu menu;
	private JMenu submenu;
	private JMenuItem menuItem;
	private JRadioButtonMenuItem rbMenuItem;
	private JCheckBoxMenuItem cbMenuItem;

	
	public NetworkDesignWorkbenchFrame(String title){
		super(title);
	}
	
	private void criarMenu(){
		menuBar = new JMenuBar();
		//Build the first menu.
		menu = new JMenu("Experimentos");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription(
		        "The only menu in this program that has menu items");
		menuBar.add(menu);

		//a group of JMenuItems
		menuItem = new JMenuItem("Iniciar simulação...",
		                         KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_1, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
		        "This doesn't really do anything");
		menu.add(menuItem);

		menuItem = new JMenuItem("Analisar simulação...",
		                         new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_B);
		menu.add(menuItem);

		menuItem = new JMenuItem(new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_D);
		menu.add(menuItem);

		//a group of radio button menu items
		menu.addSeparator();
		
		menuItem = new JMenuItem("Sair");
		menuItem.setMnemonic(KeyEvent.VK_R);
		menu.add(menuItem);

		//Build second menu in the menu bar.
		menu = new JMenu("Editar");
		menu.setMnemonic(KeyEvent.VK_N);
		menu.getAccessibleContext().setAccessibleDescription(
		        "This menu does nothing");
		menuBar.add(menu);
		
		JMenu menuConfig = new JMenu("Configurações");
		menuConfig.setMnemonic(KeyEvent.VK_C);
		menuConfig.getAccessibleContext().setAccessibleDescription(
		        "This menu does nothing");
		menuBar.add(menuConfig);
		
		ButtonGroup group = new ButtonGroup();
		rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
		rbMenuItem.setSelected(true);
		rbMenuItem.setMnemonic(KeyEvent.VK_R);
		group.add(rbMenuItem);
		menuConfig.add(rbMenuItem);

		rbMenuItem = new JRadioButtonMenuItem("Another one");
		rbMenuItem.setMnemonic(KeyEvent.VK_O);
		group.add(rbMenuItem);
		menuConfig.add(rbMenuItem);

		//a group of check box menu items
		menu.addSeparator();
		cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
		cbMenuItem.setMnemonic(KeyEvent.VK_C);
		menuConfig.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Another one");
		cbMenuItem.setMnemonic(KeyEvent.VK_H);
		menuConfig.add(cbMenuItem);

		//a submenu
		menu.addSeparator();
		submenu = new JMenu("A submenu");
		submenu.setMnemonic(KeyEvent.VK_S);

		menuItem = new JMenuItem("An item in the submenu");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_2, ActionEvent.ALT_MASK));
		submenu.add(menuItem);

		menuItem = new JMenuItem("Another item");
		submenu.add(menuItem);
		menuConfig.add(submenu);
		
		JMenu menuSobre = new JMenu("Ajuda");
		menuSobre.setMnemonic(KeyEvent.VK_S);
		menuSobre.getAccessibleContext().setAccessibleDescription(
		        "This menu does nothing");
		menuBar.add(menuSobre);

		setJMenuBar(menuBar);

	}
	
	private void initialize() throws ParseException{
		JPanel panel = new JPanel();

		criarMenu();
		
		
		setIconImage(Util.loadImage("brasao_upe.png"));
		setPreferredSize(new Dimension(1024, 720));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		getContentPane().setBackground(Color.white);
		
		getContentPane().add(panel);
		pack();
		setVisible(true);
		setResizable(false);
	}
	
	public static void main(String[] args) {
		NetworkDesignWorkbenchFrame app = new NetworkDesignWorkbenchFrame("Otimização Multiobjetivos para projeto de redes ópticas");
		try {
			app.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
