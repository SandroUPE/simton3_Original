package br.upe.jol.base.comparators;

import java.util.Comparator;

import br.upe.jol.base.Solution;

public class ObjectiveSolutionComparator<T> implements Comparator<Solution<T>> {
	/*TODO: ObjectiveSolutionComparator só leva em consideração 2 objetivos.*/
	
	private int nObj;

	public ObjectiveSolutionComparator(int nObj) {
		this.nObj = nObj;
	} 

	public int compare(Solution<T> solution1, Solution<T> solution2) {
		if (solution1 == null){
			return 1;
		}else if (solution2 == null){
			return -1;
		}
		double objetive1 = solution1.getObjective(this.nObj);
		double objetive2 =  solution2.getObjective(this.nObj);
		
		if (objetive1 < objetive2) {
			return -1;
		} else if (objetive1 > objetive2) {
			return 1;
		} else {
			return 0;
		}
	} 
} 
