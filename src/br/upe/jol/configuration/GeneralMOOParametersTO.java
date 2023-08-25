/*
 * *****************************************************************************
 * Projeto: JOL - Java Optimization Lab
 * Arquivo: GeneralMOOParametersTO.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	26/07/2010	Versão Inicial
 * ****************************************************************************
 */
package br.upe.jol.configuration;

import java.io.Serializable;

/**
 * @author Danilo Araújo
 *
 */
public class GeneralMOOParametersTO implements Serializable{
	private static final long serialVersionUID = 1L;

	private int populationSize;

	private double crossoverProbility;
	
	private double mutationProbability;
	
	public GeneralMOOParametersTO(){
		this(50, 0.9, 0.1);
	}
	
	public GeneralMOOParametersTO(int populationSize,
			double crossoverProbility, double mutationProbability) {
		super();
		this.populationSize = populationSize;
		this.crossoverProbility = crossoverProbility;
		this.mutationProbability = mutationProbability;
	}

	/**
	 * Método acessor para obter o valor do atributo populationSize.
	 * @return O valor de populationSize
	 */
	public int getPopulationSize() {
		return populationSize;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo populationSize.
	 * @param populationSize O novo valor de populationSize
	 */
	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	/**
	 * Método acessor para obter o valor do atributo crossoverProbility.
	 * @return O valor de crossoverProbility
	 */
	public double getCrossoverProbability() {
		return crossoverProbility;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo crossoverProbility.
	 * @param crossoverProbility O novo valor de crossoverProbility
	 */
	public void setCrossoverProbility(double crossoverProbility) {
		this.crossoverProbility = crossoverProbility;
	}

	/**
	 * Método acessor para obter o valor do atributo mutationProbability.
	 * @return O valor de mutationProbability
	 */
	public double getMutationProbability() {
		return mutationProbability;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo mutationProbability.
	 * @param mutationProbability O novo valor de mutationProbability
	 */
	public void setMutationProbability(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}
	
	
}
