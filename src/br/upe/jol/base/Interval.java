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
 * Danilo Araújo	14/08/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.base;

/**
 * 
 * @author Danilo
 * @since 14/08/2014
 */
public class Interval {
	private double minValue;
	
	private double maxValue;
	
	
	/**
	 * Construtor da classe.
	 * @param minValue
	 * @param maxValue
	 */
	public Interval(double minValue, double maxValue) {
		super();
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("min = %.4f; max = %.4f ", minValue, maxValue);
	}

	/**
	 * Construtor da classe.
	 */
	public Interval() {
		// TODO Auto-generated constructor stub
	}

	public boolean contains(double value) {
		return value > minValue && value <= maxValue;
	}

	/**
	 * @return o valor do atributo minValue
	 */
	public double getMinValue() {
		return minValue;
	}



	/**
	 * Altera o valor do atributo minValue
	 * @param minValue O valor para setar em minValue
	 */
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}



	/**
	 * @return o valor do atributo maxValue
	 */
	public double getMaxValue() {
		return maxValue;
	}



	/**
	 * Altera o valor do atributo maxValue
	 * @param maxValue O valor para setar em maxValue
	 */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

}
