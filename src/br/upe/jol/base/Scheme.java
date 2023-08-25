/**
 * 
 */
package br.upe.jol.base;

import java.util.Arrays;

/**
 * @author Danilo
 *
 */
public class Scheme implements Comparable<Scheme>{
	private String[] valor;
	
	public static final String STR_CORINGA = "*";
	
	private double fitness;
	
	public Scheme(int n){
		valor = new String[n];
		Arrays.fill(valor, STR_CORINGA);
	}
	
	public void incrementarFitness(int frequencia){
		fitness += frequencia;
	}

	/**
	 * Método acessor para obter o valor de valor
	 *
	 * @return O valor de valor
	 */
	public String[] getValor() {
		return valor;
	}

	/**
	 * Método acessor para modificar o valor de valor
	 *
	 * @param valor O novo valor de valor
	 */
	public void setValor(String[] valor) {
		this.valor = valor;
	}

	/**
	 * Método acessor para obter o valor de fitness
	 *
	 * @return O valor de fitness
	 */
	public double getFitness() {
		return fitness;
	}

	/**
	 * Método acessor para modificar o valor de fitness
	 *
	 * @param fitness O novo valor de fitness
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	@Override
	public int compareTo(Scheme o) {
		if (o.getFitness() > this.getFitness()){
			return 1;
		}else if (o.getFitness() < this.getFitness()){
			return -1;
		}else if (!o.equals(this)){
			return 1;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(valor);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Scheme other = (Scheme) obj;
		if (!Arrays.equals(valor, other.valor))
			return false;
		return true;
	}
}
