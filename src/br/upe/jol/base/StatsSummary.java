/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: StatsSummary.java
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
public class StatsSummary {
	private double mean;
	
	private double stdDeviation;
	
	private double lastUpdate;
	
	
	/**
	 * Construtor da classe.
	 * @param mean
	 * @param stdDeviation
	 */
	public StatsSummary(double mean, double stdDeviation) {
		super();
		this.mean = mean;
		this.stdDeviation = stdDeviation;
		this.lastUpdate = 0;
	}
	
	/**
	 * Construtor da classe.
	 * @param mean
	 * @param stdDeviation
	 */
	public StatsSummary(double mean, double stdDeviation, double iteration) {
		super();
		this.mean = mean;
		this.stdDeviation = stdDeviation;
		this.lastUpdate = iteration;
	}



	/**
	 * Construtor da classe.
	 */
	public StatsSummary() {
		// TODO Auto-generated constructor stub
	}



	/**
	 * @return o valor do atributo mean
	 */
	public double getMean() {
		return mean;
	}



	/**
	 * Altera o valor do atributo mean
	 * @param mean O valor para setar em mean
	 */
	public void setMean(double mean) {
		this.mean = mean;
	}



	/**
	 * @return o valor do atributo stdDeviation
	 */
	public double getStdDeviation() {
		return stdDeviation;
	}



	/**
	 * Altera o valor do atributo stdDeviation
	 * @param stdDeviation O valor para setar em stdDeviation
	 */
	public void setStdDeviation(double stdDeviation) {
		this.stdDeviation = stdDeviation;
	}



	/**
	 * @return o valor do atributo lastUpdate
	 */
	public double getLastUpdate() {
		return lastUpdate;
	}



	/**
	 * Altera o valor do atributo lastUpdate
	 * @param lastUpdate O valor para setar em lastUpdate
	 */
	public void setLastUpdate(double lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

}
