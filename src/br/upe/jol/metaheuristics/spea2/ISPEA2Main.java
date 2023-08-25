/**
 * 
 */
package br.upe.jol.metaheuristics.spea2;

import java.text.NumberFormat;

import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;

/**
 * .
 * 
 * @author Danilo Araújo
 */
public class ISPEA2Main {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		SolutionSet<Integer> solutions1 = new SolutionSet<Integer>(50);
		ISPEA2 spea2 = new ISPEA2(50, 50, 1000, ontd);
		ontd.setSimulate(true);

		for (int i = 0; i < 50; i++) {
			solutions1.add(new ISPEA2Solution(ontd, new Integer[ontd.getNumberOfVariables()]));
		}
		solutions1.readExistingIntVariablesFromFile(
				"C:/doutorado/experimentos/nsfnet/isda_limited/0_spea2_50_100_1,00_0,01_0.710_var.txt", ontd);
		solutions1.readExistingObjectivesFromFile(
				"C:/doutorado/experimentos/nsfnet/isda_limited/0_spea2_50_100_1,00_0,01_0.710_pf.txt", ontd);

		for (int i = 1; i < 2; i++) {
			spea2 = new ISPEA2(50, 50, 10000, ontd);
			spea2.setPathFiles("C:/doutorado/experimentos/nsfnet/isda_limited/r0" + i + "/");
			spea2.execute(solutions1, 710);
		}
	}

	public static void main1(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);

		SolutionSet<Integer> solutions1 = new SolutionSet<Integer>(50);
		for (int i = 0; i < 50; i++) {
			solutions1.add(new ISPEA2Solution(ontd, new Integer[ontd.getNumberOfVariables()]));
		}
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(8);
		nf.setMaximumFractionDigits(8);
		solutions1.readExistingIntVariablesFromFile("C:/Temp/_spea2_50_100_1,00_0,01_0.070_var.txt", ontd);
		StringBuffer sb = new StringBuffer();
		for (Solution sol : solutions1.getSolutionsList()) {
			ontd.evaluate(sol);
			sb.append(nf.format(sol.getObjective(0))).append(" ").append(nf.format(sol.getObjective(1))).append("\n");
		}
		System.out.println(sb.toString());
	}

}
