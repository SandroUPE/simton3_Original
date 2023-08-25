package br.upe.jol.base;

import br.upe.jol.base.comparators.ObjectiveSolutionComparator;

public class DWAssignment {
	private static final DWAssignment instance = new DWAssignment();

	private DWAssignment() {
	}

	/**
	 * Seta o valor de crowding distances para todas as soluções do conjunto
	 * <code>SolutionSet</code>.
	 * 
	 * @param solutionSet
	 *            O conjuneto de soluções <code>SolutionSet</code>.
	 * @param nObjs
	 *            Número de objetivos.
	 */
	public void hvAssignment(SolutionSet<Double> solutionSet) {
		int size = solutionSet.size();
		Problem<Double> problem = solutionSet.get(0).getProblem();
		double[] minimumValues;
		double[] maximumValues;

		if (problem.getUpperLimitObjective() != null) {
			maximumValues = problem.getUpperLimitObjective();
			minimumValues = problem.getLowerLimitObjective();
		} else {
			maximumValues = Util.getMaximumValues(solutionSet.writeObjectivesToMatrix(), problem
					.getNumberOfObjectives());
			minimumValues = Util.getMinimumValues(solutionSet.writeObjectivesToMatrix(), problem
					.getNumberOfObjectives());
		}

		double[] var = new double[problem.getNumberOfObjectives()];
		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			var[i] = maximumValues[i] - minimumValues[i];
		}
		if (size == 0)
			return;

		if (size == 1) {
			setDistance(solutionSet.get(0), maximumValues, var);
			return;
		}

		if (size == 2) {
			setDistance(solutionSet.get(0), maximumValues, var);
			setDistance(solutionSet.get(1), maximumValues, var);
			return;
		}
		double volume = 0;
		for (int i = 0; i < size; i++) {
			volume += setDistance(solutionSet.get(i), maximumValues, var);
		}
		double objetiveMaxn;
		double objetiveMinn;
		double distance;
		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			// Ordena a população pelo objetivo n
			solutionSet.sort(new ObjectiveSolutionComparator(i));
			objetiveMinn = solutionSet.get(0).getObjective(i);
			objetiveMaxn = solutionSet.get(solutionSet.size() - 1).getObjective(i);
			// Seta crowding distance
			solutionSet.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
			solutionSet.get(size - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);

			for (int j = 1; j < size - 1; j++) {
				if (i == 0) {
					solutionSet.get(j - 1).setdAngle(solutionSet.get(j - 1).getwDistance() / volume);
				}
				distance = solutionSet.get(j + 1).getObjective(i) - solutionSet.get(j - 1).getObjective(i);
				distance = distance / (objetiveMaxn - objetiveMinn);
				distance += solutionSet.get(j).getCrowdingDistance();
				solutionSet.get(j).setCrowdingDistance(distance);
			}
		}
	}

	private double setDistance(Solution<Double> solution, double[] wPoint, double[] var) {
		double dX = (wPoint[0] - solution.getObjective(0)) / var[0];
		double dY = (wPoint[1] - solution.getObjective(1)) / var[1];
		// double distance = Math.sqrt((dX * dX) + (dY * dY));
		// double distance = Math.sqrt((dX * dX) + (dY * dY)) + 1.0
		// / (solution.getObjective(0) * solution.getObjective(1) + 2);
		double distance = (dX * dY - solution.getObjective(0) * solution.getObjective(1));
		// solution.setAngle(Math.atan(dX / dY));
		solution.setwDistance(distance);
		solution.setCrowdingDistance(0.0);
		return distance;
	}

	private void setIDistance(Solution<Integer> solution, double[] wPoint, double[] var) {
		double dX = (wPoint[0] - solution.getObjective(0)) / var[0];
		double dY = (wPoint[1] - solution.getObjective(1)) / var[1];
		double distance = (dX * dY - solution.getObjective(0) * solution.getObjective(1));
		solution.setwDistance(distance);
		solution.setCrowdingDistance(0.0);
	}

	public void iHvAssignment(SolutionSet<Integer> solutionSet) {
		int size = solutionSet.size();
		Problem<Integer> problem = solutionSet.get(0).getProblem();
		double[] minimumValues;
		double[] maximumValues;

		if (problem.getUpperLimitObjective() != null) {
			maximumValues = problem.getUpperLimitObjective();
			minimumValues = problem.getLowerLimitObjective();
		} else {
			maximumValues = Util.getMaximumValues(solutionSet.writeObjectivesToMatrix(), problem
					.getNumberOfObjectives());
			minimumValues = Util.getMinimumValues(solutionSet.writeObjectivesToMatrix(), problem
					.getNumberOfObjectives());
		}

		double[] var = new double[problem.getNumberOfObjectives()];
		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			var[i] = maximumValues[i] - minimumValues[i];
		}
		if (size == 0)
			return;

		if (size == 1) {
			setIDistance(solutionSet.get(0), maximumValues, var);
			return;
		}

		if (size == 2) {
			setIDistance(solutionSet.get(0), maximumValues, var);
			setIDistance(solutionSet.get(1), maximumValues, var);
			return;
		}
		for (int i = 0; i < size; i++) {
			setIDistance(solutionSet.get(i), maximumValues, var);
		}
		double objetiveMaxn;
		double objetiveMinn;
		double distance;
		for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
			// Ordena a população pelo objetivo n
			solutionSet.sort(new ObjectiveSolutionComparator(i));
			objetiveMinn = solutionSet.get(0).getObjective(i);
			objetiveMaxn = solutionSet.get(solutionSet.size() - 1).getObjective(i);
			// Seta crowding distance
			solutionSet.get(0).setCrowdingDistance(Double.POSITIVE_INFINITY);
			solutionSet.get(size - 1).setCrowdingDistance(Double.POSITIVE_INFINITY);

			for (int j = 1; j < size - 1; j++) {
				distance = solutionSet.get(j + 1).getObjective(i) - solutionSet.get(j - 1).getObjective(i);
				distance = distance / (objetiveMaxn - objetiveMinn);
				distance += solutionSet.get(j).getCrowdingDistance();
				solutionSet.get(j).setCrowdingDistance(distance);
			}
		}
	}

	public static DWAssignment getInstance() {
		return instance;
	}
}
