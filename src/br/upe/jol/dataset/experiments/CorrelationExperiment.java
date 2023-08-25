/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: CorrelationExperiment.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	08/11/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset.experiments;

import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import br.cns.util.Statistics;
import br.grna.PadraoTreinamento;
import br.upe.jol.dataset.Dataset;

/**
 * 
 * @author Danilo
 * @since 08/11/2013
 */
public class CorrelationExperiment {
	private static int NUM_PATTERNS_CLASS = 1250;

	public static void main(String[] args) {
		// computeClasses();
		// computeEx();
		compute();
	}

	private static void compute() {
		double[][] intervalos = new double[][] { { 0.0, 0.001 }, { 0.001, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 },
				{ 0.20, 0.30 }, { 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 0.80 },
				{ 0.80, 0.90 }, { 0.90, 1.00 } };
		NumberFormat formatter = NumberFormat.getInstance();
		Dataset ds = new DatasetCorrelationExperiment("C:\\doutorado\\experimentos\\nfsnet_model2\\dataset_lc",
				intervalos, null, NUM_PATTERNS_CLASS);
		// String[] labels = new String[] { "PB", "W", "OXC", "CC", "D", "DFT",
		// "NF", "AC", "NC", "AD", "APL", "DIAM",
		// "ENT", "DF", "APLF", "SR", "SAT", "SP" };
		// String[] labels = new String[] {"PB", "W", "OXC", "CC", "D", "DFT",
		// "AC", "NC", "AD", "APL", "DIAM",
		// "ENT", "DF", "APLF", "SR", "SP" };
		String[] labels = new String[] { "W", "OXC", "CC", "D", "DFT", "AC", "NC", "AD", "APL", "DIAM", "ENT", "DF",
				"APLF", "SR", "SP" };
		ds.populate();

		// List<Double> pb = new Vector<>();
		List<Double> cc = new Vector<>();
		List<Double> dft = new Vector<>();
		List<Double> nf = new Vector<>();
		List<Double> w = new Vector<>();
		List<Double> oxc = new Vector<>();
		List<Double> d = new Vector<>();
		List<Double> ac = new Vector<>();
		List<Double> nc = new Vector<>();
		List<Double> ad = new Vector<>();
		List<Double> apl = new Vector<>();
		List<Double> diam = new Vector<>();
		List<Double> ent = new Vector<>();
		List<Double> df = new Vector<>();
		List<Double> aplf = new Vector<>();
		List<Double> sr = new Vector<>();
		List<Double> sat = new Vector<>();
		List<Double> sp = new Vector<>();
		// double[][] observations = new
		// double[ds.getPadroesTreinamento().size()][15];
		double[][] observations = new double[15][ds.getPadroesTreinamento().size()];
		int x = 0;
		for (PadraoTreinamento p : ds.getPadroesTreinamento()) {
			// pb.add(p.getSaida()[0]);
			w.add(p.getEntrada()[0]);
			oxc.add(p.getEntrada()[1]);
			cc.add(p.getEntrada()[2]);
			d.add(p.getEntrada()[3]);
			dft.add(p.getEntrada()[4]);
			// nf.add(p.getEntrada()[5]);
			ac.add(p.getEntrada()[5]);
			nc.add(p.getEntrada()[6]);
			ad.add(p.getEntrada()[7]);
			apl.add(p.getEntrada()[8]);
			diam.add(p.getEntrada()[9]);
			ent.add(p.getEntrada()[10]);
			df.add(p.getEntrada()[11]);
			aplf.add(p.getEntrada()[12]);
			sr.add(p.getEntrada()[13]);
			// sat.add(p.getEntrada()[15]);
			sp.add(p.getEntrada()[14]);

			for (int k = 0; k < 15; k++) {
				observations[k][x] = p.getEntrada()[k];
			}

			x++;
		}

		Statistics s = new Statistics();
		// s.addRandomVariableValues(pb);
		s.addRandomVariableValues(w);
		s.addRandomVariableValues(oxc);
		s.addRandomVariableValues(cc);
		s.addRandomVariableValues(d);
		s.addRandomVariableValues(dft);
		// s.addRandomVariableValues(nf);
		s.addRandomVariableValues(ac);
		s.addRandomVariableValues(nc);
		s.addRandomVariableValues(ad);
		s.addRandomVariableValues(apl);
		s.addRandomVariableValues(diam);
		s.addRandomVariableValues(ent);
		s.addRandomVariableValues(df);
		s.addRandomVariableValues(aplf);
		s.addRandomVariableValues(sr);
		// s.addRandomVariableValues(sat);
		s.addRandomVariableValues(sp);

		int l = s.getSize();

		double[][] correlationMatrix = new double[l][l];

		System.out.printf("\nCorrelação total \n");
		for (int i = 0; i < labels.length; i++) {
			System.out.print("& $X_{" + (i+1) + "}$ ");
		}
		System.out.println("\\\\\\hline");
		for (int i = 0; i < l; i++) {
			System.out.print(" $X_{" + (i+1) + "}$ ");
			for (int j = 0; j < l; j++) {
				System.out.printf(" & %2.2f", s.getCorrelationCoefficient(i, j));
				correlationMatrix[i][j] = s.getCorrelationCoefficient(i, j);
			}
			System.out.println("\\\\\\hline");
		}
		
		double[] realEigenvalues = null;
		RealMatrix rm = new Array2DRowRealMatrix(correlationMatrix);
		System.out.println("Autovalores: ");
		double acum = 0;
		try {
			EigenDecomposition solver = new EigenDecomposition(rm, 0);
			realEigenvalues = solver.getRealEigenvalues();
			for (int i = 0; i < realEigenvalues.length; i++) {
				acum += realEigenvalues[i] / l;
				System.out.printf("%d & %.2f & %.2f & %.2f \\\\\\hline\n", (i + 1), realEigenvalues[i], 100
						* realEigenvalues[i] / l, 100 * acum);
			}
			System.out.println("\n% Var: ");
			for (int i = 0; i < realEigenvalues.length; i++) {
				System.out.print(formatter.format(realEigenvalues[i] / 15) + " ");
			}

			System.out.println("\nAutovetores:");
			double[][] ev = new double[l][l];
			for (int i = 0; i < realEigenvalues.length; i++) {
				RealVector rv = solver.getEigenvector(i);
				for (int j = 0; j < realEigenvalues.length; j++) {
					System.out.printf("%.2f ", rv.getEntry(j));
					ev[j][i] = rv.getEntry(j);
				}
				System.out.println();
			}

			System.out.println("\nAutovetores norm.:");
			for (int i = 0; i < l; i++) {
				System.out.print("$X_{" + (i + 1) + "}$");
				for (int j = 0; j < l; j++) {
					System.out.printf(" & %.2f", Math.sqrt(realEigenvalues[j]) * (ev[i][j] / 1.0));
				}
				System.out.println("\\\\\\hline");
			}

		} catch (Exception e) {
			System.out.println("Falha ao calcular autovalores da matriz...");
			e.printStackTrace();
		}

		// System.out.println("\nCorrelação múltipla:");
		// for (int i = 1; i < l; i++) {
		// for (int j = i + 1; j < l; j++) {
		// System.out.printf("%s\t%s\t%.4f\t%.4f\t%.4f\t\n", labels[i],
		// labels[j],
		// s.getCorrelationCoefficient(0, i), s.getCorrelationCoefficient(0, j),
		// s.getCorrelationCoefficient(0, i, j));
		// }
		// }

	}

	/**
	 * 
	 */
	private static void computeClasses() {
		double[][] intervalos = new double[][] { { 0.0, 0.001 }, { 0.001, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 },
				{ 0.20, 0.30 }, { 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 0.80 },
				{ 0.80, 0.90 }, { 0.90, 1.00 } };

		Dataset ds = new Dataset("C:\\doutorado\\experimentos\\nfsnet_model2", intervalos, null, NUM_PATTERNS_CLASS);

		ds.populate();

		for (int c = 0; c < intervalos.length; c++) {
			List<Double> pb = new Vector<>();
			List<Double> cc = new Vector<>();
			List<Double> dft = new Vector<>();
			List<Double> nf = new Vector<>();
			List<Double> w = new Vector<>();
			List<Double> oxc = new Vector<>();
			List<Double> d = new Vector<>();
			List<Double> ac = new Vector<>();
			List<Double> nc = new Vector<>();
			List<Double> ad = new Vector<>();
			List<Double> apl = new Vector<>();
			List<Double> diam = new Vector<>();
			List<Double> ent = new Vector<>();
			List<Double> df = new Vector<>();
			List<Double> aplf = new Vector<>();
			List<Double> pdft = new Vector<>();
			List<Double> sr = new Vector<>();

			for (PadraoTreinamento p : ds.getMapPadroesTreinamento().get(c)) {
				pb.add(p.getSaida()[0]);
				w.add(p.getEntrada()[0]);
				oxc.add(p.getEntrada()[1]);
				cc.add(p.getEntrada()[2]);
				d.add(p.getEntrada()[3]);
				dft.add(p.getEntrada()[4]);
				nf.add(p.getEntrada()[5]);
				ac.add(p.getEntrada()[6]);
				nc.add(p.getEntrada()[7]);
				ad.add(p.getEntrada()[8]);
				apl.add(p.getEntrada()[9]);
				diam.add(p.getEntrada()[10]);
				ent.add(p.getEntrada()[11]);
				df.add(p.getEntrada()[12]);
				aplf.add(p.getEntrada()[13]);
				pdft.add(p.getEntrada()[14]);
				sr.add(p.getEntrada()[15]);
			}

			Statistics s = new Statistics();
			s.addRandomVariableValues(pb);
			s.addRandomVariableValues(w);
			s.addRandomVariableValues(oxc);
			s.addRandomVariableValues(cc);
			s.addRandomVariableValues(d);
			s.addRandomVariableValues(dft);
			s.addRandomVariableValues(nf);
			s.addRandomVariableValues(ac);
			s.addRandomVariableValues(nc);
			s.addRandomVariableValues(ad);
			s.addRandomVariableValues(apl);
			s.addRandomVariableValues(diam);
			s.addRandomVariableValues(ent);
			s.addRandomVariableValues(df);
			s.addRandomVariableValues(aplf);
			s.addRandomVariableValues(pdft);
			s.addRandomVariableValues(sr);

			int l = s.getSize();

			System.out.printf("\nCorrelação para a faixa %.4f - %.4f \n", intervalos[c][0], intervalos[c][1]);
			System.out.println("PB\tW\tOXC\tCC\tD\tDFT\tNF\tAC\tNC\tAD\tAPL\tDIAM\tENT\tDF\tAPLF\tPDFT\tSR ");
			for (int i = 0; i < l; i++) {
				for (int j = 0; j < l; j++) {
					System.out.printf("%s%4.4f\t", s.getCorrelationCoefficient(i, j) >= 0 ? "+" : "",
							s.getCorrelationCoefficient(i, j));
				}
				System.out.println();
			}
		}
	}
}
