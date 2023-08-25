package br.upe.jol.metaheuristics.dwga;

import java.util.Comparator;

import br.upe.jol.base.SolutionSet;

public class InverseHVGroup<T> implements Comparator<SolutionSet<Double>> {

	public int compare(SolutionSet<Double> solution1, SolutionSet<Double> solution2) {
		if (solution1 == null) {
			return 1;
		} else if (solution2 == null) {
			return -1;
		}

		double distance1 = 1/(1+solution1.getDifHVSolutions() + solution1.getDifAngleSolutions());
		double distance2 = 1/(1+solution2.getDifHVSolutions() + solution2.getDifAngleSolutions());
		if (distance1 < distance2) {
			return -1;
		} else if (distance1 > distance2) {
			return 1;
		}

		return 0;
	}
}
