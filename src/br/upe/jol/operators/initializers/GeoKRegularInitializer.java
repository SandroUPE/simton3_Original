/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GeoKRegularInitializer.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	20/11/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import br.cns.Geolocation;
import br.cns.models.GeokRegularTraffic;
import br.upe.jol.base.Problem;
import br.upe.jol.base.RankUtil;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.GmlSimton;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo Araujo
 * @since 20/11/2014
 */
public class GeoKRegularInitializer extends Initializer<Integer> {
	private GeokRegularTraffic gp;

	private int numNodes;

	private int ampLabel;

	private String prefix;

	private double minDensity;

	private double maxDensity;

	private Double[][] traffic;

	public GeoKRegularInitializer() {
	}

	public GeoKRegularInitializer(int numNodes, double minDensity, double maxDensity, Geolocation[] pos,
			Double[][] traffic, int ampLabel, String prefix) {
		this.numNodes = numNodes;
		this.ampLabel = ampLabel;
		this.traffic = traffic;
		gp = new GeokRegularTraffic(pos, traffic, 0.2);
		this.prefix = prefix;
		this.minDensity = minDensity;
		this.maxDensity = maxDensity;
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> ssReturn = new SolutionSet<Integer>(size);

		int[][] oxc = new int[][] { { 0, 1 }, { 2, 3 } };

		int[][] w1 = new int[][] { { 4, 6 }, { 8, 10 } };
		int[][] w2 = new int[][] { { 12, 14 }, { 16, 18 } };
		int[][] w3 = new int[][] { { 20, 22 }, { 24, 26 } };
		int[][] w4 = new int[][] { { 28, 32 }, { 36, 40 } };

		int[][][][] w = new int[][][][] { { w1, w2 }, { w3, w4 } };

		double[][] links1 = new double[][] { { 1.90, 2.00 }, { 2.15, 2.50 } };
		double[][] links2 = new double[][] { { 3.00, 3.50 }, { 4.00, 4.50 } };
		double[][][] links = new double[][][] { links1, links2 };

		int numBitsW = 6;
		int numExps = (int) Math.pow(2, numBitsW);
		int[][] experimentsW = create2kExps(numBitsW, numExps);
		int[][] experimentsOXC = create2kExps(2, numExps);
		int[][] experimentsAD = create2kExps(3, numExps);
		// int[][] experiments = FractionalFatorialUtil.FF_2_10_3;
		int ampLabel = 3;
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		SolutionSet ss = new SolutionSet<Integer>(numExps);
		gp = new GeokRegularTraffic(gp.getGeoCoords(), traffic, 0.2);
		Integer[] variables = null;
		int lastIndex = problem.getNumberOfVariables() - 1;
		Solution<Integer> solution = null;

		for (int i = 0; i < numExps; i++) {
			int oxcLabel = oxc[experimentsOXC[i][0]][experimentsOXC[i][1]];
			int wavelengths = w[experimentsW[i][0]][experimentsW[i][1]][experimentsW[i][2]][experimentsW[i][3]];
			double d = links[experimentsAD[i][0]][experimentsAD[i][1]][experimentsAD[i][2]];
			gp.setD(d);
			adjacencyMatrix = gp.transform(adjacencyMatrix);
			variables = new Integer[problem.getNumberOfVariables()];
			int index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * ampLabel;
					index++;
				}
			}
			variables[lastIndex - 1] = oxcLabel;
			variables[lastIndex] = wavelengths;

			solution = new SolutionONTD(new GmlSimton(numNodes, problem.getNumberOfObjectives(),
					((GmlSimton) problem).getData(), ((GmlSimton) problem).getNetworkLoad()), variables);
			ss.add(solution);
			problem.evaluate(solution);
		}

		RankUtil<Integer> rank = RankUtil.getIntegerInstance();
		SolutionSet<Integer>[] pfs = rank.getRankedSolutions(ss);

		for (SolutionSet<Integer> ssaux : pfs) {
			for (Solution<Integer> sol : ssaux.getSolutionsList()) {
				if (ssReturn.getSolutionsList().size() < size) {
					ssReturn.add(sol);
				} else {
					return ssReturn;
				}
			}

		}

		return ssReturn;
	}

	public static int[][] create2kExps(int k, int repts) {
		int numExps = (int) Math.pow(2, k);
		int[][] matrix = new int[numExps][k];

		int numSwap = numExps / 2;
		int counter = 0;
		int num = 0;
		for (int i = 0; i < k; i++) {
			counter = 0;
			for (int j = 0; j < numExps; j++) {
				matrix[j][i] = num;
				counter++;
				if (counter == numSwap) {
					counter = 0;
					if (num == 0) {
						num = 1;
					} else {
						num = 0;
					}
				}
			}
			numSwap /= 2;
		}
		int[][] finalMatrix = new int[repts][k];

		for (int r = 0; r < repts / numExps; r++) {
			for (int i = 0; i < numExps; i++) {
				for (int j = 0; j < k; j++) {
					finalMatrix[r + i][j] = matrix[i][j];
				}
			}
		}

		// for (int j = 0; j < numExps; j++) {
		// for (int i = 0; i < k; i++) {
		// System.out.print(finalMatrix[j][i] + " ");
		// }
		// System.out.println();
		// }

		return finalMatrix;
	}

	public String getOpID() {
		return String.format(prefix);
	}
}
