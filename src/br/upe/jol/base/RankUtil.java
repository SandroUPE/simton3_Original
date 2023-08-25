package br.upe.jol.base;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import br.upe.jol.base.comparators.ConstraintViolationComparator;
import br.upe.jol.base.comparators.DominanceComparator;

/**
 * Esta classe foi adaptada a partir da classe Rank do framework JMetal.
 * 
 * @author Danilo Araújo
 */
public class RankUtil<T> {
	private static final RankUtil<Double> doubleInstance = new RankUtil<Double>();
	private static final RankUtil<Integer> integerInstance = new RankUtil<Integer>();

	private static Comparator dominance = new DominanceComparator();

	private static Comparator constraint = new ConstraintViolationComparator();
	
	private  RankUtil(){
	}
	
	public static RankUtil<Double> getDoubleInstance(){
		return doubleInstance;
	}
	
	public static RankUtil<Integer> getIntegerInstance(){
		return integerInstance;
	}

	public SolutionSet<T>[] getRankedSolutions(SolutionSet<T> aSolutionSet) {
		SolutionSet<T>[] ranking;
		SolutionSet<T> solutionSet = aSolutionSet;

		// dominateMe[i] contem o numero de solucoes dominando i
		int[] dominateMe = new int[solutionSet.size()];

		// iDominate[k] contem a lista de solucoes dominadas por k
		List<Integer>[] iDominate = new List[solutionSet.size()];

		// front[i] contem a lista de individuos ao longo do front i
		List<Integer>[] front = new List[solutionSet.size() + 1];

		int flagDominate;

		for (int i = 0; i < front.length; i++){
			front[i] = new LinkedList<Integer>();
		}

		for (int p = 0; p < solutionSet.size(); p++) {
			iDominate[p] = new LinkedList<Integer>();
			dominateMe[p] = 0;
			for (int q = 0; q < solutionSet.size(); q++) {
				flagDominate = constraint.compare(solutionSet.get(p),
						solutionSet.get(q));
				if (flagDominate == 0) {
					flagDominate = dominance.compare(solutionSet.get(p),
							solutionSet.get(q));
				}

				if (flagDominate == -1) {
					iDominate[p].add(new Integer(q));
				} else if (flagDominate == 1) {
					dominateMe[p]++;
				}
			}

			if (dominateMe[p] == 0) {
				front[0].add(new Integer(p));
				solutionSet.get(p).setRank(0);
			}
		}

		int i = 0;
		Iterator<Integer> it1, it2; // Iterators
		while (front[i].size() != 0) {
			i++;
			it1 = front[i - 1].iterator();
			while (it1.hasNext()) {
				it2 = iDominate[it1.next().intValue()].iterator();
				while (it2.hasNext()) {
					int index = it2.next().intValue();
					dominateMe[index]--;
					if (dominateMe[index] == 0) {
						front[i].add(new Integer(index));
						solutionSet.get(index).setRank(i);
					}
				}
			}
		}

		ranking = new SolutionSet[i];
		for (int j = 0; j < i; j++) {
			ranking[j] = new SolutionSet(front[j].size());
			it1 = front[j].iterator();
			while (it1.hasNext()) {
				ranking[j].add(solutionSet.get(it1.next().intValue()));
			}
		}
		return ranking;
	} 
}
