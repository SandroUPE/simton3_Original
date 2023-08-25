package br.upe.jol.metaheuristics.dwga;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

import br.upe.jol.base.SolutionSet;

public class BinaryTournamentGroup<T> {
	private static final long serialVersionUID = 1L;
	
	protected Comparator<SolutionSet<T>> comparator;

	public Comparator<SolutionSet<T>> getComparator() {
		return comparator;
	}

	public void setComparator(Comparator<SolutionSet<T>> comparator) {
		this.comparator = comparator;
	}

	public BinaryTournamentGroup(Comparator<SolutionSet<T>> comparator){
		setComparator(comparator);
	}
	
	public Object execute(SolutionSet<T>... object) {
		SolutionSet<T> solution1, solution2;
		
		solution1 = object[0];
		solution2 = object[1];

		int flag = comparator.compare(solution1, solution2);
		if (flag == -1)
			return solution1;
		else if (flag == 1)
			return solution2;
		else if (Math.random() < 0.5)
			return solution1;
		else
			return solution2;
	}
	
	public int getRandom(List<SolutionSet<T>> lista, int other) {
		if(lista.size() == 0){
			return 0;
		}else if(lista.size() == 1){
			return 0;
		}
		int i = other;
		do {
			i = new Random().nextInt(lista.size()-1);
		}while (i == other);
		
		return i;
	}
	
	public Object execute(List<SolutionSet<T>> object) {
		SolutionSet<T> solution1, solution2;
		int i = getRandom(object, -1);
		int j = getRandom(object, i);
		solution1 = object.get(i);
		solution2 = object.get(j);

		int flag = comparator.compare(solution1, solution2);
		if (flag == -1)
			return solution1;
		else if (flag == 1)
			return solution2;
		else if (Math.random() < 0.5)
			return solution1;
		else
			return solution2;
	}
	
}
