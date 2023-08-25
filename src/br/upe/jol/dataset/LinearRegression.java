/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: LinearRegression.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	15/11/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import br.cns.util.Statistics;
import br.grna.PadraoTreinamento;
import br.grna.Problem;
import br.grna.levenberg_marquardt.LevenbergMarquardt;
import br.grna.levenberg_marquardt.RetornoIteracaoLM;
import br.upe.jol.dataset.experiments.DatasetCorrelationExperiment;

/**
 * 
 * @author Danilo
 * @since 15/11/2013
 */
public class LinearRegression implements Problem, Serializable {
	private static final long serialVersionUID = 1L;

	private transient LevenbergMarquardt lm;

	private double[] pesos;

	private double pbMedTreinamento;

	private double sqTotal = 0;

	private transient List<PadraoTreinamento> padroesTreinamento;

	public LinearRegression() {
		pesos = new double[5];
		lm = new LevenbergMarquardt(this, pesos.length, 0.0001);
	}

	public void carregar(String path) {
		try {
			FileInputStream fis = new FileInputStream(new File(path));
			ObjectInputStream ois = new ObjectInputStream(fis);

			LinearRegression rede = (LinearRegression) ois.readObject();
			this.pesos = rede.pesos;
			this.pbMedTreinamento = rede.pbMedTreinamento;
			this.sqTotal = rede.sqTotal;

			ois.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		double[][] intervalos = new double[][] { { 0.0, 0.01 }, { 0.01, 0.10 }, { 0.10, 0.20 }, { 0.20, 0.30 },
				{ 0.30, 0.40 }, { 0.40, 0.50 }, { 0.5, 0.60 }, { 0.6, 0.70 }, { 0.70, 0.80 }, { 0.80, 0.90 },
				{ 0.90, 1.00 } };

		DatasetCorrelationExperiment ds = new DatasetCorrelationExperiment(
				"C:\\doutorado\\experimentos\\nfsnet_model2", intervalos, null, 1980);

		ds.populate();

		List<PadraoTreinamento> padroesTreinamento = ds.getPadroesTreinamento();

		LinearRegression lr = new LinearRegression();
		for (int i = 0; i < 30; i++) {
			System.out.println("==================================================");
			System.out.println("EXECUÇÃO " + i);
			System.out.println("==================================================");
			lr.treinar(padroesTreinamento, ds.getPadroesValidacao(), 500000, 1e-4, 1, "results/linearRegression/04-04variables/lnr" + i + ".txt");
		}
		showSummary("results/linearRegression/04-04variables/lnr", ds);
	}
	
	private static void showSummary(String sufixo, DatasetCorrelationExperiment dataset) {
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
		s.append(nf.format(stats.getMean(0))).append("\t");
		s.append(nf.format(stats.getStandardDeviation(0))).append("\t");
		s.append(nf.format(stats.getMean(1))).append("\t").append(nf.format(stats.getStandardDeviation(1)));
		s.append("\n");
		
		content.append(s);
		
		System.out.println(s.toString());
		try {
			FileWriter fw = new FileWriter(sufixo + "summary16var.txt");
			fw.write(content.toString());
			fw.close();
		} catch (IOException e) {
		}
	}

	public void treinar(List<PadraoTreinamento> padroesTreinamento, List<PadraoTreinamento> padroesTeste,
			int iteracoes, double erro, double taxaAprendizado, String rede) {
		this.padroesTreinamento = new Vector<>();
		lm.setNumPtosAmostrados(padroesTreinamento.size());
		for (int i = 0; i < pesos.length; i++) {
			pesos[i] = Math.random();
		}
		double sqres = 0;

		double r2 = 0;
		double yValidacao = 0;
		List<Integer> ordem = new Vector<>();
		List<Integer> indexes = new Vector<>();
		pbMedTreinamento = 0;
		for (int i = 0; i < padroesTreinamento.size(); i++) {
			indexes.add(i);
			pbMedTreinamento += padroesTreinamento.get(i).getSaida()[0];
		}
		pbMedTreinamento /= padroesTreinamento.size();
		int index = 0;
		sqTotal = 0;
		while (ordem.size() < padroesTreinamento.size()) {
			index = (int) (Math.round(Math.random() * (indexes.size() - 1)));
			ordem.add(indexes.get(index));
			this.padroesTreinamento.add(padroesTreinamento.get(indexes.get(index)));
			sqTotal += Math.pow(padroesTreinamento.get(indexes.get(index)).getSaida()[0] - pbMedTreinamento, 2);
		}
		RetornoIteracaoLM ret = new RetornoIteracaoLM(pesos, 1, 2);
		for (int i = 0; i < iteracoes; i++) {
			ret = lm.getNextValues(pesos, 1.e-10, 1.e-10, ret.getAmortecimento(), i, ret.getV());
			pesos = ret.getX();
			sqres = 0;
			for (PadraoTreinamento pt : padroesTreinamento) {
				yValidacao = obterSaida(pt.getEntrada(), pesos);
				sqres += (pt.getSaida()[0] - yValidacao) * (pt.getSaida()[0] - yValidacao);
			}
			r2 = 1 - sqres / sqTotal;

			if (sqres / padroesTreinamento.size() < erro || (i > 0 && ret.isConvergiu())) {
				System.out.printf("R2 da iteração %d: %.10f \nPesos: ", i, r2);
				for (double d : pesos) {
					System.out.printf("%.6f ", d);
				}
				System.out.println();
				salvarRede(rede);
				break;
			}
		}
	}

	public void salvarRede(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(new File(path));
			ObjectOutputStream dos = new ObjectOutputStream(fos);

			dos.writeObject(this);

			dos.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double obterSaida(double[] entrada, double[] pesos) {
		double pbCalc = 0;

		for (int i = 0; i < entrada.length; i++) {
			pbCalc += entrada[i] * pesos[i];
		}
		return pbCalc;
	}

	public double obterSaida(double[] entrada) {
		double pbCalc = 0;

		for (int i = 0; i < entrada.length; i++) {
			pbCalc += entrada[i] * pesos[i];
		}
		return pbCalc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.grna.Problem#compute(double[])
	 */
	@Override
	public double[] compute(double[] x) {
		double[] e = new double[padroesTreinamento.size()];
		double yValidacao;

		for (int i = 0; i < padroesTreinamento.size(); i++) {
			yValidacao = obterSaida(padroesTreinamento.get(i).getEntrada(), x);
			e[i] = padroesTreinamento.get(i).getSaida()[0] - yValidacao;
		}
		return e;
	}

	/**
	 * @return o valor do atributo pbMedTreinamento
	 */
	public double getPbMedTreinamento() {
		return pbMedTreinamento;
	}

	/**
	 * Altera o valor do atributo pbMedTreinamento
	 * @param pbMedTreinamento O valor para setar em pbMedTreinamento
	 */
	public void setPbMedTreinamento(double pbMedTreinamento) {
		this.pbMedTreinamento = pbMedTreinamento;
	}

	/**
	 * @return o valor do atributo sqTotal
	 */
	public double getSqTotal() {
		return sqTotal;
	}

	/**
	 * Altera o valor do atributo sqTotal
	 * @param sqTotal O valor para setar em sqTotal
	 */
	public void setSqTotal(double sqTotal) {
		this.sqTotal = sqTotal;
	}
}
