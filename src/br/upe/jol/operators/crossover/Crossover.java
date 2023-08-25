/**
 * 
 */
package br.upe.jol.operators.crossover;

import br.upe.jol.base.Operator;

/**
 * @author Danilo
 *
 */
public abstract class Crossover<T> extends Operator<T> {
	private static final long serialVersionUID = 1L;

	protected double crossoverProbability;
	
	public Crossover(double crossoverPropability){
		this.crossoverProbability = crossoverPropability;
	}
}
