/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ExperimentInitialization.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	19/10/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.metaheuristics.spea2;

import java.io.File;
import java.text.NumberFormat;

import br.cns.models.BarabasiDensity;
import br.cns.models.Toroid;
import br.cns.models.WattsStrogatzDensity;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;

/**
 * 
 * @author Danilo
 * @since 19/10/2013
 */
public class ExperimentInitialization {
	private static final int NUM_NODES = 14;
	private Problem<Integer> problem = new SimtonProblem(NUM_NODES, 2);

	private static final WattsStrogatzDensity gp = new WattsStrogatzDensity(0.24, 1, 0.2, true);

	private static final BarabasiDensity ba = new BarabasiDensity(0.2, 0.8);

	private static final Toroid toroid = new Toroid(0.2, NUM_NODES);

	private static int populationSize = 50;

	private static final NumberFormat nf = NumberFormat.getInstance();

	private static final String basePath = "C:\\Temp\\";
	
	private static final int W = 40;
	
	private static final int OXC = 5;
	
	private static final int AMP = 3;


	public static void main(String[] args) {
		ExperimentInitialization ei = new ExperimentInitialization();

		SolutionSet<Integer> solutionSet = null;

		for (double d = 0.1; d <= 0.4; d += 0.1) {
			for (double t = 0.8; t < 1.1; t += 0.1) {
				solutionSet = ei.initializePopulationBA(d, t);
				ei.gravarResultados(solutionSet, basePath, d, "ba", t);
			}
		}

		for (double d = 0.1; d <= 0.4; d += 0.1) {
			for (double t = 0.04; t < 0.24; t += 0.06) {
				solutionSet = ei.initializePopulationWS(d, t);
				ei.gravarResultados(solutionSet, basePath, d, "ws", t);
			}

		}
	}

	private SolutionSet<Integer> initializePopulationWS(double density, double rp) {
		SolutionSet<Integer> population = new SolutionSet<Integer>();
		Integer[] variables = null;
		Solution<Integer> solution = null;
		Integer[][] adjacencyMatrix = new Integer[NUM_NODES][NUM_NODES];
		gp.setD(density);
		if (gp.getD() > 4.0 / (NUM_NODES - 1)) {
			gp.setK(2);
		} else {
			gp.setK(1);
		}
		gp.setP(rp);
		for (int i = 0; i < populationSize; i++) {
			adjacencyMatrix = gp.transform(adjacencyMatrix);
			variables = new Integer[problem.getNumberOfVariables()];
			int index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * AMP;
					index++;
				}
			}
			variables[91] = OXC;
			variables[92] = W;
			solution = new ISPEA2Solution(problem, variables);
			problem.evaluate(solution);
			problem.evaluateConstraints(solution);
			population.add(solution);
		}
		return population;
	}

	private void gravarResultados(SolutionSet<Integer> archive, String pathFiles, double density, String algorithm,
			double param) {
		archive.printObjectivesToFile(pathFiles + File.separator + algorithm + "_" + populationSize + "_"
				+ nf.format(density) + "_" + nf.format(param) + "_pf.txt");
		archive.printVariablesToFile(pathFiles + File.separator + algorithm + "_" + populationSize + "_"
				+ nf.format(density) + "_" + nf.format(param) + "_var.txt");
	}

	private SolutionSet<Integer> initializePopulationBA(double density, double t) {
		SolutionSet<Integer> population = new SolutionSet<Integer>();
		Integer[] variables = null;
		Solution<Integer> solution = null;
		Integer[][] adjacencyMatrix = new Integer[NUM_NODES][NUM_NODES];
		int qtde = 0;
		ba.setDensity(density);
		ba.setExponent(t);
		while (qtde < populationSize) {
			adjacencyMatrix = ba.transform(adjacencyMatrix);
			variables = new Integer[problem.getNumberOfVariables()];
			int index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * AMP;
					index++;
				}
			}
			variables[91] = OXC;
			variables[92] = W;
			solution = new ISPEA2Solution(problem, variables, 0);

			problem.evaluate(solution);
			problem.evaluateConstraints(solution);
			population.add(solution);
			qtde++;
		}
		return population;
	}
}
