/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: WaxmanInitializer.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	05/11/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import java.util.HashMap;
import java.util.Map;

import br.cns.Geolocation;
import br.cns.models.Waxman;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo Araujo
 * @since 05/11/2014
 */
public class WaxmanInitializer extends Initializer<Integer> {
	private Waxman gp;

	private int numNodes;

	private int ampLabel;

	private String prefix;

	public WaxmanInitializer() {
		this(14, 1.0 / (14 - 1), 0.40, null, 3, "wxm");
	}

	public WaxmanInitializer(int numNodes, double minDensity, double maxDensity, Geolocation[] pos, int ampLabel,
			String prefix) {
		this.numNodes = numNodes;
		this.ampLabel = ampLabel;
		gp = new Waxman(pos, 0.8, 0.2);
		this.prefix = prefix;
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> ss = new SolutionSet<Integer>(size);

		Integer[] variables = null;
		Solution<Integer> solution = null;
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		int lastIndex = problem.getNumberOfVariables() - 1;

		Map<Integer, DecisionVariablesCombination> mapa = new HashMap<Integer, DecisionVariablesCombination>();
		int counter = 0;
		while (mapa.size() < size) {
			l1: for (int w = 40; w >= 4; w -= 1) {
				for (int oxc = 5; oxc >= 1; oxc--) {
					mapa.put(counter, new DecisionVariablesCombination(w, oxc, 0));
					counter++;
					if (mapa.size() > size) {
						break l1;
					}
				}
			}
		}
		for (int i = 0; i < size; i++) {
			gp.setBeta(Math.random());
			gp.setAlpha(Math.random());
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
			System.out.printf("alpha = %.2f; beta = %.2f\n", gp.getAlpha(), gp.getBeta());
			problem.evaluate(solution);
			if (solution.getObjective(0) < 1) {
				ss.add(solution);
				
			}

		}

		return ss;
	}

	public String getOpID() {
		return String.format(prefix);
	}
}
