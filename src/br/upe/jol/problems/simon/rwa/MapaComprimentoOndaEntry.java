/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: MapaComprimentoOndaEntry.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	10/04/2015		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.problems.simon.rwa;

/**
 * 
 * @author Danilo Araujo
 * @since 10/04/2015
 */
public class MapaComprimentoOndaEntry {
	private Integer[][] matrix;

	private double metricValue;
	
	private double secundaryMetricValue;
	
	private boolean notInitialized = true;
	
	/**
	 * Construtor da classe.
	 * @param matrix
	 * @param metricValue
	 * @param secundaryMetricValue
	 */
	public MapaComprimentoOndaEntry(Integer[][] matrix, double metricValue, double secundaryMetricValue) {
		super();
		this.matrix = matrix;
		this.metricValue = metricValue;
		this.secundaryMetricValue = secundaryMetricValue;
		notInitialized = true;
	}

	/**
	 * Construtor da classe.
	 */
	public MapaComprimentoOndaEntry() {
		notInitialized = true;
	}

	/**
	 * @return o valor do atributo matrix
	 */
	public Integer[][] getMatrix() {
		return matrix;
	}

	/**
	 * Altera o valor do atributo matrix
	 * @param matrix O valor para setar em matrix
	 */
	public void setMatrix(Integer[][] matrix) {
		this.matrix = matrix;
	}

	/**
	 * @return o valor do atributo metricValue
	 */
	public double getMetricValue() {
		return metricValue;
	}

	/**
	 * Altera o valor do atributo metricValue
	 * @param metricValue O valor para setar em metricValue
	 */
	public void setMetricValue(double metricValue) {
		this.metricValue = metricValue;
		notInitialized = false;
	}

	/**
	 * @return o valor do atributo secundaryMetricValue
	 */
	public double getSecundaryMetricValue() {
		return secundaryMetricValue;
	}

	/**
	 * Altera o valor do atributo secundaryMetricValue
	 * @param secundaryMetricValue O valor para setar em secundaryMetricValue
	 */
	public void setSecundaryMetricValue(double secundaryMetricValue) {
		this.secundaryMetricValue = secundaryMetricValue;
	}

	/**
	 * @return o valor do atributo notInitialized
	 */
	public boolean isNotInitialized() {
		return notInitialized;
	}

	/**
	 * Altera o valor do atributo notInitialized
	 * @param notInitialized O valor para setar em notInitialized
	 */
	public void setNotInitialized(boolean notInitialized) {
		this.notInitialized = notInitialized;
	}

}
