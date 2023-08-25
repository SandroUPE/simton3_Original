/**
 * 
 */
package br.upe.jol.metaheuristics.nsgaii;

import br.cns.model.GmlData;
import br.cns.persistence.GmlDao;
import br.upe.jol.base.Algorithm;
import br.upe.jol.base.DistanceCalculator;
import br.upe.jol.base.Problem;
import br.upe.jol.base.RankUtil;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.comparators.CrowdingComparator;
import br.upe.jol.configuration.GeneralMOOParametersTO;
import br.upe.jol.operators.crossover.Crossover;
import br.upe.jol.operators.crossover.OnePointCrossover;
import br.upe.jol.operators.mutation.Mutation;
import br.upe.jol.operators.mutation.UniformMutation;

/**
 * @author Danilo
 * 
 */
public class NSGAII extends Algorithm<Double> {
	private static final long serialVersionUID = 1L;

	private int maxIterations = 100;

	private int iteration = 0;

	private int populationSize = 30;

	private SolutionSet<Double> population;

	private double mutationProbability = 0.06;

	private double crossoverProbability = 0.9;

	private GmlData network = new GmlData();

	private GmlDao dao = new GmlDao();

	private int idSequence = 0;
	
	private int line = 0;

	@Override
	public String toString() {
		return "NSGA II";
	}

	public NSGAII() {
		super();
	}

	public NSGAII(int populationSize, int maxIterations, Problem<Double> problem) {
		this.populationSize = populationSize;
		this.maxIterations = maxIterations;
		this.problem = problem;
	}

	@Override
	public SolutionSet<Double> execute() {
		this.initializePopulation();
		return this.execute(this.population, 1);
	}

	@Override
	public SolutionSet<Double> execute(SolutionSet<Double> ss, int lastGeneration) {
		iteration = lastGeneration;
		population = ss;
		SolutionSet<Double> offsprings = new SolutionSet<Double>();
		SolutionSet<Double> union = new SolutionSet<Double>();
		int frontIndex = 0;
		Crossover<Double> crossover = new OnePointCrossover(this.crossoverProbability);
		Mutation<Double> mutation = new UniformMutation(this.mutationProbability);
		DistanceCalculator distance = DistanceCalculator.getInstance();
		initializePopulation();
		RankUtil<Double> ranking = RankUtil.getDoubleInstance();
		SolutionSet<Double>[] rankedSolutions;
		network.getInformations().put("GeoLocation", "ZDT1");
		network.getInformations().put("Network", "NSGAII " + iteration);

		while (nextIteration()) {
			// System.out.println(iteration);
			offsprings.getSolutionsList().clear();
			generateOffspringPopulation(offsprings, crossover, mutation, iteration);

			union.getSolutionsList().clear();
			union.getSolutionsList().addAll(population.getSolutionsList());
			union.getSolutionsList().addAll(offsprings.getSolutionsList());

			rankedSolutions = ranking.getRankedSolutions(union);
			population.getSolutionsList().clear();
			frontIndex = 0;
			SolutionSet<Double> front = null;
			while (population.size() < populationSize / 2) {
				front = rankedSolutions[frontIndex];
				distance.crowdingDistanceAssignment(front, problem.getNumberOfObjectives());
				front.sort(new CrowdingComparator());
				for (Solution<Double> solution : front.getSolutionsList()) {
					population.getSolutionsList().add(solution);
					if (population.size() >= populationSize / 2) {
						break;
					}
				}
				frontIndex++;
			}
			updateObserver();
			iteration++;
			network.getInformations().put("Network", "NSGAII " + iteration);
			dao.save(network, "C:\\Temp\\it-" + iteration + ".gml");
		}
		return population;
	}

	private void generateOffspringPopulation(SolutionSet<Double> offsprings, Crossover<Double> crossover,
			Mutation<Double> mutation, int iteration) {
		Solution<Double> offspring1;
		Solution<Double> offspring2;
		for (int i = 0; i < populationSize / 2; i++) {
			offspring1 = population.getRandom();
			offspring2 = population.getRandom();

			int idparent1 = offspring1.getId();
			int idparent2 = offspring2.getId();

			Solution<Double>[] vector = (Solution<Double>[]) crossover.execute(offspring1, offspring2);

			offspring1 = vector[0];

			offspring1.setId(idSequence);
			addNode(line);
			network.addEdge(idparent1, idSequence, "crossover");
			network.addEdge(idparent2, idSequence, "crossover");
			idSequence++; 

			offspring2 = vector[1];

			offspring2.setId(idSequence);
			addNode(line);
			network.addEdge(idparent1, idSequence, "crossover");
			network.addEdge(idparent2, idSequence, "crossover");
			idSequence++;

			mutation.execute(offspring1);

			network.addEdge(idSequence - 1, idSequence, "mutation");

			mutation.execute(offspring2);

			offspring2.setId(idSequence);
			network.addEdge(idSequence - 2, idSequence, "mutation");
			
			if (iteration % 500 == 0) {
				problem.evaluate(offspring1, true);
				problem.evaluate(offspring2, true);
			} else {
				problem.evaluate(offspring1, false);
				problem.evaluate(offspring2, false);
			}

			offsprings.add(offspring1);
			offsprings.add(offspring2);
		}
		line++;
	}

	private void initializePopulation() {
		Double[] variables = null;
		line = 0;
		network.addNode(idSequence, idSequence + "", idSequence + "", (idSequence + populationSize/2) % (populationSize), line, 0);
		idSequence++;
		line++;
		Solution<Double> solution = null;
		this.population = new SolutionSet<Double>(populationSize);
		for (int i = 0; i < populationSize; i++) {
			variables = new Double[problem.getNumberOfVariables()];
			for (int j = 0; j < problem.getNumberOfVariables(); j++) {
				variables[j] = Math.random() * problem.getUpperLimit(j) + problem.getLowerLimit(j);
			}
			solution = new Solution<Double>(problem, variables);
			population.add(solution);

			solution.setId(idSequence);
			addNode(line);
			network.addEdge(0, idSequence, "initialize");
			idSequence++;
		}
		line++;
		if (observer != null) {
			observer.setSolutionSet(population);
		}
	}

	/**
	 * 
	 */
	private void addNode(double line) {
		network.addNode(idSequence, idSequence + "", idSequence + "", (idSequence-1) % (populationSize), line*5, 0);
	}

	private synchronized void updateObserver() {
		if (observer != null) {
			observer.setSolutionSet(population);
			observer.setIteration(iteration);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
	}

	private boolean nextIteration() {
		return iteration <= maxIterations;
	}

	@Override
	public void setParameters(GeneralMOOParametersTO parameters) {
		this.mutationProbability = parameters.getMutationProbability();
		this.crossoverProbability = parameters.getCrossoverProbability();
		this.populationSize = parameters.getPopulationSize();
	}
}
