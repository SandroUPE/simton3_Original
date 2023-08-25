package br.upe.jol.experiments;

import static br.upe.jol.base.Util.LOGGER;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;

public class SimulONDApplet extends JApplet{
	private static final long serialVersionUID = 1L;
	
	private ParetoCanvas sprite;
	
	private JTextField txtExperimento;
	
	private JTextField txtExperimento2;
	
	private JTextField txtExperimento3;
	
	private JTextField txtExperimento4;
	
	private JTextField txtExperimento5;
	
	private JTextField txtTituloExperimento;
	
	private JTextField txtTituloExperimento2;
	
	private JTextField txtTituloExperimento3;
	
	private JTextField txtTituloExperimento4;
	
	private JTextField txtTituloExperimento5;
	
	private JFileChooser fileChooser = new JFileChooser();
	
	private SolutionSet<Double> solutionSet1 = new SolutionSet<Double>();
	
	private SolutionSet<Double> solutionSet2 = new SolutionSet<Double>();
	
	private SolutionSet<Double> solutionSet3 = new SolutionSet<Double>();
	
	private SolutionSet<Double> solutionSet4 = new SolutionSet<Double>();
	
	private SolutionSet<Double> solutionSet5 = new SolutionSet<Double>();
	
	private NFSNetSprite nfsnet;
	
	private SimtonProblem problem = new SimtonProblem(14, 2);
	
	private JCheckBox chkAutoAjuste;
	
	private JCheckBox chkEscalaLogX;
	
	private JCheckBox chkEscalaLogY;
	
	public void init() {
	    //Execute a job on the event-dispatching thread:
	    //creating this applet's GUI.
	    try {
	        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
	            public void run() {
	                createGUI();
	            }
	        });
	    } catch (Exception e) {
	        System.err.println("createGUI didn't successfully complete");
	    }
	}
	
	public static final void readObjectivesFromFile(SolutionSet<Double> ss, String path) {
		try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			int j = 0;
			while (br.ready()) {
				String[] objectives = br.readLine().split(" ");
				for (int i = 0; i < 2; i++)
					ss.getSolutionsList().get(j)
							.setObjective(i, Double.parseDouble(objectives[i].replaceAll(",", ".")));
				j++;
			}

			br.close();
		} catch (IOException e) {
			LOGGER.severe("Error acceding to the file");
			e.printStackTrace();
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
	
	public void atualizarTopologia(Integer[] net, double pb, double custo){
		nfsnet.setNetwork(net);
	    nfsnet.setCapex(custo);
	    nfsnet.setPb(pb);
	    nfsnet.repaint();
	}

	private void createGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Error setting native LAF: " + e);
		}
		int widthDefault = 270;
		Dimension defaultDimension = new Dimension(widthDefault, 24);
		JPanel pControle = new JPanel();
		pControle.setPreferredSize(new Dimension(290, 630));
		
		JLabel labelExp = new JLabel("Selecione os arquivos dos experimentos:");
		labelExp.setPreferredSize(defaultDimension);
		
		JButton btnExportar = new JButton("EPS Pareto");
		btnExportar.setPreferredSize(new Dimension(widthDefault/3-2, 24));
		JButton btnExportarRede = new JButton("EPS Rede");
		btnExportarRede.setPreferredSize(new Dimension(widthDefault/3-2, 24));
		JButton btnAtualizar = new JButton("Atualizar");
		btnAtualizar.setPreferredSize(new Dimension(widthDefault/3-2, 24));
		
		btnAtualizar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String arqVar;
				String arqPf;
				
				if (txtExperimento.getText().trim().length() > 0){
					arqVar = txtExperimento.getText().trim().replaceAll("_pf.", "_var.");
					arqPf = txtExperimento.getText().trim().replaceAll("_var.", "_pf.");
					solutionSet1.readIntVariablesFromFile(arqVar, problem);
					SimulONDApplet.readObjectivesFromFile(solutionSet1, arqPf);
					sprite.atualizarExperimento(solutionSet1, new int[]{450,315}, 0.5, 900, 630, 0);
				}
				if (txtExperimento2.getText().trim().length() > 0){
					arqVar = txtExperimento2.getText().trim().replaceAll("_pf.", "_var.");
					arqPf = txtExperimento2.getText().trim().replaceAll("_var.", "_pf.");
					solutionSet2.readIntVariablesFromFile(arqVar, problem);
					SimulONDApplet.readObjectivesFromFile(solutionSet2, arqPf);
					sprite.atualizarExperimento(solutionSet2, new int[]{450,315}, 0.5, 900, 630, 1);
				}
				if (txtExperimento3.getText().trim().length() > 0){
					arqVar = txtExperimento3.getText().trim().replaceAll("_pf.", "_var.");
					arqPf = txtExperimento3.getText().trim().replaceAll("_var.", "_pf.");
					solutionSet3.readIntVariablesFromFile(arqVar, problem);
					SimulONDApplet.readObjectivesFromFile(solutionSet3, arqPf);
					sprite.atualizarExperimento(solutionSet3, new int[]{450,315}, 0.5, 900, 630, 2);
				}
				if (txtExperimento4.getText().trim().length() > 0){
					arqVar = txtExperimento4.getText().trim().replaceAll("_pf.", "_var.");
					arqPf = txtExperimento4.getText().trim().replaceAll("_var.", "_pf.");
					solutionSet4.readIntVariablesFromFile(arqVar, problem);
					SimulONDApplet.readObjectivesFromFile(solutionSet4, arqPf);
					sprite.atualizarExperimento(solutionSet4, new int[]{450,315}, 0.5, 900, 630, 3);
				}
				if (txtExperimento5.getText().trim().length() > 0){
					arqVar = txtExperimento5.getText().trim().replaceAll("_pf.", "_var.");
					arqPf = txtExperimento5.getText().trim().replaceAll("_var.", "_pf.");
					solutionSet5.readIntVariablesFromFile(arqVar, problem);
					SimulONDApplet.readObjectivesFromFile(solutionSet5, arqPf);
					sprite.atualizarExperimento(solutionSet5, new int[]{450,315}, 0.5, 900, 630, 4);
				}
				repaint();
			}
		});
		
		btnExportar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String conteudo = sprite.getEPSString();
				
				if (fileChooser == null){
					fileChooser = new JFileChooser();	
				}
				int returnVal = fileChooser.showSaveDialog(getContentPane());

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fileChooser.getSelectedFile();
		            //Salvar no arquivo selecionado
		            try {
		            	FileOutputStream fos = new FileOutputStream(file);
		            	fos.write(conteudo.getBytes());
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		btnExportarRede.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String conteudo = nfsnet.getEPSString();
				
				if (fileChooser == null){
					fileChooser = new JFileChooser();	
				}
				int returnVal = fileChooser.showSaveDialog(getContentPane());

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fileChooser.getSelectedFile();
		            //Salvar no arquivo selecionado
		            try {
		            	FileOutputStream fos = new FileOutputStream(file);
		            	fos.write(conteudo.getBytes());
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		txtExperimento = new JTextField();
		txtExperimento.setPreferredSize(defaultDimension);
		txtTituloExperimento = new JTextField("EXPERIMENTO 1");
		txtTituloExperimento.setPreferredSize(new Dimension(widthDefault/3, 24));
		JButton btnProcurarExp = new JButton("Procurar");
		btnProcurarExp.setPreferredSize(new Dimension(widthDefault/3-5, 24));
		JButton btnRemoverExp = new JButton("Remover");
		btnRemoverExp.setPreferredSize(new Dimension(widthDefault/3-5, 24));
		
		txtExperimento2 = new JTextField();
		txtExperimento2.setPreferredSize(defaultDimension);
		txtTituloExperimento2 = new JTextField("EXPERIMENTO 2");
		txtTituloExperimento2.setPreferredSize(new Dimension(widthDefault/3, 24));
		JButton btnProcurarExp2 = new JButton("Procurar");
		btnProcurarExp2.setPreferredSize(new Dimension(widthDefault/3-5, 24));
		JButton btnRemoverExp2 = new JButton("Remover");
		btnRemoverExp2.setPreferredSize(new Dimension(widthDefault/3-5, 24));
		
		txtExperimento3 = new JTextField();
		txtExperimento3.setPreferredSize(defaultDimension);
		txtTituloExperimento3 = new JTextField("EXPERIMENTO 3");
		txtTituloExperimento3.setPreferredSize(new Dimension(widthDefault/3, 24));
		JButton btnProcurarExp3 = new JButton("Procurar");
		btnProcurarExp3.setPreferredSize(new Dimension(widthDefault/3-5, 24));
		JButton btnRemoverExp3 = new JButton("Remover");
		btnRemoverExp3.setPreferredSize(new Dimension(widthDefault/3-5, 24));
		
		txtExperimento4 = new JTextField();
		txtExperimento4.setPreferredSize(defaultDimension);
		txtTituloExperimento4 = new JTextField("EXPERIMENTO 4");
		txtTituloExperimento4.setPreferredSize(new Dimension(widthDefault/3, 24));
		JButton btnProcurarExp4 = new JButton("Procurar");
		btnProcurarExp4.setPreferredSize(new Dimension(widthDefault/3-5, 24));
		JButton btnRemoverExp4 = new JButton("Remover");
		btnRemoverExp4.setPreferredSize(new Dimension(widthDefault/3-5, 24));
		
		txtExperimento5 = new JTextField();
		txtExperimento5.setPreferredSize(defaultDimension);
		txtTituloExperimento5 = new JTextField("EXPERIMENTO 5");
		txtTituloExperimento5.setPreferredSize(new Dimension(widthDefault/3, 24));
		JButton btnProcurarExp5 = new JButton("Procurar");
		btnProcurarExp5.setPreferredSize(new Dimension(widthDefault/3-5, 24));
		JButton btnRemoverExp5 = new JButton("Remover");
		btnRemoverExp5.setPreferredSize(new Dimension(widthDefault/3-5, 24));
		JPanel painelLog = new JPanel();
		painelLog.setPreferredSize(new Dimension(widthDefault+10, 24));
		painelLog.setLayout(new FlowLayout());
		chkEscalaLogX = new JCheckBox("Escala logarítmica X");
		chkEscalaLogX.setPreferredSize(new Dimension(widthDefault/2-3, 24));
		chkEscalaLogX.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sprite.setEscalaLogaritmicaX(chkEscalaLogX.isSelected());
			}
		});
		chkEscalaLogX.setSelected(true);
		chkEscalaLogY = new JCheckBox("Escala logarítmica Y");
		chkEscalaLogY.setPreferredSize(new Dimension(widthDefault/2-3, 24));
		chkEscalaLogY.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sprite.setEscalaLogaritmicaY(chkEscalaLogY.isSelected());
			}
		});
		chkAutoAjuste = new JCheckBox("Auto ajuste dos valores");
		chkAutoAjuste.setSelected(true);
		chkAutoAjuste.setPreferredSize(new Dimension(widthDefault/2+8, 24));
		chkAutoAjuste.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sprite.setAjustarDados(chkAutoAjuste.isSelected());
			}
		});
		
		JButton btnFontMais = new JButton("Z+");
		btnFontMais.setPreferredSize(new Dimension(58, 24));
		btnFontMais.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sprite.incrementarFontSize();
			}
		});
		
		JButton btnFontMenos = new JButton("Z-");
		btnFontMenos.setPreferredSize(new Dimension(58, 24));
		btnFontMenos.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sprite.decrementarFontSize();
			}
		});
		
	    JLabel labelC = new JLabel("Danilo R. B. de Araújo");
	    labelC.setPreferredSize(defaultDimension);
	    labelC.setHorizontalAlignment(JLabel.CENTER);
	    double scaleNets = 1.65;
	    nfsnet = new NFSNetSprite();
	    nfsnet.setPosCenterZoom(new int[]{(int)(89.5 * scaleNets), (int)(44 * scaleNets)});
//		nfsnet.setNetwork(new Integer[] { 0, 0, 6, 6, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 4, 8, 0, 0, 0, 0, 0, 0, 6,
//				6, 4, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 4, 6, 0, 8, 0, 0, 0, 0, 4, 0, 0, 6, 0, 4, 2, 4, 0, 2, 2, 0, 6, 0,
//				0, 0, 4, 4, 0, 0, 2, 6, 0, 8, 8, 6, 6, 0, 8, 6, 0, 0, 2, 0, 2, 0, 0, 0, 2, 2, 2, 0, 0, 6, 4, 4, 25 });
		nfsnet.setNetwork(new Integer[] { 0, 4, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 6, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 4, 7, 0, 4, 0, 0, 0, 0, 2, 0, 0, 1, 0, 4, 2, 27} );
	    nfsnet.setBorder(0);
	    nfsnet.setScale(scaleNets);
	    nfsnet.setCapex(1000);
	    nfsnet.setPb(0.00005);
	    nfsnet.setPreferredSize(new Dimension(285, 190));
		
	    pControle.add(btnExportar);
	    pControle.add(btnExportarRede);
	    pControle.add(btnAtualizar);
	    pControle.add(labelExp);
	    pControle.add(txtTituloExperimento);
	    pControle.add(btnProcurarExp);
	    pControle.add(btnRemoverExp);
	    pControle.add(txtExperimento);
	    pControle.add(txtTituloExperimento2);
	    pControle.add(btnProcurarExp2);
	    pControle.add(btnRemoverExp2);
	    pControle.add(txtExperimento2);
	    pControle.add(txtTituloExperimento3);
	    pControle.add(btnProcurarExp3);
	    pControle.add(btnRemoverExp3);
	    pControle.add(txtExperimento3);
	    pControle.add(txtTituloExperimento4);
	    pControle.add(btnProcurarExp4);
	    pControle.add(btnRemoverExp4);
	    pControle.add(txtExperimento4);
	    pControle.add(txtTituloExperimento5);
	    pControle.add(btnProcurarExp5);
	    pControle.add(btnRemoverExp5);
	    pControle.add(txtExperimento5);
	    painelLog.add(chkEscalaLogX);
	    painelLog.add(chkEscalaLogY);
	    pControle.add(painelLog);
	    pControle.add(chkAutoAjuste);
	    pControle.add(btnFontMais);
	    pControle.add(btnFontMenos);
	    pControle.add(nfsnet);
	    nfsnet.repaint();

	    pControle.add(labelC);
	    ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sprite.repaint();
			}
		}; 
	    txtTituloExperimento.addActionListener(al); 
	    txtTituloExperimento2.addActionListener(al); 
	    txtTituloExperimento3.addActionListener(al); 
	    txtTituloExperimento4.addActionListener(al); 
	    txtTituloExperimento5.addActionListener(al);
	    
	    btnProcurarExp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(getContentPane());

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					String arqVar;
					String arqPf;
					
					arqVar = file.getAbsolutePath().replaceAll("_pf.", "_var.");
					arqPf = file.getAbsolutePath().replaceAll("_var.", "_pf.");

					solutionSet1.readIntVariablesFromFile(arqVar, problem);
					SimulONDApplet.readObjectivesFromFile(solutionSet1, arqPf);
					atualizarInfoExp(txtExperimento, arqVar);
					sprite.atualizarExperimento(solutionSet1, new int[]{450,315}, 0.5, 900, 630, 0);
					repaint();
				} 
			}
		});
	    
	    btnRemoverExp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				solutionSet1.getSolutionsList().clear();
				txtExperimento.setText("");
				sprite.atualizarExperimento(solutionSet1, new int[]{450,315}, 0.5, 900, 630, 0);
				repaint();
			}
		});
	    
	    btnRemoverExp2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				solutionSet2.getSolutionsList().clear();
				txtExperimento2.setText("");
				sprite.atualizarExperimento(solutionSet2, new int[]{450,315}, 0.5, 900, 630, 1);
				repaint();
			}
		});
	    
	    btnRemoverExp3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				solutionSet3.getSolutionsList().clear();
				txtExperimento3.setText("");
				sprite.atualizarExperimento(solutionSet3, new int[]{450,315}, 0.5, 900, 630, 2);
				repaint();
			}
		});
	    
	    btnRemoverExp4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				solutionSet4.getSolutionsList().clear();
				txtExperimento4.setText("");
				sprite.atualizarExperimento(solutionSet4, new int[]{450,315}, 0.5, 900, 630, 3);
				repaint();
			}
		});
	    
	    btnRemoverExp5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				solutionSet5.getSolutionsList().clear();
				txtExperimento5.setText("");
				sprite.atualizarExperimento(solutionSet5, new int[]{450,315}, 0.5, 900, 630, 4);
				repaint();
			}
		});
	    
	    btnProcurarExp2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(getContentPane());

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					String arqVar;
					String arqPf;
					
					arqVar = file.getAbsolutePath().replaceAll("_pf.", "_var.");
					arqPf = file.getAbsolutePath().replaceAll("_var.", "_pf.");

					solutionSet2.readIntVariablesFromFile(arqVar, problem);
					SimulONDApplet.readObjectivesFromFile(solutionSet2, arqPf);
					atualizarInfoExp(txtExperimento2, arqVar);
					sprite.atualizarExperimento(solutionSet2, new int[]{450,315}, 0.5, 900, 630, 1);
					repaint();
				} 
			}
		});
	    
	    btnProcurarExp3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(getContentPane());

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					String arqVar;
					String arqPf;
					
					arqVar = file.getAbsolutePath().replaceAll("_pf.", "_var.");
					arqPf = file.getAbsolutePath().replaceAll("_var.", "_pf.");

					solutionSet3.readIntVariablesFromFile(arqVar, problem);
					SimulONDApplet.readObjectivesFromFile(solutionSet3, arqPf);
					atualizarInfoExp(txtExperimento3, arqVar);
					sprite.atualizarExperimento(solutionSet3, new int[]{450,315}, 0.5, 900, 630, 2);
					repaint();
				} 
			}
		});
	    
	    btnProcurarExp4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(getContentPane());

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					String arqVar;
					String arqPf;
					
					arqVar = file.getAbsolutePath().replaceAll("_pf.", "_var.");
					arqPf = file.getAbsolutePath().replaceAll("_var.", "_pf.");

					solutionSet4.readIntVariablesFromFile(arqVar, problem);
					SimulONDApplet.readObjectivesFromFile(solutionSet4, arqPf);
					atualizarInfoExp(txtExperimento4, arqVar);
					sprite.atualizarExperimento(solutionSet4, new int[]{450,315}, 0.5, 900, 630, 3);
					repaint();
				} 
			}
		});
	    
	    btnProcurarExp5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnVal = fileChooser.showOpenDialog(getContentPane());

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					String arqVar;
					String arqPf;
					
					arqVar = file.getAbsolutePath().replaceAll("_pf.", "_var.");
					arqPf = file.getAbsolutePath().replaceAll("_var.", "_pf.");

					solutionSet5.readIntVariablesFromFile(arqVar, problem);
					SimulONDApplet.readObjectivesFromFile(solutionSet5, arqPf);
					atualizarInfoExp(txtExperimento5, arqVar);
					sprite.atualizarExperimento(solutionSet5, new int[]{450,315}, 0.5, 900, 630, 4);
					repaint();
				} 
			}
		});
	    
//	    solutionSet1.readIntVariablesFromFile("D:\\results_c2_m3_003_100\\nsgaii_02\\._nsgaii_C2_M3_50_1.00_0.03_1,000_var.txt",
//				problem);
//		readObjectivesFromFile(solutionSet1, "D:\\results_c2_m3_003_100\\nsgaii_02\\._nsgaii_C2_M3_50_1.00_0.03_1,000_pf.txt");
	    sprite = new ParetoCanvas(solutionSet1, new int[]{450,315}, 0.5, 900, 630, this);
	    getContentPane().add(sprite, BorderLayout.CENTER);
	    getContentPane().add(pControle, BorderLayout.EAST);
	    repaint();
	}
	
	private void atualizarInfoExp(JTextField txt, String nomeArq){
		StringBuilder sb = new StringBuilder();
		sb.append(nomeArq);
//		sb.append(nomeArq.substring(nomeArq.lastIndexOf(File.separator)));
		
		txt.setText(sb.toString());
	}

	/**
	 * Método acessor para obter o valor do atributo problem.
	 *
	 * @return Retorna o atributo problem.
	 */
	public SimtonProblem getProblem() {
		return problem;
	}

	/**
	 * Método acessor para modificar o valor do atributo problem.
	 *
	 * @param problem O valor de problem para setar.
	 */
	public void setProblem(SimtonProblem problem) {
		this.problem = problem;
	}

	/**
	 * Método acessor para obter o valor do atributo txtExperimento.
	 *
	 * @return Retorna o atributo txtExperimento.
	 */
	public JTextField getTxtExperimento() {
		return txtExperimento;
	}

	/**
	 * Método acessor para modificar o valor do atributo txtExperimento.
	 *
	 * @param txtExperimento O valor de txtExperimento para setar.
	 */
	public void setTxtExperimento(JTextField txtExperimento) {
		this.txtExperimento = txtExperimento;
	}

	/**
	 * Método acessor para obter o valor do atributo txtExperimento2.
	 *
	 * @return Retorna o atributo txtExperimento2.
	 */
	public JTextField getTxtExperimento2() {
		return txtExperimento2;
	}

	/**
	 * Método acessor para modificar o valor do atributo txtExperimento2.
	 *
	 * @param txtExperimento2 O valor de txtExperimento2 para setar.
	 */
	public void setTxtExperimento2(JTextField txtExperimento2) {
		this.txtExperimento2 = txtExperimento2;
	}

	/**
	 * Método acessor para obter o valor do atributo txtExperimento3.
	 *
	 * @return Retorna o atributo txtExperimento3.
	 */
	public JTextField getTxtExperimento3() {
		return txtExperimento3;
	}

	/**
	 * Método acessor para modificar o valor do atributo txtExperimento3.
	 *
	 * @param txtExperimento3 O valor de txtExperimento3 para setar.
	 */
	public void setTxtExperimento3(JTextField txtExperimento3) {
		this.txtExperimento3 = txtExperimento3;
	}

	/**
	 * Método acessor para obter o valor do atributo txtExperimento4.
	 *
	 * @return Retorna o atributo txtExperimento4.
	 */
	public JTextField getTxtExperimento4() {
		return txtExperimento4;
	}

	/**
	 * Método acessor para modificar o valor do atributo txtExperimento4.
	 *
	 * @param txtExperimento4 O valor de txtExperimento4 para setar.
	 */
	public void setTxtExperimento4(JTextField txtExperimento4) {
		this.txtExperimento4 = txtExperimento4;
	}

	/**
	 * Método acessor para obter o valor do atributo txtExperimento5.
	 *
	 * @return Retorna o atributo txtExperimento5.
	 */
	public JTextField getTxtExperimento5() {
		return txtExperimento5;
	}

	/**
	 * Método acessor para modificar o valor do atributo txtExperimento5.
	 *
	 * @param txtExperimento5 O valor de txtExperimento5 para setar.
	 */
	public void setTxtExperimento5(JTextField txtExperimento5) {
		this.txtExperimento5 = txtExperimento5;
	}

	/**
	 * Método acessor para obter o valor do atributo txtTituloExperimento.
	 *
	 * @return Retorna o atributo txtTituloExperimento.
	 */
	public JTextField getTxtTituloExperimento() {
		return txtTituloExperimento;
	}

	/**
	 * Método acessor para modificar o valor do atributo txtTituloExperimento.
	 *
	 * @param txtTituloExperimento O valor de txtTituloExperimento para setar.
	 */
	public void setTxtTituloExperimento(JTextField txtTituloExperimento) {
		this.txtTituloExperimento = txtTituloExperimento;
	}

	/**
	 * Método acessor para obter o valor do atributo txtTituloExperimento2.
	 *
	 * @return Retorna o atributo txtTituloExperimento2.
	 */
	public JTextField getTxtTituloExperimento2() {
		return txtTituloExperimento2;
	}

	/**
	 * Método acessor para modificar o valor do atributo txtTituloExperimento2.
	 *
	 * @param txtTituloExperimento2 O valor de txtTituloExperimento2 para setar.
	 */
	public void setTxtTituloExperimento2(JTextField txtTituloExperimento2) {
		this.txtTituloExperimento2 = txtTituloExperimento2;
	}

	/**
	 * Método acessor para obter o valor do atributo txtTituloExperimento3.
	 *
	 * @return Retorna o atributo txtTituloExperimento3.
	 */
	public JTextField getTxtTituloExperimento3() {
		return txtTituloExperimento3;
	}

	/**
	 * Método acessor para modificar o valor do atributo txtTituloExperimento3.
	 *
	 * @param txtTituloExperimento3 O valor de txtTituloExperimento3 para setar.
	 */
	public void setTxtTituloExperimento3(JTextField txtTituloExperimento3) {
		this.txtTituloExperimento3 = txtTituloExperimento3;
	}

	/**
	 * Método acessor para obter o valor do atributo txtTituloExperimento4.
	 *
	 * @return Retorna o atributo txtTituloExperimento4.
	 */
	public JTextField getTxtTituloExperimento4() {
		return txtTituloExperimento4;
	}

	/**
	 * Método acessor para modificar o valor do atributo txtTituloExperimento4.
	 *
	 * @param txtTituloExperimento4 O valor de txtTituloExperimento4 para setar.
	 */
	public void setTxtTituloExperimento4(JTextField txtTituloExperimento4) {
		this.txtTituloExperimento4 = txtTituloExperimento4;
	}

	/**
	 * Método acessor para obter o valor do atributo txtTituloExperimento5.
	 *
	 * @return Retorna o atributo txtTituloExperimento5.
	 */
	public JTextField getTxtTituloExperimento5() {
		return txtTituloExperimento5;
	}

	/**
	 * Método acessor para modificar o valor do atributo txtTituloExperimento5.
	 *
	 * @param txtTituloExperimento5 O valor de txtTituloExperimento5 para setar.
	 */
	public void setTxtTituloExperimento5(JTextField txtTituloExperimento5) {
		this.txtTituloExperimento5 = txtTituloExperimento5;
	}
}
