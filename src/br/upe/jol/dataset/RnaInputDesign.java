/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: RnaInputDesign.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	15/11/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset;

import java.util.List;
import java.util.Vector;

/**
 * 
 * @author Danilo
 * @since 15/11/2013
 */
public class RnaInputDesign {
	private int id;
	
	private List<Interval> mainRnaintervals = new Vector<>();
	
	private Interval parentInterval;
	
	private List<RnaInputDesign> innerRnaDesignList = new Vector<>();
	
	/**
	 * Construtor da classe.
	 */
	public RnaInputDesign() {
		super();
	}

	/**
	 * Construtor da classe.
	 * @param id
	 * @param parentInterval
	 * @param innerRnaDesignList
	 */
	public RnaInputDesign(int id, Interval parentInterval) {
		super();
		this.id = id;
		this.parentInterval = parentInterval;
	}

	public void addInterval(Interval i){
		mainRnaintervals.add(i);
	}

	/**
	 * @return o valor do atributo id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Altera o valor do atributo id
	 * @param id O valor para setar em id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return o valor do atributo mainRnaintervals
	 */
	public List<Interval> getMainRnaintervals() {
		return mainRnaintervals;
	}

	/**
	 * Altera o valor do atributo mainRnaintervals
	 * @param mainRnaintervals O valor para setar em mainRnaintervals
	 */
	public void setMainRnaintervals(List<Interval> mainRnaintervals) {
		this.mainRnaintervals = mainRnaintervals;
	}

	/**
	 * @return o valor do atributo parentInterval
	 */
	public Interval getParentInterval() {
		return parentInterval;
	}

	/**
	 * Altera o valor do atributo parentInterval
	 * @param parentInterval O valor para setar em parentInterval
	 */
	public void setParentInterval(Interval parentInterval) {
		this.parentInterval = parentInterval;
	}

	/**
	 * @return o valor do atributo innerRnaDesignList
	 */
	public List<RnaInputDesign> getInnerRnaDesignList() {
		return innerRnaDesignList;
	}

	/**
	 * Altera o valor do atributo innerRnaDesignList
	 * @param innerRnaDesignList O valor para setar em innerRnaDesignList
	 */
	public void setInnerRnaDesignList(List<RnaInputDesign> innerRnaDesignList) {
		this.innerRnaDesignList = innerRnaDesignList;
	}
}
