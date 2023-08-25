/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: SWUniformDensityTrafficInitializer.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	17/01/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import br.cns.models.SWDistance;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 17/01/2014
 */
public class SWUniformDensityTrafficInitializer extends Initializer<Integer> {
	private SWDistance gp;

	private int numNodes;

	private double minDensity;

	private double maxDensity;

	private int ampLabel;
	
	private String prefix;

	public SWUniformDensityTrafficInitializer() {
		this(14, 1.0 / (14 - 1), 0.40, null, 3, "swtud");
	}

	public SWUniformDensityTrafficInitializer(int numNodes, double minDensity, double maxDensity, Double[][] distances, int ampLabel, String prefix) {
		this.numNodes = numNodes;
		this.minDensity = minDensity;
		this.maxDensity = maxDensity;
		this.ampLabel = ampLabel;
		gp = new SWDistance(distances, 1, (maxDensity + minDensity) / 2);
		this.prefix = prefix;
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> ss = new SolutionSet<Integer>(size);

		Integer[] variables = null;
		Solution<Integer> solution = null;
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		int lastIndex = problem.getNumberOfVariables() - 1;
		double densityIncrement = (maxDensity - minDensity) / size;
		double density = minDensity;
		for (int i = 0; i < size; i++) {
			gp.setD(density);
			if (gp.getD() > 6.0 / (numNodes - 1)) {
				gp.setK(3);
			} else if (gp.getD() > 4.0 / (numNodes - 1)) {
				gp.setK(2);
			} else {
				gp.setK(1);
			}
			adjacencyMatrix = gp.transform(adjacencyMatrix);
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
			density += densityIncrement;
		}

		return ss;
	}

	public String getOpID() {
		return String.format(prefix);
	}
}
