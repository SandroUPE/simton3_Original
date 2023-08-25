/**
 * 
 */
package br.upe.jol.operators.selection;

import java.util.Comparator;

import br.upe.jol.base.Operator;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;

/**
 * @author Danilo
 *
 */
public abstract class Selection<T> extends Operator<T> {
	private static final long serialVersionUID = 1L;
	
	protected Comparator<Solution<T>> comparator;

	public Comparator<Solution<T>> getComparator() {
		return comparator;
	}

	public abstract Object execute(SolutionSet<T> object);

	public void setComparator(Comparator<Solution<T>> comparator) {
		this.comparator = comparator;
	}

}
