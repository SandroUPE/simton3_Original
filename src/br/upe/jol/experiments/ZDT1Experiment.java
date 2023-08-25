/**
 * 
 */
package br.upe.jol.experiments;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

import javax.swing.JFrame;

import ChartDirector.Chart;
import ChartDirector.ChartViewer;
import ChartDirector.XYChart;
import br.upe.jol.base.Observer;
import br.upe.jol.metaheuristics.dwga.DDWGA;
import br.upe.jol.metaheuristics.nsgaii.NSGAII;
import br.upe.jol.problems.zdt.ZDT1;

/**
 */
public class ZDT1Experiment implements Runnable {
	private static final long serialVersionUID = 1L;
	
	private Observer[] observers;

	private boolean running;

	private ChartViewer viewer;

	public String toString() {
		return "Curva de Objetivos ZDT1 usando NSGAII e SPEA2";
	}

	public int getNoOfCharts() {
		return 1;
	}

	public void createChart(ChartViewer viewer, Observer[] observers) {
		if (observers[0].getSolutionSet() == null || observers[1].getSolutionSet() == null){
			return;
		}
		double[] dataX0 = new double[observers[0].getSolutionSet().getSolutionsList().size()];
		double[] dataY0 = new double[observers[0].getSolutionSet().getSolutionsList().size()];
		
		for (int i = 0; i < observers[0].getSolutionSet().getSolutionsList().size(); i++){
			dataX0[i] = observers[0].getSolutionSet().getSolutionsList().get(i).getObjective(0);
			dataY0[i] = observers[0].getSolutionSet().getSolutionsList().get(i).getObjective(1);
		}

		double[] dataX1 = new double[observers[1].getSolutionSet().getSolutionsList().size()];
		double[] dataY1 = new double[observers[1].getSolutionSet().getSolutionsList().size()];
		
		for (int i = 0; i < observers[1].getSolutionSet().getSolutionsList().size(); i++){
			dataX1[i] = observers[1].getSolutionSet().getSolutionsList().get(i).getObjective(0);
			dataY1[i] = observers[1].getSolutionSet().getSolutionsList().get(i).getObjective(1);
		}

		XYChart c = new XYChart(550, 520);

		c.setPlotArea(55, 65, 350, 300, -1, -1, 0xc0c0c0, 0xc0c0c0, -1);

		c.addLegend(50, 30, false, "Times New Roman Bold Italic", 12)
				.setBackground(Chart.Transparent);

		c.addTitle("ZDT1 usando NSGAII e SPEA2", "Times New Roman Bold Italic",
				18);

		c.yAxis().setTitle("y1", "Arial Bold Italic", 12);

		c.xAxis().setTitle("y2", "Arial Bold Italic", 12);

		c.xAxis().setWidth(3);
		c.yAxis().setWidth(3);

		c.addScatterLayer(dataX0, dataY0, observers[0].getTitle(), Chart.DiamondSymbol, 13,
				0xff9933);

		c.addScatterLayer(dataX1, dataY1, observers[1].getTitle(), Chart.TriangleSymbol, 11,
				0x33ff33);

		viewer.setImage(c.makeImage());

		viewer.setImageMap(c.getHTMLImageMap("clickable", "",
				"title='[{dataSetName}] y1 = {x}, y2 = {value}'"));
	}

	public static void main(String[] args) {
		ZDT1 zdt1 = new ZDT1(30, 2);
		DDWGA spea2 = new DDWGA(50, 50, zdt1);
		NSGAII nsgaii = new NSGAII(30, 100, zdt1);
		
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(4);
		
		ZDT1Experiment demo = new ZDT1Experiment();
		
		demo.observers = new Observer[2];
		demo.observers[0] = new Observer();
		demo.observers[0].setTitle("NSGAII");
		demo.observers[1] = new Observer();
		demo.observers[1].setTitle("SPEA2");
		
		nsgaii.setObserver(demo.observers[0]);
		spea2.setObserver(demo.observers[1]);
		
		JFrame frame = new JFrame(demo.toString());
		frame.setPreferredSize(new Dimension(520, 550));
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.getContentPane().setBackground(Color.white);

		demo.viewer = new ChartViewer();
		demo.createChart(demo.viewer, demo.observers);
		frame.getContentPane().add(demo.viewer);

		frame.pack();
		frame.setVisible(true);
		
		demo.running = true;
		new Thread(demo).start();
		spea2.execute();
		nsgaii.execute();
		demo.running = false;
	}

	@Override
	public void run() {
		while (running) {
			createChart(viewer, observers);

			viewer.repaint();
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
		}
	}
}