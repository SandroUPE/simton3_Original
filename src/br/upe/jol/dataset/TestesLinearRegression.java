/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: Testes5Inputs.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	03/11/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset;

import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import br.cns.util.Statistics;
import br.grna.PadraoTreinamento;
import br.upe.jol.dataset.experiments.DatasetCorrelationExperiment;

/**
 * 
 * @author Danilo
 * @since 03/11/2013
 */
public class TestesLinearRegression {
	private static final int NUM_PATTERNS_CLASS = 1980;

	private static DatasetCorrelationExperiment dataset;

	public static void main(String[] args) {
		if (dataset == null) {
			dataset = buildPatterns(NUM_PATTERNS_CLASS);
		}
//		gravarErroMedClassNovo();
//		showSummary("results/linearRegression/16variables/lnr");
		showSummary("results/linearRegression/12-04variables/lnr");
	}

	private static void showSummary(String sufixo) {
		StringBuffer content = new StringBuffer();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(8);
		nf.setMinimumFractionDigits(8);
		String nomeRede;
		List<Double> r2 = new Vector<>();
		List<Double> emq = new Vector<>();
		content.append("R2").append("\t").append("EMQ");
		System.out.println("\nResumo de " + sufixo);
		System.out.println("R2\t\tEMQ");
		for (int l = 0; l < 30; l++) {
			nomeRede = sufixo + l + ".txt";

			LinearRegression rede = new LinearRegression();
			rede.carregar(nomeRede);
			double sqres = 0;
			for (PadraoTreinamento pt : dataset.getPadroesTreinamento()) {
				sqres += Math.pow(pt.getSaida()[0] - rede.obterSaida(pt.getEntrada()), 2);
			}
			r2.add(1 - sqres / rede.getSqTotal());
			emq.add(sqres / dataset.getPadroesTreinamento().size());
			content.append(nf.format(r2.get(l))).append("\t").append(nf.format(emq.get(l)));
			content.append("\n");
		}
		Statistics stats = new Statistics();
		stats.addRandomVariableValues(r2);
		stats.addRandomVariableValues(emq);
		
		StringBuffer s = new StringBuffer();
		s.append("\n");
		s.append(nf.format(stats.getMean(0))).append("\t").append(nf.format(stats.getMean(1)));
		s.append("\n");
		s.append(nf.format(stats.getStandardDeviation(0))).append("\t").append(nf.format(stats.getStandardDeviation(1)));
		
		content.append(s);
		
		System.out.println(s.toString());
		try {
			FileWriter fw = new FileWriter(sufixo + "summary16var.txt");
			fw.write(content.toString());
			fw.close();
		} catch (IOException e) {
		}
	}

	private static void gravarErroMedClassNovo() {
		String sufixo = "results/lnr_emq_log_estratificado";
		sufixo = "results/linearRegression/16variables/lnr";
		StringBuffer content = new StringBuffer();
		double[] erroMed;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(8);
		nf.setMinimumFractionDigits(8);
		String nomeRede;
		for (int l = 0; l < 30; l++) {
			nomeRede = sufixo + l + ".txt";
			erroMed = calculateErroMedioClassNovo(nomeRede);
			for (double erro : erroMed) {
				content.append(nf.format(erro)).append(" ");
			}
			content.append("\n");
		}

		try {
			FileWriter fw = new FileWriter("results/linearRegression/16variables/erros-consol-class-estrat.txt");
			fw.write(content.toString());
			fw.close();
		} catch (IOException e) {
		}
	}

	/**
	 * @param nomeRede
	 * @return
	 */
	private static double[] calculateErroMedioClassNovo(String nomeRede) {
		LinearRegression rede = new LinearRegression();
		rede.carregar(nomeRede);
		double[] erros = new double[dataset.getIntervals().length];
		for (int i = 0; i < erros.length; i++) {
			for (PadraoTreinamento pt : dataset.getMapPadroesTeste().get(i)) {
				erros[i] += Math.pow(pt.getSaida()[0] - rede.obterSaida(pt.getEntrada()), 2);
			}
			erros[i] /= dataset.getMapPadroesTeste().get(i).size();
		}
		return erros;
	}

	/**
	 * @param nomeRede
	 * @param numPadroesClasse
	 * @param rede
	 * @return
	 */
	private static DatasetCorrelationExperiment buildPatterns(int numPadroesClasse) {
		double[][] intervalos = new double[][] { { 0.0, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 }, { 0.20, 0.30 },
				{ 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 0.80 }, { 0.80, 0.90 },
				{ 0.90, 1.00 } };
		DatasetCorrelationExperiment ds = new DatasetCorrelationExperiment(
				"C:\\doutorado\\experimentos\\nfsnet_model2", intervalos, null, NUM_PATTERNS_CLASS);
		ds.populate();

		return ds;
	}

}
