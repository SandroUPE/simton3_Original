package br.upe.jol.metaheuristics.dwga;

import java.util.Comparator;

import br.upe.jol.base.Solution;

public class InverseHVComparator<T> implements Comparator<Solution<Double>> {

	public int compare(Solution<Double> solution1, Solution<Double> solution2) {
		if (solution1 == null) {
			return 1;
		} else if (solution2 == null) {
			return -1;
		}

		double distance1 = 1/(1+((HVCASolution)solution1).getHvContributionIndividual() + ((HVCASolution)solution1).getVarAngleGroup());
		double distance2 = 1/(1+((HVCASolution)solution2).getHvContributionIndividual() + ((HVCASolution)solution2).getVarAngleGroup());
		if (distance1 < distance2) {
			return -1;
		} else if (distance1 > distance2) {
			return 1;
		}

		return 0;
	}
}
