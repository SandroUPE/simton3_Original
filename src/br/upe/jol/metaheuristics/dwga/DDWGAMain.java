package br.upe.jol.metaheuristics.dwga;

import br.upe.jol.base.SolutionSet;
import br.upe.jol.metaheuristics.spea2.SPEA2;
import br.upe.jol.problems.zdt.ZDT1;

public class DDWGAMain {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ZDT1 problem = new ZDT1(30, 2);

		HVCA dwga = new HVCA();
		dwga.setProblem(problem);
		SPEA2 spea2 = new SPEA2(50, 50, 500, problem);
		long tIni = System.currentTimeMillis();
		SolutionSet<Double> solutions1 = dwga.execute();
		System.out.println((System.currentTimeMillis() - tIni));
		solutions1.printObjectivesToFile("C:\\Temp\\zdt_dwga_pf.txt");
		solutions1.printVariablesToFile("C:\\Temp\\zdt_dwga_var.txt");
		tIni = System.currentTimeMillis();

		SolutionSet<Double> solutions2 = spea2.execute();
		System.out.println((System.currentTimeMillis() - tIni));

		solutions2.printObjectivesToFile("C:\\Temp\\results\\zdt_spea2_pf.txt");

	}
}
