package br.upe.jol.base;

import java.util.List;
import java.util.Vector;

public class SchemeFrequency {
	protected List<String> listValues = new Vector<String>();
	
	protected double frequency;
	
	/**
	 * Método acessor para obter o valor do atributo listValues.
	 *
	 * @return Retorna o atributo listValues.
	 */
	public List<String> getListValues() {
		return listValues;
	}

	/**
	 * Método acessor para modificar o valor do atributo listValues.
	 *
	 * @param listValues O valor de listValues para setar.
	 */
	public void setListValues(List<String> listValues) {
		this.listValues = listValues;
	}

	/**
	 * Método acessor para obter o valor do atributo frequency.
	 *
	 * @return Retorna o atributo frequency.
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * Método acessor para modificar o valor do atributo frequency.
	 *
	 * @param frequency O valor de frequency para setar.
	 */
	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
}
