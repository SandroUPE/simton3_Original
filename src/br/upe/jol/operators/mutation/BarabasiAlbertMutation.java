/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: BarabasiAlbertMutation.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	14/10/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.mutation;

import br.cns.metrics.AlgebraicConnectivity;
import br.cns.models.BarabasiDensity;
import br.cns.transformations.DegreeMatrix;
import br.cns.util.RandomUtils;
import br.upe.jol.base.Scheme;
import br.upe.jol.base.Solution;

/**
 * 
 * @author Danilo
 * @since 14/10/2013
 */
public class BarabasiAlbertMutation extends Mutation<Integer> {
	public BarabasiAlbertMutation(double mutationProbability) {
		super(mutationProbability);
	}

	private double exponent = 0.8;

	private static final long serialVersionUID = 23L;

	private BarabasiDensity gp = new BarabasiDensity(.2, 0.8);

	@Override
	public Object execute(Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];
		int numNodes = 14;
		double minDensity = 1.0 / (numNodes - 1);
		double maxDensity = 0.40;
		double density = Math.random() * (maxDensity - minDensity) + minDensity;
		Integer[][] nMatrix = new Integer[numNodes][numNodes];
		if (Math.random() < 0.20) {
			gp.setDensity(density);
			nMatrix = gp.transform(nMatrix);
			
			while (AlgebraicConnectivity.getInstance().calculate(nMatrix) <= 0) {
				gp.setDensity(Math.random() * (maxDensity - minDensity) + minDensity);
				nMatrix = gp.transform(nMatrix);
			}
			
			doMutation(solution, nMatrix);
		}
//		else {
//			adjustDensity(solution, numNodes, density, nMatrix);
//		}
//		doMutation(solution, nMatrix);
		return solution;
	}

	/**
	 * @param solution
	 * @param nMatrix
	 */
	private void doMutation(Solution<Integer> solution, Integer[][] nMatrix) {
		Integer[] variables = new Integer[solution.getDecisionVariables().length];
		int index = 0;
		for (int j = 0; j < nMatrix.length; j++) {
			for (int k = j + 1; k < nMatrix.length; k++) {
				variables[index] = nMatrix[j][k] * 3;
				index++;
			}
		}
		int iValue = 0;

		for (int var = 0; var < solution.getDecisionVariables().length; var++) {
			if (var >= solution.getDecisionVariables().length - 2) {
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
			solution.setValue(var, iValue);
		}
	}

	/**
	 * @param solution
	 * @param numNodes
	 * @param density
	 * @param nMatrix
	 */
	private void adjustDensity(Solution<Integer> solution, int numNodes, double density, Integer[][] nMatrix) {
		int pos = 0;
		int numCreatedLinks = 0;
		for (int i = 0; i < numNodes; i++) {
			nMatrix[i][i] = 0;
			for (int j = i + 1; j < numNodes; j++) {
				if (solution.getDecisionVariables()[pos] != 0) {
					nMatrix[i][j] = 1;
					nMatrix[j][i] = 1;
					numCreatedLinks++;
				} else {
					nMatrix[i][j] = 0;
					nMatrix[j][i] = 0;
				}
				pos++;
			}
		}
		double r;
		double[] cdf = new double[numNodes];

		Integer[][] degree = null;
		degree = DegreeMatrix.getInstance().transform(nMatrix);
		double sumToCdf = 0;
		boolean linked = false;
		int n = 0;
		while (numCreatedLinks < (numNodes * (numNodes - 1) / 2.0) * density) {
			n = RandomUtils.getInstance().nextInt(14 - 1);
			sumToCdf = 0;
			for (int i = 0; i < degree.length; i++) {
				sumToCdf += Math.pow(degree[i][i], exponent);
			}
			cdf[0] = Math.pow(degree[0][0], exponent) / sumToCdf;
			for (int i = 1; i < 14; i++) {
				cdf[i] = cdf[i - 1] + Math.pow(degree[i][i], exponent) / sumToCdf;
			}

			linked = false;
			r = Math.random();
			for (int j = 0; j < 14; j++) {
				if (j != n && ((j == 0 && r < cdf[0]) || (r < cdf[j] && r >= cdf[j - 1])) && nMatrix[n][j] != 1) {
					nMatrix[n][j] = 1;
					nMatrix[j][n] = 1;
					degree[n][n]++;
					degree[j][j]++;
					numCreatedLinks++;
					linked = true;
					break;
				}
			}
			if (!linked) {
				for (int j = 0; j < 14; j++) {
					if (j != n && nMatrix[n][j] != 1) {
						nMatrix[n][j] = 1;
						nMatrix[j][n] = 1;
						degree[n][n]++;
						degree[j][j]++;
						numCreatedLinks++;
						break;
					}
				}
			}
		}
		while (numCreatedLinks > (numNodes * (numNodes - 1) / 2.0) * density) {
			n = RandomUtils.getInstance().nextInt(numNodes - 1);
			while (degree[n][n] < 2) {
				n = RandomUtils.getInstance().nextInt(numNodes - 1);
			}
			sumToCdf = 0;
			for (int i = 0; i < degree.length; i++) {
				sumToCdf += Math.pow(degree[i][i], exponent);
			}
			cdf[0] = Math.pow(degree[0][0], exponent) / sumToCdf;
			for (int i = 1; i < 14; i++) {
				cdf[i] = cdf[i - 1] + Math.pow(degree[i][i], exponent) / sumToCdf;
			}
			linked = false;
			r = Math.random();
			for (int j = 0; j < numNodes; j++) {
				if (j != n && ((j == 0 && r < cdf[0]) || (r < cdf[j] && r >= cdf[j - 1])) && nMatrix[n][j] != 0
						&& degree[j][j] >= 2) {
					nMatrix[n][j] = 0;
					nMatrix[j][n] = 0;
					degree[n][n]--;
					degree[j][j]--;
					numCreatedLinks--;
					linked = true;
					break;
				}
			}
			if (!linked) {
				for (int j = 0; j < numNodes; j++) {
					if (j != n && nMatrix[n][j] != 0 && degree[j][j] >= 2) {
						nMatrix[n][j] = 0;
						nMatrix[j][n] = 0;
						degree[n][n]--;
						degree[j][j]--;
						numCreatedLinks--;
						break;
					}
				}
			}
		}
	}

	@Override
	public Object execute(Scheme scheme, Solution<Integer>... object) {
		Solution<Integer> solution = (Solution<Integer>) object[0];

		for (int var = 0; var < solution.getDecisionVariables().length; var++) {
			if (!scheme.getValor()[var].equals(Scheme.STR_CORINGA)) {
				solution.setValue(var, Integer.valueOf(scheme.getValor()[var]));
			} else if (Math.random() < mutationProbability) {
				int iValue = (int) (Math.round(Math.random()
						* (solution.getProblem().getUpperLimit(var) - solution.getProblem().getLowerLimit(var))) + solution
						.getProblem().getLowerLimit(var));

				if (iValue < solution.getProblem().getLowerLimit(var))
					iValue = (int) solution.getProblem().getLowerLimit(var);
				else if (iValue > solution.getProblem().getUpperLimit(var))
					iValue = (int) solution.getProblem().getUpperLimit(var);

				solution.setValue(var, iValue);
			}
		}
		return solution;
	}

	@Override
	public String getOpID() {
		return "M3";
	}
}
