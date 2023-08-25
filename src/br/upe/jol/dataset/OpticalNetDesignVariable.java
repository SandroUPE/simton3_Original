/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Variables.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	17/11/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset;

/**
 * 
 * @author Danilo
 * @since 17/11/2013
 */
public enum OpticalNetDesignVariable {
	COST(0, "Cost"), 
	BP(1, "BP"), 
	W(2, "W"), 
	OXC(3, "OXC"), 
	AC(4, "Algebraic Conectivity"), 
	NC(5, "Natural Conectivity"), 
	DENSITY(6, "Density"), 
	AVERAGE_DEGREE(7, "Average Degree"), 
	AVERAGE_PATH_LENGTH(8, "Average path length"), 
	CC(9, "Clustering Coefficient"), 
	DIAMETER(10, "Diameter"), 
	ENTROPY(11, "Entropy"), 
	DIAMETER_PHYSICAL(12, "Diameter physical"), 
	AVERAGE_PATH_LENGTH_PHYSICAL(13, "Average path length physycal"), 
	NF_MEAN(15, "Mean Noise Figure"), 
	ENTROPY_DFT(14, "Entropy of DFT of Laplacian Eigenvalues"), 
	SR(16, "Spectral Radius"), 
	ENTROPY_DFT_PHYSICAL(17, "Entropy of DFT of Laplacian Eigenvalues - Physical"), 
	POWER_SAT(18, "Amplifier saturation power"),
	OXC_LABEL(22, "OXC_LABEL"),
	PHYSICAL_DENSITY(18, "PHYSICAL_DENSITY"),
	BLOCKING_DUE_BER(23, "BLOCKING_DUE_BER"),
	BLOCKING_DUE_DISPERSION(24, "BLOCKING_DUE_DISPERSION"),
	NETWORK_LOAD(25, "NETWORK_LOAD"),
	PATH_LENGTH_PHYSICAL_SD(26, "PATH_LENGTH_PHYSICAL_SD"),
	MEAN_ASE(27, "MEAN_ASE"),
	STD_ASE(28, "STD_ASE"),
	CR(29, "CR"),
	NUM_NODES(30, "Number of nodes"),
	NUM_LINKS(31, "Number of links"),
	HUB_DEGREE(32, "HUB_DEGREE"),
	ASSORTATIVITY(33, "ASSORTATIVITY");

	private int index;
	private String description;

	private OpticalNetDesignVariable(int index, String description) {
		this.index = index;
		this.description = description;
	}

	/**
	 * @return o valor do atributo index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Altera o valor do atributo index
	 * @param index O valor para setar em index
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return o valor do atributo description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Altera o valor do atributo description
	 * @param description O valor para setar em description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
