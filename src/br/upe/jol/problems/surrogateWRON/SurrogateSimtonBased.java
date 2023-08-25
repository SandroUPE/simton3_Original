/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: SurrogateSimton.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	21/08/2014		Versão inicial
 * ****************************************************************************
 */
package br.upe.jol.problems.surrogateWRON;

import static br.upe.jol.problems.simon.rwa.Funcoes.INF;
import static br.upe.jol.problems.simon.rwa.OpticalNetworkSimulatorAbstract.SNR_BLOCK;
import static br.upe.jol.problems.simon.util.SimonUtil.LAMBDA_FIRSTFIT;
import static br.upe.jol.problems.simon.util.SimonUtil.UTILIZAR_DIJ;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;

import br.cns.TMetric;
import br.cns.experiments.ComplexNetwork;
import br.cns.experiments.nodePositions.CircularNetwork;
import br.cns.models.TModel;
import br.cns.transformations.DegreeMatrix;
import br.cns.transformations.LinkClosenessSequence;
import br.grna.bp.RedeNeural;
import br.upe.jol.base.Interval;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.StatsSummary;
import br.upe.jol.problems.simon.entity.Link;
import br.upe.jol.problems.simon.entity.NetworkProfile;
import br.upe.jol.problems.simon.entity.Node;
import br.upe.jol.problems.simon.rwa.SimpleDijkstraSimulator;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 21/08/2014
 */
public class SurrogateSimtonBased extends Problem<Integer> {
	private static final int INSIDE_BAND = 1;
	private static final int NOT_INITIALIZED = 0;
	private static final int OUTSIDE_RANGE_LEFT = 3;
	private static final int OUTSIDE_RANGE_RIGHT = 2;

	private RedeNeural redeNeural;

	public static final int NUMBER_OF_AMPLIFIER_LABELS = 10;
	public static final int MAX_NUMBER_OF_WAVELENGHTS = 40;
	public static final int MIN_NUMBER_OF_WAVELENGHTS = 4;
	public static final int NUMBER_OF_NODES = 14;
	public static final double RAND_MAX = 0x7FFF;
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
	private int networkLoad = 200;
	private boolean simulate = false;
	private StringBuffer sbHistorico = new StringBuffer();
	private int rows = 250000;

	private Map<Interval, StatsSummary> mapCosts;

	/**
	 * Fator de amortecimento no cálculo da média
	 */
	private static final double ALPHA = 0.60;

	/**
	 * Número de vezes acima do desvio padrão para aceite
	 */
	private static final double RANGE_STD_DVT = 2;

	public static final NumberFormat format = DecimalFormat.getInstance();

	static {
		format.setMinimumFractionDigits(8);
		format.setMaximumFractionDigits(8);
	}

	public static double[][] NODES_POSITIONS = new double[][] { { -85.692672, 14.728428 }, { -86.51664, -31.619772 },
			{ -69.213312, 34.606656 }, { -51.189012, 0.51498 }, { -29.044872, 3.810852 }, { 4.531824, 46.142208 },
			{ 0, 0 }, { 28.3239, 3.192876 }, { 56.13282, 1.853928 }, { 41.610384, 30.177828 },
			{ 43.464312, -6.282756 }, { 67.97736, -7.003728 }, { 74.15712, 2.677896 }, { 66.741408, 8.033688 } };

	public static double[][] AMPLIFIERS_COSTS_AND_LABELS = new double[][] {
			{ 0, 0.75, 1.5, 2.25, 0.5, 1, 1.5, 0.25, 0.5, 0.75 }, // cost
			// index
			{ 0, 13, 16, 19, 13, 16, 19, 13, 16, 19 }, // amplifier saturation
			// power
			{ 0, 5, 5, 5, 7, 7, 7, 9, 9, 9 } // amplifier noise figure
	};

	public static double[][] SWITCHES_COSTS_AND_LABELS = new double[][] { { 0, 0.5, 0.75, 1, 1.5, 2.0 }, // costs
			{ 0, 30, 33, 35, 38, 40 } }; // isolation factor in dB

	double LINK_ANT_COST = 0.4;

	private double[] limits = new double[] { 0.10, 0.01, 0.001, 0.0001, 0.00001 };

	private int[] limitsMinCalls = new int[] { 0, 500, 500, 250, 250 };

	private double[] limitsMaxCalls = new double[] { 0, 2000, 10000, 100000, 1000000 };

	public SurrogateSimtonBased(int numberOfNodes_ppr, int numberOfObjectives) {
		this(numberOfNodes_ppr, numberOfObjectives, 200);
	}

	public SurrogateSimtonBased(int numberOfNodes_ppr, int numberOfObjectives, int networkLoad) {
		super(((numberOfNodes_ppr * numberOfNodes_ppr - numberOfNodes_ppr) / 2) + 2, numberOfObjectives,
				"Projeto de Redes Ópticas");
		this.networkLoad = networkLoad;
		setSimulate(true);
		redeNeural = new RedeNeural(7, 1, 15, true);
		redeNeural.carregarRede("mlp_7_booster_0_15.txt");
		this.numberOfNodes = numberOfNodes_ppr;
		for (int i = 0; i < numberOfVariables - 2; i++) {
			upperLimit[i] = NUMBER_OF_AMPLIFIER_LABELS - 1;
			lowerLimit[i] = 0;
		}

		upperLimitObjective = new double[numberOfObjectives];
		lowerLimitObjective = new double[numberOfObjectives];

		// MEDIANET
//		 upperLimitObjective[0] = 1;
//		 upperLimitObjective[1] = 30000.7028;
//		 lowerLimitObjective[0] = 0.0;
//		 lowerLimitObjective[1] = 400;

		// ARNES
//		upperLimitObjective[0] = 1;
//		upperLimitObjective[1] = 60000;
//		lowerLimitObjective[0] = 0.0;
//		lowerLimitObjective[1] = 1000;

		// FLTG
		upperLimitObjective[0] = 1;
		upperLimitObjective[1] = 100000;
		lowerLimitObjective[0] = 0.0;
		lowerLimitObjective[1] = 2000;

		// FLTG
		// upperLimitObjective[0] = 1;
		// upperLimitObjective[1] = 200141.7028;
		// lowerLimitObjective[0] = 0.00000001;
		// lowerLimitObjective[1] = 1420;

		upperLimit[numberOfVariables - 1] = MAX_NUMBER_OF_WAVELENGHTS;
		lowerLimit[numberOfVariables - 1] = MIN_NUMBER_OF_WAVELENGHTS;

		upperLimit[numberOfVariables - 2] = SWITCHES_COSTS_AND_LABELS[0].length - 1;
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
			for (int j = 0; j < SWITCHES_COSTS_AND_LABELS[0].length; j++) {
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

		profile = new NetworkProfile(null, nodes, networkLoad * 0.01, 0.01, 100000, SNR_BLOCK, true, true, true, true,
				false, true, 40e9, 0.013e-9, 1.0, 0.04e-12 / sqrt(1000), 0.0, 10.0, LAMBDA_FIRSTFIT, UTILIZAR_DIJ,
				false, MAX_NUMBER_OF_WAVELENGHTS);
		simulator = new SimpleDijkstraSimulator();
		mapCosts = getMap(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.upe.jol.base.Problem#getHypervolumeDif()
	 */
	@Override
	public double getHypervolumeDif() {
		return 0.0;
		// return 0.0;
	}

	private Map<Interval, StatsSummary> getMap(Problem<Integer> problem) {
		Map<Interval, StatsSummary> mapCosts = new HashMap<Interval, StatsSummary>();
		int numIntervals = 400;
		double cost = problem.getLowerLimitObjective()[1];
		double increment = (problem.getUpperLimitObjective()[1] - problem.getLowerLimitObjective()[1]) / numIntervals;

		for (int i = 0; i < numIntervals; i++) {
			mapCosts.put(new Interval(cost, cost + increment), new StatsSummary(0, 0));
			cost += increment;
		}

		return mapCosts;
	}

	private void updateMapCosts(Map<Interval, StatsSummary> map, double pb, double cost) {
		double mean = 0;
		double stdDeviation = 0;
		StatsSummary stats = null;
		for (Interval interval : map.keySet()) {
			stats = map.get(interval);
			if (interval.contains(cost)) {
				if (stats.getMean() == 0 && stats.getStdDeviation() == 0) {
					mean = pb;
					stdDeviation = pb * (10.0 / 100);
				} else {
					mean = ALPHA * stats.getMean() + (1 - ALPHA) * pb;
					stdDeviation = ALPHA * stats.getStdDeviation() + (1 - ALPHA) * Math.abs(mean - pb);
				}
				System.out.printf("intervalo = [%s]; média = %.4f; desvio = %.4f \n", interval.toString(), mean,
						stdDeviation);
				stats = new StatsSummary(mean, stdDeviation);
				map.put(interval, stats);
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.upe.jol.base.Problem#getLogScale()
	 */
	@Override
	public boolean[] getLogScale() {
		return new boolean[] { false, false };
	}

	private void updateMapCosts(Map<Interval, StatsSummary> map, Solution<Integer> sol) {
		updateMapCosts(map, sol.getObjective(0), sol.getObjective(1));
	}

	private int assertSolutionEvaluation(SolutionONTD sol) {
		double pb = sol.getObjective(0);
		double cost = sol.getObjective(1);
		StatsSummary stats = null;
		for (Interval interval : mapCosts.keySet()) {
			if (interval.contains(cost)) {
				stats = mapCosts.get(interval);
				if (stats.getMean() == 0 && stats.getStdDeviation() == 0) {
					return NOT_INITIALIZED;
				} else if (pb < stats.getMean() + RANGE_STD_DVT * stats.getStdDeviation()
						&& pb > stats.getMean() - RANGE_STD_DVT * stats.getStdDeviation()) {
					return INSIDE_BAND;
				} else if (pb > stats.getMean() + RANGE_STD_DVT * stats.getStdDeviation()) {
					return OUTSIDE_RANGE_RIGHT;
				} else if (pb < stats.getMean() - RANGE_STD_DVT * stats.getStdDeviation()) {
					return OUTSIDE_RANGE_LEFT;
				}
				break;
			}
		}

		return OUTSIDE_RANGE_RIGHT;
	}

	@Override
	public synchronized void evaluate(Solution<Integer> solution) {
		List<Integer> networkRepresentation_ppr = Arrays.asList(solution.getDecisionVariables());
		long t = System.currentTimeMillis();
		Vector<Vector<Double>> adjacencyMatrixLabels;

		adjacencyMatrixLabels = buildAdjacencyMatrixLabels(networkRepresentation_ppr);

		Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				adjacencyMatrixLabels);
		ComplexNetwork cn = createComplexNetworkDistance((SolutionONTD) solution);
		int minCalls = 0;
		double maxCalls = 0;
		int i = 0;
		try {
			double cost = evaluateNetworkCost((SolutionONTD) solution, networkRepresentation_ppr,
					adjacencyMatrixLabels, adjacencyMatrix);

			double pb = netwokSimulationRegression((SolutionONTD) solution, cn);
			solution.setObjective(0, pb);
			solution.setObjective(1, cost);
			if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY) <= 1e-5) {
				solution.setObjective(0, 1);
			} else {
				i = 0;
				if (pb < limits[i] || assertSolutionEvaluation((SolutionONTD) solution) == INSIDE_BAND) {
					i++;
					minCalls = limitsMinCalls[i];
					maxCalls = limitsMaxCalls[i];
					pb = netwokSimulation(maxCalls, (SolutionONTD) solution, networkRepresentation_ppr,
							adjacencyMatrixLabels, adjacencyMatrix, minCalls);
					solution.setObjective(0, pb);
					while (pb < limits[i]
							&& (assertSolutionEvaluation((SolutionONTD) solution) == NOT_INITIALIZED || assertSolutionEvaluation((SolutionONTD) solution) == OUTSIDE_RANGE_LEFT)
							&& i < limits.length - 1) {
						i++;
						minCalls = limitsMinCalls[i];
						maxCalls = limitsMaxCalls[i];
						pb = netwokSimulation(maxCalls, (SolutionONTD) solution, networkRepresentation_ppr,
								adjacencyMatrixLabels, adjacencyMatrix, minCalls);
						solution.setObjective(0, pb);
					}
				}
				if (pb >= limits[i] && assertSolutionEvaluation((SolutionONTD) solution) != OUTSIDE_RANGE_RIGHT) {
					updateMapCosts(mapCosts, solution);
				}
				if (solution.getObjective(0) < 1e-5) {
					solution.setObjective(0, 1);
				}
			}
			// sbHistorico.append(String.format("%.5f; %.2f; %d; %d; %d; TEMPO = %d \n",
			// pb, cost, i, minCalls,
			// (int) maxCalls, (System.currentTimeMillis() - t)));
			// System.out.printf("PB = %.5f; Custo = %.2f; MIN_CALLS = %d; MAX_CALLS = %d; TEMPO = %d \n",
			// pb, cost,
			// minCalls, (int) maxCalls, (System.currentTimeMillis() - t));
			// rows++;
			// if (rows % 10000 == 0) {
			// FileWriter fw = new FileWriter("history" + rows + ".txt");
			// fw.write(sbHistorico.toString());
			// sbHistorico = new StringBuffer();
			// fw.close();
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void evaluateConstraints(Solution<Integer> solution) {
		Integer[][] am = new Integer[numberOfNodes][numberOfNodes];
		int counter = 0;
		int violations = 0;
		for (int i = 0; i < numberOfNodes; i++) {
			am[i][i] = 0;
			for (int j = i + 1; j < numberOfNodes; j++) {
				if (solution.getDecisionVariables()[counter] != 0) {
					am[i][j] = 1;
					am[j][i] = 1;
				} else {
					am[i][j] = 0;
					am[j][i] = 0;
				}
				counter++;
			}
		}
		ComplexNetwork cn = new ComplexNetwork(0, am, CircularNetwork.getInstance().createNodePositions(numberOfNodes),
				TModel.CUSTOM, TMetric.getDefaults());

		if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY) <= 0) {
			violations--;
		}

		Integer[][] degree = DegreeMatrix.getInstance().transform(am);
		for (int i = 0; i < numberOfNodes; i++) {
			if (degree[i][i] < 1) {
				violations--;
			}
		}

		// System.out.println("Violations = " + violations);
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
			// adjacencyMatrix.get(node1).set(node2, 1.0);
			// adjacencyMatrix.get(node2).set(node1, 1.0);
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
				// adjacencyMatrix.get(node1).set(node2, 1.0);
				// adjacencyMatrix.get(node2).set(node1, 1.0);
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
				// adjacencyMatrix.get(node1).set(node2, 1.0);
				// adjacencyMatrix.get(node2).set(node1, 1.0);
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

	public synchronized double evaluateNetworkCost(SolutionONTD solution, List<Integer> networkRepresentation_ppr,
			Vector<Vector<Double>> labelMatrix_loc, Vector<Vector<Double>> adjacencyMatrix) {
		int vectorSize_loc = networkRepresentation_ppr.size();
		double amplifierCost_loc = 0;
		double switchCost_loc = 0;
		double wavelengthCost_loc = 0;
		double totalCost_loc = 0;

		int numberOfWavelenghts_loc = networkRepresentation_ppr.get(vectorSize_loc - 1);
		int indexOxc = networkRepresentation_ppr.get(vectorSize_loc - 2);

		totalCost_loc = evaluateSimpleCost(networkRepresentation_ppr, adjacencyMatrix, vectorSize_loc,
				amplifierCost_loc, switchCost_loc, wavelengthCost_loc, numberOfWavelenghts_loc, indexOxc);
		// assigns capital costs data
		solution.setNetwork(profile);
		solution.setObjective(1, totalCost_loc);

		return totalCost_loc;
	}

	/**
	 * @param networkRepresentation_ppr
	 * @param adjacencyMatrix
	 * @param vectorSize_loc
	 * @param amplifierCost_loc
	 * @param switchCost_loc
	 * @param wavelengthCost_loc
	 * @param numberOfWavelenghts_loc
	 * @param indexOxc
	 * @return
	 */
	private double evaluateSimpleCost(List<Integer> networkRepresentation_ppr, Vector<Vector<Double>> adjacencyMatrix,
			int vectorSize_loc, double amplifierCost_loc, double switchCost_loc, double wavelengthCost_loc,
			int numberOfWavelenghts_loc, int indexOxc) {
		double dcfCost_loc;
		double ssmfCost_loc;
		double deploymentCost_loc;
		double totalCost_loc;
		double betaOXC_loc = switchCostsAndTypes.get(0).get(indexOxc);

		for (int i = 0; i < vectorSize_loc - 2; i++) {
			amplifierCost_loc += 3.84 * amplifierCostsAndTypes.get(0).get(networkRepresentation_ppr.get(i));
		}

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
			// as antes da duas equaï¿½ï¿½es anteriores vem do modelo de custo
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

		// state the total network cost by adding the separated costs
		totalCost_loc = amplifierCost_loc + switchCost_loc + wavelengthCost_loc + dcfCost_loc + ssmfCost_loc
				+ deploymentCost_loc;
		return totalCost_loc;
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
		// melhorar este algoritmo dps da para fazer o segundo for comeï¿½ando
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

		for (int k = 0; (k < (vectorSize_loc - 2)); k++) {
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
		return (j + (numberOfNodes - 1) * i - i * (i + 1) / 2);
	}

	private Double[][] getDistancias(Vector<Vector<Double>> adjacencyMatrixDistances) {
		Double[][] distancias;
		// creates the ad1acency distance matrix
		distancias = new Double[numberOfNodes][numberOfNodes];
		for (int k = 0; k < numberOfNodes; k++)
			distancias[k] = new Double[numberOfNodes];
		for (int k = 0; k < numberOfNodes; k++)
			for (int w = 0; w < numberOfNodes; w++)
				distancias[k][w] = adjacencyMatrixDistances.get(k).get(w);
		return distancias;
	}

	public double netwokSimulation(double numberOfCalls_loc, SolutionONTD solution,
			List<Integer> networkRepresentation_ppr, Vector<Vector<Double>> adjacencyMatrixLabels,
			Vector<Vector<Double>> adjacencyMatrixDistances, int numMinCalls) {
		int vectorSize_loc = networkRepresentation_ppr.size();
		Double[][] distancias = getDistancias(adjacencyMatrixDistances);
		Link[][] links;
		double[][] matrizGanho;

		// Creating the matrix that defines network topology
		links = new Link[numberOfNodes][numberOfNodes];
		for (int k = 0; k < numberOfNodes; k++)
			links[k] = new Link[numberOfNodes];

		// Creating the matrix that defines the gains in amp on network
		matrizGanho = new double[numberOfNodes][numberOfNodes];
		for (int k = 0; k < numberOfNodes; k++)
			matrizGanho[k] = new double[numberOfNodes];

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

		profile.setEpsilon(pow(10,
				-0.1 * switchCostsAndTypes.get(1).get(networkRepresentation_ppr.get(vectorSize_loc - 2))));
		profile.setLinks(links);
		profile.cleanBp();
		profile.setNumOfCalls(numberOfCalls_loc);
		simulator.simulate(profile, numMinCalls);

		distancias = null;
		matrizGanho = null;
		links = null;

		return profile.getBp().getTotal();
	}

	public double netwokSimulationRegression(double numberOfCalls_loc, SolutionONTD solution,
			List<Integer> networkRepresentation_ppr, Vector<Vector<Double>> adjacencyMatrixLabels,
			Vector<Vector<Double>> adjacencyMatrixDistances) {
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
		for (int k = 0; k < numberOfNodes; k++)
			for (int w = 0; w < numberOfNodes; w++)
				distancias[k][w] = adjacencyMatrixDistances.get(k).get(w);

		// inicia matriz de ganhos com zeros
		for (int k = 0; k < numberOfNodes; k++)
			for (int w = 0; w < numberOfNodes; w++)
				matrizGanho[k][w] = 0;

		// inicia matriz de ganhos com valor de ganhos corretos
		for (int k = 0; k < numberOfNodes; k++)
			for (int w = 0; w < numberOfNodes; w++)
				if (distancias[k][w] != INF)
					matrizGanho[k][w] = ((distancias[k][w] / 2.0) * 0.2 - GMUX - GSWITCH / 2) * MF;
		ComplexNetwork cn = createComplexNetwork(solution);

		if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY) > redeNeural.getMaxValues()[2]) {
			System.out.printf("Normalização violada (conectividade algébrica): ",
					cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY), redeNeural.getMaxValues()[2]);
		}
		if (cn.getMetricValues().get(TMetric.NATURAL_CONNECTIVITY) > redeNeural.getMaxValues()[3]) {
			System.out.printf("Normalização violada (conectividade natural): ",
					cn.getMetricValues().get(TMetric.NATURAL_CONNECTIVITY), redeNeural.getMaxValues()[3]);
		}
		if (cn.getMetricValues().get(TMetric.AVERAGE_DEGREE) > redeNeural.getMaxValues()[4]) {
			System.out.printf("Normalização violada (grau médio): ", cn.getMetricValues().get(TMetric.AVERAGE_DEGREE),
					redeNeural.getMaxValues()[4]);
		}
		if (cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH) > redeNeural.getMaxValues()[5]) {
			System.out.printf("Normalização violada (comprimento médio do caminho): ",
					cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH), redeNeural.getMaxValues()[5]);
		}
		if (cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT) > redeNeural.getMaxValues()[6]) {
			System.out.printf("Normalização violada (coef. agrupamento): ",
					cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT), redeNeural.getMaxValues()[6]);
		}
		if (cn.getMetricValues().get(TMetric.ENTROPY) > redeNeural.getMaxValues()[7]) {
			System.out.printf("Normalização violada (entropia): ", cn.getMetricValues().get(TMetric.ENTROPY),
					redeNeural.getMaxValues()[7]);
		}

		double pb = redeNeural.obterSaida(
				new double[] { solution.getDecisionVariables()[solution.getDecisionVariables().length - 1],
						solution.getDecisionVariables()[solution.getDecisionVariables().length - 2],
						cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY),
						cn.getMetricValues().get(TMetric.NATURAL_CONNECTIVITY),
						cn.getMetricValues().get(TMetric.DENSITY), cn.getMetricValues().get(TMetric.AVERAGE_DEGREE),
						cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH),
						cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT),
						cn.getMetricValues().get(TMetric.DIAMETER), cn.getMetricValues().get(TMetric.ENTROPY) }, true);
		return pb;
	}

	public double netwokSimulationRegression(SolutionONTD solution, ComplexNetwork cn) {
		List<Double> lNfAmps = new Vector<>();
		List<Double> lSatAmps = new Vector<>();
		StringBuffer am = new StringBuffer();
		for (int i = 0; i < solution.getDecisionVariables().length - 2; i++) {
			am.append(solution.getDecisionVariables()[i]).append(" ");
			if (solution.getDecisionVariables()[i] != 0) {
				lNfAmps.add(AMPLIFIERS_COSTS_AND_LABELS[2][solution.getDecisionVariables()[i]]);
				lSatAmps.add(AMPLIFIERS_COSTS_AND_LABELS[1][solution.getDecisionVariables()[i]]);
			}
		}

		List<Integer> orderedCloseness = new Vector<>();
		Double[][] linkCloseness = LinkClosenessSequence.getInstance().transform(cn.getDistances());
		for (int i = 0; i < 14; i++) {
			for (int j = i + 1; j < 14; j++) {
				orderedCloseness.add(linkCloseness[i][j].intValue());
			}
		}
		Collections.sort(orderedCloseness);

		double maxLc = orderedCloseness.get(orderedCloseness.size() - 1);
		double minLc = orderedCloseness.get(orderedCloseness.size() - 1);
		double countLc = 0;
		for (int i = 0; i < orderedCloseness.size(); i++) {
			if (orderedCloseness.get(i) > 0) {
				countLc++;
				if (orderedCloseness.get(i) < minLc) {
					minLc = orderedCloseness.get(i);
				}
			}
		}

		double[] input = new double[] {
				solution.getDecisionVariables()[solution.getDecisionVariables().length - 1],
				10 * SWITCHES_COSTS_AND_LABELS[1][solution.getDecisionVariables()[solution.getDecisionVariables().length - 2]],
				cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT), cn.getMetricValues().get(TMetric.DENSITY),
				cn.getMetricValues().get(TMetric.PHYSICAL_AVERAGE_PATH_LENGTH),
				cn.getMetricValues().get(TMetric.DFT_LAPLACIAN_ENTROPY), (maxLc - minLc) / countLc };

		return redeNeural.obterSaida(input, true);

	}

	public ComplexNetwork createComplexNetwork(SolutionONTD solution) {
		List<TMetric> metrics = new ArrayList<TMetric>();
		metrics.add(TMetric.NATURAL_CONNECTIVITY);
		metrics.add(TMetric.ALGEBRAIC_CONNECTIVITY);
		metrics.add(TMetric.DENSITY);
		metrics.add(TMetric.AVERAGE_DEGREE);
		metrics.add(TMetric.AVERAGE_PATH_LENGTH);
		metrics.add(TMetric.CLUSTERING_COEFFICIENT);
		metrics.add(TMetric.DIAMETER);
		metrics.add(TMetric.ENTROPY);
		metrics.add(TMetric.DFT_LAPLACIAN_ENTROPY);
		metrics.add(TMetric.PHYSICAL_AVERAGE_PATH_LENGTH);

		Integer[][] matrix = null;
		int value = 0;
		int counter = 0;
		matrix = new Integer[NUMBER_OF_NODES][NUMBER_OF_NODES];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i][i] = 0;
			for (int j = i + 1; j < matrix.length; j++) {
				value = Integer.valueOf(solution.getDecisionVariables()[counter]);
				if (value != 0) {
					matrix[i][j] = 1;
					matrix[j][i] = 1;
				} else {
					matrix[i][j] = 0;
					matrix[j][i] = 0;
				}
				counter++;
			}
		}
		return new ComplexNetwork(0, matrix, new double[14][14], TModel.CUSTOM, metrics);
	}

	public Double[][] getDistanceMatrix() {
		Integer[] var = new Integer[93];
		Arrays.fill(var, 1);
		List<Integer> networkRepresentation_ppr = Arrays.asList(var);
		Double[][] distances = getDistancias(buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				buildAdjacencyMatrixLabels(networkRepresentation_ppr)));

		return distances;
	}

	public ComplexNetwork createComplexNetworkDistance(SolutionONTD solution) {
		List<Integer> networkRepresentation_ppr = Arrays.asList(solution.getDecisionVariables());
		Double[][] distances = getDistancias(buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				buildAdjacencyMatrixLabels(networkRepresentation_ppr)));

		List<TMetric> metrics = new ArrayList<TMetric>();
		metrics.add(TMetric.NATURAL_CONNECTIVITY);
		metrics.add(TMetric.ALGEBRAIC_CONNECTIVITY);
		metrics.add(TMetric.DENSITY);
		metrics.add(TMetric.AVERAGE_DEGREE);
		metrics.add(TMetric.AVERAGE_PATH_LENGTH);
		metrics.add(TMetric.PHYSICAL_AVERAGE_PATH_LENGTH);
		metrics.add(TMetric.CLUSTERING_COEFFICIENT);
		metrics.add(TMetric.DIAMETER);
		metrics.add(TMetric.PHYSICAL_DIAMETER);
		metrics.add(TMetric.ENTROPY);
		metrics.add(TMetric.DFT_LAPLACIAN_ENTROPY);
		metrics.add(TMetric.PHYSICAL_DFT_LAPLACIAN_ENTROPY);
		metrics.add(TMetric.SPECTRAL_RADIUS);
		metrics.add(TMetric.MAXIMUM_CLOSENESS);

		Integer[][] matrix = new Integer[NUMBER_OF_NODES][NUMBER_OF_NODES];
		double value = 0;
		int counter = 0;
		for (int i = 0; i < matrix.length; i++) {
			matrix[i][i] = 0;
			for (int j = i + 1; j < matrix.length; j++) {
				value = Integer.valueOf(solution.getDecisionVariables()[counter]);
				if (value != 0) {
					matrix[i][j] = 1;
					matrix[j][i] = 1;
				} else {
					matrix[i][j] = 0;
					matrix[j][i] = 0;
				}
				counter++;
			}
		}
		return new ComplexNetwork(0, matrix, new double[14][14], distances, TModel.CUSTOM, metrics);
	}

	/**
	 * @return o valor do atributo simulate
	 */
	public boolean isSimulate() {
		return simulate;
	}

	/**
	 * Altera o valor do atributo simulate
	 * 
	 * @param simulate
	 *            O valor para setar em simulate
	 */
	public void setSimulate(boolean simulate) {
		this.simulate = simulate;
	}

	/**
	 * @return o valor do atributo profile
	 */
	public NetworkProfile getProfile() {
		return profile;
	}

	/**
	 * Altera o valor do atributo profile
	 * 
	 * @param profile
	 *            O valor para setar em profile
	 */
	public void setProfile(NetworkProfile profile) {
		this.profile = profile;
	}

	/**
	 * @return o valor do atributo networkLoad
	 */
	public int getNetworkLoad() {
		return networkLoad;
	}

	/**
	 * Altera o valor do atributo networkLoad
	 * 
	 * @param networkLoad
	 *            O valor para setar em networkLoad
	 */
	public void setNetworkLoad(int networkLoad) {
		this.networkLoad = networkLoad;
	}

}
