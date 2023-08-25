/**
 * 
 */
package br.upe.jol.metaheuristics.pesaii;

import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SimonProblem_3Obj;

/**
 * .
 * 
 * @author Danilo Araújo
 */
public class IPesa2Main {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimonProblem_3Obj ontd = new SimonProblem_3Obj(14, 3);
		IPesaII spea2 = new IPesaII(50, 50, 10100, ontd);
		spea2.setPathFiles("D:\\Temp\\3obj\\" + "pesaii_02\\");
		
		SolutionSet<Integer> ss = new SolutionSet<Integer>();
		ss.readIntVariablesFromFile("D:\\Temp\\3obj\\" + "pesaii_02\\._pesaii_50_50_5_1.00_0.02_10,000_var.txt", ontd);
		for (Solution<Integer> sol : ss.getSolutionsList()){
			ontd.evaluate(sol);
		}
		SolutionSet<Integer> solutions1 = spea2.execute(ss, 10000);
		
		solutions1.printObjectivesToFile("C:/Temp/results/ONTD_SPEA2_PF.txt");
		solutions1.printVariablesToFile("C:/Temp/results/ONTD_SPEA2_VAR.txt");
		
	}

}
