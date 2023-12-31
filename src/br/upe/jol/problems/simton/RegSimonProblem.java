/**
 * 
 */
package br.upe.jol.problems.simton;

import static br.upe.jol.problems.simon.rwa.Funcoes.INF;
import static br.upe.jol.problems.simon.rwa.OpticalNetworkSimulatorAbstract.SNR_BLOCK;
import static br.upe.jol.problems.simon.util.SimonUtil.LAMBDA_FIRSTFIT;
import static br.upe.jol.problems.simon.util.SimonUtil.UTILIZAR_DIJ;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.Util;
import br.upe.jol.problems.simon.dbcache.CacheSimulRedeDAO;
import br.upe.jol.problems.simon.dbcache.CacheSimulRedeTO;
import br.upe.jol.problems.simon.entity.BlockUtil;
import br.upe.jol.problems.simon.entity.Link;
import br.upe.jol.problems.simon.entity.NetworkProfile;
import br.upe.jol.problems.simon.entity.Node;
import br.upe.jol.problems.simon.rwa.RegDijkstraSimulator;
import br.upe.jol.problems.simon.rwa.SimpleDijkstraSimulator;

/**
 * .
 * 
 * @author Danilo Ara�jo
 */
public class RegSimonProblem extends Problem<Integer> {
	public static final int NUMBER_OF_AMPLIFIER_LABELS = 10;
	public static final int NUMBER_OF_SWITCH_LABELS = 5;
	public static final int MAX_NUMBER_OF_WAVELENGHTS = 40;
	public static final int MIN_NUMBER_OF_WAVELENGHTS = 4;
	public int MAX_NUMBER_OF_REGENERATORS = 10000;
	public static final int NUMBER_OF_NODES = 14;
	public static final double RAND_MAX = 0x7FFF;
	public int avals = 0;
	public int cacheAvals = 0;
	public int probAvals = 0;
	public double minProbability = 1;
	private static boolean usarCache = false;
	private NetworkProfile profile;
	private SimpleDijkstraSimulator simulator;
	private static double GFIBRA = -0.2; // in dB/km
	private static double GMUX = -3.0; // in dB
	private static double GSWITCH = -3.0; // in dB
	private static int FIBRAS = 1;
	private static double MF = 1.0;
	private boolean ganhodinamico_loc = true;
	private int numberOfNodes;
	private Vector<Vector<Double>> nodePositions = new Vector<Vector<Double>>();
	private Vector<Vector<Double>> amplifierCostsAndTypes = new Vector<Vector<Double>>();
	private Vector<Vector<Double>> switchCostsAndTypes = new Vector<Vector<Double>>();
	private static final long serialVersionUID = 1L;

	public int carga;
	
	private PruningProblem pruning;
	
	public static final NumberFormat format = DecimalFormat.getInstance();
	
	private BlockUtil block = BlockUtil.getInstance();
	
	static {
		format.setMinimumFractionDigits(5);
		format.setMaximumFractionDigits(5);
	}

	public static double[][] NODES_POSITIONS = new double[][] { { -85.692672, 14.728428 }, { -86.51664, -31.619772 },
			{ -69.213312, 34.606656 }, { -51.189012, 0.51498 }, { -29.044872, 3.810852 }, { 4.531824, 46.142208 },
			{ 0, 0 }, { 28.3239, 3.192876 }, { 56.13282, 1.853928 }, { 41.610384, 30.177828 },
			{ 43.464312, -6.282756 }, { 67.97736, -7.003728 }, { 74.15712, 2.677896 }, { 66.741408, 8.033688 } };
	
	private static Integer[] extremeConnectedNetwork = {
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
		   3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
		      3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
		         3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
		            3, 3, 3, 3, 3, 3, 3, 3, 3, 
		               3, 3, 3, 3, 3, 3, 3, 3, 
		                  3, 3, 3, 3, 3, 3, 3, 
		                     3, 3, 3, 3, 3, 3, 
		                        3, 3, 3, 3, 3, 
		                           3, 3, 3, 3, 
		                              3, 3, 3, 
		                                 3, 3, 
		                                    3,
	 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
		                                4, 40 };

	/*//Normalizada para 160/120
	  public static double[][] NODES_POSITIONS = new double[][] { { -113.97125376000001, 19.58880924 }, 
	  { -115.0671312, -42.05429676000001 }, { -92.05370496, 46.02685248 }, { -68.08138596, 0.6849234000000001 }, 
	  { -38.62967976, 5.0684331600000005 }, { 6.027325920000001, 61.36913664 }, { 0.0, 0.0 }, 
	  { 37.670787, 4.2465250800000005 }, { 74.6566506, 2.46572424 }, { 55.341810720000005, 40.136511240000004 }, 
	  { 57.807534960000005, -8.35606548 }, { 90.4098888, -9.31495824 }, { 98.62896960000002, 3.5616016800000003 }, 
	  { 88.76607264000002, 10.68480504 },  };
	*/
	
	/*//Normalizada para 200/120
	  double[][] NODES_POSITIONS = new double[][] { { -142.24983552, 24.44919048 }, { -143.6176224, -52.48882152 }, 
	  { -114.89409792, 57.44704896 }, { -84.97375991999999, 0.8548667999999999 }, { -48.21448752, 6.32601432 }, 
	  { 7.52282784, 76.59606527999999 }, { 0.0, 0.0 }, { 47.01767399999999, 5.30017416 }, { 93.1804812, 3.07752048 }, 
	  { 69.07323744, 50.09519448 }, { 72.15075791999999, -10.429374959999999 }, { 112.8424176, -11.62618848 }, 
	  { 123.1008192, 4.44530736 }, { 110.79073728, 13.33592208 },  };
	*/
	
	public static double[][] AMPLIFIERS_COSTS_AND_LABELS = new double[][] { { 0, 0.75, 1.5, 2.25, 0.5, 1, 1.5, 0.25, 0.5, 0.75 }, // cost
			// index
			{ 0, 13, 16, 19, 13, 16, 19, 13, 16, 19 }, // amplifier saturation
			// power
			{ 0, 5, 5, 5, 7, 7, 7, 9, 9, 9 } // amplifier noise figure
	};

	public static double[][] SWITCHES_COSTS_AND_LABELS = new double[][] { { 0, 0.5, 0.75, 1, 1.5, 2.0 }, // costs
			{ 0, 30, 33, 35, 38, 40 } }; // isolation factor in dB

	double LINK_ANT_COST = 0.4;
	double CALLS = 20000;
	double PRECISION = 0.00001;

	public RegSimonProblem(int numberOfNodes_ppr, int numberOfObjectives, int numerOfRegenerators, int carga){
		super(((numberOfNodes_ppr * numberOfNodes_ppr - numberOfNodes_ppr) / 2) + 2 + numberOfNodes_ppr, numberOfObjectives,
				"Projeto de Redes �pticas");
		this.numberOfNodes = numberOfNodes_ppr;
		
		this.MAX_NUMBER_OF_REGENERATORS = numerOfRegenerators;
		this.carga = carga;
		
		for (int i = 0; i < numberOfVariables - 2; i++) {
			upperLimit[i] = NUMBER_OF_AMPLIFIER_LABELS - 1;
			lowerLimit[i] = 0;
		}
		
		upperLimitObjective = new double[numberOfObjectives];
		lowerLimitObjective = new double[numberOfObjectives];
		
		upperLimitObjective[0] = 1;
		
		if(MAX_NUMBER_OF_REGENERATORS == 0)
			//NFSNET sem regenerador
			upperLimitObjective[1] = 21441.3927;
		else
			//NFSNET com regenerador
			upperLimitObjective[1] = 41825.3927;
		
		lowerLimitObjective[0] = 0;
		lowerLimitObjective[1] = 0;
		
		for(int i=(numberOfNodes*numberOfNodes-numberOfNodes)/2; i<numberOfVariables-2; i++){
			upperLimit[i] = MAX_NUMBER_OF_REGENERATORS;
			lowerLimit[i] = 0;
		}

		upperLimit[numberOfVariables - 1] = MAX_NUMBER_OF_WAVELENGHTS;
		lowerLimit[numberOfVariables - 1] = MIN_NUMBER_OF_WAVELENGHTS;

		upperLimit[numberOfVariables - 2] = NUMBER_OF_SWITCH_LABELS - 1;
		lowerLimit[numberOfVariables - 2] = 0;

		// build up matrixes with zeros
		// initialize matrix nodePositions_ppr with zeros
		for (int i = 0; i < numberOfNodes_ppr; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			for (int j = 0; j < 2; j++) {
				temp_loc.add(0.0);
			}
			nodePositions.add(temp_loc);
		}

		// initialize matrix amplifierCostsAndTypes_ppr with zeros
		for (int i = 0; i < 3; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			for (int j = 0; j < NUMBER_OF_AMPLIFIER_LABELS; j++) {
				temp_loc.add(0.0);
			}
			amplifierCostsAndTypes.add(temp_loc);
		}

		// initialize matrix switchCostsAndTypes_ppr with zeros
		for (int i = 0; i < 2; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			for (int j = 0; j < NUMBER_OF_SWITCH_LABELS + 1; j++) {
				temp_loc.add(0.0);
			}
			switchCostsAndTypes.add(temp_loc);
		}
		// build up matrixes with correct values

		// initialize matrix nodePositions_ppr with the right values
		for (int i = 0; i < nodePositions.size(); i++)
			for (int j = 0; j < nodePositions.get(i).size(); j++)
				nodePositions.get(i).set(j, NODES_POSITIONS[i][j]);

		// initialize matrix amplifierCostsAndTypes_ppr with the right values
		for (int i = 0; i < amplifierCostsAndTypes.size(); i++)
			for (int j = 0; j < amplifierCostsAndTypes.get(i).size(); j++)
				amplifierCostsAndTypes.get(i).set(j, AMPLIFIERS_COSTS_AND_LABELS[i][j]);

		// initialize matrix aswitchCostsAndTypes_ppr with the right values
		for (int i = 0; i < switchCostsAndTypes.size(); i++)
			for (int j = 0; j < switchCostsAndTypes.get(i).size(); j++)
				switchCostsAndTypes.get(i).set(j, SWITCHES_COSTS_AND_LABELS[i][j]);

		Vector<Node> nodes = new Vector<Node>();
		// network nodes
		double GSWITCH = -3.0; // in dB
		double SNR = 40.0; // in dB
		double LPOWER = 0.0; // in DBm
		for (int k = 0; k < numberOfNodes_ppr; k++) {
			Node newNode_loc = new Node(GSWITCH, LPOWER, SNR);
			nodes.add(newNode_loc);
		}

		profile = new NetworkProfile(null, nodes, carga * 0.01, 0.01, CALLS, SNR_BLOCK, true, true, true, true, false,
				true, 40e9, 0.013e-9, 1.0, 0.04e-12 / sqrt(1000), 0.0, 10.0, LAMBDA_FIRSTFIT, UTILIZAR_DIJ, false,
				MAX_NUMBER_OF_WAVELENGHTS);
		
		//fazer a poda
		this.pruning = new PruningProblem(numberOfNodes_ppr, numberOfObjectives,profile);
		
		SolutionONTD solution = new SolutionONTD(this, extremeConnectedNetwork);
		
		//retorna as posi��es impossiveis
		try {
			this.permission = this.pruning.pruning(solution);
		} catch (UnreliableSimulationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		simulator = new RegDijkstraSimulator();
	}

	@Override
	public synchronized void evaluate(Solution<Integer> solution) {
		avals++;
		List<Integer> networkRepresentation_ppr = Arrays.asList(solution.getDecisionVariables());
		long t = System.currentTimeMillis();
		Vector<Vector<Double>> adjacencyMatrixLabels;
				
		adjacencyMatrixLabels = buildAdjacencyMatrixLabels(networkRepresentation_ppr);
		
		Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				adjacencyMatrixLabels);

		if (usarCache) {
			CacheSimulRedeTO cache = new CacheSimulRedeTO((SolutionONTD) solution);

			CacheSimulRedeDAO.getCacheValue(cache);
			if (cache.getQtdeAcessos() == 0) {
				try {
					evaluateNetwokBlockingProbability((SolutionONTD) solution, networkRepresentation_ppr,
							adjacencyMatrixLabels, adjacencyMatrix);
					evaluateNetworkCost((SolutionONTD) solution, networkRepresentation_ppr, adjacencyMatrixLabels,
							adjacencyMatrix);
				} catch (UnreliableSimulationException e) {
					((SolutionONTD) solution).setObjective(0, upperLimitObjective[0] + PRECISION);
					((SolutionONTD) solution).setObjective(1, upperLimitObjective[1] + PRECISION);
				}
				cache.setCusto(((SolutionONTD) solution).getNetwork().getCost());
				cache.setProbBloqueio(((SolutionONTD) solution).getObjective(0));
				cache.setCustoTotal(((SolutionONTD) solution).getObjective(1));
				CacheSimulRedeDAO.salvarCacheSolution(cache);
			} else {
				((SolutionONTD) solution).getNetwork().setCost(cache.getCusto());
				((SolutionONTD) solution).getNetwork().setBp(cache.getBp());
				((SolutionONTD) solution).setObjective(0, cache.getProbBloqueio());
				((SolutionONTD) solution).setObjective(1, cache.getCustoTotal());
				cacheAvals++;
			}

			Util.LOGGER.info("Avalia��o (" + format.format(((SolutionONTD) solution).getObjective(0)) + ", "
					+ format.format(((SolutionONTD) solution).getObjective(1)) + "). Tempo de avalia��o do indiv�duo ("
					+ avals + ") = " + (System.currentTimeMillis() - t) + ". % Uso cache = "
					+ format.format(100.0 * cacheAvals / avals));
		} else {
			try {
				evaluateNetworkCost((SolutionONTD) solution, networkRepresentation_ppr, adjacencyMatrixLabels,
						adjacencyMatrix);
				evaluateNetwokBlockingProbability((SolutionONTD) solution, networkRepresentation_ppr,
						adjacencyMatrixLabels, adjacencyMatrix);
			} catch (UnreliableSimulationException e) {
				//Modificada em 08/07 para adequar os resultados de saida
				((SolutionONTD) solution).setObjective(0, 1e-7);
				//((SolutionONTD) solution).setObjective(1, upperLimitObjective[1] + PRECISION);
			}

			Util.LOGGER.info("Avalia��o (" + format.format(((SolutionONTD) solution).getObjective(0)) + ", "
					+ format.format(((SolutionONTD) solution).getObjective(1)) + "). Tempo de avalia��o do indiv�duo ("
					+ avals + ") = " + (System.currentTimeMillis() - t) + ".");
		}
	}
	
	private void evaluateConnected(Set<Integer> visitedNodes, Set<Integer> allNodes, Vector<Vector<Double>> adjacencyMatrix, int actualNode){
		for (int j = 0; j < numberOfNodes; j++){
			if (adjacencyMatrix.get(actualNode).get(j) != 0 && !visitedNodes.contains(j)) {
				visitedNodes.add(j);
				if (visitedNodes.containsAll(allNodes)){
					return;
				}
				evaluateConnected(visitedNodes, allNodes, adjacencyMatrix, j);
			}
		}
	}
	
	public synchronized void evaluateConstraints(Solution<Integer> solution) {
		Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixLabels(Arrays.asList(solution.getDecisionVariables()));
		Set<Integer> visitedNodes = new HashSet<Integer>();
		Set<Integer> nodes = new HashSet<Integer>();
		int actualNode = (int)(Math.random() * (numberOfNodes));
		visitedNodes.add(actualNode);
		for (int j = 0; j < numberOfNodes; j++){
			nodes.add(j);
		}
		for (int j = 0; j < numberOfNodes; j++){
			if (adjacencyMatrix.get(actualNode).get(j) != 0 && !visitedNodes.contains(j)) {
				visitedNodes.add(j);
				evaluateConnected(visitedNodes, nodes, adjacencyMatrix, actualNode);
			}
		}
		int violations = 0;
		for (int j = 0; j < numberOfNodes; j++){
			if (!visitedNodes.contains(j)) {
				violations--;
			}
		}
		for (int i = 0; i < numberOfNodes; i++){
			nodes.clear();
			for (int j = 0; j < numberOfNodes; j++){
				if (adjacencyMatrix.get(i).get(j) != 0) {
					nodes.add(j);
				}
			}
			if (nodes.size() < 2){
				violations--;
			}
		}
		
		for(int i=0; i<numberOfVariables-2; i++){
			
			if(solution.getDecisionVariables()[i] < lowerLimit[i]){
				solution.getDecisionVariables()[i] = (int)lowerLimit[i];
			}else if(solution.getDecisionVariables()[i] > upperLimit[i]){
				solution.getDecisionVariables()[i] = (int)upperLimit[i];
			}
			
		}
		
		
		System.out.println("Violations = " + violations);
		solution.setNumberOfViolatedConstraint(violations);
		solution.setOverallConstraintViolation(violations);
	}

	public synchronized void evaluateConstraints1(Solution<Integer> solution) {
		List<Integer> networkRepresentation = Arrays.asList(solution.getDecisionVariables());
		// determines if there is any partition in graph
		// stores the actual group in breath first algorithm
		Vector<Integer> actualGroup = new Vector<Integer>();
		// stores the founded graph parts
		Vector<Vector<Integer>> graphParts = new Vector<Vector<Integer>>();
		Queue<Integer> nextNodeToBeVisited = new LinkedList<Integer>();
		Queue<Integer> neighbors = new LinkedList<Integer>();
		Vector<Boolean> markedNodes = new Vector<Boolean>();

		Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixLabels(networkRepresentation);
		for (int i = 0; i < numberOfNodes; i++) {
			markedNodes.add(false);
		}
		int p = 0;
		while (p != -1) {
			actualGroup.clear();
			actualGroup.add(p);
			markedNodes.set(p, true);
			nextNodeToBeVisited.add(p);

			
			while (!(nextNodeToBeVisited.isEmpty())) {
				int n = nextNodeToBeVisited.element();
				nextNodeToBeVisited.remove();
				// find the neighbors of node n
				for (int j = 0; j < numberOfNodes; j++)
					if (adjacencyMatrix.get(n).get(j) != 0) {
						neighbors.add(j);
					}
				// visit all neighbors of node n
				int m;
				while (!neighbors.isEmpty()) {
					m = neighbors.element();
					neighbors.remove();
					if (markedNodes.get(m) == false) {
						actualGroup.add(m);
						markedNodes.set(m, true);
						nextNodeToBeVisited.add(m);
					}
				}
			}
			graphParts.add(actualGroup);
			// checks if all nodes are already visited
			p = -1;
			for (int i = 0; i < numberOfNodes; i++)
				if (markedNodes.get(i) == false) {
					p = i;
				}
		}
		// fix separated graphs problem
		while (graphParts.size() > 1) {
			// there is a problem concerning to separated graphs
			int node1 = -1;
			int node2 = -1;
			double minDistanceNode1Node2_loc = Double.MAX_VALUE;
			for (int i = 0; i < graphParts.get(0).size(); i++)
				for (int j = 0; j < graphParts.get(1).size(); j++) {
					double tempDist_loc = distanceBetweenNodes_mpr(graphParts.get(0).get(i), graphParts.get(1).get(j));
					if (tempDist_loc < minDistanceNode1Node2_loc) {
						node1 = graphParts.get(0).get(i);
						node2 = graphParts.get(1).get(j);
						minDistanceNode1Node2_loc = tempDist_loc;
					}
				}
			adjacencyMatrix.get(node1).set(node2, Math.floor(Math.random() * (NUMBER_OF_AMPLIFIER_LABELS - 1) + 1));
			adjacencyMatrix.get(node2).set(node1, Math.floor(Math.random() * (NUMBER_OF_AMPLIFIER_LABELS - 1) + 1));
//			adjacencyMatrix.get(node1).set(node2, 1.0);
//			adjacencyMatrix.get(node2).set(node1, 1.0);
			int tam = graphParts.get(1).size();
			for (int j = 0; j < tam; j++) {
				graphParts.get(0).add(graphParts.get(1).get(j));
			}
			graphParts.remove(graphParts.get(1));
		}
		// assingn to the network representatio vector
		int k = 0;
		for (int i = 0; i < numberOfNodes; i++)
			for (int j = i + 1; j < numberOfNodes; j++) {
				networkRepresentation.set(k, adjacencyMatrix.get(i).get(j).intValue());
				k++;
			}

		// try to solve biconnectivity problem
		Vector<Integer> notBiConnectedNodes_loc = new Vector<Integer>();

		for (int i = 0; i < numberOfNodes; i++) {
			int count_loc = 0;
			for (int j = 0; j < numberOfNodes; j++)
				if (adjacencyMatrix.get(i).get(j) != 0)
					count_loc++;
			if (count_loc <= 1)
				notBiConnectedNodes_loc.add(i);
		}

		// int debug1 = notBiConnectedNodes_loc.size();
		while (notBiConnectedNodes_loc.size() != 0) {
			int node1 = -1;
			int node2 = -1;
			int tempJ_loc = -1;
			node1 = notBiConnectedNodes_loc.get(0);
			double minDistanceNode1Node2_loc = 1e20;
			for (int j = 1; j < notBiConnectedNodes_loc.size(); j++)
				if ((adjacencyMatrix.get(node1).get(notBiConnectedNodes_loc.get(j)) == 0)
						&& (node1 != notBiConnectedNodes_loc.get(j))
						&& (minDistanceNode1Node2_loc > distanceBetweenNodes_mpr(notBiConnectedNodes_loc.get(0),
								notBiConnectedNodes_loc.get(j)))) {
					node2 = notBiConnectedNodes_loc.get(j);
					tempJ_loc = j;
					minDistanceNode1Node2_loc = distanceBetweenNodes_mpr(node1, node2);
				}
			if (node2 != -1) { // there was found a solution inside non
				// biconected set
//				adjacencyMatrix.get(node1).set(node2, 1.0);
//				adjacencyMatrix.get(node2).set(node1, 1.0);
				adjacencyMatrix.get(node1).set(node2, Math.floor(Math.random() * (NUMBER_OF_AMPLIFIER_LABELS - 1) + 1));
				adjacencyMatrix.get(node2).set(node1, Math.floor(Math.random() * (NUMBER_OF_AMPLIFIER_LABELS - 1) + 1));
				notBiConnectedNodes_loc.remove(tempJ_loc);
			} else {
				double minDistanceNode1Node2_loc2 = 1e20;
				for (int j = 0; j < adjacencyMatrix.get(node1).size(); j++)
					if ((adjacencyMatrix.get(node1).get(j) == 0) && (node1 != j)
							&& (minDistanceNode1Node2_loc2 > distanceBetweenNodes_mpr(node1, j))) {
						node2 = j;
						tempJ_loc = j;
						minDistanceNode1Node2_loc2 = distanceBetweenNodes_mpr(node1, node2);
					}
//				adjacencyMatrix.get(node1).set(node2, 1.0);
//				adjacencyMatrix.get(node2).set(node1, 1.0);
				adjacencyMatrix.get(node1).set(node2, Math.floor(Math.random() * (NUMBER_OF_AMPLIFIER_LABELS - 1) + 1));
				adjacencyMatrix.get(node2).set(node1, Math.floor(Math.random() * (NUMBER_OF_AMPLIFIER_LABELS - 1) + 1));
			}
			notBiConnectedNodes_loc.remove(0);
		}

		// assign to the network representation vector
		k = 0;
		for (int i = 0; i < numberOfNodes; i++)
			for (int j = i + 1; j < numberOfNodes; j++) {
				solution.setValue(k, adjacencyMatrix.get(i).get(j).intValue());
				k++;
			}
	}

	synchronized void evaluateNetworkCost(SolutionONTD solution, List<Integer> networkRepresentation_ppr,
			Vector<Vector<Double>> labelMatrix_loc, Vector<Vector<Double>> adjacencyMatrix) {
		int vectorSize_loc = networkRepresentation_ppr.size();
		double amplifierCost_loc = 0;
		double switchCost_loc = 0;
		double wavelengthCost_loc = 0;
		double dcfCost_loc = 0;
		double ssmfCost_loc = 0;
		double deploymentCost_loc = 0;
		double regeneratorCost_loc = 0;
		double totalCost_loc = 0;

		int numberOfWavelenghts_loc = networkRepresentation_ppr.get(vectorSize_loc - 1);
		for (int i = 0; i < vectorSize_loc - 2 - numberOfNodes; i++) {
			amplifierCost_loc += 3.84 * amplifierCostsAndTypes.get(0).get(networkRepresentation_ppr.get(i));
		}

		double betaOXC_loc = switchCostsAndTypes.get(0).get(networkRepresentation_ppr.get(vectorSize_loc - 2));
		for (int m = 0; m < numberOfNodes; m++) {
			int nodeDegree_loc = 0;

			// os proximos dois for sao para calcular o grau do no de indice m
			for (int j = m + 1; j < numberOfNodes; j++) { // varre para uma
				// linha fixa da matriz
				int k = indiceVetor_mpr(j, m); // k1
				if (networkRepresentation_ppr.get(k) != 0)
					nodeDegree_loc++;
			}
			for (int i = 0; i < m; i++) { // varre para uma coluna fixa da
				// matriz
				int k = indiceVetor_mpr(m, i); // k2
				if (networkRepresentation_ppr.get(k) != 0)
					nodeDegree_loc++;
			}

			wavelengthCost_loc += 2 * numberOfWavelenghts_loc * nodeDegree_loc;
			switchCost_loc += ((0.05225 * numberOfWavelenghts_loc + 6.24) * nodeDegree_loc + 2.5) * betaOXC_loc;
			// as antes da duas equa��es anteriores vem do modelo de custo
		}

		double totalNetworkLength = 0;
		for (int i = 0; i < numberOfNodes; i++)
			for (int j = 0; j < numberOfNodes; j++)
				if (adjacencyMatrix.get(i).get(j) < INF) {
					totalNetworkLength += adjacencyMatrix.get(i).get(j);
				}

		dcfCost_loc = 0.0036 * totalNetworkLength;
		ssmfCost_loc = 0.013 * totalNetworkLength;
		deploymentCost_loc = 0.2 * totalNetworkLength;

		//TODO: calculo do custo dos regeneradores
		for(int i=numberOfVariables-3; i>numberOfVariables-3-numberOfNodes; i--){
			regeneratorCost_loc += 2.8*networkRepresentation_ppr.get(i);
		}
				
		// state the total network cost by adding the separated costs
		totalCost_loc = amplifierCost_loc + switchCost_loc + wavelengthCost_loc + dcfCost_loc + ssmfCost_loc
				+ deploymentCost_loc + regeneratorCost_loc;

		// assigns capital costs data
		solution.setNetwork(profile);

		solution.setObjective(1, totalCost_loc);
	}

	public void assign(Vector<Double> temp_loc, int tam, double val) {
		for (int i = 0; i < tam; i++) {
			temp_loc.add(val);
		}
	}

	private Vector<Vector<Double>> buildAdjacencyMatrixDistances(List<Integer> solution,
			Vector<Vector<Double>> labelMatrix_loc) {
		Vector<Vector<Double>> adjacencyMatrix_loc = new Vector<Vector<Double>>();

		// fills up adjacencyMatrix_loc with zeros
		for (int i = 0; i < numberOfNodes; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			assign(temp_loc, numberOfNodes, 0.0);
			adjacencyMatrix_loc.add(temp_loc);
		}
		// melhorar este algoritmo dps da para fazer o segundo for come�ando
		// apenas do i atual
		for (int i = 0; i < numberOfNodes; i++)
			for (int j = 0; j < numberOfNodes; j++) {
				if ((i == j) || (labelMatrix_loc.get(i).get(j) == 0))
					adjacencyMatrix_loc.get(i).set(j, INF);
				else {
					adjacencyMatrix_loc.get(i).set(j, distanceBetweenNodes_mpr(i, j));
				}
			}

		return adjacencyMatrix_loc;
	}

	private Vector<Vector<Double>> buildAdjacencyMatrixLabels(List<Integer> networkRepresentation_ppr) {

		int vectorSize_loc = networkRepresentation_ppr.size();
		Vector<Vector<Double>> adjacencyMatrix_loc = new Vector<Vector<Double>>();

		// fills up adjacencyMatrix_loc with zeros
		for (int i = 0; i < numberOfNodes; i++) {
			Vector<Double> temp_loc = new Vector<Double>();
			assign(temp_loc, numberOfNodes, 0.0);
			adjacencyMatrix_loc.add(temp_loc);
		}
		int i = 0;
		int j = 1;

		for (int k = 0; (k < (vectorSize_loc - 2 - numberOfNodes)); k++) {
			adjacencyMatrix_loc.get(i).set(j, (double) networkRepresentation_ppr.get(k));
			adjacencyMatrix_loc.get(j).set(i, (double) networkRepresentation_ppr.get(k));
			j++;
			if (j == numberOfNodes) {
				i++;
				j = i + 1;
			}

		}
		return adjacencyMatrix_loc;
	}

	private double distanceBetweenNodes_mpr(int nodeS_par, int nodeD_par) {
		// x possition of the node nodeS_par
		double x1_loc = nodePositions.get(nodeS_par).get(0);
		// y possition of the node nodeS_par
		double y1_loc = nodePositions.get(nodeS_par).get(1);
		// x possition of the node nodeD_par
		double x2_loc = nodePositions.get(nodeD_par).get(0);
		// y possition of the node nodeD_par
		double y2_loc = nodePositions.get(nodeD_par).get(1);

		return sqrt((x1_loc - x2_loc) * (x1_loc - x2_loc) + (y1_loc - y2_loc) * (y1_loc - y2_loc));
	}

	private int indiceVetor_mpr(int j, int i) {
		return (j + (numberOfNodes - 1) * i - i * (i + 1) / 2)-1;
	}

	private synchronized void evaluateNetwokBlockingProbability(SolutionONTD solution,
			List<Integer> networkRepresentation_ppr, Vector<Vector<Double>> labelMatrix_loc,
			Vector<Vector<Double>> adjacencyMatrix) throws UnreliableSimulationException{
		double numberOfCalls_loc = CALLS;

		int TIMEOUT_LOC = 4;
		int i = 1;

		//System.out.println(numberOfCalls_loc);
		double lastSimulation_loc = netwokSimulation(numberOfCalls_loc, solution, networkRepresentation_ppr,
				labelMatrix_loc, adjacencyMatrix);
		// numberOfCalls_loc = 1;
		while ((lastSimulation_loc * numberOfCalls_loc < 500) && (i <= TIMEOUT_LOC)) {
			
			if(lastSimulation_loc != 0)
				numberOfCalls_loc *= 600/(lastSimulation_loc * numberOfCalls_loc);
			else
				numberOfCalls_loc = 1000000;
						
			lastSimulation_loc = netwokSimulation(numberOfCalls_loc, solution, networkRepresentation_ppr,
					labelMatrix_loc, adjacencyMatrix);
			
			//System.out.println(numberOfCalls_loc);
			
			if(numberOfCalls_loc >= 1000000 && lastSimulation_loc == 0){
				break;
			}
			
			i++;
		}
		
		if (lastSimulation_loc == 0){
			throw new UnreliableSimulationException();
		}
		
		block.addBlockLambda(profile.getBp().getLambda());
		block.addBlockBer(profile.getBp().getBer());
		block.addBlockDispersion(profile.getBp().getDispersion());
		
		block.addConv(profile.getBp().getContConv());
		block.addReg(profile.getBp().getContReg());
		block.addRegenerou(profile.getBp().getContRegenerou());
		
		//System.out.println(profile.getBp().getLambda());
		//System.out.println(profile.getBp().getBer());
		//System.out.println(profile.getBp().getDispersion());
		
		solution.setObjective(0, lastSimulation_loc);
	}

	public double netwokSimulation(double numberOfCalls_loc, SolutionONTD solution,
			List<Integer> networkRepresentation_ppr, Vector<Vector<Double>> adjacencyMatrixLabels,
			Vector<Vector<Double>> adjacencyMatrixDistances) {
		int vectorSize_loc = networkRepresentation_ppr.size();
		double[][] distancias;
		Link[][] links;
		double[][] matrizGanho;

		// creates the ad1acency distance matrix
		distancias = new double[numberOfNodes][numberOfNodes];
		for (int k = 0; k < numberOfNodes; k++)
			distancias[k] = new double[numberOfNodes];

		// Creating the matrix that defines network topology
		links = new Link[numberOfNodes][numberOfNodes];
		for (int k = 0; k < numberOfNodes; k++)
			links[k] = new Link[numberOfNodes];

		// Creating the matrix that defines the gains in amp on network
		matrizGanho = new double[numberOfNodes][numberOfNodes];
		for (int k = 0; k < numberOfNodes; k++)
			matrizGanho[k] = new double[numberOfNodes];

		// initializes the distances matrix
		for (int k = 0; k < numberOfNodes; k++){
			for (int w = 0; w < numberOfNodes; w++){
				distancias[k][w] = adjacencyMatrixDistances.get(k).get(w);
				
				/*if( distancias[k][w] != INF )
				System.out.println(k + " -> " + w + "  distancia = " + distancias[k][w]);*/
			}
		}
		
		// inicia matriz de ganhos com zeros
		for (int k = 0; k < numberOfNodes; k++)
			for (int w = 0; w < numberOfNodes; w++)
				matrizGanho[k][w] = 0;

		// inicia matriz de ganhos com valor de ganhos corretos
		for (int k = 0; k < numberOfNodes; k++)
			for (int w = 0; w < numberOfNodes; w++)
				if (distancias[k][w] != INF)
					matrizGanho[k][w] = ((distancias[k][w] / 2.0) * 0.2 - GMUX - GSWITCH / 2) * MF;

		// determines the number of wavelength per link

		int NLAMBDAS = networkRepresentation_ppr.get(vectorSize_loc - 1);

		// instancia os enlaces

		for (int k = 0; k < numberOfNodes; k++)
			for (int w = 0; w < numberOfNodes; w++) {
				// reads the amplifier satuarion power and noise figure
				double NF = amplifierCostsAndTypes.get(2).get((int) round(adjacencyMatrixLabels.get(k).get(w)));
				double PSAT = amplifierCostsAndTypes.get(1).get((int) round(adjacencyMatrixLabels.get(k).get(w)));

				if (distancias[k][w] != INF)
					links[k][w] = new Link(k, w, FIBRAS, NLAMBDAS, GMUX, matrizGanho[k][w], NF, PSAT, distancias[k][w],
							GFIBRA, matrizGanho[k][w], NF, PSAT, ganhodinamico_loc);
				else
					links[k][w] = new Link(k, w, FIBRAS, 0, -4.0, 0.0, 5.0, 16.0, INF, -0.2, 0.0, 5.0, 16.0,
							ganhodinamico_loc);
			}
		
		int inicioRegeneradores = (numberOfNodes * numberOfNodes - numberOfNodes) / 2;
		//atribuir numero de regeneradores aos n�s.
		for(int i=0; i<numberOfNodes; i++){
			profile.getNodes().get(i).setNumRegenerators(solution.getDecisionVariables()[inicioRegeneradores+i]);
		}
	
		profile.setEpsilon(pow(10, -0.1
				* switchCostsAndTypes.get(1).get(networkRepresentation_ppr.get(vectorSize_loc - 2))));
		profile.setLinks(links);
		simulator.simulate(profile);

		distancias = null;
		matrizGanho = null;
		links = null;
		
		
		return profile.getBp().getTotal();
	}
	
	public String drawTopologies(Solution<Integer> solution){
		   StringBuffer networkPicture = new StringBuffer();
		   StringBuffer networkPictureLabels = new StringBuffer();

		   List<Integer> networkRepresentation_ppr = Arrays.asList(solution.getDecisionVariables());
		   Vector<Vector<Double>> adjacencyMatrixLabels;
					
		   adjacencyMatrixLabels = buildAdjacencyMatrixLabels(networkRepresentation_ppr);

		   Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				   adjacencyMatrixLabels);

		   NumberFormat nf = NumberFormat.getInstance();
		   nf.setMaximumFractionDigits(1);
		   nf.setMinimumFractionDigits(1);

		   networkPictureLabels.append("% linklabels\n");

		   double radius_loc = 3;
		   double scale_loc = 0.15;
		   double adjustLegend_loc = 1;

		   networkPicture.append("\\scalebox{1}\n");
		   networkPicture.append("\n");
		   networkPicture.append("\\begin{pspicture}(-10,-10)(10,10)\n");
		   
			int inicioRegeneradores = (numberOfNodes * numberOfNodes - numberOfNodes) / 2;

		   for(int i=0;i<numberOfNodes;i++){
		      networkPicture.append("\\pscircle[linewidth=0.04,dimen=outer](" + scale_loc*NODES_POSITIONS[i][0] + "," + 
		    		  scale_loc*NODES_POSITIONS[i][1] + "){" + scale_loc*radius_loc + "}\n");
		      networkPicture.append("\\usefont{T1}{ptm}{m}{n}\n");
		      networkPicture.append("\\rput(" + scale_loc*NODES_POSITIONS[i][0] + "," + scale_loc*NODES_POSITIONS[i][1] + "){\\large" + 
		    		  networkRepresentation_ppr.get(inicioRegeneradores+i)*2 + "}\n");


		      for(int j=i+1;j<numberOfNodes;j++){
		         if(adjacencyMatrix.get(i).get(j) != INF) {
		            double modulus_loc = distanceBetweenNodes_mpr(i,j);
		            double xdif_loc = NODES_POSITIONS[i][0] - NODES_POSITIONS[j][0];
		            double ydif_loc = NODES_POSITIONS[i][1] - NODES_POSITIONS[j][1];
		            double xcorrection_loc = radius_loc*xdif_loc/modulus_loc;
		            double ycorrection_loc = radius_loc*ydif_loc/modulus_loc;
		            double ux = NODES_POSITIONS[i][0];
		            double uy = NODES_POSITIONS[i][1];
		            double vx = NODES_POSITIONS[j][0];
		            double vy = NODES_POSITIONS[j][1];
		            //if ( ((ux<vx)&&(uy<=vy))||((ux>vx)&&(uy>vy)) ){
		            if (ux!=vx){
		               double angle = (180.0/Math.PI)*Math.atan(  (vy-uy)/(vx-ux)   );
		               String temp_loc = "(" + nf.format(modulus_loc) + ";" + 
		               amplifierCostsAndTypes.get(1).get( (int) ((double) adjacencyMatrixLabels.get(i).get(j)) ) + ";" + 
		               amplifierCostsAndTypes.get(2).get( (int) ((double) adjacencyMatrixLabels.get(i).get(j))  )  + ")";
		               
		               temp_loc = temp_loc.replace(',', '.');
		               double positionXlabel = 0.5*(ux+vx) - adjustLegend_loc*Math.sin((Math.PI/180.0)*angle);
		               double positionYlabel = 0.5*(uy+vy) + adjustLegend_loc*Math.cos((Math.PI/180.0)*angle);
		               networkPictureLabels.append("\\rput{" + angle + "}(" + scale_loc*positionXlabel + "," +  scale_loc*positionYlabel + "){\\small" 
		            		   + temp_loc + "}\n");
		            }

		            else {
		               
		               String temp_loc = "(" + nf.format(modulus_loc) + ";" + 
		               amplifierCostsAndTypes.get(1).get( (int) ((double) adjacencyMatrixLabels.get(i).get(j)) ) 
		               + ";" + amplifierCostsAndTypes.get(2).get( (int) ((double) adjacencyMatrixLabels.get(i).get(j)) ) + ")";
		               temp_loc = temp_loc.replace(',', '.');
		               
		               double positionXlabel = 0.5*(ux+vx) - adjustLegend_loc;
		               double positionYlabel = 0.5*(uy+vy) ;
		               networkPictureLabels.append( "\\rput{" + "90" + "}(" + scale_loc*positionXlabel + "," + scale_loc*positionYlabel + 
		            		   "){\\small" + temp_loc + "}\n" );
		            }

		            ux -= xcorrection_loc;
		            uy -= ycorrection_loc;
		            vx += xcorrection_loc;
		            vy += ycorrection_loc;
		            networkPicture.append("\\psline[linewidth=0.04cm](" + scale_loc*ux + "," + scale_loc*uy + ")(" + scale_loc*vx + "," + scale_loc*vy 
		            		+ ")\n");
		         }
		      }
		   }
		   networkPicture.append(networkPictureLabels.toString());
		   networkPicture.append("\\end{pspicture}" + "}\n\n");
		   return networkPicture.toString();

		}
	
	public int[] numberMaxOfRegenerator(Integer[] variables){
		int[] retorno = new int[numberOfNodes];
		
		int lambda = variables[numberOfVariables-1];
		
		List<Integer> networkRepresentation_ppr = Arrays.asList(variables);
		
		for (int m = 0; m < numberOfNodes; m++) {
			int nodeDegree_loc = 0;

			// os proximos dois for sao para calcular o grau do no de indice m
			for (int j = m + 1; j < numberOfNodes; j++) { // varre para uma
				// linha fixa da matriz
				int k = indiceVetor_mpr(j, m); // k1
				if (networkRepresentation_ppr.get(k) != 0)
					nodeDegree_loc++;
			}
			for (int i = 0; i < m; i++) { // varre para uma coluna fixa da
				// matriz
				int k = indiceVetor_mpr(m, i); // k2
				if (networkRepresentation_ppr.get(k) != 0)
					nodeDegree_loc++;
			}

			retorno[m] = (nodeDegree_loc*lambda)/2;
		}
		
		return retorno;
	}
	
	public void trunkMaxOfRegenerator(Solution<Integer> solution){
		int[] maxOfRegenerator = this.numberMaxOfRegenerator(solution.getDecisionVariables());
		
		int inicioReg = (numberOfNodes * (numberOfNodes - 1))/2;
		
		for(int i=inicioReg; i<numberOfVariables-2; i++){
			if(solution.getDecisionVariables()[i] > maxOfRegenerator[i-inicioReg]){
				solution.getDecisionVariables()[i] = maxOfRegenerator[i-inicioReg];
			}
		}
	}

	public static void main(String[] args) {
//		Integer[] network = { 3, 4, 5, 5, 1, 4, 3, 2, 3, 6, 7, 5, 3, 3, 2, 6, 9, 1, 0, 6, 3, 0, 7, 1, 0, 3, 4, 6, 3, 0,
//				8, 8, 5, 5, 7, 3, 6, 3, 4, 5, 1, 6, 2, 0, 3, 5, 2, 5, 1, 1, 6, 8, 8, 8, 6, 3, 4, 9, 7, 6, 4, 1, 0, 8,
//				1, 9, 8, 6, 7, 6, 1, 1, 4, 5, 6, 4, 1, 1, 6, 8, 2, 6, 8, 7, 5, 5, 7, 3, 0, 4, 2, 3, 12 };
		
//		Integer[] network = { 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
//				9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
//				9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 4, 40 };
		
//		Integer[] network = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4 };
		
//		Integer[] network = { 7, 7, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 7, 0, 0, 0, 0, 0,
//				0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 7, 0, 0, 7, 0, 0, 0, 0, 7, 0, 0, 7, 0, 7, 0, 4 };
		
		Integer[] network = { 
				3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
					3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
						3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
							3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 
								3, 3, 3, 3, 3, 3, 3, 3, 3,
									3, 3, 3, 3, 3, 3, 3, 3, 
										3, 3, 3, 3, 3, 3, 3,
											3, 3, 3, 3, 3, 3, 
												3, 3, 3, 3, 3, 
													3, 3, 3, 3,
														3, 3, 3, 
															3, 3, 
																3,
				520, 520, 520, 520, 520, 520, 520, 520, 520, 520, 520, 520, 520, 520,
				                                5, 40 };
		
		
		double[] pb = new double[30];
		double[] cost = new double[30];
		RegSimonProblem ontd = new RegSimonProblem(14, 2, 0, 100);
		SolutionONTD sol = new SolutionONTD(ontd, network);
		RegSimonProblem.setUsarCache(false);
		ontd.evaluateConstraints(sol);
		ontd.evaluate(sol);
//		for (int i = 0; i < 30; i++){
//			ontd.evaluateConstraints(sol);
//			ontd.evaluate(sol);
//			pb[i] = sol.getObjective(0);
//			cost[i] = sol.getObjective(1);
//		}
//		for (int i = 0; i < 30; i++){
//			System.out.println(pb[i]);
////			System.out.println(pb[i] + " " + cost[i]);
//		}
	}

	/**
	 * M�todo acessor para obter o valor de usarCache
	 * 
	 * @return O valor de usarCache
	 */
	public static boolean isUsarCache() {
		return usarCache;
	}

	/**
	 * M�todo acessor para setar o valor de usarCache
	 * 
	 * @param usarCache
	 *            O valor de usarCache
	 */
	public static void setUsarCache(boolean usarCache) {
		RegSimonProblem.usarCache = usarCache;
	}

	/**
	 * M�todo acessor para obter o valor do atributo cALLS.
	 * 
	 * @return O valor de cALLS
	 */
	public double getCALLS() {
		return CALLS;
	}

	/**
	 * Metodo acessor para alterar o valor do atributo cALLS.
	 * 
	 * @param cALLS
	 *            O novo valor de cALLS
	 */
	public void setCALLS(double cALLS) {
		CALLS = cALLS;
	}

}
