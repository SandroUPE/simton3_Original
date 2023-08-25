package br.upe.jol.base.comparators;

import java.util.Comparator;

import br.upe.jol.base.Solution;

public class AngleComparator<T> implements Comparator<Solution<T>> {

	public int compare(Solution<T> solution1, Solution<T> solution2) {
		if (solution1 == null) {
			return 1;
		} else if (solution2 == null) {
			return -1;
		}

		double distance1 = solution1.getAngle();
		double distance2 = solution2.getAngle();
		if (distance1 > distance2) {
			return -1;
		} else if (distance1 < distance2) {
			return 1;
		}

		return 0;
	}
}
