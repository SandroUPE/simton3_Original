/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: ExemploArtigoEconomia.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	30/05/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset.experiments;

import java.util.List;
import java.util.Vector;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import br.cns.util.Statistics;

/**
 * 
 * @author Danilo
 * @since 30/05/2014
 */
public class ExemploArtigoEconomia {

	/**
	 * Construtor da classe.
	 */
	public ExemploArtigoEconomia() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		double[][] dadosOriginais = new double[][]{
			{1980, 108.6, 4.8, 13.7, 6.7, 10.0, 12.3, 9.9},
			{1981, 99.6, 5.0, 15.7, 2.9, 18.6, 11.4, 9.6},
			{1982, 103.0, 5.9, 13.8, 4.2, 12.5, 23.9, 11.0},
			{1983, 91.9, 25.0, 11.7, 5.1, 8.5, 21.4, 9.2},
			{1984, 88.6, 25.8, 9.9, 3.7, 6.1, 22.1, 8.2},
			{1985, 107.9, 21.8, 13.8, 10.7, 8.8, 32.3, 10.9},
			{1986, 120.3, 23.3, 14.4, 35.2, 18.3, 40.5, 11.8},
			{1987, 109.8, 21.0, 14.8, 10.0, 21.7, 40.0, 12.6},
			{1988, 105.0, 25.8, 18.9, 38.3, 18.9, 37.2, 14.4},
			{1989, 93.4, 32.4, 28.7, 89.8, 9.0, 36.0, 21.3},
			{1990, 103.2, 114.2, 77.6, 35.8, 13.5, 40.8, 94.1},
			{1991, 81.0, 97.4, 59.2, 6.1, 15.4, 34.8, 85.1},
			{1992, 85.9, 97.7, 55.6, 27.5, 12.5, 36.6, 83.2},
			{1993, 92.2, 111.6, 61.4, 35.0, 17.4, 42.8, 64.5},
			{1994, 111.3, 124.9, 73.1, 39.4, 15.3, 68.0, 52.3},
			{1995, 119.8, 143.3, 95.0, 42.0, 12.0, 54.2, 81.6},
			{1996, 120.8, 178.9, 92.3, 44.0, 13.0, 57.9, 93.6},
			{1997, 122.6, 194.1, 93.1, 44.0, 15.8, 97.1, 61.4},
			{1998, 136.6, 188.7, 96.5, 62.0, 16.7, 75.4, 107.7},
			{1999, 135.0, 205.6, 93.2, 82.0, 12.6, 75.7, 105.1},
			{2000, 125.0, 222.0, 92.5, 61.7, 16.0, 81.9, 102.8},
			{2001, 131.9, 232.0, 94.2, 76.0, 21.0, 86.1, 107.7},
			{2002, 136.8, 245.5, 95.2, 70.0, 12.8, 93.3, 111.0},
			{2003, 124.0, 243.4, 85.0, 70.7, 6.9, 86.3, 116.8}};
		Statistics s = new Statistics();
		int nExplic = dadosOriginais[0].length - 2;
		List<List<Double>> vars = new Vector<>();
		for (int i = 0; i < nExplic; i++) {
			vars.add(new Vector<Double>());
		}
		double[][] observations = new double[dadosOriginais.length][nExplic];
		for (int k = 0; k < dadosOriginais.length; k++) {
			for (int l = 1; l < dadosOriginais[0].length-1; l++) {
				observations[k][l-1] = dadosOriginais[k][l];
				vars.get(l-1).add(observations[k][l-1]);
			}
		}
		for (int i = 0; i < nExplic; i++) {
			s.addRandomVariableValues(vars.get(i));
		}
		for (int k = 0; k < dadosOriginais.length; k++) {
			for (int l = 0; l < nExplic; l++) {
				observations[k][l] = (observations[k][l] - s.getMean(l))/s.getVariance(l);
			}
		}
		
		double[][] correlationMatrix = new double[nExplic][nExplic];

		System.out.printf("\nCorrelação total \n");
		for (int i = 0; i < nExplic; i++) {
			for (int j = 0; j < nExplic; j++) {
				System.out.printf("%2.2f%s", 
						s.getCorrelationCoefficient(i, j), " & ");
				correlationMatrix[i][j] = s.getCorrelationCoefficient(i, j);
			}
			System.out.println("\\\\");
		}
		
		double[] realEigenvalues = null;
		RealMatrix rm = new Array2DRowRealMatrix(correlationMatrix);
		System.out.println("Autovalores: ");
		try {
			EigenDecomposition solver = new EigenDecomposition(rm, 0);
			realEigenvalues = solver.getRealEigenvalues();
			for (int i = 0; i < realEigenvalues.length; i++) {
				System.out.printf("%.2f ", realEigenvalues[i]);
			}
			System.out.println("\n% Var: ");
			for (int i = 0; i < realEigenvalues.length; i++) {
				System.out.printf("%.2f ", realEigenvalues[i]/nExplic);
			}
			
			
			System.out.println("\nAutovetores:");
			double[][] ev = new double[nExplic][nExplic];
			for (int i = 0; i < nExplic; i++) {
				RealVector rv = solver.getEigenvector(i);
				for (int j = 0; j < nExplic; j++) {
					System.out.printf("%.2f ", rv.getEntry(j));
					ev[j][i] = rv.getEntry(j);
				}
				System.out.println();
			}
			
			System.out.println("\nAutovetores norm.:");
			for (int i = 0; i < nExplic; i++) {
				for (int j = 0; j < nExplic; j++) {
					System.out.printf("%.2f ", -1 * Math.sqrt(realEigenvalues[j]) * (ev[i][j] / 1.0));
				}
				System.out.println();
			}
			
			System.out.println("\nVariância: ");
			for (int i = 0; i < realEigenvalues.length; i++) {
				System.out.printf("%.2f ", s.getVariance(i));
			}
			System.out.println();
		} catch (Exception e) {
			System.out.println("Falha ao calcular autovalores da matriz...");
			e.printStackTrace();
		}
		
	}

}
