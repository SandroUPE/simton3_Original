/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ErrosSimton.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	23/02/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset.experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 
 * @author Danilo
 * @since 23/02/2014
 */
public class ErrosSimton {
	public static void main(String[] args) {
		double[][] intervalos = new double[][] { { 0.0, 0.001 }, { 0.001, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 },
				{ 0.20, 0.30 }, { 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 0.80 },
				{ 0.80, 0.90 }, { 0.90, 1.00 } };

		double[][] erroIntervalos = new double[intervalos.length][1];

		int[] countIntervalos = new int[intervalos.length];

		NumberFormat nf = NumberFormat.getInstance(new Locale("PT", "br"));
		String base = "results/mlp/simton105/evals-simton";
		File file = null;
		FileReader fr = null;
		char[] buf = null;
		String[] valores = null;
		double pbReal = 0;
		double pbSimton = 0;
		double se = 0;
		for (int i = 0; i < 20; i++) {
			file = new File(base + i + ".txt");
			try {
				buf = new char[(int) file.length()];
				fr = new FileReader(file);
				fr.read(buf);
				String content = new String(buf);
				String[] linhas = content.split("\n");
				double mse = 0;
				for (String linha : linhas) {
					valores = linha.split(";");
					pbReal = nf.parse(valores[0]).doubleValue();
					pbSimton = nf.parse(valores[1]).doubleValue();

					se = (pbReal - pbSimton) * (pbReal - pbSimton);
					mse += se;

					for (int j = 0; j < intervalos.length; j++) {
						if (pbReal > intervalos[j][0] && pbReal < intervalos[j][1]) {
							countIntervalos[j]++;
							erroIntervalos[j][0] += se;
						}
					}
				}
				mse /= linhas.length;
				System.out.println(mse);
				fr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println();
		for (int j = 0; j < intervalos.length; j++) {
			erroIntervalos[j][0] /= countIntervalos[j];
			System.out.print(erroIntervalos[j][0] + " ");
		}
		System.out.println();
	}
}
