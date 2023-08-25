package br.upe.jol.problems.simton;

import java.util.Vector;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.problems.simon.entity.NetworkProfile;

public class SolutionONTD extends Solution<Integer> {
	private static final long serialVersionUID = 1L;
	public static final int NUMBER_OF_AMPLIFIER_LABELS = 10; // 10 Labels plus
	// the zero
	public static final int NUMBER_OF_NODES = 14;
	long nAcessos;
	int numberOfNodes_ppr; // number of nodes to be interconnected by the
	// network topology
	double networkBlockingProbability_ppr; // the network blocking probability
	// of the network represented by the
	// individual
	double networkCost_ppr; // the capital cost of the network represented by
	// the individual
	Vector<Integer> networkRepresentation_ppr; // vector represetqtion of the
	// network adjacency matrix
	Vector<Vector<Double>> nodePositions_ppr; // the position x and y of each
	// node in the network
	Vector<Vector<Double>> amplifierCostsAndTypes_ppr; // the cost and types of
	// amplifiers
	// coluns = labels
	// line 1 = cost
	// line 2 = Saturation power values
	// line 3 = noise figure values
	Vector<Vector<Double>> switchCostsAndTypes_ppr; // cost and switchs types
	// coluns = labels
	// line 1 = costs
	// line 2 = switch isolation factor
	// void repairNetwork_mpr(void);
	static long callsOfObjectiveFunctions_ppr;
	Vector<Integer> fixedPositions_ppr; // determines fixed positions i.e. 1
	// means fixed possitions
	// 0 means a free possition
	NetworkProfile network = new NetworkProfile();

	public SolutionONTD(Problem<Integer> problem, Integer[] variables) {
		this.problem = problem;
		numberOfObjectives = problem.getNumberOfObjectives();
		objective = new double[numberOfObjectives];

		fitness = 0.0;
		crowdingDistance = 0.0;

		this.variable = variables.clone();
		network = new NetworkProfile();
	}

	public SolutionONTD(Problem<Integer> problem, Integer[] variables, int id) {
		this.problem = problem;
		numberOfObjectives = problem.getNumberOfObjectives();
		objective = new double[numberOfObjectives];

		fitness = 0.0;
		crowdingDistance = 0.0;

		this.variable = variables.clone();
		network = new NetworkProfile();
		setId(id);
	}

	public SolutionONTD() {
		numberOfNodes_ppr = NUMBER_OF_NODES;
		network = new NetworkProfile();
	}
	
	@Override
	public SolutionONTD clone() {
		SolutionONTD clone = new SolutionONTD();
		clone.setProblem(this.problem);
		clone.setCrowdingDistance(this.crowdingDistance);
		clone.variable = this.variable.clone();
		clone.setFitness(this.fitness);
		clone.objective = this.objective.clone();
		clone.numberOfObjectives = this.numberOfObjectives;
		
		return clone;
	}

	int indiceVetor_mpr(int j, int i) {
		return (j + (numberOfNodes_ppr - 1) * i - i * (i + 1) / 2);
	}

	int readNetworkRepresentation_mpu(int position_par) {
		return networkRepresentation_ppr.get(position_par);
	}

	int sizeNetworkRepresentation_mpu() {
		return networkRepresentation_ppr.size();
	}

	void setFixedPositions_mpu(Vector<Integer> fixedPositions_par) {
		fixedPositions_ppr = fixedPositions_par;
	}

	void clone(SolutionONTD rightSideOperator_par) {

		numberOfNodes_ppr = rightSideOperator_par.numberOfNodes_ppr;
		networkBlockingProbability_ppr = rightSideOperator_par.networkBlockingProbability_ppr;
		networkCost_ppr = rightSideOperator_par.networkCost_ppr;
		networkRepresentation_ppr = rightSideOperator_par.networkRepresentation_ppr;
		nodePositions_ppr = rightSideOperator_par.nodePositions_ppr;
		amplifierCostsAndTypes_ppr = rightSideOperator_par.amplifierCostsAndTypes_ppr;
		fixedPositions_ppr = rightSideOperator_par.fixedPositions_ppr;
		network = rightSideOperator_par.network;
	}

	public boolean equals(SolutionONTD solution) {
		if (networkRepresentation_ppr == solution.networkRepresentation_ppr)
			return true;
		return false;
	}

	void generateNSFnetNetwork_mpr() {
		int vectorSize_loc = ((numberOfNodes_ppr * numberOfNodes_ppr - numberOfNodes_ppr) / 2) + 2;

		// fills up the network representation concerning to the links between
		// the nodes
		// i= (j-i)+(i-1)*N-i*(i-1)/2 - 1;
		// (ver artigo. ultimo termo (-1) devido a indexaï¿½ï¿½o de vetores em
		// C)
		Vector<Integer> linksExistentes_loc = new Vector<Integer>();
		int tempCount_loc = 0;

		linksExistentes_loc.add(0);
		linksExistentes_loc.add(1);
		linksExistentes_loc.add(2);
		linksExistentes_loc.add(13);
		linksExistentes_loc.add(18);

		linksExistentes_loc.add(27);
		linksExistentes_loc.add(36);
		linksExistentes_loc.add(42);
		linksExistentes_loc.add(46);
		linksExistentes_loc.add(47);

		linksExistentes_loc.add(58);
		linksExistentes_loc.add(62);
		linksExistentes_loc.add(63);
		linksExistentes_loc.add(70);
		linksExistentes_loc.add(76);

		linksExistentes_loc.add(78);
		linksExistentes_loc.add(79);
		linksExistentes_loc.add(85);
		linksExistentes_loc.add(86);
		linksExistentes_loc.add(89);

		linksExistentes_loc.add(90);

		for (int i = 0; (i < (vectorSize_loc - 2)); i++) {
			if (i == linksExistentes_loc.get(tempCount_loc)) {
				networkRepresentation_ppr.add(4);
				tempCount_loc++;
			} else {
				networkRepresentation_ppr.add(0);
				// if (fixedPositions_ppr[i]) // this is a fixed position
				// networkRepresentation_ppr[i] = fixedPositions_ppr[i];
			}
		}

		// fills up the network representation concerning to the switch type
		networkRepresentation_ppr.add(3);

		// fills up the network representation concerning to the number of
		// wavelenghts
		int temp_loc = 40;
		networkRepresentation_ppr.add(temp_loc);

	}

	/**
	 * Método acessor para obter o valor do atributo network.
	 * 
	 * @return O valor de network
	 */
	public NetworkProfile getNetwork() {
		return network;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo network.
	 * 
	 * @param network
	 *            O novo valor de network
	 */
	public void setNetwork(NetworkProfile network) {
		this.network = network;
	}

}
