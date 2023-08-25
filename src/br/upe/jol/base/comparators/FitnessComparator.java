package br.upe.jol.base.comparators;

import java.util.Comparator;

import br.upe.jol.base.Solution;

public class  FitnessComparator<T> implements Comparator<Solution<T>> {

	@Override
	public int compare(Solution<T> o1, Solution<T> o2) {
		
		double fitness1 = o1.getFitness();
		double fitness2 = o2.getFitness();
		
		if(fitness1 > fitness2)
			return 1;
		else if(fitness1 < fitness2)
			return -1;
		
		return 0;
	}

}
