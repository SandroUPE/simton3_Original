/**
 * 
 */
package br.upe.jol.metaheuristics.modeii;

import br.upe.jol.problems.simton.SimtonProblem;

/**
 * .
 * 
 * @author Danilo Araújo
 */
public class IMODEMain {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		IMODE2 mode = new IMODE2(50, 50, 1000, ontd);
		mode.setPathFiles("C:\\Temp\\");
				
//		SolutionSet<Integer> ss = new SolutionSet<Integer>();
//		ss.readIntVariablesFromFile("D:\\Temp\\results_cluster\\mode_7\\._mode_var_909.txt", ontd);
//		for (Solution<Integer> sol : ss.getSolutionsList()){
//			ontd.evaluate(sol);
//		}

		mode.execute();
//		mode.execute();
	}

}
