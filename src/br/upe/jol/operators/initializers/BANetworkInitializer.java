/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: BANetworkInitializer.java
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

import br.cns.models.BarabasiDensity;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 31/12/2013
 */
public class BANetworkInitializer extends Initializer<Integer> {
	private BarabasiDensity ba;

	private int numNodes;

	private double minDensity;

	private double maxDensity;

	private int ampLabel;
	
	private double attachmentExponent;

	public BANetworkInitializer() {
		this(14, 1.0 / (14 - 1), 0.40, 0.8, 3);
	}

	public BANetworkInitializer(int numNodes, double minDensity, double maxDensity, double attachmentExponent,
			int ampLabel) {
		this.numNodes = numNodes;
		this.minDensity = minDensity;
		this.maxDensity = maxDensity;
		this.ampLabel = ampLabel;
		this.attachmentExponent = attachmentExponent;
		ba = new BarabasiDensity((minDensity + maxDensity) / 2, attachmentExponent);
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> ss = new SolutionSet<Integer>(size);
		Integer[] variables = null;
		Solution<Integer> solution = null;
		Integer[][] adjacencyMatrix = new Integer[numNodes][numNodes];
		int qtde = 0;
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
		while (qtde < size) {
			ba.setDensity(mapa.get(qtde).getDensity());
			adjacencyMatrix = ba.transform(adjacencyMatrix);
			variables = new Integer[problem.getNumberOfVariables()];
			int index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * ampLabel;
					index++;
				}
			}
			variables[lastIndex - 1] = mapa.get(qtde).getOxc();
			variables[lastIndex] = mapa.get(qtde).getW();

			solution = new SolutionONTD(problem, variables);

			problem.evaluate(solution);
			if (solution.getObjective(0) < 1) {
				ss.add(solution);
			}
			qtde++;
		}
		return ss;
	}
	
	public String getOpID() {
		return String.format("ba-%.2f", attachmentExponent);
	}

}
