/**
 * 
 */
package br.upe.jol.base;

/**
 * .
 * 
 * @author Danilo Araújo
 */
public abstract class Metric<T> {
	public abstract double getValue(SolutionSet<T> solutionSet);
}
