/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: SetupRna.java
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
public class SetupRna {
	private RnaInputDesign mainInputDesign;
	
	public SetupRna(){
		int numPtosMain = 1980;
		
		mainInputDesign = new RnaInputDesign(0, null);
		mainInputDesign.addInterval(new Interval(0.00001, 0.01, numPtosMain));
		mainInputDesign.addInterval(new Interval(0.01, 0.10, numPtosMain));
		mainInputDesign.addInterval(new Interval(0.10, 0.20, numPtosMain));
		mainInputDesign.addInterval(new Interval(0.20, 0.30, numPtosMain));
		mainInputDesign.addInterval(new Interval(0.30, 0.40, numPtosMain));
		mainInputDesign.addInterval(new Interval(0.40, 0.50, numPtosMain));
		mainInputDesign.addInterval(new Interval(0.50, 0.60, numPtosMain));
		mainInputDesign.addInterval(new Interval(0.60, 0.70, numPtosMain));
		mainInputDesign.addInterval(new Interval(0.70, 0.80, numPtosMain));
		mainInputDesign.addInterval(new Interval(0.80, 0.90, numPtosMain));
		mainInputDesign.addInterval(new Interval(0.90, 1.00, numPtosMain));
		
		int numPtosInner = 2000;
		RnaInputDesign innerRna = new RnaInputDesign(1, new Interval(0.00001, 0.01, numPtosMain));
		innerRna.addInterval(new Interval(0.00001, 0.00010, numPtosInner));
		innerRna.addInterval(new Interval(0.00010, 0.00100, numPtosInner));
		innerRna.addInterval(new Interval(0.00100, 0.00200, numPtosInner));
		innerRna.addInterval(new Interval(0.00200, 0.00300, numPtosInner));
		innerRna.addInterval(new Interval(0.00300, 0.00400, numPtosInner));
		innerRna.addInterval(new Interval(0.00400, 0.00500, numPtosInner));
		innerRna.addInterval(new Interval(0.00500, 0.00600, numPtosInner));
		innerRna.addInterval(new Interval(0.00600, 0.00700, numPtosInner));
		innerRna.addInterval(new Interval(0.00700, 0.00800, numPtosInner));
		innerRna.addInterval(new Interval(0.00800, 0.00900, numPtosInner));
		innerRna.addInterval(new Interval(0.00900, 0.01000, numPtosInner));
		
		innerRna = new RnaInputDesign(1, new Interval(0.01, 0.10, numPtosMain));
		innerRna.addInterval(new Interval(0.01, 0.02, numPtosInner));
		innerRna.addInterval(new Interval(0.02, 0.03, numPtosInner));
		innerRna.addInterval(new Interval(0.03, 0.04, numPtosInner));
		innerRna.addInterval(new Interval(0.04, 0.05, numPtosInner));
		innerRna.addInterval(new Interval(0.05, 0.06, numPtosInner));
		innerRna.addInterval(new Interval(0.06, 0.07, numPtosInner));
		innerRna.addInterval(new Interval(0.07, 0.08, numPtosInner));
		innerRna.addInterval(new Interval(0.08, 0.09, numPtosInner));
		innerRna.addInterval(new Interval(0.09, 0.10, numPtosInner));
		
		
	}
}
