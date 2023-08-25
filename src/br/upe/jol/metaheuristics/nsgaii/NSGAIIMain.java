/**
 * 
 */
package br.upe.jol.metaheuristics.nsgaii;

import java.text.NumberFormat;

import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.zdt.ZDT1;

/**
 * @author Danilo Araújo
 */
public class NSGAIIMain {
	public static void main(String[] args) {
		ZDT1 zdt1 = new ZDT1(30, 2);
		NSGAII nsgaii = new NSGAII(30, 10, zdt1);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(4);
		
		SolutionSet<Double> solution = nsgaii.execute();
		
		for (Solution<Double> valor : solution.getSolutionsList()){			
			System.out.println(nf.format(valor.getObjective(0)) + ";" + nf.format(valor.getObjective(1)));
		}
	}
}
