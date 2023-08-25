/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: MOEA_Topology_Main.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	17/01/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.metaheuristics;

import java.io.File;
import java.text.NumberFormat;

import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.surrogateWRON.SurrogateSimtonBased;
import br.upe.jol.problems.surrogateWRON.SurrogateSimtonBasedNonUniform;

/**
 * 
 * @author Danilo
 * @since 17/01/2014
 */
public class MOEA_Topology_Main {
	/**
	 * @param args
	 */
	public static void main1(String[] args) {
		SurrogateSimtonBased ontd = new SurrogateSimtonBased(14, 2);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		String basePath = "C:/doutorado/submissões/elsarticle/eaai_2014/experiments/surrogate_alpha_06/";
		// basePath = "C:/doutorado/";
		File newDir = new File(basePath);
		newDir.mkdirs();
		for (int i = 8; i < 17; i++) {
			MOEA_Topology moea = new MOEA_Topology(50, 100, ontd);
			ontd.setSimulate(true);
			newDir = new File(basePath + i + "/");
			newDir.mkdir();
			moea.setPathFiles(basePath + i + "/");

			SolutionSet<Integer> solutions1 = moea.execute();
		}

	}
	
	public static void main2(String[] args) {
		SurrogateSimtonBasedNonUniform ontd = new SurrogateSimtonBasedNonUniform(14, 2);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		String basePath = "C:/doutorado/submissões/elsarticle/eaai_2014/experiments/surrogate_alpha_07_non_uniform_1/";
		// basePath = "C:/doutorado/";
		File newDir = new File(basePath);
		newDir.mkdirs();
		for (int i = 0; i < 30; i++) {
			MOEA_Topology moea = new MOEA_Topology(50, 1000, ontd);
			ontd.setSimulate(true);
			newDir = new File(basePath + i + "/");
			newDir.mkdir();
			moea.setPathFiles(basePath + i + "/");

			SolutionSet<Integer> solutions1 = moea.execute();
		}

	}
	
	public static void main(String[] args) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		String basePath = MOEA_GabrielBased.basePath + "/moea_geok_40e/";
		File newDir = new File(basePath);
		newDir.mkdirs();
		for (int i = 0; i < 1; i++) {
			MOEA_GabrielBased moea = new MOEA_GabrielBased(50, 1000, MOEA_GabrielBased.DEFAULT_PROBLEM);
			newDir = new File(basePath + i + "/");
			newDir.mkdir();
			moea.setPathFiles(basePath + i + "/");
			moea.execute();
		}

	}

}
