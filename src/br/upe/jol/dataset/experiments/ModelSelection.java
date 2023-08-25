/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ModelSelection.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	17/11/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset.experiments;

import static br.upe.jol.dataset.OpticalNetDesignVariable.AC;
import static br.upe.jol.dataset.OpticalNetDesignVariable.AVERAGE_PATH_LENGTH;
import static br.upe.jol.dataset.OpticalNetDesignVariable.AVERAGE_PATH_LENGTH_PHYSICAL;
import static br.upe.jol.dataset.OpticalNetDesignVariable.CC;
import static br.upe.jol.dataset.OpticalNetDesignVariable.DIAMETER;
import static br.upe.jol.dataset.OpticalNetDesignVariable.DIAMETER_PHYSICAL;
import static br.upe.jol.dataset.OpticalNetDesignVariable.ENTROPY;
import static br.upe.jol.dataset.OpticalNetDesignVariable.ENTROPY_DFT;
import static br.upe.jol.dataset.OpticalNetDesignVariable.NC;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import br.grna.PadraoTreinamento;
import br.grna.bp.RedeNeural;
import br.upe.jol.dataset.Dataset;
import br.upe.jol.dataset.OpticalNetDesignVariable;

/**
 * 
 * @author Danilo
 * @since 17/11/2013
 */
public class ModelSelection {
	/**
	 * 
	 */
	private static final int NUM_PATTERNS_CLASS = 1000;

	public static void main(String[] args) {
		double[][] intervalos = new double[][] { { 0.0, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 }, { 0.20, 0.30 },
				{ 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 0.80 }, { 0.80, 0.90 },
				{ 0.90, 1.00 } };

		List<OpticalNetDesignVariable> allVariablesList = new Vector<>();
//		allVariablesList.add(Variable.OXC);
//		allVariablesList.add(Variable.W);
//		allVariablesList.add(Variable.DENSITY);
//		 allVariablesList.add(Variable.AC);
//		 allVariablesList.add(Variable.NC);
//		 allVariablesList.add(Variable.AVERAGE_PATH_LENGTH);
//		 allVariablesList.add(Variable.CC);
//		 allVariablesList.add(Variable.DIAMETER);
//		 allVariablesList.add(Variable.ENTROPY);
//		 allVariablesList.add(Variable.DIAMETER_PHYSICAL);
//		 allVariablesList.add(Variable.AVERAGE_PATH_LENGTH_PHYSICAL);
//		 allVariablesList.add(Variable.NF_MEAN);
//		 allVariablesList.add(Variable.ENTROPY_DFT);
//		 allVariablesList.add(Variable.SR);
//		 allVariablesList.add(Variable.POWER_SAT);

		List<OpticalNetDesignVariable> variables = new Vector<>();

		OpticalNetDesignVariable[] allVariables = new OpticalNetDesignVariable[] { OpticalNetDesignVariable.W, OpticalNetDesignVariable.OXC, ENTROPY_DFT, AVERAGE_PATH_LENGTH_PHYSICAL, NC, AVERAGE_PATH_LENGTH,
				AC, DIAMETER_PHYSICAL, CC, ENTROPY, DIAMETER};
		int numRuns = 5;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < allVariables.length; i++) {
			for (int j = i + 1; j < allVariables.length; j++) {
//				for (int k = j + 1; k < allVariables.length; k++) {
					variables.clear();
					variables.addAll(allVariablesList);
					variables.add(allVariables[i]);
					variables.add(allVariables[j]);
//					variables.add(allVariables[k]);
					sb.append("ANÁLISE DE  CR ");
					for (OpticalNetDesignVariable v : variables) {
						sb.append(v.toString()).append(" ");
					}
					sb.append("\n");
					System.out.println(sb.toString());
					double[] erros = treinarConjunto(intervalos, variables, numRuns);
					for (double erro : erros) {
						sb.append(erro).append("\n");
					}

					try {
						FileWriter fw = new FileWriter("model-selection-3.1-var-ext.txt");
						fw.write(sb.toString());
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
//				}
			}
		}

		try {
			FileWriter fw = new FileWriter("model-selection-3.1-var-ext.txt");
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param intervalos
	 * @param variables
	 * @param numRuns
	 * @return
	 */
	private static double[] treinarConjunto(double[][] intervalos, List<OpticalNetDesignVariable> variables, int numRuns) {
		int numInputs = variables.size()+1;

		RedeNeural rn = new RedeNeural(numInputs, 1, 2 * numInputs + 1, false);
		Dataset ds = new Dataset("C:\\doutorado\\experimentos\\nfsnet_model2\\fev14_lc", intervalos, rn, NUM_PATTERNS_CLASS,
				variables);
		ds.setValidation(false);
		ds.populate();

		List<PadraoTreinamento> tr = ds.getPadroesTreinamento();
		List<PadraoTreinamento> t = ds.getPadroesTeste();

		String dirBase = "results/modelSelection/";
		double sumErro = 0;
		double[] erros = new double[numRuns];
		String strRede;
		for (int i = 0; i < numRuns; i++) {
			rn = new RedeNeural(numInputs, 1, 2 * numInputs + 1, false);
			sumErro = 0;
			strRede = dirBase + "mlp" + "_" + numInputs + "inputs_" + "_" + i + "_" + (2 * numInputs + 1) + ".txt";
			rn.treinar(tr, t, 20000, 2e-7, 0.02, 0.0, strRede);
			for (PadraoTreinamento pt : t) {
				sumErro += rn.calcularErroPadrao(false, pt);
			}
			erros[i] = sumErro / t.size();
		}
		return erros;
	}
}
