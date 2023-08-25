/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: EvaluateRnaDesignPrecision.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	03/12/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.metaheuristics.spea2;

import java.text.NumberFormat;

import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;

/**
 * 
 * @author Danilo
 * @since 03/12/2013
 */
public class EvaluateRnaDesignPrecision {
	public static void main(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
			
		SolutionSet<Integer> solutions1 = new SolutionSet<Integer>(50);
		for (int i = 0; i < 50; i++){
			solutions1.add(new ISPEA2Solution(ontd, new Integer[ontd.getNumberOfVariables()]));
		}
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(8);
		nf.setMaximumFractionDigits(8);
		solutions1.readExistingIntVariablesFromFile("C:/Temp/_spea2_50_100_1,00_0,01_0.040_var.txt", ontd);
		StringBuffer sb = new StringBuffer();
		for (Solution sol : solutions1.getSolutionsList()){
			ontd.evaluate(sol);
			sb.append(nf.format(sol.getObjective(0))).append(" ").append(nf.format(sol.getObjective(1))).append("\n");
		}
		System.out.println(sb.toString());
	}

}
