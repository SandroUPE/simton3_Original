/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: BANetworkInitializerClass.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	01/01/2014		Vers�o inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

import java.util.List;
import java.util.Vector;

import br.cns.models.BarabasiDensity;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 01/01/2014
 */
public class BANetworkInitializerClass extends Initializer<Integer> {
	private BarabasiDensity ba;

	private int numNodes;

	private double minDensity;

	private double maxDensity;

	private int ampLabel;

	private double attachmentExponent;

	public BANetworkInitializerClass() {
		this(14, 1.0 / (14 - 1), 0.40, 0.8, 3);
	}

	public BANetworkInitializerClass(int numNodes, double minDensity, double maxDensity, double attachmentExponent,
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

		List<Integer> w = createWList(size);
		List<Integer> oxc = createOxcList(size);
		List<Double> densities = new Vector<>();
		createDensityList(size, densities);
		double density = 0;

		while (qtde < size) {
			int index = (int) (Math.random() * densities.size());
			density = densities.get(index);
			densities.remove(index);
			ba.setDensity(density);
			adjacencyMatrix = ba.transform(adjacencyMatrix);
			variables = new Integer[problem.getNumberOfVariables()];
			index = 0;
			for (int j = 0; j < adjacencyMatrix.length; j++) {
				for (int k = j + 1; k < adjacencyMatrix.length; k++) {
					variables[index] = adjacencyMatrix[j][k] * ampLabel;
					index++;
				}
			}
			index = (int) (Math.random() * oxc.size());
			variables[lastIndex - 1] = oxc.get(index);
			oxc.remove(index);
			index = (int) (Math.random() * w.size());
			variables[lastIndex] = w.get(index);
			w.remove(index);

			solution = new SolutionONTD(problem, variables);

			problem.evaluate(solution);
			problem.evaluateConstraints(solution);

			ss.add(solution);
			qtde++;
		}
		return ss;
	}

	public String getOpID() {
		return String.format("bac-%.2f", attachmentExponent);
	}

	/**
	 * @param size
	 * @param densities
	 */
	private void createDensityList(int size, List<Double> densities) {
		int count = 0;
		for (int i = 0; i < size / 3; i++) {
			densities.add(Math.random() * (maxDensity - minDensity) / 3 + minDensity);
			count++;
		}
		for (int i = 0; i < size / 3; i++) {
			densities.add(Math.random() * (maxDensity - minDensity) / 3 + minDensity + (maxDensity - minDensity) / 3);
			count++;
		}
		for (int i = 0; i < size - count; i++) {
			densities.add(Math.random() * (maxDensity - minDensity) / 3 + minDensity + 2 * (maxDensity - minDensity)
					/ 3);
		}
	}

	/**
	 * @param size
	 * @return
	 */
	private List<Integer> createOxcList(int size) {
		List<Integer> oxc = new Vector<>();
		for (int i = 0; i < size / 2; i++) {
			oxc.add((int) Math.round(Math.random() * 2));
		}
		for (int i = 0; i < size / 2; i++) {
			oxc.add((int) Math.round(Math.random() * 2 + 3));
		}
		return oxc;
	}

	/**
	 * @param size
	 * @return
	 */
	private List<Integer> createWList(int size) {
		List<Integer> w = new Vector<>();
		for (int i = 0; i < size / 2; i++) {
			w.add((int) Math.round(Math.random() * 20));
		}
		for (int i = 0; i < size / 2; i++) {
			w.add((int) Math.round(Math.random() * 20 + 20));
		}
		return w;
	}
}
