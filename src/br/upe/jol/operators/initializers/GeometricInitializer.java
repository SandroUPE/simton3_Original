/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GeometricInitializer.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	05/11/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import br.cns.Geolocation;
import br.cns.models.Geometric;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo Araujo
 * @since 05/11/2014
 */
public class GeometricInitializer extends Initializer<Integer> {
	private Geometric gp;

	private int numNodes;

	private int ampLabel;
	
	private String prefix;

	public GeometricInitializer() {
		this(14, 1.0 / (14 - 1), 0.40, null, 3, "geo");
	}

	public GeometricInitializer(int numNodes, double minDensity, double maxDensity, Geolocation[] pos, int ampLabel, String prefix) {
		this.numNodes = numNodes;
		this.ampLabel = ampLabel;
		gp = new Geometric(pos, 10000);
		this.prefix = prefix;
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> ss = new SolutionSet<Integer>(size);

		Integer[] variables = null;
		Solution<Integer> solution = null;
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		int lastIndex = problem.getNumberOfVariables() - 1;
		for (int i = 0; i < size; i++) {
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
			if (solution.getObjective(0) < 1){
				ss.add(solution);	
			}
			
		}

		return ss;
	}

	public String getOpID() {
		return String.format(prefix);
	}
}
