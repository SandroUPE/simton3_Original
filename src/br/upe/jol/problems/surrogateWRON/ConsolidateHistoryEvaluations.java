/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ConsolidateHistoryEvaluations.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	25/08/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.problems.surrogateWRON;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;

/**
 * 
 * @author Danilo
 * @since 25/08/2014
 */
public class ConsolidateHistoryEvaluations {

	/**
	 * Construtor da classe.
	 */
	public ConsolidateHistoryEvaluations() {
	}

	public static void main(String[] args) {
		String basePath = "C:/Users/Danilo/workspace_phd/simton3/";
		String path = null;
		String linha = null;
		String[] conteudoLinha = null;
		int[] qtdePorTipo = new int[5];
		double[] somaTempoPorTipo = new double[5];
		double somaTempoTotal = 0;
		double totalAvaliacoes = 0;
		for (int i = 1; i < 26; i++) {
			path = basePath + "history" + i * 10000 + ".txt";
			try {
				FileReader fr = new FileReader(path);
				LineNumberReader lnr = new LineNumberReader(fr);

				linha = lnr.readLine();
				while (linha != null) {
					conteudoLinha = linha.split(";");

					for (int j = 0; j < qtdePorTipo.length; j++) {
						if (Integer.valueOf(conteudoLinha[2].trim()) == j) {
							qtdePorTipo[j]++;
							totalAvaliacoes++;
							somaTempoPorTipo[j] += Integer.valueOf(conteudoLinha[5].trim());
							somaTempoTotal += Integer.valueOf(conteudoLinha[5].trim());
							break;
						}
					}

					linha = lnr.readLine();
				}

				lnr.close();
				fr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.printf("O tempo total de planejamento foi %.0f minutos. \n", somaTempoTotal / 60000);
		System.out.printf("O total de avaliações foi de %d. \n\n", (int) totalAvaliacoes);
		for (int j = 0; j < qtdePorTipo.length; j++) {
			System.out.println("AVALIAÇÕES DO TIPO " + j);
			System.out
					.printf("Quantidade de avaliações = %d.\nTempo total = %.0f minutos.\nPercentual do tempo gasto sobre o total = %.2f %%\nPercentual da quantidade de avaliações sobre o total = %.2f %%.\nTempo médio por avaliação = %d ms.\n",
							qtdePorTipo[j], somaTempoPorTipo[j] / 60000, 100.0 * somaTempoPorTipo[j] / somaTempoTotal,
							100.0 * qtdePorTipo[j] / totalAvaliacoes,
							(int) (somaTempoPorTipo[j] / (qtdePorTipo[j])));
			System.out.println();
		}

	}

}
