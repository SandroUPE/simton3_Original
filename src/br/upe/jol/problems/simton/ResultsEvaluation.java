/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ResultsCostEvaluation.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	17/07/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.problems.simton;

/**
 * 
 * @author Danilo
 * @since 17/07/2014
 */
public class ResultsEvaluation {
	private int oxc;

	private int w;
	
	private double pb;
	
	private double cost;
	
	/**
	 * Construtor da classe.
	 * @param oxc
	 * @param w
	 */
	public ResultsEvaluation(int oxc, int w) {
		super();
		this.oxc = oxc;
		this.w = w;
	}
	
	/**
	 * Construtor da classe.
	 */
	public ResultsEvaluation() {
		// TODO Auto-generated constructor stub
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(oxc);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + w;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResultsEvaluation other = (ResultsEvaluation) obj;
		if (Double.doubleToLongBits(oxc) != Double.doubleToLongBits(other.oxc))
			return false;
		if (w != other.w)
			return false;
		return true;
	}

	/**
	 * @return o valor do atributo pb
	 */
	public double getPb() {
		return pb;
	}

	/**
	 * Altera o valor do atributo pb
	 * @param pb O valor para setar em pb
	 */
	public void setPb(double pb) {
		this.pb = pb;
	}

	/**
	 * @return o valor do atributo cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * Altera o valor do atributo cost
	 * @param cost O valor para setar em cost
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

}
