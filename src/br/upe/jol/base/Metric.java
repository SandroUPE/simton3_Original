/**
 * 
 */
package br.upe.jol.base;

/**
 * .
 * 
 * @author Danilo Ara�jo
 */
public abstract class Metric<T> {
	public abstract double getValue(SolutionSet<T> solutionSet);
}
