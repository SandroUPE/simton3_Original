/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: HybridInitializer.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	12/01/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;

/**
 * 
 * @author Danilo
 * @since 12/01/2014
 */
public class HybridInitializer extends Initializer<Integer> {
	private Double[][] traffic;

	private Double[][] distances;

	private int numNodes;

	private double minDensity;

	private double maxDensity;

	private int ampLabel;

	public HybridInitializer(int numNodes, double minDensity, double maxDensity, Double[][] traffic,
			Double[][] distances, int ampLabel, double a) {
		this.numNodes = numNodes;
		this.minDensity = minDensity;
		this.maxDensity = maxDensity;
		this.ampLabel = ampLabel;
		this.traffic = traffic;
		this.distances = distances;
	}

	@Override
	public SolutionSet<Integer> execute(Problem<Integer> problem, int size) {
		SolutionSet<Integer> ss = new SolutionSet<Integer>(size);

		Initializer<Integer> ini1 = new PowerLawCustomInitializer(numNodes, minDensity, maxDensity, traffic, distances,
				ampLabel, 0);
		Initializer<Integer> ini2 = new WSInitializer(numNodes, minDensity, maxDensity, 0.15, ampLabel);

		SolutionSet<Integer> ss1 = ini1.execute(problem, size / 2);
		SolutionSet<Integer> ss2 = ini2.execute(problem, size / 2);

		for (Solution<Integer> sol : ss1.getSolutionsList()) {
			ss.add(sol);
		}
		for (Solution<Integer> sol : ss2.getSolutionsList()) {
			ss.add(sol);
		}

		return ss;
	}

	public String getOpID() {
		return String.format("hybrid");
	}

}
