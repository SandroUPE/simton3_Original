/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: PermutationGeoInitializer.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	25/11/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import br.cns.Geolocation;
import br.cns.models.GeokRegularTraffic;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.GmlSimton;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo Araujo
 * @since 25/11/2014
 */
public class PermutationGeoInitializer extends Initializer<Integer> {
	private GeokRegularTraffic gp;

	private int numNodes;

	private int ampLabel;

	private String prefix;

	public PermutationGeoInitializer() {
	}

	public PermutationGeoInitializer(int numNodes, Geolocation[] pos, Double[][] traffic, int ampLabel, String prefix) {
		this.numNodes = numNodes;
		this.ampLabel = ampLabel;
		gp = new GeokRegularTraffic(pos, traffic, 0.2);
		this.prefix = prefix;
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> ss = new SolutionSet<Integer>(size);
		Integer[] variables = null;
		Solution<Integer> solution = null;
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		int lastIndex = problem.getNumberOfVariables() - 1;

		double density = 2.8 / (numNodes - 1);
		double stepDensity = density / 10;

		List<Double> densities = new Vector<Double>();
		List<Integer[][]> ams = new Vector<Integer[][]>();
		for (double d = 0; d < density; d += stepDensity) {
			densities.add(d);
			gp.setD(d);
			ams.add(gp.transform(adjacencyMatrix));
			System.out.println(d);
		}

		Map<Integer, DecisionVariablesCombination> mapa = new HashMap<Integer, DecisionVariablesCombination>();
		int counter = 0;
		int di = 0;
		l1: for (double d : densities) {
			for (int w = 40; w >= 4; w -= 1) {
				if (di % 2 != w % 2) {
					continue;
				}
				for (int oxc = 5; oxc >= 1; oxc = oxc - 2) {
					mapa.put(counter, new DecisionVariablesCombination(w, oxc, d, ams.get(di)));
					// System.out.println(w + " - " + oxc + " - " + d);
					counter++;
					if (mapa.size() > size) {
						break l1;
					}
				}
			}
			di++;
		}
		ss = new SolutionSet<Integer>(counter);
		for (int i = 0; i < counter; i++) {
//			adjacencyMatrix = mapa.get(i).getAdjacencyMatrix();
			gp.setD(mapa.get(i).getDensity());
			adjacencyMatrix = gp.transform(adjacencyMatrix);
			variables = new Integer[problem.getNumberOfVariables()];
			int index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * ampLabel;
					index++;
				}
			}
			variables[lastIndex - 1] = mapa.get(i).getOxc();
			variables[lastIndex] = mapa.get(i).getW();

			solution = new SolutionONTD(new GmlSimton(numNodes, problem.getNumberOfObjectives(),
					((GmlSimton) problem).getData(), ((GmlSimton) problem).getNetworkLoad()), variables);
			ss.add(solution);

		}
		ss.evaluate();

		return ss;
	}

	public String getOpID() {
		return String.format(prefix);
	}
}
