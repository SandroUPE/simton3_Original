package br.upe.jol.metrics;

import br.upe.jol.base.Metric;
import br.upe.jol.base.Problem;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Util;

/**
 * Esta classe foi adaptada a partir da classe Hypervolume do framework JMetal.
 * 
 * @author Danilo Ara�jo
 */
public class Hypervolume<T> extends Metric<T> {
	/*
	 * returns true if 'point1' dominates 'points2' with respect to the to the
	 * first 'noObjectives' objectives
	 */
	private boolean dominates(double point1[], double point2[], int noObjectives) {
		int i;
		int betterInAnyObjective;

		betterInAnyObjective = 0;
		for (i = 0; i < noObjectives && point1[i] >= point2[i]; i++)
			if (point1[i] > point2[i])
				betterInAnyObjective = 1;

		return ((i >= noObjectives) && (betterInAnyObjective > 0));
	}

	/**
	 * Inverte o valor das posi��es especificadas por par�metro
	 * 
	 * @param front
	 *            Pareto front
	 * @param i
	 *            �ndice da coordenada i
	 * @param j
	 *            �ndice da coordenada j
	 */
	private void swap(double[][] front, int i, int j) {
		double[] temp;

		temp = front[i];
		front[i] = front[j];
		front[j] = temp;
	}

	/*
	 * all nondominated points regarding the first 'noObjectives' dimensions are
	 * collected; the points referenced by 'front[0..noPoints-1]' are
	 * considered; 'front' is resorted, such that 'front[0..n-1]' contains the
	 * nondominated points; n is returned
	 */
	private int filterNondominatedSet(double[][] front, int noPoints, int noObjectives) {
		int i, j;
		int n;

		n = noPoints;
		i = 0;
		while (i < n) {
			j = i + 1;
			while (j < n) {
				if (dominates(front[i], front[j], noObjectives)) {
					/* remove point 'j' */
					n--;
					swap(front, j, n);
				} else if (dominates(front[j], front[i], noObjectives)) {
					/*
					 * remove point 'i'; ensure that the point copied to index
					 * 'i' is considered in the next outer loop (thus, decrement
					 * i)
					 */
					n--;
					swap(front, i, n);
					i--;
					break;
				} else {
					j++;
				}
			}
			i++;
		}
		return n;
	}

	protected double getHypervolumeRecursivo(double[][] front, int noPoints, int noObjectives) {
		int n;
		double volume, distance;

		volume = 0;
		distance = 0;
		n = noPoints;
		while (n > 0) {
			int noNondominatedPoints;
			double tempVolume, tempDistance;

			noNondominatedPoints = filterNondominatedSet(front, n, noObjectives - 1);
			tempVolume = 0;
			if (noObjectives < 3) {
				if (noNondominatedPoints < 1)
					System.err.println("run-time error");

				tempVolume = front[0][0];
			} else {
				tempVolume = getHypervolumeRecursivo(front, noNondominatedPoints, noObjectives - 1);
			}

			tempDistance = surfaceUnchangedTo(front, n, noObjectives - 1);
			volume += tempVolume * (tempDistance - distance);
			distance = tempDistance;
			n = reduceNondominatedSet(front, n, noObjectives - 1, distance);
		}
		return volume;
	}

	/*
	 * remove all points which have a value <= 'threshold' regarding the
	 * dimension 'objective'; the points referenced by 'front[0..noPoints-1]'
	 * are considered; 'front' is resorted, such that 'front[0..n-1]' contains
	 * the remaining points; 'n' is returned
	 */
	private int reduceNondominatedSet(double[][] front, int noPoints, int objective, double threshold) {
		int n;
		int i;

		n = noPoints;
		for (i = 0; i < n; i++) {
			if (front[i][objective] <= threshold) {
				n--;
				swap(front, i, n);
			}
		}

		return n;
	}

	/*
	 * calculate next value regarding dimension 'objective'; consider points
	 * referenced in 'front[0..noPoints-1]'
	 */
	private double surfaceUnchangedTo(double[][] front, int noPoints, int objective) {
		int i;
		double minValue, value;

		if (noPoints < 1)
			System.err.println("run-time error");

		minValue = front[0][objective];

		for (i = 1; i < noPoints; i++) {
			value = front[i][objective];

			if (value < minValue)
				minValue = value;
		}
		return minValue;
	}

	@Override
	public double getValue(SolutionSet<T> solutionSet) {
		int nObjectives = solutionSet.getSolutionsList().get(0).numberOfObjectives();
		Problem<T> problem = solutionSet.get(0).getProblem();
		double[] maximumValues;
		double[] minimumValues;

		if (problem.getUpperLimitObjective() != null) {
			maximumValues = problem.getUpperLimitObjective();
			minimumValues = problem.getLowerLimitObjective();
		} else {
			maximumValues = Util.getMaximumValues(solutionSet.writeObjectivesToMatrix(), nObjectives);
			minimumValues = Util.getMinimumValues(solutionSet.writeObjectivesToMatrix(), nObjectives);
		}

		double[][] normalizedFront = Util.getNormalizedFront(solutionSet.writeObjectivesToMatrix(), maximumValues,
				minimumValues);

		double[][] invertedFront = Util.getInvertedFront(normalizedFront);

		return getHypervolumeRecursivo(invertedFront, invertedFront.length, nObjectives) + problem.getHypervolumeDif();
	}

}
