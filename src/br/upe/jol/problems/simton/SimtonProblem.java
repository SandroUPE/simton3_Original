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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import br.cns.TMetric;
import br.cns.experiments.ComplexNetwork;
import br.cns.experiments.nodePositions.CircularNetwork;
import br.cns.models.TModel;
import br.cns.transformations.DegreeMatrix;
import br.cns.transformations.LinkClosenessSequence;
import br.grna.bp.RedeNeural;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.Util;
import br.upe.jol.problems.simon.entity.CallSchedulerNonUniformHub;
import br.upe.jol.problems.simon.entity.Link;
import br.upe.jol.problems.simon.entity.NetworkProfile;
import br.upe.jol.problems.simon.entity.Node;
import br.upe.jol.problems.simon.rwa.SimpleDijkstraSimulator;

/**
 * .
 * 
 * @author Danilo Ara�jo
 */
public class SimtonProblem extends Problem<Integer> {
	private static int simtonEvaluations = 0;
	private static int totalEvaluations = 0;
	private RedeNeural redeNeural;

	public static final int NUMBER_OF_AMPLIFIER_LABELS = 10;
	public static final int MAX_NUMBER_OF_WAVELENGHTS = 40;
	public static final int MIN_NUMBER_OF_WAVELENGHTS = 4;
	public static final int NUMBER_OF_NODES = 14;
	public static final double RAND_MAX = 0x7FFF;
	public int avals = 0;
	public int probAvals = 0;
	public double minProbability = 1;
	private NetworkProfile profile;
	protected SimpleDijkstraSimulator simulator;
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
	private static StringBuffer sbErrosRna = new StringBuffer();
	private int networkLoad = 100;
	private boolean simulate = false;

	private static final double PB_DELTA_MAX = 1E-3;

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

	// public static double[][] SWITCHES_COSTS_AND_LABELS = new double[][] { {
	// 0, 0.5, 0.75, 1, 1.5, 2.0, 0.25, 0.6, 0.8, 1.1, 1.75}, // costs
	// { 0, 30, 33, 35, 38, 40, 28.5, 31.5, 34, 36.5, 39} }; // isolation factor
	// in dB

	double LINK_ANT_COST = 0.4;
	// public static final double MIN_CALLS = 500;
	// public static final double PRECISION = 0.002;
	public static final double MIN_CALLS = 500;
	public static final double PRECISION = 0.00001;
	// public static final double CALLS = MIN_CALLS / PRECISION;
	public static final double CALLS = 1000000;

	public static int NUM_EVALS = 0;

	public SimtonProblem(int numberOfNodes_ppr, int numberOfObjectives) {
		this(numberOfNodes_ppr, numberOfObjectives, 200);
	}

	public SimtonProblem(int numberOfNodes_ppr, int numberOfObjectives, int networkLoad) {
		super(((numberOfNodes_ppr * numberOfNodes_ppr - numberOfNodes_ppr) / 2) + 2, numberOfObjectives,
				"Projeto de Redes �pticas");
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

		upperLimitObjective[0] = 1;
		upperLimitObjective[1] = 20141.7028;
		lowerLimitObjective[0] = 0.00001;
		lowerLimitObjective[1] = 420;

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
		// profile = new NetworkProfile(null, nodes, 20 * 0.01, 0.01, 10000,
		// SNR_BLOCK, true, true, true, true, false,
		// true, 40e9, 0.013e-9, 1.0, 0.04e-12 / sqrt(1000), 0.0, 10.0,
		// LAMBDA_FIRSTFIT, UTILIZAR_DIJ, false,
		// MAX_NUMBER_OF_WAVELENGHTS);
		simulator = new SimpleDijkstraSimulator();
	}

	public static StringBuffer getLine(SimtonProblem ontd, SolutionONTD solution, Double[][] linkCloseness) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(6);
		nf.setMaximumFractionDigits(6);
		ComplexNetwork cn = ontd.createComplexNetworkDistance(solution);

		StringBuffer fileContent = new StringBuffer();
		String innerSeparator = " ";
		String outerSeparator = ";";
		StringBuffer am = new StringBuffer();
		StringBuffer aam = new StringBuffer();
		StringBuffer hash = new StringBuffer();
		int count = 0;
		for (Integer value : solution.getDecisionVariables()) {
			if (count < solution.getDecisionVariables().length - 2) {
				if (value != 0) {
					am.append(1).append(innerSeparator);
				} else {
					am.append(0).append(innerSeparator);
				}
				aam.append(value).append(innerSeparator);
			}
			hash.append(value).append(";");
			count++;
		}

		fileContent.append(nf.format(solution.getObjective(1))); // custo
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(solution.getObjective(0))); // BP
		fileContent.append(outerSeparator);
		fileContent.append(solution.getDecisionVariables()[solution.getDecisionVariables().length - 1].shortValue()); // wavelengths
		fileContent.append(outerSeparator);
		fileContent
				.append(SWITCHES_COSTS_AND_LABELS[1][solution.getDecisionVariables()[solution.getDecisionVariables().length - 2]]); // oxc
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.NATURAL_CONNECTIVITY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.DENSITY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.AVERAGE_DEGREE)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.DIAMETER)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.ENTROPY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.PHYSICAL_DIAMETER)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.PHYSICAL_AVERAGE_PATH_LENGTH)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.DFT_LAPLACIAN_ENTROPY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(normalizedNf(solution.getDecisionVariables(), cn.getDistances())));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.SPECTRAL_RADIUS)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.PHYSICAL_DFT_LAPLACIAN_ENTROPY)));
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(normalizedSat(solution.getDecisionVariables(), cn.getDistances())));
		fileContent.append(outerSeparator);
		fileContent.append(am.toString()); // adjacencyMatrix
		fileContent.append(outerSeparator);
		fileContent.append(aam.toString()); // adjacencyMatrix
		fileContent.append(outerSeparator);
		for (int i = 0; i < 14; i++) {
			for (int j = i + 1; j < 14; j++) {
				fileContent.append(linkCloseness[i][j]).append(" ");
			}
		}

		return fileContent;
	}

	public static double normalizedNf(Integer[] values, Double[][] distances) {
		double nf = 0;
		double sumDistance = 0;

		for (int i = 0; i < values.length - 2; i++) {
			if (values[i] != 0) {
				sumDistance += 1;
				nf += (getNf(values[i]) / 9.0);
			}
		}

		return nf / sumDistance;
	}

	public static double normalizedSat(Integer[] values, Double[][] distances) {
		double sat = 0;
		double sumDistance = 0;

		for (int i = 0; i < values.length - 2; i++) {
			if (values[i] != 0) {
				sumDistance += 1;
				sat += (getSat(values[i]) / 19.0);
			}
		}

		return sat / sumDistance;
	}

	private static double getNf(int ampLabel) {
		if (ampLabel > 6) {
			return 9;
		} else if (ampLabel > 3) {
			return 7;
		} else if (ampLabel > 0) {
			return 5;
		}
		return 0;
	}

	private static double getSat(int ampLabel) {
		// { 0, 13, 16, 19, 13, 16, 19, 13, 16, 19 }, // amplifier saturation
		if (ampLabel == 1 || ampLabel == 4 || ampLabel == 7) {
			return 13;
		} else if (ampLabel == 2 || ampLabel == 5 || ampLabel == 8) {
			return 16;
		} else if (ampLabel == 3 || ampLabel == 6 || ampLabel == 9) {
			return 19;
		}
		return 0;
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

	@Override
	public synchronized void evaluate(Solution<Integer> solution) {
		avals++;
		List<Integer> networkRepresentation_ppr = Arrays.asList(solution.getDecisionVariables());
		long t = System.currentTimeMillis();
		Vector<Vector<Double>> adjacencyMatrixLabels;

		adjacencyMatrixLabels = buildAdjacencyMatrixLabels(networkRepresentation_ppr);

		Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				adjacencyMatrixLabels);
		ComplexNetwork cn = createComplexNetworkDistance((SolutionONTD) solution);
		
		try {
			if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY) <= 1e-5) {
				solution.setObjective(0, 1);
			} else { 
//				if (simulate ) { 
//					&& rna < pbth_rna) {
				evaluateNetwokBlockingProbability((SolutionONTD) solution, networkRepresentation_ppr,
						adjacencyMatrixLabels, adjacencyMatrix, simulate);
				solution.setAccurateEvaluation(true);
//				double lastSimulation = solution.getObjective(0);
				// sbErrosRna.append(format.format(rna)).append(" ").append(format.format(lastSimulation)).append(" ")
				// .append(format.format(lastSimulation - rna)).append("\n");
//				System.out.println(format.format(rna) + " " + format.format(lastSimulation) + " "
//						+ format.format(lastSimulation - rna));
//			} else {
//				// System.out.println("Setando RNA...");
//				solution.setObjective(0, rna);
//				if (rna > pbth_rna) {
//					solution.setAccurateEvaluation(true);
//				} else {
//					solution.setAccurateEvaluation(false);
//				}
//
//				solution.setObjective(1, costNew);
//				solution.setValue(solution.getDecisionVariables().length - 1, w);
//				solution.setValue(solution.getDecisionVariables().length - 2, oxc);
			}
			solution.setObjective(1, evaluateNetworkCost((SolutionONTD) solution, networkRepresentation_ppr,
					adjacencyMatrixLabels, adjacencyMatrix));
			// if (evals % 200 == 0) {
			// try {
			// FileWriter fw = new FileWriter("erros-rna.txt");
			// fw.write(sbErrosRna.toString());
			// fw.close();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// }
			netwokSimulationRegression((SolutionONTD)solution, cn);
			if (solution.getObjective(0) < 1e-5) {
				solution.setObjective(0, 1e-5);
			}
		} catch (UnreliableSimulationException e) {
			((SolutionONTD) solution).setObjective(0, upperLimitObjective[0] + PRECISION);
			((SolutionONTD) solution).setObjective(1, upperLimitObjective[1] + PRECISION);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Util.LOGGER.info("Avalia��o (" + format.format(((SolutionONTD) solution).getObjective(0)) + ", "
				+ format.format(((SolutionONTD) solution).getObjective(1)) + "). Tempo de avalia��o do indiv�duo ("
				+ avals + ") = " + (System.currentTimeMillis() - t) + ".");
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

		// for (int w = 4; w <= 40; w += 4) {

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
		profile.setCompleteDistances(new Double[numberOfNodes][numberOfNodes]);
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
				profile.getCompleteDistances()[i][j] = distanceBetweenNodes_mpr(i, j);
				if ((i == j) || (labelMatrix_loc.get(i).get(j) == 0)) {
					adjacencyMatrix_loc.get(i).set(j, INF);
				} else {
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

	private synchronized void evaluateNetwokBlockingProbability(SolutionONTD solution,
			List<Integer> networkRepresentation_ppr, Vector<Vector<Double>> labelMatrix_loc,
			Vector<Vector<Double>> adjacencyMatrix, boolean simulate) throws UnreliableSimulationException {
		double numberOfCalls_loc = CALLS;
		int maximum = 1;
		double lastSimulation_loc = 0;
		double parcialSimul = 0;
		NUM_EVALS++;
		for (int i = 0; i < maximum; i++) {
			parcialSimul = netwokSimulation(numberOfCalls_loc, solution, networkRepresentation_ppr, labelMatrix_loc,
					adjacencyMatrix);
			lastSimulation_loc += parcialSimul;
			// System.out.printf("Avalia��o %d: %.6f\n", i, parcialSimul);
		}
		lastSimulation_loc /= maximum;

		solution.setObjective(0, lastSimulation_loc);
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
			Vector<Vector<Double>> adjacencyMatrixDistances) {
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
		simulator.simulate(profile, new CallSchedulerNonUniformHub(14, networkLoad));
//		simulator.simulate(profile);

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
			System.out.printf("Normaliza��o violada (conectividade alg�brica): ",
					cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY), redeNeural.getMaxValues()[2]);
		}
		if (cn.getMetricValues().get(TMetric.NATURAL_CONNECTIVITY) > redeNeural.getMaxValues()[3]) {
			System.out.printf("Normaliza��o violada (conectividade natural): ",
					cn.getMetricValues().get(TMetric.NATURAL_CONNECTIVITY), redeNeural.getMaxValues()[3]);
		}
		if (cn.getMetricValues().get(TMetric.AVERAGE_DEGREE) > redeNeural.getMaxValues()[4]) {
			System.out.printf("Normaliza��o violada (grau m�dio): ", cn.getMetricValues().get(TMetric.AVERAGE_DEGREE),
					redeNeural.getMaxValues()[4]);
		}
		if (cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH) > redeNeural.getMaxValues()[5]) {
			System.out.printf("Normaliza��o violada (comprimento m�dio do caminho): ",
					cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH), redeNeural.getMaxValues()[5]);
		}
		if (cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT) > redeNeural.getMaxValues()[6]) {
			System.out.printf("Normaliza��o violada (coef. agrupamento): ",
					cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT), redeNeural.getMaxValues()[6]);
		}
		if (cn.getMetricValues().get(TMetric.ENTROPY) > redeNeural.getMaxValues()[7]) {
			System.out.printf("Normaliza��o violada (entropia): ", cn.getMetricValues().get(TMetric.ENTROPY),
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

	private static StringBuffer sbNetworks = new StringBuffer();
	private static final int INTERVAL_SAVE = 100;
	private static int evals = 0;
	private static int seq = 0;
	private static Set<String> setNets = new HashSet<>();

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
		// for (double v : input) {
		// System.out.printf("%.8f\n", v);
		// }
		// double[] input = new double[] {
		// solution.getDecisionVariables()[solution.getDecisionVariables().length
		// - 1],
		// SWITCHES_COSTS_AND_LABELS[1][solution.getDecisionVariables()[solution.getDecisionVariables().length
		// - 2]],
		// cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT),
		// cn.getMetricValues().get(TMetric.DENSITY),
		// cn.getMetricValues().get(TMetric.PHYSICAL_AVERAGE_PATH_LENGTH),
		// cn.getMetricValues().get(TMetric.DFT_LAPLACIAN_ENTROPY), (maxLc -
		// minLc)/countLc };

		String hash = am.toString() + input[0] + input[1];
		if (!setNets.contains(hash) && cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY) > 1e-5) {
			evals++;
			sbNetworks.append(getLine(this, solution, linkCloseness)).append("\n");

			if (evals % INTERVAL_SAVE == 0) {
				seq++;
				try {
					FileWriter fw = new FileWriter(new File("non-uniform/complenet_consol_" + seq + ".txt"));

					fw.write(sbNetworks.toString());
					sbNetworks = new StringBuffer();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			setNets.add(hash);
		}
		if (redeNeural!= null) {
		return redeNeural.obterSaida(input, true);
		}else {
		       return 0;
		}

	}

	public static void main(String[] args) {
		// revalidadeFile(new String[] {
		// "C:/BACKUP_PENDRIVE_AZUL/Mestrado/Experimentos/2o/spea2/perfil4/r01/_spea2_50_100_100_001_1000_var.txt"
		// });
//		testLC();
		testSimples();
	}

	public static void testLC() {
		SimtonProblem p = new SimtonProblem(14, 2);
		Integer[][] nsfnetorigem = new Integer[][] { { 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
				{ 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0 }, { 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1 },
				{ 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0 }, //
				{ 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0 }, //
				{ 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1, 0 },//
				{ 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0 },//
				{ 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0 },//
				{ 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1 },
				{ 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0 } };
		Integer[] variables = new Integer[] { 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 4,
				20 };

		Double[][] distances = new Double[][] {
				{ 1.0E50, 1100.0, 1000.0, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 2800.00, 1.0E50, 1.0E50, 1.0E50, 1.0E50,
						1.0E50, 1.0E50 },
				{ 1100.0, 1.0E50, 600.0, 1000.0, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50,
						1.0E50, 1.0E50 },
				{ 1000.0, 600.0, 1.0E50, 1.0E50, 1.0E50, 2000.0, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50,
						1.0E50, 1.0E50 },
				{ 1.0E50, 1000.0, 1.0E50, 1.0E50, 600.0, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1600.0, 1.0E50,
						1.0E50, 1.0E50 },
				{ 1.0E50, 1.0E50, 1.0E50, 600.0, 1.0E50, 1100.0, 800.0, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50,
						1.0E50 },
				{ 1.0E50, 1.0E50, 2000.0, 1.0E50, 1100.0, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1200.0, 1.0E50, 1.0E50,
						1.0E50, 2000.0 },
				{ 1.0E50, 1.0E50, 1.0E50, 1.0E50, 800.0, 1.0E50, 1.0E50, 700.0, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50,
						1.0E50 }, //
				{ 2800.0, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 700.0, 1.0E50, 700.0, 1.0E50, 1.0E50, 1.0E50, 1.0E50,
						1.0E50 }, //
				{ 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 700.0, 1.0E50, 900.0, 1.0E50, 500.0, 500.0,
						1.0E50 },//
				{ 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1200.0, 1.0E50, 1.0E50, 900.0, 1.0E50, 1.0E50, 1.0E50,
						1.0E50, 1.0E50 },//
				{ 1.0E50, 1.0E50, 1.0E50, 2400.0, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 800.0, 800.0,
						1.0E50 },//
				{ 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 500.0, 1.0E50, 800.0, 1.0E50, 1.0E50,
						300.0 },
				{ 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 500.0, 1.0E50, 800.0, 1.0E50, 1.0E50,
						300.0 },
				{ 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 2000.0, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 1.0E50, 300.0, 300.0,
						1.0E50 } };

		SolutionONTD solution = new SolutionONTD(p, variables);
		List<Integer> networkRepresentation_ppr = Arrays.asList(solution.getDecisionVariables());
		long t = System.currentTimeMillis();
		Vector<Vector<Double>> adjacencyMatrixLabels;

		adjacencyMatrixLabels = p.buildAdjacencyMatrixLabels(networkRepresentation_ppr);

		Vector<Vector<Double>> adjacencyMatrix = p.buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				adjacencyMatrixLabels);
		ComplexNetwork cn = p.createComplexNetworkDistance((SolutionONTD) solution);

		// Double[][] linkCloseness =
		// LinkClosenessSequence.getInstance().transform(cn.getDistances());
		Double[][] linkCloseness = LinkClosenessSequence.getInstance().transform(distances);
		int maior = -1;
		int menor = Integer.MAX_VALUE;
		int count = 0;
		for (int i = 0; i < linkCloseness.length; i++) {
			for (int j = 0; j < linkCloseness.length; j++) {
				System.out.print(linkCloseness[i][j] + " ");
				if (linkCloseness[i][j] > 0) {
					count++;
					if (linkCloseness[i][j] > maior) {
						maior = linkCloseness[i][j].intValue();
					}
					if (linkCloseness[i][j] < menor) {
						menor = linkCloseness[i][j].intValue();
					}
				}
			}
			System.out.println();
		}
		System.out.println();
		for (int i = 0; i < linkCloseness.length; i++) {
			System.out.print("\\textbf{" + (i + 1) + "}");
			for (int j = 0; j < linkCloseness.length; j++) {
				if (i == j) {
					System.out.print(" & - ");
				} else {
					System.out.print(" & " + linkCloseness[i][j].intValue() + " ");
				}
			}
			System.out.println("\\\\\\hline");
		}

		System.out.println();
		System.out.println("Maior = " + maior);
		System.out.println("Menor = " + menor);
		System.out.println("r = " + count);
		System.out.println((maior - menor) / (1.0 * count));
	}

	public static void testSimples() {
		SimtonProblem p = new SimtonProblem(14, 2, 100);
		Integer[][] nsfnetorigem = new Integer[][] { 
				{ 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
				{ 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
				{ 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0 }, 
				{ 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1 },
				{ 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0 }, //
				{ 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0 }, //
				{ 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1, 0 },//
				{ 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0 },//
				{ 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0 },//
				{ 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1 }, { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1 },
				{ 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0 } };
		Integer[] variables = new Integer[] { 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 4,
				40 };


		SolutionONTD solution = new SolutionONTD(p, variables);

		SimtonProblem problem = new SimtonProblem(14, 2, 200);
		for (int i = 0; i <1; i++) {
			problem.evaluate(solution);
		}

	}

	
	/**
	 * @param args
	 */
	private static void revalidadeFile(String[] args) {
		SimtonProblem problem = new SimtonProblem(14, 2);
		problem.setSimulate(true);
		String path = args[0];
		SolutionSet<SolutionONTD> ss = new SolutionSet<>(50);
		ss.readIntVariablesFromFile(path, problem);
		StringBuffer sbLog = new StringBuffer();
		for (Solution sol : ss.getSolutionsList()) {
			problem.evaluate(sol);

			System.out.printf("%.8f %.8f \n", sol.getObjective(0), sol.getObjective(1));
			sbLog.append(String.format("%.8f %.8f \n", sol.getObjective(0), sol.getObjective(1)));
		}
		try {
			FileWriter fw = new FileWriter(new File("log-simton.txt"));
			fw.write(sbLog.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
