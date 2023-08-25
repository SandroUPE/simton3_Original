/**
 * 
 */
package br.upe.jol.experiments;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.MaskFormatter;

import ChartDirector.Chart;
import ChartDirector.ChartViewer;
import ChartDirector.XYChart;
import br.upe.jol.base.Algorithm;
import br.upe.jol.base.Observer;
import br.upe.jol.configuration.ArchiveMOOParametersTO;
import br.upe.jol.configuration.ConfigurationTO;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.configuration.HyperBoxMOOParametersTO;
import br.upe.jol.metaheuristics.AlgorithmFactory;
import br.upe.jol.metaheuristics.TAlgorithm;
import br.upe.jol.metrics.TMetrics;
import br.upe.jol.problems.TProblems;

/**
 * @author Danilo
 * 
 */
public class MainFrame implements Runnable{
	private static final long serialVersionUID = 1L;

	private ChartViewer viewer;

	private ChartViewer boxPlotViewer;
	
	private ConfigurationTO configTO;
	
	private JList aList = new JList(TAlgorithm.values());
	
	private JList mList = new JList(TMetrics.values());
	
	private JComboBox pList = new JComboBox(TProblems.values());
	
	private JButton btnStart;
	
	private JButton btnConfig;
	
	private JFormattedTextField txtNumRuns;
	
	private JTextField txtDefaultPath;
	
	private JFormattedTextField txtNSGAIIPopulation;
	
	private JFormattedTextField txtNSGAIIMutation;
	
	private JFormattedTextField txtNSGAIICrossover;
	
	private JFormattedTextField txtSpea2Population;
	
	private JFormattedTextField txtSpea2FileSize;
	
	private JFormattedTextField txtSpea2Mutation;
	
	private JFormattedTextField txtSpea2Crossover;
	
	private JFormattedTextField txtPesaIIPopulation;
	
	private JFormattedTextField txtPesaIIFileSize;
	
	private JFormattedTextField txtPesaIIMutation;
	
	private JFormattedTextField txtPesaIICrossover;
	
	private JFormattedTextField txtPesaIIHyperBox;
	
	private JFormattedTextField txtPaesFileSize;
	
	private JFormattedTextField txtPaesBoxSize;
	
	private Observer[] observers;
	
	private boolean running;

	private JFrame frame;
	
	private static final NumberFormat nf = NumberFormat.getNumberInstance();
	
	private int[] colors = new int[]{0xffffff, 0xff0000, 0x00ff00, 0x0000ff, 0xff9933, 0x99ff33, 0x9933ff, 0x000000};

	private JLabel jLabel1 = null;

	private JLabel jLabel2 = null;

	private JLabel jLabel3 = null;

	private JLabel jLabel31 = null;

	private JPanel getPanelMetrics() {
		JPanel panelResumo = new JPanel();
		
		boxPlotViewer = new ChartViewer();
		createBoxWhiskerChart(boxPlotViewer);
		panelResumo.add(boxPlotViewer);

		return panelResumo;
	}

	public synchronized void createChart(ChartViewer viewer, Observer[] observers) {
		if (observers == null){
			return;
		}
		XYChart c = new XYChart(780, 680);

		int desenhoLegenda = 1;
		boolean atualizacao = false;
		for (Observer observer : observers){
			if (observer.getSolutionSet() == null || observer.getSolutionSet().getSolutionsList() == null || observer.getSolutionSet().getSolutionsList().isEmpty()){
				continue;
			}
			atualizacao = true;
			double[] dataX0 = new double[observer.getSolutionSet().getSolutionsList().size()];
			double[] dataY0 = new double[observer.getSolutionSet().getSolutionsList().size()];
			
			for (int i = 0; i < observer.getSolutionSet().getSolutionsList().size(); i++){
				dataX0[i] = observer.getSolutionSet().getSolutionsList().get(i).getObjective(0);
				dataY0[i] = observer.getSolutionSet().getSolutionsList().get(i).getObjective(1);
			}
			c.addScatterLayer(dataX0, dataY0, observer.getTitle() + " (" + observer.getIteration() + ")", desenhoLegenda, 13, colors[desenhoLegenda-1]);
			desenhoLegenda++;
		}

		if (atualizacao){
			c.setPlotArea(55, 65, 720, 560, -1, -1, 0xc0c0c0, 0xc0c0c0, -1);

			c.addLegend(50, 30, false, "Times New Roman Bold Italic", 12)
					.setBackground(Chart.Transparent);

			c.addTitle("Otimização Multi-objetivos - " + pList.getSelectedItem().toString(), "Times New Roman Bold Italic", 18);

			c.yAxis().setTitle("y1", "Arial Bold Italic", 12);

			c.xAxis().setTitle("y2", "Arial Bold Italic", 12);

			c.xAxis().setWidth(3);
			c.yAxis().setWidth(3);
			
			viewer.setImage(c.makeImage());
			viewer.setImageMap(c.getHTMLImageMap("clickable", "", "title='[{dataSetName}] y1 = {x}, y2 = {value}'"));
			viewer.repaint();	
		}
	}
	
	public void createBoxWhiskerChart(ChartViewer viewer){
        double[] Q0Data = {40, 45, 40, 30};
        double[] Q1Data = {55, 60, 50, 40};
        double[] Q2Data = {62, 70, 60, 50};
        double[] Q3Data = {70, 80, 65, 60};
        double[] Q4Data = {80, 90, 75, 70};

        String[] labels = new String[4];
        
        for (int i = 0; i < 4; i++){
        	labels[i] = TAlgorithm.values()[i].toString();
        }

        XYChart c = new XYChart(200, 320);

        c.setPlotArea(30, 30, 160, 250).setGridColor(0xc0c0c0, 0xc0c0c0);

        c.addTitle(mList.getSelectedValue().toString());

        c.xAxis().setLabels(labels).setFontStyle("Arial Bold");

        c.yAxis().setLabelStyle("Arial Bold");

        c.addBoxWhiskerLayer(Q3Data, Q1Data, Q4Data, Q0Data, Q2Data, 0x9999ff,
            0x0000cc).setLineWidth(2);

        viewer.setImage(c.makeImage());

        viewer.setImageMap(c.getHTMLImageMap("clickable", "",
            "title='{xLabel}: min/med/max = {min}/{med}/{max} Inter-quartile " +
            "range: {bottom} to {top}'"));
        viewer.repaint();	
    }
	
	private void initialize() throws ParseException{
		JPanel panel = new JPanel();
		JPanel panelChart = new JPanel();
		
		configTO = new ConfigurationTO();

		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		frame = new JFrame("Otimização Multi-objetivos");
		frame.setIconImage(Util.loadImage("brasao_upe.png"));
		frame.setPreferredSize(new Dimension(1024, 720));
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				running = false;
				System.exit(0);
			}
		});
		frame.getContentPane().setBackground(Color.white);

		viewer = new ChartViewer();
		panelChart.add(viewer);
		
		panel.setLayout(new BorderLayout());
		panel.add(panelChart, BorderLayout.EAST);
		panel.add(getPanelEsq(), BorderLayout.WEST);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		running = true;
		new Thread(this).start();
	}
	
	private JTabbedPane getPanelEsq() throws ParseException{
		JTabbedPane tabbedPane = new JTabbedPane();
		
		tabbedPane.addTab( "Execução", getPanelRuns() );		
		tabbedPane.addTab( "Configuração", getPanelConfigs() );
		
		return tabbedPane;
	}
	
	private JPanel getPanelRuns(){
		JPanel panelEsq = new JPanel();
		JPanel panelInf = new JPanel();
		JPanel panelOptions = new JPanel();
		
		aList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		aList.setLayoutOrientation(JList.VERTICAL);
		aList.setVisibleRowCount(-1);
		aList.setSelectedIndices(new int[]{0});
		JScrollPane listScroller = new JScrollPane(aList);
		listScroller.setPreferredSize(new Dimension(215, 150));

		panelOptions.setLayout(new BorderLayout());
		
		JPanel pAlg = new JPanel();
		pAlg.setLayout(new BorderLayout());
		pAlg.add(new JLabel("Algoritmos:"), BorderLayout.NORTH);
		pAlg.add(listScroller, BorderLayout.SOUTH);
		final MainFrame mf = this;
		panelOptions.add(pAlg, BorderLayout.NORTH);
		
		JPanel pProblems = new JPanel();
		pProblems.setLayout(new BorderLayout());
		pProblems.add(new JLabel("Problema:"), BorderLayout.NORTH);
		pProblems.add(pList, BorderLayout.SOUTH);
		
		mList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mList.setLayoutOrientation(JList.VERTICAL);
		mList.setVisibleRowCount(-1);
		mList.setSelectedIndex(0);
		mList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				createBoxWhiskerChart(boxPlotViewer);
			}
		});
		JScrollPane listScroller1 = new JScrollPane(mList);
		listScroller1.setPreferredSize(new Dimension(200, 80));
		
		JPanel pMetrics = new JPanel();
		pMetrics.setLayout(new BorderLayout());
		pMetrics.add(new JLabel("Métricas:"), BorderLayout.NORTH);
		pMetrics.add(listScroller1, BorderLayout.SOUTH);
		
		panelOptions.add(pProblems, BorderLayout.CENTER);
		
		panelOptions.add(pMetrics, BorderLayout.SOUTH);
		
		btnStart = new JButton("Executar Simulação");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				mf.executarAlgoritmos();
			}
		});
		
		panelEsq.setLayout(new BorderLayout());
		panelEsq.add(panelOptions, BorderLayout.NORTH);
		panelInf.setPreferredSize(new Dimension(200, 330));
		panelInf.add(getPanelMetrics());
		panelEsq.add(panelInf, BorderLayout.CENTER);
		
		panelEsq.add(btnStart, BorderLayout.SOUTH);
		
		return panelEsq;
	}
	
	private JPanel getPanelConfigs() throws ParseException{
		jLabel31 = new JLabel();
		jLabel31.setBounds(new Rectangle(86, 14, 76, 16));
		jLabel31.setText("Tx Mutação");
		jLabel3 = new JLabel();
		jLabel3.setBounds(new Rectangle(86, 35, 80, 19));
		jLabel3.setText("Tx Crossover");
		jLabel2 = new JLabel();
		jLabel2.setBounds(new Rectangle(86, 59, 70, 15));
		jLabel2.setText("HyperBoxs");
		jLabel1 = new JLabel();
		jLabel1.setBounds(new Rectangle(14, 44, 10, 15));
		jLabel1.setText("F");
		JLabel jLabel = new JLabel("N");
		jLabel.setBounds(new Rectangle(15, 20, 8, 16));
		JPanel panelEsq = new JPanel();
		Dimension preferredSize = new Dimension(190, 19);
		panelEsq.setPreferredSize(new Dimension(220, 800));
		panelEsq.setLayout(new FlowLayout());
		final MainFrame mf = this;

		
		JPanel panelConfigGeneral = new JPanel();
		panelConfigGeneral.setPreferredSize(new Dimension(220, 120));
		panelConfigGeneral.setBorder(BorderFactory.createTitledBorder("Geral"));
		panelConfigGeneral.add(new JLabel("Execuções por experimento"));
		txtNumRuns = new JFormattedTextField(new MaskFormatter("###"));			
		txtNumRuns.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtNumRuns.setPreferredSize(preferredSize);
		txtNumRuns.setText(Integer.toString(this.configTO.getnRuns()));
		panelConfigGeneral.add(txtNumRuns);
		panelConfigGeneral.add(new JLabel("Caminho para exportação"));
		txtDefaultPath = new JTextField();			
		txtDefaultPath.setPreferredSize(preferredSize);
		panelConfigGeneral.add(txtDefaultPath);
		
		JPanel panelConfigNSGAII = new JPanel();
		panelConfigNSGAII.setPreferredSize(new Dimension(220, 70));
		panelConfigNSGAII.setBorder(BorderFactory.createTitledBorder("NSGA II"));
		GeneralMOOParametersTO parametersNSGAII = this.configTO.getNSGAIIConfig();
		panelConfigNSGAII.add(new JLabel("<html>N&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Tx Mutação&nbsp;&nbsp;&nbsp;Tx Crossover</html>"));
		txtNSGAIIPopulation = new JFormattedTextField(new MaskFormatter("###"));			
		txtNSGAIIPopulation.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtNSGAIIPopulation.setPreferredSize(new Dimension(29, 18));
		panelConfigNSGAII.add(txtNSGAIIPopulation);
		int aux1 = parametersNSGAII.getPopulationSize();
		txtNSGAIIPopulation.setText(Integer.toString(aux1));
		txtNSGAIIMutation = new JFormattedTextField(new MaskFormatter("#,##"));			
		txtNSGAIIMutation.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtNSGAIIMutation.setPreferredSize(new Dimension(35, 18));
		panelConfigNSGAII.add(txtNSGAIIMutation);
		double aux2 = parametersNSGAII.getMutationProbability();
		String s = Double.toString(aux2);
		s = s.replace('.', ',');
		txtNSGAIIMutation.setText(s);
		txtNSGAIICrossover = new JFormattedTextField(new MaskFormatter("#,##"));			
		txtNSGAIICrossover.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtNSGAIICrossover.setPreferredSize(new Dimension(35, 18));
		panelConfigNSGAII.add(txtNSGAIICrossover);
		aux2 = parametersNSGAII.getCrossoverProbability();
		s = Double.toString(aux2);
		s = s.replace('.', ',');
		txtNSGAIICrossover.setText(s);
		
		
		JPanel panelConfigSpea2 = new JPanel();
		panelConfigSpea2.setPreferredSize(new Dimension(220, 75));
		panelConfigSpea2.setBorder(BorderFactory.createTitledBorder("SPEA 2"));
		panelConfigSpea2.add(new JLabel("<html>N&nbsp;&nbsp;&nbsp;F&nbsp;&nbsp;Tx Mutação&nbsp;&nbsp;Tx Crossover</html>"));
		ArchiveMOOParametersTO parametersSpea2 = (ArchiveMOOParametersTO) this.configTO.getSpea2Config();
		txtSpea2Population = new JFormattedTextField(new MaskFormatter("###"));			
		txtSpea2Population.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtSpea2Population.setPreferredSize(new Dimension(29, 18));
		panelConfigSpea2.add(txtSpea2Population);
		aux1 = parametersSpea2.getPopulationSize();
		txtSpea2Population.setText(Integer.toString(aux1));
		txtSpea2FileSize = new JFormattedTextField(new MaskFormatter("###"));			
		txtSpea2FileSize.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtSpea2FileSize.setPreferredSize(new Dimension(29, 18));
		panelConfigSpea2.add(txtSpea2FileSize);
		aux1 = parametersSpea2.getArchiveSize();
		txtSpea2FileSize.setText(Integer.toString(aux1));
		txtSpea2Mutation = new JFormattedTextField(new MaskFormatter("#,##"));			
		txtSpea2Mutation.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtSpea2Mutation.setPreferredSize(new Dimension(35, 18));
		panelConfigSpea2.add(txtSpea2Mutation);
		aux2 = parametersSpea2.getMutationProbability();
		s = Double.toString(aux2);
		s = s.replace('.', ',');
		txtSpea2Mutation.setText(s);
		txtSpea2Crossover = new JFormattedTextField(new MaskFormatter("#,##"));
		txtSpea2Crossover.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtSpea2Crossover.setPreferredSize(new Dimension(35, 18));
		panelConfigSpea2.add(txtSpea2Crossover);
		aux2 = parametersSpea2.getCrossoverProbability();
		s = Double.toString(aux2);
		s = s.replace('.', ',');
		txtSpea2Crossover.setText(s);
					

		JPanel panelConfigPesaII = new JPanel();
		panelConfigPesaII.setLayout(null);
		panelConfigPesaII.setPreferredSize(new Dimension(220, 81));
		panelConfigPesaII.setBorder(BorderFactory.createTitledBorder("PESA II"));
		HyperBoxMOOParametersTO parametersPESAII = (HyperBoxMOOParametersTO) this.configTO.getPESAIIConfig();
		//panelConfigPesaII.add(new JLabel("<html>N&nbsp;&nbsp;&nbsp;F&nbsp;&nbsp;Tx Mutação&nbsp;&nbsp;Tx Crossover</html>"));
		txtPesaIIPopulation = new JFormattedTextField(new MaskFormatter("###"));			
		txtPesaIIPopulation.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtPesaIIPopulation.setBounds(new Rectangle(40, 20, 29, 18));
		txtPesaIIPopulation.setPreferredSize(new Dimension(29, 18));
		aux1 = parametersPESAII.getPopulationSize();
		txtPesaIIPopulation.setText(Integer.toString(aux1));
		txtPesaIIFileSize = new JFormattedTextField(new MaskFormatter("###"));			
		txtPesaIIFileSize.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtPesaIIFileSize.setBounds(new Rectangle(40, 44, 29, 18));
		txtPesaIIFileSize.setPreferredSize(new Dimension(29, 18));
		aux1 = parametersPESAII.getArchiveSize();
		txtPesaIIFileSize.setText(Integer.toString(aux1));
		txtPesaIIMutation = new JFormattedTextField(new MaskFormatter("#,##"));			
		txtPesaIIMutation.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtPesaIIMutation.setBounds(new Rectangle(172, 14, 35, 18));
		txtPesaIIMutation.setPreferredSize(new Dimension(35, 18));
		aux2 = parametersPESAII.getMutationProbability();
		s = s.replace('.',',');
		txtPesaIIMutation.setText(s);
		panelConfigPesaII.add(jLabel, null);
		panelConfigPesaII.add(txtPesaIIFileSize, null);
		panelConfigPesaII.add(txtPesaIIPopulation, null);
		txtPesaIICrossover = new JFormattedTextField(new MaskFormatter("#,##"));			
		txtPesaIICrossover.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtPesaIICrossover.setBounds(new Rectangle(172, 35, 35, 18));
		txtPesaIICrossover.setPreferredSize(new Dimension(35, 18));
		aux2 = parametersNSGAII.getCrossoverProbability();
		s = s.replace('.', ',');
		txtPesaIICrossover.setText(s);
		panelConfigPesaII.add(txtPesaIIMutation, null);
		txtPesaIIHyperBox = new JFormattedTextField(new MaskFormatter("##"));			
		txtPesaIIHyperBox.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtPesaIIHyperBox.setBounds(new Rectangle(178, 59, 29, 18));
		txtPesaIIHyperBox.setPreferredSize(new Dimension(29, 18));
		aux1 = parametersPESAII.getBisections();
		txtPesaIIHyperBox.setText(Integer.toString(aux1));
		panelConfigPesaII.add(txtPesaIICrossover, null);
		panelConfigPesaII.add(txtPesaIIHyperBox, null);
		panelConfigPesaII.add(jLabel1, null);
		panelConfigPesaII.add(jLabel2, null);
		panelConfigPesaII.add(jLabel3, null);
		panelConfigPesaII.add(jLabel31, null);
		
		JPanel panelConfigPaes = new JPanel();
		panelConfigPaes.setPreferredSize(new Dimension(220, 75));
		panelConfigPaes.setBorder(BorderFactory.createTitledBorder("PAES"));
		HyperBoxMOOParametersTO parametersPAES = (HyperBoxMOOParametersTO) this.configTO.getPaesiiConfig();
		panelConfigPaes.add(new JLabel("<html>N&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Quantidade de Hiperbox</html>"));
		txtPaesFileSize = new JFormattedTextField(new MaskFormatter("###"));			
		txtPaesFileSize.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtPaesFileSize.setPreferredSize(new Dimension(29, 18));
		aux1 = parametersPAES.getArchiveSize();
		txtPaesFileSize.setText(Integer.toString(aux1));
		panelConfigPaes.add(txtPaesFileSize);
		txtPaesBoxSize = new JFormattedTextField(new MaskFormatter("##"));			
		txtPaesBoxSize.setFocusLostBehavior(JFormattedTextField.COMMIT);
		txtPaesBoxSize.setPreferredSize(new Dimension(35, 18));
		aux1 = parametersPAES.getBisections();
		txtPaesBoxSize.setText(Integer.toString(aux1));
		panelConfigPaes.add(txtPaesBoxSize);
		
		JPanel panelMoPso = new JPanel();
		panelMoPso.setPreferredSize(new Dimension(220, 75));
		panelMoPso.setBorder(BorderFactory.createTitledBorder("MO-PSO"));
		
		JPanel panelMoPsoCdr = new JPanel();
		panelMoPsoCdr.setPreferredSize(new Dimension(220, 75));
		panelMoPsoCdr.setBorder(BorderFactory.createTitledBorder("MO-PSO CDR"));
		
		JPanel panelcssMoPso = new JPanel();
		panelcssMoPso.setPreferredSize(new Dimension(220, 75));
		panelcssMoPso.setBorder(BorderFactory.createTitledBorder("CSS MO-PSO"));
		
		JPanel paneldnMoPso = new JPanel();
		paneldnMoPso.setPreferredSize(new Dimension(220, 75));
		paneldnMoPso.setBorder(BorderFactory.createTitledBorder("DN-MOPSO"));
		
		panelEsq.add(panelConfigGeneral);
		panelEsq.add(panelConfigNSGAII);
		panelEsq.add(panelConfigSpea2);
		panelEsq.add(panelConfigPaes);
		panelEsq.add(panelConfigPesaII, panelConfigPesaII.getName());
//		panelEsq.add(panelMoPsoCdr);
//		panelEsq.add(panelcssMoPso);
//		panelEsq.add(paneldnMoPso);
		
		btnConfig = new JButton("Aplicar Configurações");
		btnConfig.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				mf.carregarConfiguracoes();
			}
		});
		
		panelEsq.add(btnConfig, BorderLayout.SOUTH);
		
		return panelEsq;
	}
	
	public void executarAlgoritmos(){
		Object[] tAlgorithms = aList.getSelectedValues();
		TProblems tProblem = (TProblems)pList.getSelectedItem();
		
		final Algorithm<Double>[] algorithms = new Algorithm[tAlgorithms.length];
		final GeneralMOOParametersTO[] parameters = new GeneralMOOParametersTO[tAlgorithms.length];
		observers = new Observer[tAlgorithms.length];
		for (int i = 0; i < tAlgorithms.length; i++){
			TAlgorithm tAlgorithm  = (TAlgorithm)tAlgorithms[i];
			algorithms[i] = AlgorithmFactory.getInstance().getAlgorithm(tAlgorithm, tProblem);
			parameters[i] = this.configTO.getParameters(tAlgorithm);
			observers[i] = new Observer(algorithms[i].toString());
			algorithms[i].setObserver(observers[i]);
		}
		for (int i = 0; i < tAlgorithms.length; i++){
			final Algorithm<Double> a = algorithms[i];
			a.setParameters(parameters[i]);
			final Observer o = observers[i];
			Thread thread = new Thread(){
				public void run(){
					o.setSolutionSet(a.execute());
				}
			};
			thread.start();
		}
		createChart(viewer, observers);
	}

	public String pontuado(String s){
		return s.replace(',', '.');
	}
	
	public void carregarConfiguracoes(){
		int nRuns = Integer.parseInt(txtNumRuns.getText().trim());
		String aux = null;
		
		this.configTO.setnRuns(nRuns);
		
		//Parametros NSGAII
		int NSGAIIPopulationSize = Integer.parseInt(txtNSGAIIPopulation.getText().trim());
		aux = txtNSGAIICrossover.getText().trim();
		double NSGAIIcrossoverProbility = Double.parseDouble(pontuado(aux));
		aux = txtNSGAIIMutation.getText().trim();
		double NSGAIImutationProbability = Double.parseDouble(pontuado(aux));
		GeneralMOOParametersTO nsgaiiConfig = new GeneralMOOParametersTO(NSGAIIPopulationSize, NSGAIIcrossoverProbility, 
				NSGAIImutationProbability);
		
		this.configTO.setNsgaiiConfig(nsgaiiConfig);
		
		//Parametros SPEA2
		int SPEA2PopulationSize = Integer.parseInt(txtSpea2Population.getText().trim());
		aux = txtSpea2Crossover.getText().trim();
		double SPEA2CrossoverProbability = Double.parseDouble(pontuado(aux));
		aux = txtSpea2Mutation.getText().trim();
		double SPEA2MutationProbability = Double.parseDouble(pontuado(aux));
		int SPEA2FileSize = Integer.parseInt(txtSpea2FileSize.getText().trim());
		ArchiveMOOParametersTO spea2Config = new ArchiveMOOParametersTO(SPEA2PopulationSize, SPEA2FileSize, 
				SPEA2CrossoverProbability, SPEA2MutationProbability);
		
		this.configTO.setSpea2Config(spea2Config);
		
		//Parametros PESAII
		int PESAIIPopulationSize = Integer.parseInt(txtPesaIIPopulation.getText().trim());
		aux = txtPesaIICrossover.getText().trim();
		double PESAIICrossoverProbability = Double.parseDouble(pontuado(aux));
		aux = txtPesaIIMutation.getText().trim();
		double PESAIIMutationProbability = Double.parseDouble(pontuado(aux));
		int PESAIIFileSize = Integer.parseInt(txtPesaIIFileSize.getText().trim());
		int numberHyperBoxs = Integer.parseInt(txtPesaIIHyperBox.getText().trim());
		HyperBoxMOOParametersTO pesaiiConfig = new HyperBoxMOOParametersTO(PESAIIPopulationSize, PESAIIFileSize, numberHyperBoxs,
				PESAIICrossoverProbability, PESAIIMutationProbability);
		
		this.configTO.setPesaiiConfig(pesaiiConfig);
		
		//Parametros PAES
		int PAESPopulation = Integer.parseInt(txtPaesFileSize.getText().trim());
		int PAESHyperBoxs = Integer.parseInt(txtPaesBoxSize.getText().trim());
		HyperBoxMOOParametersTO paesConfig = new HyperBoxMOOParametersTO(1, PAESPopulation, PAESHyperBoxs, 0.0, 0.0);
		
		this.configTO.setPaesiiConfig(paesConfig);
		
	}
	
	public static void main(String[] args) {
		MainFrame app = new MainFrame();
		try {
			app.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (running){
			createChart(viewer, observers);
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}	
		}
	}
}
