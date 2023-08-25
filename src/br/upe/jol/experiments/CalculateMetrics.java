/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: CalculateMetrics.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	15/01/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import br.upe.jol.base.RankUtil;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.metrics.Coverage;
import br.upe.jol.metrics.Hypervolume;
import br.upe.jol.metrics.MaximumSpread;
import br.upe.jol.metrics.Spacing;
import br.upe.jol.problems.simton.SimtonNonUniformRestriction;

/**
 * 
 * @author Danilo
 * @since 15/01/2014
 */
public class CalculateMetrics {
	private static final int STEP = 10;
	private static final int INI = 10;
	private static final int FIM = 710;

	public static void calculateMetrics(String pathVar, String pathPf, String pathMetrics, List<Double> lHv,
			List<Double> lSpacing, List<Double> lSpreading) {
		int nRuns = FIM;
		Spacing<Integer> spacing = new Spacing<Integer>();
		MaximumSpread<Integer> ms = new MaximumSpread<Integer>();
		Hypervolume<Integer> hyper = new Hypervolume<Integer>();
		Coverage<Integer> cov = new Coverage<Integer>();
		RankUtil<Integer> ranking = RankUtil.getIntegerInstance();
		int numNodes = 14;
		// SimtonProblem problem = new SimtonProblem(numNodes, 2);
		SimtonNonUniformRestriction problem = new SimtonNonUniformRestriction(numNodes, 2);
		problem.setSimulate(true);
		int size = 100;
		String var = "";
		String pf = "";
		SolutionSet<Integer> popRnd = new SolutionSet(size);
		SolutionSet<Integer> completeFile = new SolutionSet(size);
		StringBuffer metrics = new StringBuffer();

		double hvAux = 0;
		double spacingAux = 0;
		double msAux = 0;

		NumberFormat itf = NumberFormat.getInstance();
		itf.setMaximumFractionDigits(0);
		itf.setMinimumIntegerDigits(4);

		metrics = new StringBuffer();
		if (lHv.isEmpty()) {
			for (int i = INI; i < nRuns; i += STEP) {
				lHv.add(0.0);
				lSpreading.add(0.0);
				lSpacing.add(0.0);
			}
		}

		for (int i = INI; i < nRuns; i += STEP) {
			popRnd = new SolutionSet(size);
			completeFile = new SolutionSet(size);

			var = pathVar.replaceAll("ID", itf.format(i));
			pf = pathPf.replaceAll("ID", itf.format(i));
			completeFile.readIntVariablesFromFile(var, problem);
			completeFile.readExistingObjectivesFromFile(pf, problem);

			for (Solution<Integer> sol : completeFile.getSolutionsList()) {
				if (sol.getObjective(0) < problem.getUpperLimitObjective()[0]) {
					popRnd.add(sol);
				}
			}
			hvAux = hyper.getValue(popRnd);
			msAux = ms.getValue(popRnd);
			spacingAux = spacing.getValue(popRnd);

			lHv.set(i / STEP - 1, lHv.get(i / STEP - 1) + hvAux);
			lSpreading.set(i / STEP - 1, lSpreading.get(i / STEP - 1) + msAux);
			lSpacing.set(i / STEP - 1, lSpacing.get(i / STEP - 1) + spacingAux);

			popRnd = ranking.getRankedSolutions(popRnd)[0];
			metrics.append(String.format("%.6f %.6f %.6f %d \n", hvAux, msAux, spacingAux, popRnd.size()));
		}
		printMetrics(metrics, pathMetrics);

	}

	public static void consolidate() {
		String basePath = "C:/doutorado/experimentos/nsfnet/isda_limited/";
		List<Double> hv = new Vector<>();
		List<Double> ms = new Vector<>();
		List<Double> spc = new Vector<>();
		int count = 0;
		for (int i = 0; i < 1; i++) {
			calculateMetrics(basePath + i + "_spea2_50_100_1,00_0,01_ID_var.txt", basePath + i
					+ "_spea2_50_100_1,00_0,01_ID_pf.txt", basePath + i + "_metrics-new.txt", hv, spc, ms);
			count++;
		}
		StringBuffer consol = new StringBuffer();
		for (int i = INI; i < FIM; i += STEP) {
			consol.append(String.format("%.4f %.4f %.4f\n", hv.get(i / STEP - 1) / count, ms.get(i / STEP - 1) / count,
					spc.get(i / STEP - 1) / count));
		}
		printMetrics(consol, basePath + "/metrics-consol.txt");
	}

	public static void main(String[] args) {
		// String basePath =
		// "C:/Users/Danilo/workspace_phd/simton3/results/ini/non-uniform-wh/";
		// String basePath = "C:/doutorado/experimentos/nsfnet/new/";

		// calculateMetrics(basePath + "0_spea2_50_100_1,00_0,01_ID_var.txt",
		// basePath + "0_spea2_50_100_1,00_0,01_ID_pf.txt",
		// "hybrid-metrics-new.txt");
		// calculateMetrics(basePath + "_top_M99_50_1,00_0,06_ID_var.txt",
		// basePath + "_top_M99_50_1,00_0,06_ID_pf.txt", "top-metrics.txt");

		consolidate();
	}

	public static void printMetrics(StringBuffer content, String path) {
		try {
			FileWriter fw = new FileWriter(new File(path));
			fw.write(content.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
