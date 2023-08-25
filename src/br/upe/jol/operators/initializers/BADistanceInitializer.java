/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: BADistanceInitializer.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	01/01/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import br.cns.models.PAByDistance;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 01/01/2014
 */
public class BADistanceInitializer extends Initializer<Integer> {
	private PAByDistance ba;

	private int numNodes;

	private double minDensity;

	private double maxDensity;

	private int ampLabel;

	public BADistanceInitializer() {
	}

	public BADistanceInitializer(int numNodes, double minDensity, double maxDensity, Double[][] distances, int ampLabel, String id) {
		this.numNodes = numNodes;
		this.minDensity = minDensity;
		this.maxDensity = maxDensity;
		this.ampLabel = ampLabel;
		opId = id;
		ba = new PAByDistance((minDensity + maxDensity) / 2, distances);
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> ss = new SolutionSet<Integer>(size);
		Integer[] variables = null;
		Solution<Integer> solution = null;
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		int qtde = 0;
		int lastIndex = problem.getNumberOfVariables() - 1;
		
		int[] wavelengths = new int[]{5, 10, 15, 20, 25, 30, 35, 40};
		int[] oxcs = new int[]{1, 2, 3, 4, 5};
		
		int indexW = 0;
		int indexOxc = 0;
		double density = minDensity;
		double stepDensity = (maxDensity - minDensity)/size;
		
		while (qtde < size) {
			ba.setDensity(density);
			density += stepDensity;
			adjacencyMatrix = ba.grow(adjacencyMatrix, adjacencyMatrix.length);
			variables = new Integer[problem.getNumberOfVariables()];
			int index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * ampLabel;
					index++;
				}
			}
			variables[lastIndex - 1] = oxcs[indexOxc];
			variables[lastIndex] = wavelengths[indexW];

			indexOxc++;
			indexW++;
			if (indexOxc == oxcs.length) {
				indexOxc = 0;
			}
			if (indexW == wavelengths.length) {
				indexW = 0;
			}
//			variables[lastIndex - 1] = (int) (Math.round(Math.random()
//					* (problem.getUpperLimit(lastIndex - 1) - problem.getLowerLimit(lastIndex - 1))) + problem
//					.getLowerLimit(lastIndex - 1));
//			variables[lastIndex] = (int) (Math.round(Math.random()
//					* (problem.getUpperLimit(lastIndex) - problem.getLowerLimit(lastIndex))) + problem
//					.getLowerLimit(lastIndex));
			solution = new SolutionONTD(problem, variables);

			problem.evaluate(solution);
			problem.evaluateConstraints(solution);

			ss.add(solution);
			qtde++;
		}
		return ss;
	}

	private String opId;
	public String getOpID() {
		return opId;
	}

}
