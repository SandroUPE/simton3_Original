/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: TrafficGuidedInitializer.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	08/11/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import br.cns.Geolocation;
import br.cns.models.TrafficGuidedModel;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo Araujo
 * @since 08/11/2014
 */
public class TrafficGuidedInitializer extends Initializer<Integer> {
	private TrafficGuidedModel gp;

	private int numNodes;

	private double minDensity;

	private double maxDensity;

	private int ampLabel;
	
	private String prefix;

	public TrafficGuidedInitializer(int numNodes, double minDensity, double maxDensity, Geolocation[] locs, Double[][] traffic, int ampLabel, String prefix) {
		this(numNodes, minDensity, maxDensity, locs, traffic, ampLabel, prefix, 0);
	}

	public TrafficGuidedInitializer(int numNodes, double minDensity, double maxDensity, Geolocation[] locs, Double[][] traffic, int ampLabel, String prefix, int model) {
		this.numNodes = numNodes;
		this.minDensity = minDensity;
		this.maxDensity = maxDensity;
		this.ampLabel = ampLabel;
		gp = new TrafficGuidedModel(locs, traffic, 0.2, model);
		this.prefix = prefix;
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> ss = new SolutionSet<Integer>(size);
		
		int[] wavelengths = new int[]{5, 10, 15, 20, 25, 30, 35, 40};
		int[] oxcs = new int[]{1, 2, 3, 4, 5};
		
		int indexW = 0;
		int indexOxc = 0;

		Integer[] variables = null;
		Solution<Integer> solution = null;
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		int lastIndex = problem.getNumberOfVariables() - 1;
		double density = minDensity;
		double stepDensity = (maxDensity - minDensity)/size;
		for (int i = 0; i < size; i++) {
			gp.setD(density);
			density += stepDensity;
			adjacencyMatrix = gp.transform(adjacencyMatrix);
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