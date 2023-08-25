/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: MSLogScale.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	14/06/2015		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.metrics;

import br.upe.jol.base.Metric;
import br.upe.jol.base.Problem;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.comparators.ObjectiveSolutionComparator;
import br.upe.jol.problems.simton.SimtonProblem;

/**
 * 
 * @author Danilo Araujo
 * @since 14/06/2015
 */
public class MSLogScale <T> extends Metric<T> {
	// para 2 objetivos
	private static final double MAX_NORM = Math.sqrt(2);
	// para 3 objetivos
	private static final double MAX_NORM_3 = Math.sqrt(3);

	@Override
	public double getValue(SolutionSet<T> solutionSet) {
		return getValue(solutionSet, solutionSet.get(0).getProblem());
	}
	
	public double getValue(SolutionSet<T> solutionSet, Problem<T> problem) {
		ObjectiveSolutionComparator<T> comparator = new ObjectiveSolutionComparator<T>(0);
		solutionSet.sort(comparator);
		boolean logscale[] = problem.getLogScale();
		int numberOfObjectives = solutionSet.get(0).numberOfObjectives();
		double result = 0;
		double lengthObjective = 1;

		double[] extreme1 = solutionSet.get(0).getAllObjectives();
		double[] extreme2 = solutionSet.get(solutionSet.size() - 1).getAllObjectives();
		if (problem.getUpperLimitObjective() != null) {
			for (int i = 0; i < numberOfObjectives; i++) {
				if (logscale[i]) {
					lengthObjective = Math.log10(problem.getUpperLimitObjective()[i])
							- Math.log10(problem.getLowerLimitObjective()[i]);
					result += (((Math.log10(extreme1[i]) - Math.log10(extreme2[i])) / lengthObjective) * ((Math
							.log10(extreme1[i]) - Math.log10(extreme2[i])) / lengthObjective)) / MAX_NORM_3;
				} else {
					lengthObjective = problem.getUpperLimitObjective()[i] - problem.getLowerLimitObjective()[i];
					result += (((extreme1[i] - extreme2[i]) / lengthObjective) * ((extreme1[i] - extreme2[i]) / lengthObjective))
							/ MAX_NORM_3;
				}
			}
		} else {
			for (int i = 0; i < numberOfObjectives; i++) {
				if (logscale[i]) {
					result += (Math.log10(extreme1[i]) - Math.log10(extreme2[i])) * (Math.log10(extreme1[i]) - Math.log10(extreme2[i]));
				} else {
					result += (extreme1[i] - extreme2[i]) * (extreme1[i] - extreme2[i]);	
				}
			}
		}

		return Math.sqrt(result);
	}



	public static void main(String[] args) {
		SimtonProblem problem = new SimtonProblem(14, 2);
		int generation = 200;

		for (int i = 0; i < 4; i++) {
			SolutionSet<Integer> ss = new SolutionSet<Integer>(50);
			ss.readObjectivesFromFile("C:/doutorado/submissões/elsarticle/eaai_2014/experiments/eval_alpha_0_30/" + i
					+ "/_top_M3_50_1,00_0,06_pf.txt", problem);
			MaximumSpread<Integer> hv = new MaximumSpread<Integer>();

			System.out.println(i + " = " + hv.getValue(ss));
		}

	}
}
