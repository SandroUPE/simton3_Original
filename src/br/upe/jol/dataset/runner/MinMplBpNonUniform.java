/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: MinMplBpNonUniform.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	23/09/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset.runner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import br.grna.PadraoTreinamento;
import br.grna.bp.RedeNeural;
import br.upe.jol.dataset.Dataset;
import br.upe.jol.dataset.OpticalNetDesignVariable;

/**
 * 
 * @author Danilo
 * @since 23/09/2014
 */
public class MinMplBpNonUniform {
	/**
	 * 
	 */
	private static final int NUM_PATTERNS_CLASS = 2000;

	public static void main(String[] args) {
		double[][] intervalos = new double[][] { { 0.0, 0.001 }, { 0.001, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 },
				{ 0.20, 0.30 }, { 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 0.80 },
				{ 0.80, 0.90 }, { 0.90, 1.00 } };
		int inputs = 7;
		int hidden = 15;
		RedeNeural rn = new RedeNeural(inputs, 1, hidden, false);

		List<OpticalNetDesignVariable> variables = new Vector<>();
		variables.add(OpticalNetDesignVariable.W);
		variables.add(OpticalNetDesignVariable.OXC);
		variables.add(OpticalNetDesignVariable.CC);
		variables.add(OpticalNetDesignVariable.DENSITY);
		variables.add(OpticalNetDesignVariable.AVERAGE_PATH_LENGTH_PHYSICAL);
		variables.add(OpticalNetDesignVariable.ENTROPY_DFT);

		Dataset ds = new Dataset("C:\\doutorado\\experimentos\\nfsnet_model2\\dataset-non-uniform", intervalos, rn,
				NUM_PATTERNS_CLASS, variables);
		ds.setValidation(false);
		ds.setNetworkLoad(200);
		ds.populate();

		double[] min = rn.getMinValues();
		double[] max = rn.getMaxValues();

		List<PadraoTreinamento> tr = ds.getPadroesTreinamento();
		List<PadraoTreinamento> t = ds.getPadroesTeste();

		for (int i = 0; i < rn.getMinValues().length; i++) {
			if (rn.getMinValues()[i] < min[i]) {
				min[i] = rn.getMinValues()[i];
			}
			if (rn.getMaxValues()[i] > max[i]) {
				max[i] = rn.getMaxValues()[i];
			}
		}

		StringBuffer sb = new StringBuffer();
		String dirBase = "results/mlp-non-uniform/";
		double sumErro = 0;
		double[] erros = new double[30];
		String strRede;
		File fileDirBase = new File(dirBase);
		if (!fileDirBase.exists()) {
			fileDirBase.mkdirs();
		}
		for (int i = 0; i < 31; i++) {
			rn = new RedeNeural(inputs, 1, hidden, false);
			rn.setMinValues(min);
			rn.setMaxValues(max);
			sumErro = 0;
			strRede = dirBase + "mlp_" + inputs + "_traffic_" + i + "_" + hidden + "-non-uniform.txt";
			rn.treinar(tr, t, 500000, 2e-7, 0.02, 0.0, strRede, true);
			for (PadraoTreinamento pt : t) {
				sumErro += rn.calcularErroPadrao(false, pt);
			}
			erros[i] = sumErro / t.size();
			System.out.println(erros[i]);
		}

		int i = 0;
		for (PadraoTreinamento pt : t) {
			sb.append(i).append(" ").append(pt.getSaida()[0]).append(" \n");

			i++;
		}
		try {
			FileWriter fw = new FileWriter("erros-teste-single_class-non-uniform.txt");
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void setBounds(List<PadraoTreinamento> padroes, List<PadraoTreinamento> padroes2, RedeNeural rn) {
		int length = padroes.get(0).getEntrada().length;
		double[] minValues = new double[length];
		double[] maxValues = new double[length];

		Arrays.fill(minValues, Double.MAX_VALUE);
		Arrays.fill(maxValues, Double.MIN_VALUE);
		for (PadraoTreinamento padrao : padroes) {
			for (int i = 0; i < length; i++) {
				if (padrao.getEntrada()[i] < minValues[i]) {
					minValues[i] = padrao.getEntrada()[i];
				}
				if (padrao.getEntrada()[i] > maxValues[i]) {
					maxValues[i] = padrao.getEntrada()[i];
				}
			}
		}
		for (PadraoTreinamento padrao : padroes2) {
			for (int i = 0; i < length; i++) {
				if (padrao.getEntrada()[i] < minValues[i]) {
					minValues[i] = padrao.getEntrada()[i];
				}
				if (padrao.getEntrada()[i] > maxValues[i]) {
					maxValues[i] = padrao.getEntrada()[i];
				}
			}
		}
		rn.setMinValues(minValues);
		rn.setMaxValues(maxValues);
	}

}
