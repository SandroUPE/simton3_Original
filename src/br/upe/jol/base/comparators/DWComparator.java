package br.upe.jol.base.comparators;

import java.util.Comparator;

import br.upe.jol.base.Solution;

public class DWComparator<T> implements Comparator<Solution<T>> {
	private double percentIterations;

	public int compare(Solution<T> solution1, Solution<T> solution2) {
		if (solution1 == null) {
			return 1;
		} else if (solution2 == null) {
			return -1;
		}
//		double distance1 = (2 - percentIterations) * solution1.getwDistance() + (3 * percentIterations) * solution1.getCrowdingDistance();
//		double distance2 = (2 - percentIterations) * solution2.getwDistance() + (3 * percentIterations) * solution2.getCrowdingDistance();
		double distance1 = (2 - percentIterations) * solution1.getwDistance() + (3 * percentIterations) * solution1.getCrowdingDistance();
		double distance2 = (2 - percentIterations) * solution2.getwDistance() + (3 * percentIterations) * solution2.getCrowdingDistance();
//		double distance1 = (2 - percentIterations) * solution1.getwDistance() + (5 * percentIterations) * solution1.getCrowdingDistance() + (5 * percentIterations) * solution1.getdAngle();
//		double distance2 = (2 - percentIterations) * solution2.getwDistance() + (5 * percentIterations) * solution2.getCrowdingDistance() + (5 * percentIterations) * solution2.getdAngle();
//		double distance1 = solution1.getdAngle();
//		double distance2 = solution2.getdAngle();
//		System.out.println(solution1.getwDistance() + " - " + solution1.getCrowdingDistance() + " - "
//				+ solution1.getdAngle());
		if (distance1 > distance2) {
			return -1;
		} else if (distance1 < distance2) {
			return 1;
		}

		return 0;
	}

	/**
	 * Método acessor para obter o valor do atributo percentIterations.
	 *
	 * @return Retorna o atributo percentIterations.
	 */
	public double getPercentIterations() {
		return percentIterations;
	}

	/**
	 * Método acessor para modificar o valor do atributo percentIterations.
	 *
	 * @param percentIterations O valor de percentIterations para setar.
	 */
	public void setPercentIterations(double percentIterations) {
		this.percentIterations = percentIterations;
	}
}
