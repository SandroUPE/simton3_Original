/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: DecisionVariablesCombination.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	23/11/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.operators.initializers;

/**
 * 
 * @author Danilo Araujo
 * @since 23/11/2014
 */
public class DecisionVariablesCombination {
	private int w;
	
	private int oxc;
	
	private double density;
	
	private Integer[][] adjacencyMatrix;

	/**
	 * Construtor da classe.
	 */
	public DecisionVariablesCombination(int w, int oxc, double density, Integer[][] adjacencyMatrix) {
		this.w = w;
		this.oxc = oxc;
		this.density = density;
		this.adjacencyMatrix = adjacencyMatrix;
	}

	/**
	 * Construtor da classe.
	 */
	public DecisionVariablesCombination(int w, int oxc, double density) {
		this.w = w;
		this.oxc = oxc;
		this.density = density;
		this.adjacencyMatrix = null;
	}

	/**
	 * @return o valor do atributo w
	 */
	public int getW() {
		return w;
	}

	/**
	 * Altera o valor do atributo w
	 * @param w O valor para setar em w
	 */
	public void setW(int w) {
		this.w = w;
	}

	/**
	 * @return o valor do atributo oxc
	 */
	public int getOxc() {
		return oxc;
	}

	/**
	 * Altera o valor do atributo oxc
	 * @param oxc O valor para setar em oxc
	 */
	public void setOxc(int oxc) {
		this.oxc = oxc;
	}

	/**
	 * @return o valor do atributo density
	 */
	public double getDensity() {
		return density;
	}

	/**
	 * Altera o valor do atributo density
	 * @param density O valor para setar em density
	 */
	public void setDensity(double density) {
		this.density = density;
	}

	/**
	 * @return o valor do atributo adjacencyMatrix
	 */
	public Integer[][] getAdjacencyMatrix() {
		return adjacencyMatrix;
	}

	/**
	 * Altera o valor do atributo adjacencyMatrix
	 * @param adjacencyMatrix O valor para setar em adjacencyMatrix
	 */
	public void setAdjacencyMatrix(Integer[][] adjacencyMatrix) {
		this.adjacencyMatrix = adjacencyMatrix;
	}

}
