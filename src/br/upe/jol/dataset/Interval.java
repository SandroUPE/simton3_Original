/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Interval.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	15/11/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset;

/**
 * 
 * @author Danilo
 * @since 15/11/2013
 */
public class Interval {
	private double min;
	
	private double max;
	
	private int numPtosAmostrados;
	
	/**
	 * Construtor da classe.
	 * @param min
	 * @param max
	 * @param numPtosAmostrados
	 */
	public Interval(double min, double max, int numPtosAmostrados) {
		super();
		this.min = min;
		this.max = max;
		this.numPtosAmostrados = numPtosAmostrados;
	}

	/**
	 * @return o valor do atributo min
	 */
	public double getMin() {
		return min;
	}

	/**
	 * Altera o valor do atributo min
	 * @param min O valor para setar em min
	 */
	public void setMin(double min) {
		this.min = min;
	}

	/**
	 * @return o valor do atributo max
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Altera o valor do atributo max
	 * @param max O valor para setar em max
	 */
	public void setMax(double max) {
		this.max = max;
	}

	/**
	 * @return o valor do atributo numPtosAmostrados
	 */
	public int getNumPtosAmostrados() {
		return numPtosAmostrados;
	}

	/**
	 * Altera o valor do atributo numPtosAmostrados
	 * @param numPtosAmostrados O valor para setar em numPtosAmostrados
	 */
	public void setNumPtosAmostrados(int numPtosAmostrados) {
		this.numPtosAmostrados = numPtosAmostrados;
	}
}
