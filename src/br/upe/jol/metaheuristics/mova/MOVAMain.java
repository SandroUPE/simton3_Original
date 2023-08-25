package br.upe.jol.metaheuristics.mova;

import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;

public class MOVAMain {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		MOVA nsgaii = new MOVA(50, 1000, ontd);
		nsgaii.setPathFiles("C:\\Temp\\results_nsgaii_mut1_ctop\\");

		SolutionSet<Integer> solutions1 = nsgaii.execute();
		
		solutions1.printObjectivesToFile("./results/ONTD.txt");
		
	}
}
