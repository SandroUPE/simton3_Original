/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: PowerLawCustomInitializer.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	11/01/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import br.cns.models.PACustom;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 11/01/2014
 */
public class PowerLawCustomInitializer extends Initializer<Integer> {
	private PACustom ba;

	private int numNodes;

	private double minDensity;

	private double maxDensity;

	private int ampLabel;

	public PowerLawCustomInitializer() {
		this(14, 1.0 / (14 - 1), 0.40, null, null, 3, 1);
	}

	public PowerLawCustomInitializer(int numNodes, double minDensity, double maxDensity, Double[][] traffic, Double[][] distances, int ampLabel, double a) {
		this.numNodes = numNodes;
		this.minDensity = minDensity;
		this.maxDensity = maxDensity;
		this.ampLabel = ampLabel;
		ba = new PACustom((minDensity + maxDensity) / 2, traffic, distances, a);
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> ss = new SolutionSet<Integer>(size);
		Integer[] variables = null;
		Solution<Integer> solution = null;
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		int qtde = 0;
		int lastIndex = problem.getNumberOfVariables() - 1;
		while (qtde < size) {
			ba.setDensity(Math.random() * (maxDensity - minDensity) + minDensity);
			adjacencyMatrix = ba.grow(adjacencyMatrix, adjacencyMatrix.length);
			variables = new Integer[problem.getNumberOfVariables()];
			int index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * ampLabel;
					index++;
				}
			}
			variables[lastIndex - 1] = (int) (Math.round(Math.random()
					* (problem.getUpperLimit(lastIndex - 1) - problem.getLowerLimit(lastIndex - 1))) + problem
					.getLowerLimit(lastIndex - 1));
			variables[lastIndex] = (int) (Math.round(Math.random()
					* (problem.getUpperLimit(lastIndex) - problem.getLowerLimit(lastIndex))) + problem
					.getLowerLimit(lastIndex));
			solution = new SolutionONTD(problem, variables);

			problem.evaluate(solution);
			problem.evaluateConstraints(solution);

			ss.add(solution);
			qtde++;
		}
		return ss;
	}

	public String getOpID() {
		return String.format("plc");
	}

}
