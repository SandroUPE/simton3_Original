/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: WsDataset.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	09/12/2013		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.dataset;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ufpe.networkdb.ws.NetRepServicesProxy;
import ufpe.networkdb.ws.Network;
import ufpe.networkdb.ws.NetworkAttribute;
import br.cns.util.Statistics;
import br.grna.PadraoTreinamento;
import br.grna.PbComparator;
import br.grna.bp.RedeNeural;

/**
 * 
 * @author Danilo
 * @since 09/12/2013
 */
public class WsDataset extends Dataset {

	/**
	 * Construtor da classe.
	 * 
	 * @param datasetPath
	 * @param intervals
	 * @param redeNeural
	 * @param samples
	 * @param variables
	 */
	public WsDataset(String datasetPath, double[][] intervals, RedeNeural redeNeural, int samples,
			List<OpticalNetDesignVariable> variables) {
		super(datasetPath, intervals, redeNeural, samples, variables);
	}

	public void populate() {
		List<PadraoTreinamento> padroes = new Vector<>();
		int[] samplesCounter = new int[intervals.length];
		double[] metricValues = new double[variables.size()];
		double pb = 0;
		double ac = 0;
		double diameter = 0;
		double cost = 0;
		Set<String> redesProcessadas = new HashSet<>();
		String hash = "";
		int repetidos = 0;
		int distintos = 0;

		for (int classe = 0; classe < intervals.length; classe++) {
			mapPadroesTeste.put(classe, new Vector<PadraoTreinamento>());
			mapPadroesTreinamento.put(classe, new Vector<PadraoTreinamento>());
			mapPadroesValidacao.put(classe, new Vector<PadraoTreinamento>());
		}
		NetRepServicesProxy proxy = new NetRepServicesProxy();
		Network[] networks = null;
		String[] am = null;
		double v = 0;
		List<Double> lAmps = new Vector<>();
		double meanNf = 0;
		double sdNf = 0;
		Statistics stats = new Statistics();
		for (double[] interval : intervals) {
			try {
				networks = proxy.listAll(interval[0], interval[1], 600);
			} catch (RemoteException e) {
			}
			System.out.printf("Obteve os dados de %.5f - %.5f\n", interval[0], interval[1]);
			for (Network network : networks) {
				hash = network.getHash();
				lAmps.clear();
				am = hash.split(" ");
				for (int i = 0; i < am.length - 2; i++) {
					v = Double.parseDouble(am[i]);
					if (v != 0){
						lAmps.add(v);
					}
				}
				stats = new Statistics();
				stats.addRandomVariableValues(lAmps);
				meanNf = stats.getMean(0);
				sdNf = stats.getStandardDeviation(0);
				metricValues = new double[variables.size()+2];
				for (NetworkAttribute att : network.getAttributes()) {
					for (int i = 0; i < variables.size(); i++) {
						if (att.getName().equals(variables.get(i).name())) {
							metricValues[i] = att.getValue();
						}
						if (att.getName().equals("BP")) {
							pb = att.getValue();
						} else if (att.getName().equals(OpticalNetDesignVariable.AC.name())) {
							ac = att.getValue();
						} else if (att.getName().equals(OpticalNetDesignVariable.DIAMETER.name())) {
							diameter = att.getValue();
						} else if (att.getName().equals("COST")) {
							cost = att.getValue();
						}
					}
				}
				metricValues[metricValues.length - 2] = meanNf;
				metricValues[metricValues.length - 1] = sdNf;

				if (!redesProcessadas.contains(hash)) {
					distintos++;
					// remover outliers
					if (ac > 0 && diameter > 0 && diameter < (Integer.MAX_VALUE - 1) && cost < 20141 && pb < 1) {
						PadraoTreinamento pt = new PadraoTreinamento(metricValues, new double[] { pb });
						padroes.add(pt);
					}
					redesProcessadas.add(hash);
				} else {
					repetidos++;
				}
			}
		}

		System.out.println("Distintos = " + distintos);
		System.out.println("Repetidos = " + repetidos);
		for (int i = 0; i < samplesCounter.length; i++) {
			System.out.println(i + " - " + samplesCounter[i]);
		}
		// normalizar
		minValues = new double[padroes.get(0).getEntrada().length];
		maxValues = new double[padroes.get(0).getEntrada().length];
		Arrays.fill(minValues, Double.MAX_VALUE);
		Arrays.fill(maxValues, Double.MIN_VALUE);
		for (PadraoTreinamento padrao : padroes) {
			for (int i = 0; i < padrao.getEntrada().length; i++) {
				if (padrao.getEntrada()[i] < minValues[i]) {
					minValues[i] = padrao.getEntrada()[i];
				}
				if (padrao.getEntrada()[i] > maxValues[i]) {
					maxValues[i] = padrao.getEntrada()[i];
				}
			}
		}
		if (redeNeural != null) {
			redeNeural.setMinValues(minValues);
			redeNeural.setMaxValues(maxValues);
		}
		for (PadraoTreinamento padrao : padroes) {
			for (int i = 0; i < padrao.getEntrada().length; i++) {
				if (minValues == maxValues) {
					padrao.setValorEntrada(i, 1);
				} else {
					padrao.setValorEntrada(i, (padrao.getEntrada()[i] - minValues[i]) / (maxValues[i] - minValues[i]));
				}
			}
		}
		Collections.sort(padroes, new PbComparator());
		padroesTreinamento = new Vector<>();
		padroesTeste = new Vector<>();
		padroesValidacao = new Vector<>();
		for (int i = 0; i < padroes.size() - padroes.size() % 4; i += 4) {
			padroesTreinamento.add(padroes.get(i));
			if (validation) {
				padroesValidacao.add(padroes.get(i + 1));
			} else {
				padroesTreinamento.add(padroes.get(i + 1));
			}
			padroesTeste.add(padroes.get(i + 2));
			padroesTreinamento.add(padroes.get(i + 3));
			for (int classe = 0; classe < intervals.length; classe++) {
				if (padroes.get(i).getSaida()[0] > intervals[classe][0]
						&& padroes.get(i).getSaida()[0] <= intervals[classe][1]) {
					mapPadroesTreinamento.get(classe).add(padroes.get(i));
				}
				if (padroes.get(i + 1).getSaida()[0] > intervals[classe][0]
						&& padroes.get(i + 1).getSaida()[0] <= intervals[classe][1]) {
					if (validation) {
						mapPadroesValidacao.get(classe).add(padroes.get(i + 1));
					} else {
						mapPadroesTreinamento.get(classe).add(padroes.get(i + 1));
					}
				}
				if (padroes.get(i + 2).getSaida()[0] > intervals[classe][0]
						&& padroes.get(i + 2).getSaida()[0] <= intervals[classe][1]) {
					mapPadroesTeste.get(classe).add(padroes.get(i + 2));
				}
				if (padroes.get(i + 3).getSaida()[0] > intervals[classe][0]
						&& padroes.get(i + 3).getSaida()[0] <= intervals[classe][1]) {
					mapPadroesTreinamento.get(classe).add(padroes.get(i + 3));
				}
			}
		}
	}
}
