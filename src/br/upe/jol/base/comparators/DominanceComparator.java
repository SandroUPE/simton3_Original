package br.upe.jol.base.comparators;

import java.util.Comparator;

import br.upe.jol.base.Solution;

public class DominanceComparator<T> implements Comparator<Solution<T>> {

	private final Comparator<Solution<T>> constraintViolationComparator = new ConstraintViolationComparator<T>();

	public int compare(Solution<T> solution1, Solution<T> solution2) {
		if (solution1 == null)
			return 1;
		else if (solution2 == null)
			return -1;

		int dominate1;
		int dominate2;

		dominate1 = 0;
		dominate2 = 0;

		int flag;

		if (solution1.getOverallConstraintViolation() != solution2.getOverallConstraintViolation()
				&& (solution1.getOverallConstraintViolation() < 0) || (solution2.getOverallConstraintViolation() < 0)) {
			return (constraintViolationComparator.compare(solution1, solution2));
		}

		// Número igual de violação de restrições. Aplicar teste de
		// dominância...
		double value1, value2;
		for (int i = 0; i < solution1.numberOfObjectives(); i++) {
			value1 = solution1.getObjective(i);
			value2 = solution2.getObjective(i);
			flag = 0;
//			if (i == 0) {
				if ((solution1.isAccurateEvaluation() && solution2.isAccurateEvaluation())
						|| (!solution1.isAccurateEvaluation() && !solution2.isAccurateEvaluation())) {
					if (value1 < value2) {
						flag = -1;
					} else if (value2 < value1) {
						flag = 1;
					} else {
						flag = 0;
					}
				} else if (solution1.isAccurateEvaluation() && !solution2.isAccurateEvaluation()) {
					if (value1 < value2) {
						flag = -1;
					} else {
						flag = 0;
					}
				} else if (!solution1.isAccurateEvaluation() && solution2.isAccurateEvaluation()) {
					if (value2 < value1) {
						flag = 1;
					} else {
						flag = 0;
					}
				}
//			} else {
//				if (value1 < value2) {
//					flag = -1;
//				} else if (value2 < value1) {
//					flag = 1;
//				} else {
//					flag = 0;
//				}
//			}

			if (flag == -1) {
				dominate1 = 1;
			}

			if (flag == 1) {
				dominate2 = 1;
			}

		}

		if (dominate1 == dominate2) {
			return 0; // Ninguem domina um ao outro
		}
		if (dominate1 == 1) {
			return -1; // solution1 domina
		}
		return 1; // solution2 domina
	}
}
