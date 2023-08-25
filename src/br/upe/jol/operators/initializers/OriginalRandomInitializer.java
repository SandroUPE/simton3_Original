/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: OriginalRandomInitializer.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	01/01/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 01/01/2014
 */
public class OriginalRandomInitializer extends Initializer<Integer> {
	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> population = new SolutionSet<>(size);
		Integer[] variables = null;
		Solution<Integer> solution = null;
		for (int i = 0; i < size; i++) {
			variables = new Integer[problem.getNumberOfVariables()];
			for (int j = 0; j < variables.length; j++) {
				variables[j] = (int) (Math.round(Math.random() * (problem.getUpperLimit(j) - problem.getLowerLimit(j))) + problem
						.getLowerLimit(j));
			}

			solution = new SolutionONTD(problem, variables);
			problem.evaluate(solution);
			problem.evaluateConstraints(solution);
			population.add(solution);
		}
		return population;
	}

	public String getOpID() {
		return "ornd";
	}
}
