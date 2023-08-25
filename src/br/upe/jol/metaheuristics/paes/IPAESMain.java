/**
 * 
 */
package br.upe.jol.metaheuristics.paes;

import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;

/**
 * .
 * 
 * @author Danilo Araújo
 */
public class IPAESMain {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		IPAES paes = new IPAES(50, 2 * 50, ontd);
//		
//		SolutionSet<Integer> ss = new SolutionSet<Integer>();
//		ss.readIntVariablesFromFile("C:\\Temp\\results2\\ontd_paes_var_40.txt", ontd);
			
//		SolutionSet<Integer> solutions1 = paes.execute(ss, 40);
		SolutionSet<Integer> solutions1 = paes.execute();
		
//		solutions1.printObjectivesToFile("C:/Temp/results/ONTD_SPEA2_PF.txt");
//		solutions1.printVariablesToFile("C:/Temp/results/ONTD_SPEA2_VAR.txt");
		
	}

}
