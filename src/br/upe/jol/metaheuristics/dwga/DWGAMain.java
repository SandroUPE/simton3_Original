package br.upe.jol.metaheuristics.dwga;

import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;

public class DWGAMain {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		DWGA dwga = new DWGA(50, 5000, ontd);
		dwga.setPathFiles("C:\\Temp\\results_nsgaii_mut1_ctop\\");

		SolutionSet<Integer> solutions1 = dwga.execute();
		
		solutions1.printObjectivesToFile("./results/ONTD.txt");
		
	}
}
