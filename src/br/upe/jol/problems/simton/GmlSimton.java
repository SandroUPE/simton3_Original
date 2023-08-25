/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: GmlSimton.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	24/03/2014		Versão inicial
 * ****************************************************************************
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
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

import br.cns.Geolocation;
import br.cns.TMetric;
import br.cns.experiments.ComplexNetwork;
import br.cns.experiments.nodePositions.CircularNetwork;
import br.cns.metrics.AveragePathLength;
import br.cns.metrics.Density;
import br.cns.model.GmlData;
import br.cns.model.GmlEdge;
import br.cns.model.GmlNode;
import br.cns.models.TModel;
import br.cns.persistence.GmlDao;
import br.cns.transformations.DegreeMatrix;
import br.cns.transformations.LinkClosenessSequence;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.Util;
import br.upe.jol.problems.simon.entity.CallSchedulerNonUniformHub;
import br.upe.jol.problems.simon.entity.Link;
import br.upe.jol.problems.simon.entity.NetworkProfile;
import br.upe.jol.problems.simon.entity.Node;
import br.upe.jol.problems.simon.rwa.SimpleDijkstraSimulator;

/**
 * 
 * @author Danilo
 * @since 24/03/2014
 */
public class GmlSimton extends Problem<Integer> {
	public static final int NUMBER_OF_AMPLIFIER_LABELS = 10;
	public static final int NUMBER_OF_SWITCH_LABELS = 5;
	public static final int MAX_NUMBER_OF_WAVELENGHTS = 40;
	public static final int MIN_NUMBER_OF_WAVELENGHTS = 4;
	public int numNodes;
	public static final double RAND_MAX = 0x7FFF;
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

	private static StringBuffer sbNetworks = new StringBuffer();
	private static final int INTERVAL_SAVE = 1000;
	private static int evals = 0;
	private static int seq = 0;
	private static Set<String> setNets = new HashSet<>();

	public static final NumberFormat format = DecimalFormat.getInstance();

	public static final NumberFormat format2 = DecimalFormat.getInstance();

	private String pathFiles = "";

	static {
		format.setMinimumFractionDigits(8);
		format.setMaximumFractionDigits(8);
		format2.setMinimumFractionDigits(2);
		format2.setMaximumFractionDigits(2);
	}

	public double[][] geolocationCoords;

	public Double[][] distances;

	public static double[][] AMPLIFIERS_COSTS_AND_LABELS = new double[][] {
			{ 0, 0.75, 1.5, 2.25, 0.5, 1, 1.5, 0.25, 0.5, 0.75 }, // cost
			// index
			{ 0, 13, 16, 19, 13, 16, 19, 13, 16, 19 }, // amplifier saturation
			// power
			{ 0, 5, 5, 5, 7, 7, 7, 9, 9, 9 } // amplifier noise figure
	};

	public static double[][] SWITCHES_COSTS_AND_LABELS = new double[][] { { 0.25, 0.5, 0.75, 1, 1.5, 2.0 }, // costs
			{ 27, 30, 33, 35, 38, 40 } }; // isolation factor in dB

	double LINK_ANT_COST = 0.4;
	// public static final double MIN_CALLS = 500;
	// public static final double PRECISION = 0.002;
	public static final double MIN_CALLS = 500;
	public static final double PRECISION = 0.00001;
	// public static final double CALLS = MIN_CALLS / PRECISION;

	public static final double CALLS = 1000000;
	// public static final double CALLS = 100000;

	private static final GmlDao dao = new GmlDao();

	private double networkLoad;

	private GmlData data;

	public GmlSimton(int numberOfNodes_ppr, int numberOfObjectives, String gmlFile, double networkLoad) {
		this(numberOfNodes_ppr, numberOfObjectives, dao.loadGmlData(gmlFile), networkLoad);
	}

	public GmlSimton(int numberOfNodes_ppr, int numberOfObjectives, GmlData data, double networkLoad) {
		super(((numberOfNodes_ppr * numberOfNodes_ppr - numberOfNodes_ppr) / 2) + 2, numberOfObjectives,
				"Projeto de Redes Ópticas");
		this.numNodes = numberOfNodes_ppr;
		this.networkLoad = networkLoad;
		this.data = data;
		geolocationCoords = new double[numNodes][2];
		int count = 0;
		GeolocationConverter gc = new GeolocationConverter();
		for (GmlNode node : data.getNodes()) {
			geolocationCoords[count][0] = gc.mercX(node.getLongitude());
			geolocationCoords[count][1] = gc.mercY(node.getLatitude());
			count++;
		}

		Geolocation[] locations = new Geolocation[numNodes];
		for (int i = 0; i < locations.length; i++) {
			locations[i] = new Geolocation(data.getNodes().get(i).getLatitude(), data.getNodes().get(i).getLongitude());
		}
		distances = new Double[numNodes][numNodes];
		for (int i = 0; i < numNodes; i++) {
			distances[i][i] = 0.0;
			for (int j = i + 1; j < numNodes; j++) {
				distances[i][j] = computeDistance(locations[i], locations[j], true);
				distances[j][i] = distances[i][j];
			}
		}

		this.numberOfNodes = numberOfNodes_ppr;
		for (int i = 0; i < numberOfVariables - 2; i++) {
			upperLimit[i] = NUMBER_OF_AMPLIFIER_LABELS - 1;
			lowerLimit[i] = 0;
		}

		upperLimitObjective = new double[numberOfObjectives];
		lowerLimitObjective = new double[numberOfObjectives];

		lowerLimitObjective[0] = 0;
		upperLimitObjective[0] = 1;
		lowerLimitObjective[1] = 420;
		upperLimitObjective[1] = 100141.7028;
		// lowerLimitObjective[2] = 0;
		// upperLimitObjective[2] = 40000000;
		// lowerLimitObjective[3] = 0;
		// upperLimitObjective[3] = 1000;

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
				nodePositions.get(i).set(j, geolocationCoords[i][j]);

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
				false, true, 10e9, 0.013e-9, 1.0, 0.04e-12 / sqrt(1000), 0.0, 10.0, LAMBDA_FIRSTFIT, UTILIZAR_DIJ,
				false, MAX_NUMBER_OF_WAVELENGHTS);
		simulator = new SimpleDijkstraSimulator();
	}

	protected double computeDistance(Geolocation startCoords, Geolocation destCoords) {
		double startLatRads = startCoords.getLatitude();
		double startLongRads = startCoords.getLongitude();
		double destLatRads = destCoords.getLatitude();
		double destLongRads = destCoords.getLongitude();
		double radius = 6371; // raio da Terra em km
		double distance = Math
				.acos(Math.sin(startLatRads) * Math.sin(destLatRads)
						+ Math.cos(startLatRads) * Math.cos(destLatRads) * Math.cos(startLongRads - destLongRads))
				* radius;
		return distance;
	}

	protected double computeDistance(Geolocation startCoords, Geolocation destCoords, boolean convertToRadians) {
		if (convertToRadians) {
			return computeDistance(
					new Geolocation(degreesToRadians(startCoords.getLatitude()),
							degreesToRadians(startCoords.getLongitude())),
					new Geolocation(degreesToRadians(destCoords.getLatitude()),
							degreesToRadians(destCoords.getLongitude())));
		}
		return computeDistance(startCoords, destCoords);
	}

	protected double degreesToRadians(double degrees) {
		return (degrees * Math.PI) / 180;
	}

	public synchronized void evaluate(Solution<Integer> solution) {
		List<Integer> networkRepresentation_ppr = Arrays.asList(solution.getDecisionVariables());
		Vector<Vector<Double>> adjacencyMatrixLabels;

		adjacencyMatrixLabels = buildAdjacencyMatrixLabels(networkRepresentation_ppr);

		Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				adjacencyMatrixLabels);
		ComplexNetwork cn = createComplexNetworkDistance((SolutionONTD) solution);
		try {
			if (cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY) <= 1e-5) {
				solution.setObjective(0, 1);
			} else {
				evaluateNetwokBlockingProbability((SolutionONTD) solution, networkRepresentation_ppr,
						adjacencyMatrixLabels, adjacencyMatrix);
				if (solution.getObjective(0) < 1e-5) {
					solution.setObjective(0, 1e-5);
				}
			}

			int absence = 0;
			for (GmlEdge edge : data.getEdges()) {
				if (cn.getAdjacencyMatrix()[edge.getSource().getId()][edge.getTarget().getId()] == 0) {
					absence++;
				}
			}
			double percPenalty = absence * 0.015;
			solution.setObjective(1, solution.getObjective(1) * (1 + percPenalty));

			evaluateNetworkCapex((SolutionONTD) solution, networkRepresentation_ppr, adjacencyMatrixLabels,
					adjacencyMatrix);
			// evaluateNetworkPowerConsumption((SolutionONTD) solution,
			// networkRepresentation_ppr, adjacencyMatrixLabels,
			// adjacencyMatrix, cn);
			// solution.setObjective(3,
			// cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY));
			// saveDataToRna((SolutionONTD) solution, cn);
		} catch (UnreliableSimulationException e) {
			((SolutionONTD) solution).setObjective(0, upperLimitObjective[0] + PRECISION);
			((SolutionONTD) solution).setObjective(1, upperLimitObjective[1] + PRECISION);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Util.LOGGER.info("Avaliação (" + format.format(((SolutionONTD) solution).getObjective(0)) + ", "
				+ format2.format(((SolutionONTD) solution).getObjective(1)) + ")");
		// + format2.format(((SolutionONTD) solution).getObjective(2)) + ", "
		// + format2.format(((SolutionONTD) solution).getObjective(3)) + ").");
	}

	public SolutionONTD getDefaultSolution() {

		int numNodes = data.getNodes().size();

		Integer[] dv = new Integer[numNodes * (numNodes - 1) / 2 + 2];
		int counter = 0;
		for (int i = 0; i < numNodes; i++) {
			for (int j = i+1; j < numNodes; j++) {
				dv[counter] = 0;
				for (GmlEdge edge : data.getEdges()) {
					if ((edge.getSource().getId() == i && edge.getTarget().getId() == j)
							|| (edge.getSource().getId() == j && edge.getTarget().getId() == i)) {
						dv[counter] = 3;
					}
				}
				counter++;
			}
		}
		dv[dv.length - 1] = 96; // w
		dv[dv.length - 2] = 4; // oxc

		return new SolutionONTD(this, dv);
	}

	public void saveDataToRna(SolutionONTD solution, ComplexNetwork cn) {
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
		for (int i = 0; i < numNodes; i++) {
			for (int j = i + 1; j < numNodes; j++) {
				orderedCloseness.add(linkCloseness[i][j].intValue());
			}
		}
		Collections.sort(orderedCloseness);

		double[] input = createDataToRna(solution, cn);

		// for (double d : input) {
		// System.out.printf("%.4f ", d);
		// }

		String hash = am.toString() + input[0] + input[1];
		if (!setNets.contains(hash) && cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY) > 1e-5) {
			evals++;
			sbNetworks.append(getLine(this, solution, linkCloseness)).append("\n");

			if (evals % INTERVAL_SAVE == 0) {
				seq++;
				try {
					FileWriter fw = new FileWriter(new File(pathFiles + "/dataset_" + seq + ".txt"));

					fw.write(sbNetworks.toString());
					sbNetworks = new StringBuffer();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public double[] createDataToRna(SolutionONTD solution, ComplexNetwork cn) {
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

		List<Integer> networkRepresentation_ppr = Arrays.asList(solution.getDecisionVariables());
		Vector<Vector<Double>> adjacencyMatrixLabels;

		adjacencyMatrixLabels = buildAdjacencyMatrixLabels(networkRepresentation_ppr);

		Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				adjacencyMatrixLabels);
		netwokSimulationMinimumLoad(solution, networkRepresentation_ppr, adjacencyMatrixLabels, adjacencyMatrix);

		List<Integer> orderedCloseness = new Vector<>();
		Double[][] linkCloseness = LinkClosenessSequence.getInstance().transform(cn.getDistances());
		for (int i = 0; i < numNodes; i++) {
			for (int j = i + 1; j < numNodes; j++) {
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

		double[] input = new double[] { solution.getDecisionVariables()[solution.getDecisionVariables().length - 1],
				SWITCHES_COSTS_AND_LABELS[1][solution.getDecisionVariables()[solution.getDecisionVariables().length
						- 2]],
				cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT), cn.getMetricValues().get(TMetric.DENSITY),
				cn.getMetricValues().get(TMetric.PHYSICAL_AVERAGE_PATH_LENGTH),
				cn.getMetricValues().get(TMetric.DFT_LAPLACIAN_ENTROPY), (maxLc - minLc) / countLc, 100,
				cn.getMetricValues().get(TMetric.PHYSICAL_DENSITY), profile.getMinimumBp().getBer() };

		return input;
	}

	public double[] createDataToRna4(SolutionONTD solution, ComplexNetwork cn) {
		double[] input = new double[] { solution.getDecisionVariables()[solution.getDecisionVariables().length - 1],
				Density.getInstance().calculate(cn.getAdjacencyMatrix()),
				AveragePathLength.getInstance().calculate(cn.getAdjacencyMatrix()), 100 };

		return input;
	}

	public double[] createDataToRna(SolutionONTD solution, ComplexNetwork cn, int numParams) {
		List<Integer> networkRepresentation_ppr = Arrays.asList(solution.getDecisionVariables());
		Vector<Vector<Double>> adjacencyMatrixLabels;

		adjacencyMatrixLabels = buildAdjacencyMatrixLabels(networkRepresentation_ppr);

		Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				adjacencyMatrixLabels);

		return calculateMetricsWithSimulation(solution, networkRepresentation_ppr, adjacencyMatrixLabels,
				adjacencyMatrix, numParams);
	}

	private StringBuffer getLine(GmlSimton ontd, SolutionONTD solution, Double[][] linkCloseness) {
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

		fileContent.append(nf.format(solution.getObjective(1))); // 0 - custo
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(solution.getObjective(0))); // 1 - BP
		fileContent.append(outerSeparator);
		fileContent.append(solution.getDecisionVariables()[solution.getDecisionVariables().length - 1].shortValue()); // 2
																														// -
																														// wavelengths
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(SWITCHES_COSTS_AND_LABELS[1][solution
				.getDecisionVariables()[solution.getDecisionVariables().length - 2]])); // 3
																						// -
																						// oxc
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY))); // 4
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.NATURAL_CONNECTIVITY))); // 5
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.DENSITY))); // 6
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.AVERAGE_DEGREE))); // 7
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH))); // 8
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.CLUSTERING_COEFFICIENT))); // 9
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.DIAMETER))); // 10
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.ENTROPY))); // 11
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.PHYSICAL_DIAMETER))); // 12
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.PHYSICAL_AVERAGE_PATH_LENGTH))); // 13
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.DFT_LAPLACIAN_ENTROPY))); // 14
		fileContent.append(outerSeparator);
		fileContent.append(am.toString()); // 15 - adjacencyMatrix
		fileContent.append(outerSeparator);
		fileContent.append(aam.toString()); // 16 - adjacencyMatrix
		fileContent.append(outerSeparator);
		for (int i = 0; i < numNodes; i++) {
			for (int j = i + 1; j < numNodes; j++) {
				fileContent.append(linkCloseness[i][j]).append(" "); // 17
			}
		}
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(cn.getMetricValues().get(TMetric.PHYSICAL_DENSITY))); // 18
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(profile.getBp().getBer())); // 19
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(profile.getBp().getDispersion())); // 20
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(profile.getBp().getLambda())); // 21
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(profile.getBp().getMeanDist())); // 22
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(profile.getMinimumBp().getBer())); // 23
		fileContent.append(outerSeparator);
		fileContent.append(nf.format(profile.getMinimumBp().getDispersion())); // 24
		fileContent.append(outerSeparator);

		return fileContent;
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

	public synchronized void evaluateNetworkCapex(SolutionONTD solution, List<Integer> networkRepresentation_ppr,
			Vector<Vector<Double>> labelMatrix_loc, Vector<Vector<Double>> adjacencyMatrix) {
		int vectorSize_loc = networkRepresentation_ppr.size();
		double amplifierCost_loc = 0;
		double switchCost_loc = 0;
		double wavelengthCost_loc = 0;
		double dcfCost_loc = 0;
		double ssmfCost_loc = 0;
		double deploymentCost_loc = 0;
		double totalCost_loc = 0;

		int numberOfWavelenghts_loc = networkRepresentation_ppr.get(vectorSize_loc - 1);
		for (int i = 0; i < vectorSize_loc - 2; i++) {
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

		// assigns capital costs data
		solution.setNetwork(profile);
		solution.setObjective(1, totalCost_loc);
	}

	public synchronized void evaluateNetworkPowerConsumption(SolutionONTD solution,
			List<Integer> networkRepresentation_ppr, Vector<Vector<Double>> labelMatrix_loc,
			Vector<Vector<Double>> adjacencyMatrix, ComplexNetwork cn) {
		int nc = 2; // facilities for cooling and overhead
		int npr = 2; // facilities for protection
		int nd = 1; // constant related to the number of ip/mpls demands
		int dc = 1; // average demand capacity

		int constantFactor = nc * npr * nd * dc;

		double H = cn.getMetricValues().get(TMetric.AVERAGE_PATH_LENGTH); // average
																			// layer
																			// hop
																			// count

		int vectorSize_loc = networkRepresentation_ppr.size();
		double amplifierConsumption = 0;
		double wdmTerminalConsumption = 0; // mux/demux, pre e booster por par
											// de fibra
		double trConsumption = 0;
		double switchConsumption = 0;
		double totalConsumption = 0;

		int numberOfWavelenghts = networkRepresentation_ppr.get(vectorSize_loc - 1);

		for (int m = 0; m < numberOfNodes; m++) {
			int nodeDegree = 0;

			// os proximos dois for sao para calcular o grau do no de indice m
			for (int j = m + 1; j < numberOfNodes; j++) { // varre para uma
				// linha fixa da matriz
				int k = indiceVetor_mpr(j, m); // k1
				if (networkRepresentation_ppr.get(k) != 0)
					nodeDegree++;
			}
			for (int i = 0; i < m; i++) { // varre para uma coluna fixa da
				// matriz
				int k = indiceVetor_mpr(m, i); // k2
				if (networkRepresentation_ppr.get(k) != 0)
					nodeDegree++;
			}
			trConsumption += 50 * numberOfWavelenghts;
			switchConsumption += 150 + numberOfWavelenghts * ((85 + 50) * nodeDegree) / 40;
			wdmTerminalConsumption += numberOfWavelenghts * (230 * nodeDegree) / 40;
		}

		double totalNetworkLength = 0;
		for (int i = 0; i < numberOfNodes; i++)
			for (int j = 0; j < numberOfNodes; j++)
				if (adjacencyMatrix.get(i).get(j) < INF) {
					totalNetworkLength += adjacencyMatrix.get(i).get(j);
				}

		amplifierConsumption = (110 * totalNetworkLength) / 80;

		// state the total network cost by adding the separated costs
		totalConsumption = amplifierConsumption + switchConsumption + wdmTerminalConsumption + trConsumption;
		totalConsumption *= H * constantFactor * networkLoad / 20;
		// assigns capital costs data
		solution.setNetwork(profile);
		solution.setObjective(2, totalConsumption);
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
					adjacencyMatrix_loc.get(i).set(j, getDistances()[i][j]);
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
			Vector<Vector<Double>> adjacencyMatrix) throws UnreliableSimulationException {
		double numberOfCalls_loc = CALLS;
		int maximum = 1;
		double lastSimulation_loc = 0;
		double parcialSimul = 0;

		for (int i = 0; i < maximum; i++) {
			parcialSimul = netwokSimulation(numberOfCalls_loc, solution, networkRepresentation_ppr, labelMatrix_loc,
					adjacencyMatrix);
			lastSimulation_loc += parcialSimul;
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

		profile.setEpsilon(
				pow(10, -0.1 * switchCostsAndTypes.get(1).get(networkRepresentation_ppr.get(vectorSize_loc - 2))));
		profile.setLinks(links);
		profile.cleanBp();
		profile.setCompleteDistances(distances);
		// double[] metrics =
		// simulator.calculate12MetricsWithSimulation(profile);
		simulator.simulate(profile, new CallSchedulerNonUniformHub(numberOfNodes, networkLoad), (int) MIN_CALLS);
		// simulator.simulate(profile, new
		// CallSchedulerUniform(profile.getMeanRateBetweenCalls(),
		// profile.getMeanRateOfCallsDuration(), profile.getNodes().size()),
		// (int) MIN_CALLS);

		distancias = null;
		matrizGanho = null;
		links = null;

		return profile.getBp().getTotal();
	}

	public double[] calculateMetricsWithSimulation(SolutionONTD solution, int numParams) {
		List<Integer> networkRepresentation_ppr = Arrays.asList(solution.getDecisionVariables());
		Vector<Vector<Double>> adjacencyMatrixLabels;

		adjacencyMatrixLabels = buildAdjacencyMatrixLabels(networkRepresentation_ppr);

		Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				adjacencyMatrixLabels);

		return calculateMetricsWithSimulation(solution, networkRepresentation_ppr, adjacencyMatrixLabels,
				adjacencyMatrix, numParams);
	}

	public static double totalTime;
	public static int totalEvals;

	public double[] calculateMetricsWithSimulation(SolutionONTD solution, List<Integer> networkRepresentation_ppr,
			Vector<Vector<Double>> adjacencyMatrixLabels, Vector<Vector<Double>> adjacencyMatrixDistances,
			int numParams) {
		double[] metrics = null;
		int vectorSize_loc = networkRepresentation_ppr.size();

		Double[][] distancias = getDistancias(adjacencyMatrixDistances);
		Link[][] links;
		double[][] matrizGanho;

		// Creating the matrix that defines network topology
		links = new Link[numberOfNodes][numberOfNodes];
		// Creating the matrix that defines the gains in amp on network
		matrizGanho = new double[numberOfNodes][numberOfNodes];
		for (int k = 0; k < numberOfNodes; k++) {
			links[k] = new Link[numberOfNodes];
			matrizGanho[k] = new double[numberOfNodes];
			// inicia matriz de ganhos com zeros
			for (int w = 0; w < numberOfNodes; w++) {
				matrizGanho[k][w] = 0;
			}
		}

		// determines the number of wavelength per link
		int NLAMBDAS = networkRepresentation_ppr.get(vectorSize_loc - 1);

		// inicia matriz de ganhos com valor de ganhos corretos
		for (int k = 0; k < numberOfNodes; k++) {
			for (int w = 0; w < numberOfNodes; w++) {
				if (distancias[k][w] != INF) {
					matrizGanho[k][w] = ((distancias[k][w] / 2.0) * 0.2 - GMUX - GSWITCH / 2) * MF;
				}
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
		}

		profile.setOxcIsolationFactor(
				switchCostsAndTypes.get(1).get(networkRepresentation_ppr.get(vectorSize_loc - 2)));
		profile.setCompleteDistances(getDistances());
		profile.setEpsilon(pow(10, -0.1 * profile.getOxcIsolationFactor()));
		profile.setLinks(links);
		profile.cleanBp();
		if (numParams == 5) {
			long t0 = System.nanoTime();
			metrics = simulator.calculate5MetricsWithSimulation(profile);
			totalTime += (System.nanoTime() - t0);
			totalEvals++;
		} else if (numParams == 8) {
			long t0 = System.nanoTime();
			metrics = simulator.calculate8MetricsWithSimulation(profile);
			totalTime += (System.nanoTime() - t0);
			totalEvals++;
		} else if (numParams == 10) {
			long t0 = System.nanoTime();
			metrics = simulator.calculate10MetricsWithSimulation(profile);
			totalTime += (System.nanoTime() - t0);
			totalEvals++;
		} else if (numParams == 9) {
			long t0 = System.nanoTime();
			metrics = simulator.calculate9MetricsWithSimulation(profile);
			totalTime += (System.nanoTime() - t0);
			totalEvals++;
		} else if (numParams == 6) {
			long t0 = System.nanoTime();
			metrics = simulator.calculate12MetricsWithSimulation(profile);
			double[] m = new double[6];
			// fltg
			m[0] = metrics[0];
			m[1] = metrics[1];
			m[2] = metrics[6];
			m[3] = metrics[7];
			m[4] = metrics[8];
			m[5] = metrics[9];
			// arnes
			// m[0] = metrics[0];
			// m[1] = metrics[1];
			// m[2] = metrics[2];
			// m[3] = metrics[3];
			// m[4] = metrics[6];
			// m[5] = metrics[7];
			// medianet
			// m[0] = metrics[0];
			// m[1] = metrics[1];
			// m[2] = metrics[3];
			// m[3] = metrics[6];
			// m[4] = metrics[7];
			// m[5] = metrics[9];
			metrics = m;
			totalTime += (System.nanoTime() - t0);
			totalEvals++;
		} else {
			long t0 = System.nanoTime();
			metrics = simulator.calculate12MetricsWithSimulation(profile);
			totalTime += (System.nanoTime() - t0);
			totalEvals++;
		}
		return metrics;
	}

	public double netwokSimulationMinimumLoad(SolutionONTD solution, List<Integer> networkRepresentation_ppr,
			Vector<Vector<Double>> adjacencyMatrixLabels, Vector<Vector<Double>> adjacencyMatrixDistances) {
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

		profile.setEpsilon(
				pow(10, -0.1 * switchCostsAndTypes.get(1).get(networkRepresentation_ppr.get(vectorSize_loc - 2))));
		profile.setLinks(links);
		profile.cleanBp();

		simulator.calculateMinimumLoad(profile);

		distancias = null;
		matrizGanho = null;
		links = null;

		return profile.getBp().getTotal();
	}

	public static void main(String[] args) {
//		changeLoad("C:/workspace_phd/Icton15/results/nsfnet_100e/", "C:/workspace_phd/Icton15/results/nsfnet_150m/",
//				150);
//		 revalidadeFile(new String[] {
//		 "C:/BACKUP_PENDRIVE_AZUL/Mestrado/Experimentos/2o/spea2/perfil4/r01/_spea2_50_100_100_001_1000_var.txt"
//		 });
		
		
		System.out.println((int)'A');
		
		System.out.println("Qual a primeira letra?");
		//int l1 = 
		
	}

	public static Double[][] createUniformMatrix(int numNodes) {
		Double[][] traffic = new Double[numNodes][numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = i + 1; j < numNodes; j++) {
				traffic[i][j] = 1.0;
				traffic[j][i] = 1.0;
			}
		}

		return traffic;
	}

	/**
	 * @param args
	 */
	private static void changeLoad(String inPath, String outPath, int targerLoad) {
		String strNet = "Nsfnet.gml";
		String strNetS = "nsfnet_150e";
		String basePath = "C:/workspace_phd/Icton15/results/";

		GmlDao dao = new GmlDao();
		GmlData data = dao.loadGmlData(basePath + strNet);
		int numNodes = data.getNodes().size();
		Double[][] traffic = createUniformMatrix(numNodes);
		CallSchedulerNonUniformHub.TRAFFICMATRIX = traffic;
		Geolocation[] locations = new Geolocation[numNodes];
		File newDir = new File(basePath + "/" + strNetS + "/");
		newDir.mkdir();
		for (int i = 0; i < locations.length; i++) {
			locations[i] = new Geolocation(data.getNodes().get(i).getLatitude(), data.getNodes().get(i).getLongitude());
		}
		StringBuffer sbLog = null;
		GmlSimton problem = new GmlSimton(numNodes, 2, data, targerLoad);
		FileReader fr = null;
		LineNumberReader lnr = null;
		String linha = null;
		String[] values = null;
		for (String arq : new File(inPath).list()) {
			File f = new File(inPath + File.separator + arq);
			if (!f.isFile() || new File(outPath + File.separator + arq).exists()) {
				continue;
			}
			try {
				sbLog = new StringBuffer();
				fr = new FileReader(new File(inPath + File.separator + arq));
				lnr = new LineNumberReader(fr);
				linha = lnr.readLine();
				NumberFormat nf = NumberFormat.getInstance();
				while (linha != null) {
					values = linha.split(";");
					String[] strDv = values[16].split(" ");
					Integer[] dv = new Integer[strDv.length + 2];
					for (int k = 0; k < strDv.length; k++) {
						dv[k] = Integer.valueOf(strDv[k]);
					}
					dv[dv.length - 1] = Integer.valueOf(values[2]); // w
					dv[dv.length - 2] = 4; // oxc
					for (int k = 0; k < SWITCHES_COSTS_AND_LABELS[1].length; k++) {
						if (SWITCHES_COSTS_AND_LABELS[1][k] == nf.parse(values[3]).doubleValue()) {
							dv[dv.length - 2] = k;
							break;
						}
					}

					SolutionONTD sol = new SolutionONTD(problem, dv);

					problem.evaluate(sol);

					linha = lnr.readLine();

					for (int k = 0; k < values.length; k++) {
						if (k == 1) {
							sbLog.append(nf.format(sol.getObjective(0)) + ";");
						} else {
							sbLog.append(values[k] + ";");
						}
					}
					sbLog.append("\n");
				}

				fr.close();
				lnr.close();

				FileWriter fw = new FileWriter(new File(outPath + File.separator + arq));
				fw.write(sbLog.toString());
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		matrix = new Integer[numNodes][numNodes];
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
		Integer[] var = new Integer[numNodes * (numNodes - 1) / 2];
		Arrays.fill(var, 1);
		List<Integer> networkRepresentation_ppr = Arrays.asList(var);
		Double[][] distances = getDistancias(buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				buildAdjacencyMatrixLabels(networkRepresentation_ppr)));

		return distances;
	}

	public ComplexNetwork createComplexNetworkDistance(SolutionONTD solution) {
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
		metrics.add(TMetric.PHYSICAL_DENSITY);

		Integer[][] matrix = new Integer[numNodes][numNodes];
		double value = 0;
		int counter = 0;
		Double[][] customDistances = new Double[numNodes][numNodes];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i][i] = 0;
			customDistances[i][i] = 0.0;
			for (int j = i + 1; j < matrix.length; j++) {
				value = Integer.valueOf(solution.getDecisionVariables()[counter]);
				if (value != 0) {
					matrix[i][j] = 1;
					matrix[j][i] = 1;
					customDistances[i][j] = distances[i][j];
					customDistances[j][i] = distances[j][i];
				} else {
					matrix[i][j] = 0;
					matrix[j][i] = 0;
					customDistances[i][j] = 0.0;
					customDistances[j][i] = 0.0;
				}
				counter++;
			}
		}
		return new ComplexNetwork(0, matrix, new double[numNodes][numNodes], distances, customDistances, TModel.CUSTOM,
				metrics);
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
	 * @return o valor do atributo data
	 */
	public GmlData getData() {
		return data;
	}

	/**
	 * Altera o valor do atributo data
	 * 
	 * @param data
	 *            O valor para setar em data
	 */
	public void setData(GmlData data) {
		this.data = data;
	}

	/**
	 * @return o valor do atributo networkLoad
	 */
	public double getNetworkLoad() {
		return networkLoad;
	}

	/**
	 * Altera o valor do atributo networkLoad
	 * 
	 * @param networkLoad
	 *            O valor para setar em networkLoad
	 */
	public void setNetworkLoad(double networkLoad) {
		this.networkLoad = networkLoad;
	}

	/**
	 * @return o valor do atributo setNets
	 */
	public static Set<String> getSetNets() {
		return setNets;
	}

	/**
	 * Altera o valor do atributo setNets
	 * 
	 * @param setNets
	 *            O valor para setar em setNets
	 */
	public static void setSetNets(Set<String> setNets) {
		GmlSimton.setNets = setNets;
	}

	/**
	 * @return o valor do atributo pathFiles
	 */
	public String getPathFiles() {
		return pathFiles;
	}

	/**
	 * Altera o valor do atributo pathFiles
	 * 
	 * @param pathFiles
	 *            O valor para setar em pathFiles
	 */
	public void setPathFiles(String pathFiles) {
		this.pathFiles = pathFiles;
	}

	/**
	 * @return o valor do atributo numberOfNodes
	 */
	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	/**
	 * Altera o valor do atributo numberOfNodes
	 * 
	 * @param numberOfNodes
	 *            O valor para setar em numberOfNodes
	 */
	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}

	/**
	 * @return o valor do atributo distances
	 */
	public Double[][] getDistances() {
		return distances;
	}

	/**
	 * Altera o valor do atributo distances
	 * 
	 * @param distances
	 *            O valor para setar em distances
	 */
	public void setDistances(Double[][] distances) {
		this.distances = distances;
	}

}
