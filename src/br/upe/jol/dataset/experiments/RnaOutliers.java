/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: RnaOutliers.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	04/12/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset.experiments;

import java.util.List;

import br.grna.PadraoTreinamento;
import br.grna.bp.RedeNeural;
import br.upe.jol.dataset.Dataset;

/**
 * 
 * @author Danilo
 * @since 04/12/2013
 */
public class RnaOutliers {
	public static void main(String[] args) {
		RedeNeural redeNeural = new RedeNeural(6, 1, true);
		double[][] intervalos = new double[][] { { 0.0, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 }, { 0.20, 0.30 },
				{ 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 0.80 }, { 0.80, 0.90 },
				{ 0.90, 1.00 } };
		redeNeural
				.carregarRede("C:\\Users\\Danilo\\workspace_phd\\GenericRna\\results\\compl_6nodes\\mlp_6inputs_ncl_main_0_10.txt");
		Dataset ds = new Dataset("C:\\doutorado\\experimentos\\nfsnet_model2", intervalos, redeNeural, 1980);
		ds.populate();
		
		DatasetOutliers dso = new DatasetOutliers("C:\\doutorado\\experimentos\\nfsnet_model2", intervalos, redeNeural, 1980);
		dso.setRnaRef(redeNeural);
		dso.populate();
		
		RedeNeural rno = new RedeNeural(7, 1, 10, false);
		
		List<PadraoTreinamento> tr = dso.getPadroesTreinamento();
		List<PadraoTreinamento> t = dso.getPadroesTeste();

		String dirBase = "results/compl_6nodes/";
		double sumErro = 0;
		double[] erros = new double[30];
		String strRede;
		for (int i = 0; i < 3; i++) {
			rno = new RedeNeural(7, 1, 10, false);
			sumErro = 0;
			strRede = dirBase + "mlp" + "_outliers" + "_" + i + "_10.txt";
			rno.treinar(tr, t, 200000, 2e-8, 0.02, 0.0, strRede);
			for (PadraoTreinamento pt : t) {
				sumErro += rno.calcularErroPadrao(false, pt);
			}
			erros[i] = sumErro / t.size();
			System.out.println(erros[i]);
		}
	}
}
