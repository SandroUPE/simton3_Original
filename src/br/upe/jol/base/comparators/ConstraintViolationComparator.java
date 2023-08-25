package br.upe.jol.base.comparators;

import java.util.Comparator;

import br.upe.jol.base.Solution;

public class ConstraintViolationComparator<T> implements
		Comparator<Solution<T>> {

	public int compare(Solution<T> o1, Solution<T> o2) {
		double violations1, violations2;
		violations1 = o1.getOverallConstraintViolation();
		violations2 = o2.getOverallConstraintViolation();

		if ((violations1 < 0) && (violations2 < 0)) {
			if (violations1 > violations2) {
				return -1;
			} else if (violations2 > violations1) {
				return 1;
			} else {
				return 0;
			}
		} else if ((violations1 == 0) && (violations2 < 0)) {
			return -1;
		} else if ((violations1 < 0) && (violations2 == 0)) {
			return 1;
		} else {
			return 0;
		}
	}
}
