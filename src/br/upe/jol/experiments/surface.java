package br.upe.jol.experiments;

import static br.upe.jol.base.Util.LOGGER;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;

import javax.swing.JFrame;

import ChartDirector.Chart;
import ChartDirector.ChartViewer;
import ChartDirector.ColorAxis;
import ChartDirector.ContourLayer;
import ChartDirector.XYChart;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimonProblem_3Obj;

public class surface {
	// Name of demo program
	public String toString() {
		return "Surface Chart (1)";
	}

	// Number of charts produced in this demo
	public int getNoOfCharts() {
		return 1;
	}

	// Main code for creating charts
	public void createChart(ChartViewer viewer, int index) {
		SimonProblem_3Obj problem = new SimonProblem_3Obj(14, 3);
		String prefixo = "H:\\3obj\\";
		SolutionSet<Integer> ssNSGAII = new SolutionSet<Integer>();
//		ssNSGAII.readIntVariablesFromFile(prefixo + "nsgaii\\r01\\._nsgaii_C2_M3_50_1.00_0.03_9,990_var.txt", problem);
//		readObjectivesFromFile(ssNSGAII, prefixo + "nsgaii\\r01\\._nsgaii_C2_M3_50_1.00_0.03_9,990_pf.txt");
		
//		ssNSGAII.readIntVariablesFromFile(prefixo + "spea2\\r01\\._spea2_50_100_1.00_0.01_10,000_var.txt", problem);
//		readObjectivesFromFile(ssNSGAII, prefixo + "spea2\\r01\\._spea2_50_100_1.00_0.01_10,000_pf.txt");
		
//		ssNSGAII.readIntVariablesFromFile(prefixo + "pesaii\\r01\\._pesaii_5_C2_M3_50_1.00_0.03_10000_var.txt", problem);
//		readObjectivesFromFile(ssNSGAII, prefixo + "pesaii\\r01\\._pesaii_5_C2_M3_50_1.00_0.03_10000_pf.txt");
		
		ssNSGAII.readIntVariablesFromFile(prefixo + "paes\\r01\\._paes_50_5_10,000_var.txt", problem);
		readObjectivesFromFile(ssNSGAII, prefixo + "paes\\r01\\._paes_50_5_10,000_pf.txt");
		
//		ssNSGAII.readIntVariablesFromFile(prefixo + "mode\\r01\\._mode_50_0.30_10,000_var.txt", problem);
//		readObjectivesFromFile(ssNSGAII, prefixo + "mode\\r01\\._mode_50_0.30_10,000_pf.txt");
		// The x and y coordinates of the grid
		int qtde = ssNSGAII.getSolutionsList().size();
		double[] dataX = new double[qtde];
		double[] dataY = new double[qtde];
		double[] dataZ = new double[qtde];

		Comparator<Solution<Integer>> comp = new Comparator<Solution<Integer>>() {
			@Override
			public int compare(Solution<Integer> o1, Solution<Integer> o2) {
				return o1.getObjective(0) > o2.getObjective(0) ? -1 : 1;
			}
			
		};
		ssNSGAII.sort(comp);
		for (int i = 0; i < dataY.length; i++) {
			dataX[i] = ssNSGAII.getSolutionsList().get(i).getObjective(2);
			dataY[i] = ssNSGAII.getSolutionsList().get(i).getObjective(1);
			dataZ[i] = ssNSGAII.getSolutionsList().get(i).getObjective(0);
			System.out.println(dataX[i] + "-" + dataY[i] + "-" + dataZ[i]);
		}
		int tam_base = 300;
		// Create a XYChart object of size 600 x 500 pixels
        XYChart c = new XYChart(tam_base + 200, tam_base + 100);
        
        // Add a title to the chart using 18 points Times New Roman Bold Italic font
        c.addTitle("PAES", "Arial Bold", 18);
        // Set the plotarea at (75, 40) and of size 400 x 400 pixels. Use
        // semi-transparent black (80000000) dotted lines for both horizontal and
        // vertical grid lines
        c.setPlotArea((int)(tam_base * .1875), tam_base/10, tam_base, tam_base, -1, -1, -1, c.dashLineColor(0x80000000,
            Chart.DotLine), -1);
        
        // Set x-axis and y-axis title using 12 points Arial Bold Italic font
        c.xAxis().setTitle("Energy expenditure (p.u.)", "Arial Bold", 12);
        c.yAxis().setTitle("Capital cost (m.u.)", "Arial Bold", 12);

        // Set x-axis and y-axis labels to use Arial Bold font
        c.xAxis().setLabelStyle("Arial Bold");
        c.yAxis().setLabelStyle("Arial Bold");

        // When auto-scaling, use tick spacing of 40 pixels as a guideline
        c.yAxis().setTickDensity(tam_base/10);
        c.xAxis().setTickDensity(tam_base/10);

        // Add a contour layer using the given data
        ContourLayer layer = c.addContourLayer(dataX, dataY, dataZ);

        // Set the contour color to transparent
        layer.setContourColor(Chart.Transparent);

        // Move the grid lines in front of the contour layer
        c.getPlotArea().moveGridBefore(layer);
        
        // Add a color axis (the legend) in which the left center point is anchored
        // at (495, 240). Set the length to 370 pixels and the labels on the right
        // side.
        ColorAxis cAxis = layer.setColorAxis((int)(tam_base*1.24), (int)(tam_base*.6), Chart.Left, (int)(tam_base*.93), Chart.Right);
//        cAxis.setLinearScale(0, 1);
        cAxis.setColorGradient(false, new int[]{0x444444, 0xeeeeee});
        cAxis.setLabelFormat("{value|4.,}");
//        cAxis.setLabelFormat("{value|E1.,}");
        cAxis.setLogScale(0, 1);
        // Add a bounding box to the color axis using light grey (eeeeee) as the
        // background and dark grey (444444) as the border.
        cAxis.setBoundingBox(0xffffff, 0x444444);

        // Add a title to the color axis using 12 points Arial Bold Italic font
        cAxis.setTitle("Blocking probability", "Arial Bold", 12);

        // Set color axis labels to use Arial Bold font
        cAxis.setLabelStyle("Arial Bold");
        
        //redes em destaque
//        c.addScatterLayer(
//        		new double[]{ssNSGAII.getSolutionsList().get(qtde/2).getObjective(2), ssNSGAII.getSolutionsList().get(qtde-1).getObjective(2)}, 
//        		new double[]{ssNSGAII.getSolutionsList().get(qtde/2).getObjective(1), ssNSGAII.getSolutionsList().get(qtde-1).getObjective(1)},
//        		"Example networks", 1, 8, 0xff0000, 0x444444);
        
        System.out.print("new Integer[] { ");
        for (int i = 0; i < ssNSGAII.getSolutionsList().get(qtde/2).getDecisionVariables().length; i++){
        	System.out.print(ssNSGAII.getSolutionsList().get(qtde/2).getDecisionVariables()[i] + ", ");
        }
        System.out.println("} ");
        
        System.out.print("new Integer[] { ");
        for (int i = 0; i < ssNSGAII.getSolutionsList().get(qtde-1).getDecisionVariables().length; i++){
        	System.out.print(ssNSGAII.getSolutionsList().get(qtde-1).getDecisionVariables()[i] + ", ");
        }
        System.out.print("} ");
        // Output the chart
        viewer.setImage(c.makeImage());


	}

	public static void readObjectivesFromFile(SolutionSet<Integer> ss, String path) {
		try {
			FileInputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			int j = 0;
			while (br.ready()) {
				String[] objectives = br.readLine().split(" ");
				for (int i = 0; i < 3; i++)
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

	// Allow this module to run as standalone program for easy testing
	public static void main(String[] args) {
		// Instantiate an instance of this demo module
		surface demo = new surface();

		// Create and set up the main window
		JFrame frame = new JFrame(demo.toString());
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.getContentPane().setBackground(Color.white);

		// Create the chart and put them in the content pane
		ChartViewer viewer = new ChartViewer();
		demo.createChart(viewer, 0);
		frame.getContentPane().add(viewer);

		// Display the window
		frame.pack();
		frame.setVisible(true);
	}
}