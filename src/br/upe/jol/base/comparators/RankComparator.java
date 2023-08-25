package br.upe.jol.base.comparators;

import java.util.Comparator;

import br.upe.jol.base.Solution;

public class RankComparator<T> implements Comparator<Solution<T>> {
	public int compare(Solution<T> solution1, Solution<T> solution2) {

		if (solution1 == null) {
			return 1;
		} else if (solution2 == null) {
			return -1;
		}

		if (solution1.getRank() < solution2.getRank()) {
			return -1;
		} else if (solution1.getRank() > solution2.getRank()) {
			return 1;
		}

		return 0;
	}
}
