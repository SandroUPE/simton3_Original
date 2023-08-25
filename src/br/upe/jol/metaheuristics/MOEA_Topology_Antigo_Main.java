/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: MOEA_Topology_Antigo_Main.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	22/08/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.metaheuristics;

import java.io.File;
import java.text.NumberFormat;

import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simton.SimtonProblem;

/**
 * 
 * @author Danilo
 * @since 22/08/2014
 */
public class MOEA_Topology_Antigo_Main {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		StringBuffer sbFinal = new StringBuffer();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		String basePath = "C:/doutorado/submissões/elsarticle/eaai_2014/experiments/isda_amp_unico_non_uniform/";
		// basePath = "C:/doutorado/";
		File newDir = new File(basePath);
		newDir.mkdirs();
		for (int i = 0; i < 11; i++) {
			MOEA_Topology_Antigo moea = new MOEA_Topology_Antigo(50, 1000, ontd);
			ontd.setSimulate(true);
			newDir = new File(basePath + i + "/");
			newDir.mkdir();
			moea.setPathFiles(basePath + i + "/");

			SolutionSet<Integer> solutions1 = moea.execute();
		}

	}

	public static void main1(String[] args) {
		SimtonProblem ontd = new SimtonProblem(14, 2);
		StringBuffer sbFinal = new StringBuffer();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		String basePath = "C:/doutorado/submissões/elsarticle/eaai_2014/experiments/isda_amp_unico_80e/2/";
		// 
		String inVarFile = basePath + "_top_M3_50_1,00_0,06_0.915_var.txt";
		String inPfFile = basePath + "_top_M3_50_1,00_0,06_0.915_pf.txt";
		SolutionSet<Integer> solutions1 = new SolutionSet<Integer>(50);
		solutions1.readIntVariablesFromFile(inVarFile, ontd);
		solutions1.readExistingObjectivesFromFile(inPfFile, ontd);
		MOEA_Topology_Antigo moea = new MOEA_Topology_Antigo(50, 1000, ontd);
		moea.setPathFiles(basePath);
		moea.execute(solutions1, 915);

	}
}
