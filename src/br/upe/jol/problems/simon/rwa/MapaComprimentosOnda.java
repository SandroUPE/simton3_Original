/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software � confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: MapaComprimentosOnda.java
 * ****************************************************************************
 * Hist�rico de revis�es
 * Nome				Data		Descri��o
 * ****************************************************************************
 * Danilo Ara�jo	10/04/2015		Vers�o inicial
 * ****************************************************************************
 */
package br.upe.jol.problems.simon.rwa;

import java.util.HashMap;
import java.util.Map;

import br.cns.MetricHelper;
import br.cns.TMetric;

/**
 * 
 * @author Danilo Araujo
 * @since 10/04/2015
 */
public class MapaComprimentosOnda {
	private Map<Integer, MapaComprimentoOndaEntry> map = new HashMap<Integer, MapaComprimentoOndaEntry>();

	/**
	 * Construtor da classe.
	 */
	public MapaComprimentosOnda(int numMaxWavelengths, int numNodes) {
		map = new HashMap<Integer, MapaComprimentoOndaEntry>();
		for (int i = 0; i <= numMaxWavelengths; i++) {
			Integer[][] disp = new Integer[numNodes][numNodes];
			for (int j = 0; j < numNodes; j++) {
				disp[j][j] = 0;
				for (int k = j + 1; k < numNodes; k++) {
					disp[j][k] = 1;
					disp[k][j] = 1;
				}
			}
			map.put(i, new MapaComprimentoOndaEntry(disp, 0, 0));
		}
	}
	
	public Integer[][] getAvailableMap(Integer wavelength){
		Integer[][] a = map.get(wavelength).getMatrix();
		Integer[][] clone = new Integer[a.length][a.length];
		for (int i = 0; i < clone.length; i++) {
			for (int j = 0; j < clone.length; j++) {
				clone[i][j] = a[i][j];
			}
		}
		return clone;
	}
	
	public MapaComprimentoOndaEntry getEntry(Integer wavelength){
		return map.get(wavelength);
	}

	/**
	 * Registra que o comprimento de onda est� em uso do n� i para o n� j.
	 * 
	 * @param wavelength
	 *            Comprimento de onda.
	 * @param posi
	 *            Posi��o inicial
	 * @param posj
	 *            Posi��o final
	 */
	public void alloc(int wavelength, int posi, int posj) {
		map.get(wavelength).getMatrix()[posi][posj] = 0;
	}

	/**
	 * Registra que o comprimento de onda est� livre do n� i para o n� j.
	 * 
	 * @param wavelength
	 *            Comprimento de onda.
	 * @param posi
	 *            Posi��o inicial
	 * @param posj
	 *            Posi��o final
	 */
	public void free(int wavelength, int posi, int posj) {
		map.get(wavelength).getMatrix()[posi][posj] = 1;
		map.get(wavelength).setNotInitialized(true);
	}

	public double getMetricValue(int wavelength, TMetric metric) {
		Integer[][] matrix = map.get(wavelength).getMatrix();
		double value = 0;

		value = MetricHelper.getInstance().calculate(metric, matrix);

		return value;
	}
	
}
