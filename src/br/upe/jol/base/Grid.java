/**
 * 
 */
package br.upe.jol.base;

/**
 * Classe adaptada a partir do JMetal.
 * 
 * @author Danilo Ara�jo
 */
public class Grid<T> {

	private int nDivs;

	private int objectives;

	private int[] hypercubes;

	private double[] lowerLimits;

	private double[] upperLimits;

	private double[] divisionSize;

	private int mostPopulated;

	private int[] occupied;

	public Grid(int bisections, int objetives) {
		this.nDivs = bisections;
		this.objectives = objetives;
		lowerLimits = new double[objectives];
		upperLimits = new double[objectives];
		divisionSize = new double[objectives];
		hypercubes = new int[(int) Math.pow(2.0, bisections * objectives)];

		for (int i = 0; i < hypercubes.length; i++) {
			hypercubes[i] = 0;
		}
	}

	private void updateLimits(SolutionSet<T> solutionSet) {
		for (int obj = 0; obj < objectives; obj++) {
			lowerLimits[obj] = Double.MAX_VALUE;
			upperLimits[obj] = Double.MIN_VALUE;
		}

		for (int ind = 0; ind < solutionSet.size(); ind++) {
			Solution<T> tmpIndividual = solutionSet.get(ind);
			for (int obj = 0; obj < objectives; obj++) {
				if (tmpIndividual.getObjective(obj) < lowerLimits[obj]) {
					lowerLimits[obj] = tmpIndividual.getObjective(obj);
				}
				if (tmpIndividual.getObjective(obj) > upperLimits[obj]) {
					upperLimits[obj] = tmpIndividual.getObjective(obj);
				}
			}
		}
	}

	private void addSolutionSet(SolutionSet<T> solutionSet) {
		mostPopulated = 0;
		int location;

		for (int ind = 0; ind < solutionSet.size(); ind++) {
			location = location(solutionSet.get(ind));
			hypercubes[location]++;
			if (hypercubes[location] > hypercubes[mostPopulated])
				mostPopulated = location;
		}

		calculateOccupied();
	}

	public void updateGrid(SolutionSet<T> solutionSet) {
		updateLimits(solutionSet);

		for (int obj = 0; obj < objectives; obj++) {
			divisionSize[obj] = upperLimits[obj] - lowerLimits[obj];
		}

		for (int i = 0; i < hypercubes.length; i++) {
			hypercubes[i] = 0;
		}

		addSolutionSet(solutionSet);
	}

	public void updateGrid(Solution<T> solution, SolutionSet<T> solutionSet) {

		int location = location(solution);
		if (location == -1) {
			updateLimits(solutionSet);

			for (int obj = 0; obj < objectives; obj++) {
				if (solution.getObjective(obj) < lowerLimits[obj])
					lowerLimits[obj] = solution.getObjective(obj);
				if (solution.getObjective(obj) > upperLimits[obj])
					upperLimits[obj] = solution.getObjective(obj);
			}

			for (int obj = 0; obj < objectives; obj++) {
				divisionSize[obj] = upperLimits[obj] - lowerLimits[obj];
			}

			for (int i = 0; i < hypercubes.length; i++) {
				hypercubes[i] = 0;
			}

			addSolutionSet(solutionSet);
		}
	}

	public int location(Solution<T> solution) {
		int[] position = new int[objectives];

		for (int obj = 0; obj < objectives; obj++) {

			if ((solution.getObjective(obj) > upperLimits[obj])
					|| (solution.getObjective(obj) < lowerLimits[obj]))
				return -1;
			else if (solution.getObjective(obj) == lowerLimits[obj])
				position[obj] = 0;
			else if (solution.getObjective(obj) == upperLimits[obj])
				position[obj] = ((int) Math.pow(2.0, nDivs)) - 1;
			else {
				double tmpSize = divisionSize[obj];
				double value = solution.getObjective(obj);
				double account = lowerLimits[obj];
				int ranges = (int) Math.pow(2.0, nDivs);
				for (int b = 0; b < nDivs; b++) {
					tmpSize /= 2.0;
					ranges /= 2;
					if (value > (account + tmpSize)) {
						position[obj] += ranges;
						account += tmpSize;
					}
				}
			}
		}

		int location = 0;
		for (int obj = 0; obj < objectives; obj++) {
			location += position[obj] * Math.pow(2.0, obj * nDivs);
		}
		return location;
	}

	public int getMostPopulated() {
		return mostPopulated;
	}
	
	public int getLessPopulated(){
		int less = Integer.MAX_VALUE;
		int indexLess = 0;
		for(int i = 0; i<this.occupiedHypercubes(); i++){
			if(hypercubes[occupied[i]] < less){
				less = hypercubes[occupied[i]];
				indexLess = occupied[i];
			}
		}
		
		return indexLess;
	}

	public int getLocationDensity(int location) {
		return hypercubes[location];
	}

	public void removeSolution(int location) {
		hypercubes[location]--;

		if (location == mostPopulated){
			for (int i = 0; i < hypercubes.length; i++){
				if (hypercubes[i] > hypercubes[mostPopulated]){
					mostPopulated = i;
				}
			}
		}

		if (hypercubes[location] == 0)
			this.calculateOccupied();
	}

	public void addSolution(int location) {
		hypercubes[location]++;

		if (hypercubes[location] > hypercubes[mostPopulated]){
			mostPopulated = location;
		}

		if (hypercubes[location] == 1){
			this.calculateOccupied();
		}
	}

	public int getBisections() {
		return nDivs;
	}

	public int rouletteWheel() {
		double inverseSum = 0.0;
		for (int i = 0; i < hypercubes.length; i++) {
			if (hypercubes[i] > 0) {
				inverseSum += 1.0 / (double) hypercubes[i];
			}
		}

		double random = PseudoRandom.randDouble(0.0, inverseSum);
		int hypercube = 0;
		double accumulatedSum = 0.0;
		while (hypercube < hypercubes.length) {
			if (hypercubes[hypercube] > 0) {
				accumulatedSum += 1.0 / (double) hypercubes[hypercube];
			}

			if (accumulatedSum > random) {
				return hypercube;
			}

			hypercube++;
		}

		return hypercube;
	}

	public int calculateOccupied() {
		int total = 0;
		for (int i = 0; i < hypercubes.length; i++) {
			if (hypercubes[i] > 0) {
				total++;
			}
		}

		occupied = new int[total];
		int base = 0;
		for (int i = 0; i < hypercubes.length; i++) {
			if (hypercubes[i] > 0) {
				occupied[base] = i;
				base++;
			}
		}

		return total;
	}

	public int occupiedHypercubes() {
		return occupied.length;
	}

	public int randomOccupiedHypercube() {
		int rand = PseudoRandom.randInt(0, occupied.length - 1);
		return occupied[rand];
	}
}
