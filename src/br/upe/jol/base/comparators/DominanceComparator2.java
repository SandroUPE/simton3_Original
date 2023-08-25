package br.upe.jol.base.comparators;

import java.util.Comparator;

import br.upe.jol.base.Solution;


public class DominanceComparator2<T> implements Comparator<Solution<T>> {

	@Override
	public int compare(Solution<T> arg0, Solution<T> arg1) {
		if(arg0.dominates(arg1))
			return 1;
		else if(arg1.dominates(arg0))
			return -1;
		
		return 0;
	}
	

}
