package br.upe.jol.base.comparators;

import java.util.Comparator;

import br.upe.jol.base.Solution;

public class CrowdingComparator<T> implements Comparator<Solution<T>> {

	private final Comparator<Solution<T>> comparator = new RankComparator<T>();

	public int compare(Solution<T> solution1, Solution<T> solution2) {
		if (solution1 == null) {
			return 1;
		} else if (solution2 == null) {
			return -1;
		}

		int resultCompareRank = comparator.compare(solution1, solution2);
		if (resultCompareRank != 0) {
			return resultCompareRank;
		}

		double distance1 = solution1.getCrowdingDistance();
		double distance2 = solution2.getCrowdingDistance();
		if (distance1 > distance2) {
			return -1;
		} else if (distance1 < distance2) {
			return 1;
		}

		return 0;
	}
}
