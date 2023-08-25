package br.upe.jol.operators.initializers;

import br.upe.jol.base.Problem;
import br.upe.jol.base.SolutionSet;

public abstract class Initializer<T> {
	private static final long serialVersionUID = 1L;

	public abstract SolutionSet<T> execute(Problem<T> problem, int size) ;

	public String getOpID() {
		return getClass().toString();
	}

}
