/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: WSInitializer.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	31/12/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import java.util.HashMap;
import java.util.Map;

import br.cns.models.WattsStrogatzDensity;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 31/12/2013
 */
public class WSInitializer extends Initializer<Integer> {
	private WattsStrogatzDensity gp;

	private int numNodes;

	private double minDensity;

	private double maxDensity;

	private int ampLabel;

	private double rp;

	public WSInitializer() {
		this(14, 1.0 / (14 - 1), 0.40, 0.24, 3);
	}

	public WSInitializer(int numNodes, double minDensity, double maxDensity, double rp, int ampLabel) {
		this.numNodes = numNodes;
		this.minDensity = minDensity;
		this.maxDensity = maxDensity;
		this.ampLabel = ampLabel;
		this.rp = rp;

		gp = new WattsStrogatzDensity(rp, 1, (maxDensity + minDensity) / 2, true);
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> ss = new SolutionSet<Integer>(size);

		Integer[] variables = null;
		Solution<Integer> solution = null;
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		int lastIndex = problem.getNumberOfVariables() - 1;
		double density = maxDensity;
		double stepDensity = (maxDensity - minDensity) / size;

		Map<Integer, DecisionVariablesCombination> mapa = new HashMap<Integer, DecisionVariablesCombination>();
		int counter = 0;
		while (mapa.size() < size) {
			l1: for (int w = 40; w >= 4; w -= 1) {
				for (int oxc = 5; oxc >= 1; oxc--) {
					mapa.put(counter, new DecisionVariablesCombination(w, oxc, density));
					density -= stepDensity;
					counter++;
					if (mapa.size() > size) {
						break l1;
					}
				}
			}
		}
		for (int i = 0; i < size; i++) {
			gp.setD(mapa.get(i).getDensity());
			gp.setK((int) Math.max(1, Math.floor(0.5 * mapa.get(i).getDensity() * numNodes)));
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

			solution = new SolutionONTD(problem, variables);

			problem.evaluate(solution);
			if (solution.getObjective(0) < 1) {
				ss.add(solution);
			}
		}

		return ss;
	}

	public String getOpID() {
		return String.format("ws-%.2f", rp);
	}
}
