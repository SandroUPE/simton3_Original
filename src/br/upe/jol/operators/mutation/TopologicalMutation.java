/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: TopologicalMuation.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	17/01/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.mutation;

import java.util.HashSet;
import java.util.Set;

import br.cns.models.WattsStrogatzDensity;
import br.cns.util.RandomUtils;
import br.upe.jol.base.Scheme;
import br.upe.jol.base.Solution;
import br.upe.jol.metaheuristics.MOEA_Topology;
import br.upe.jol.problems.simton.SimtonProblemNonUniformHub;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 17/01/2014
 */
public class TopologicalMutation extends Mutation<Integer> {
	private static final WattsStrogatzDensity gp = new WattsStrogatzDensity(0.24, 1, 0.2, true);

	private Double[][] distances;

	private static final long serialVersionUID = 23L;

	private double minDensity;

	private double maxDensity;

	public TopologicalMutation(double mutationProbability) {
		super(mutationProbability);
	}

	public TopologicalMutation(double mutationProbability, Double[][] distances, double minDensity, double maxDensity) {
		super(mutationProbability);
		this.distances = distances;
		this.minDensity = minDensity;
		this.maxDensity = maxDensity;
	}

	public static void main(String[] args) {
		SimtonProblemNonUniformHub problemNonUniform = new SimtonProblemNonUniformHub(14, 2);
		problemNonUniform.setSimulate(true);
		Integer[] variables = { 0, 0, 6, 6, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 4, 8, 0, 0, 0, 0, 0, 0, 6, 6, 4, 0,
				0, 0, 0, 0, 0, 0, 0, 4, 0, 4, 6, 0, 8, 0, 0, 0, 0, 4, 0, 0, 6, 0, 4, 2, 4, 0, 2, 2, 0, 6, 0, 0, 0, 4,
				4, 0, 0, 2, 6, 0, 8, 8, 6, 6, 0, 8, 6, 0, 0, 2, 0, 2, 0, 0, 0, 2, 2, 2, 0, 0, 6, 4, 4, 25 };

		SolutionONTD sol = new SolutionONTD(problemNonUniform, variables);
		// problemNonUniform.evaluate(sol);

		TopologicalMutation m = new TopologicalMutation(0.10, MOEA_Topology.traffic, 0.08, 0.60);
		sol = (SolutionONTD) m.execute(sol);
		problemNonUniform.evaluate(sol);
	}

	@Override
	public Object execute(Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];
		Solution<Integer> retorno = null;
		int n = 14;
		Integer[][] adjacencyMatrix = new Integer[n][n];
		int index = 0;
		for (int i = 0; i < n; i++) {
			adjacencyMatrix[i][i] = 0;
			for (int j = i + 1; j < n; j++) {
				if (solution.getDecisionVariables()[index] == 0) {
					adjacencyMatrix[i][j] = 0;
					adjacencyMatrix[j][i] = 0;
				} else {
					adjacencyMatrix[i][j] = 1;
					adjacencyMatrix[j][i] = 1;
				}

				index++;
			}
		}
		if (Math.random() < mutationProbability) {
			adjacencyMatrix = transform(adjacencyMatrix);
		}
		Integer[] variables = new Integer[solution.getDecisionVariables().length];
		index = 0;
		for (int j = 0; j < adjacencyMatrix.length; j++) {
			for (int k = j + 1; k < adjacencyMatrix.length; k++) {
				variables[index] = adjacencyMatrix[j][k] * 3;
				index++;
			}
		}
		int iValue = 0;
		retorno = new SolutionONTD(solution.getProblem(), variables);
		for (int var = 0; var < solution.getDecisionVariables().length; var++) {
			if (var >= solution.getDecisionVariables().length - 2) {
				iValue = solution.getDecisionVariables()[var];
				if (Math.random() < mutationProbability) {
					iValue = (int) (Math.round(Math.random()
							* (solution.getProblem().getUpperLimit(var) - solution.getProblem().getLowerLimit(var))) + solution
							.getProblem().getLowerLimit(var));

					if (iValue < solution.getProblem().getLowerLimit(var))
						iValue = (int) solution.getProblem().getLowerLimit(var);
					else if (iValue > solution.getProblem().getUpperLimit(var))
						iValue = (int) solution.getProblem().getUpperLimit(var);
				}
			} else {
				iValue = variables[var];
			}
			retorno.setValue(var, iValue);
		}

		return retorno;
	}

	@Override
	public Object execute(Scheme scheme, Solution<Integer>... object) {
		return object;
	}
	
	private boolean minDensity(int numLinks, int n) {
		return numLinks/(n * (n - 1)) < minDensity;
	}

	public Integer[][] transform(Integer[][] matrix) {
		int n = matrix.length;
		Integer[][] newMatrix = new Integer[n][n];
		Set<String> initialLinks = new HashSet<>();
		Set<String> newLinks = new HashSet<>();
		int source;
		int dest;
		int numLinks = 0;

		for (int i = 0; i < n; i++) {
			newMatrix[i][i] = 0;
			for (int j = i + 1; j < n; j++) {
				newMatrix[i][j] = matrix[i][j];
				newMatrix[j][i] = matrix[i][j];
				if (matrix[i][j] == 1) {
					numLinks++;
					initialLinks.add(i + ";" + j);
				}
			}
		}
		int numLinksNewNodes = numLinks + 1;
		if (Math.random() > 0.8 && !minDensity(numLinks - 1, n)) {
			numLinksNewNodes = numLinks - 1;
		} 
		
//		int numLinksNewNodes = numLinks;
//		if (Math.random() > 0.5) {
//			numLinksNewNodes = (int) (numLinksNewNodes * (1 + Math.random() * 0.05));
//		} else {
//			numLinksNewNodes = (int) (numLinksNewNodes * (1 - Math.random() * 0.05));
//		}
//		double maxLinks = n * (n - 1) / 2;
//		if (numLinksNewNodes / maxLinks > maxDensity) {
//			numLinksNewNodes = (int) (maxLinks * maxDensity);
//		}
//		if (numLinksNewNodes / maxLinks < minDensity) {
//			numLinksNewNodes = (int) (maxLinks * minDensity);
//		}

//		for (String strLink : initialLinks) {
//			String[] strLinkArray = strLink.split(";");
//			int[] link = new int[] { Integer.parseInt(strLinkArray[0]), Integer.parseInt(strLinkArray[1]) };
//
//			int c1 = 0;
//			int c2 = 0;
//			for (int j = 0; j < matrix.length; j++) {
//				if (newMatrix[link[0]][j] == 1) {
//					c1++;
//				}
//				if (newMatrix[link[1]][j] == 1) {
//					c2++;
//				}
//			}
//			if (newMatrix[link[0]][link[1]] == 1 && (c1 > 1 && c2 > 1) && Math.random() > distances[link[0]][link[1]]) {
//				newMatrix[link[0]][link[1]] = 0;
//				newMatrix[link[1]][link[0]] = 0;
//
//				// sortear novo link
//				source = RandomUtils.getInstance().nextInt(n - 1);
//				dest = RandomUtils.getInstance().nextInt(n - 1);
//				while (Math.random() < distances[source][dest] && (source == link[0] && dest == link[1])
//						|| (source == link[1] && dest == link[0]) || source == dest || newMatrix[source][dest] == 1
//						|| newLinks.contains(source + ";" + dest)) {
//					source = RandomUtils.getInstance().nextInt(n - 1);
//					dest = RandomUtils.getInstance().nextInt(n - 1);
//				}
//				newMatrix[source][dest] = 1;
//				newMatrix[dest][source] = 1;
//				newLinks.add(source + ";" + dest);
//				newLinks.add(dest + ";" + source);
//			}
//		}
		while (numLinks < numLinksNewNodes) {
			// sortear novo link
			source = RandomUtils.getInstance().nextInt(n - 1);
			dest = RandomUtils.getInstance().nextInt(n - 1);
			while (Math.random() < distances[source][dest]
					&& (source == dest || newMatrix[source][dest] == 1 || newLinks.contains(source + ";" + dest))) {
				source = RandomUtils.getInstance().nextInt(n - 1);
				dest = RandomUtils.getInstance().nextInt(n - 1);
			}
			newMatrix[source][dest] = 1;
			newMatrix[dest][source] = 1;
			newLinks.add(source + ";" + dest);
			newLinks.add(dest + ";" + source);
			numLinks++;
		}
		while (numLinks > numLinksNewNodes) {
			// sortear novo link
			source = RandomUtils.getInstance().nextInt(n - 1);
			dest = RandomUtils.getInstance().nextInt(n - 1);
			while (Math.random() > distances[source][dest] && (source == dest || newMatrix[source][dest] == 0)) {
				source = RandomUtils.getInstance().nextInt(n - 1);
				dest = RandomUtils.getInstance().nextInt(n - 1);
			}
			int c1 = 0;
			int c2 = 0;
			for (int j = 0; j < matrix.length; j++) {
				if (newMatrix[source][j] == 1) {
					c1++;
				}
				if (newMatrix[dest][j] == 1) {
					c2++;
				}
			}
			if (c1 < 2 || c2 < 2) {
				continue;
			}
			newMatrix[source][dest] = 0;
			newMatrix[dest][source] = 0;
			numLinks--;
		}
		return newMatrix;

	}

	@Override
	public String getOpID() {
		return "M99";
	}
}
