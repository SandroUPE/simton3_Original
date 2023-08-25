/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: EA_CSM.java
 * ****************************************************************************
 * Histórico de revisões
 * Nome				Data		Descrição
 * ****************************************************************************
 * Danilo Araújo	28/05/2015		Versão inicial
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import br.cns.Geolocation;
import br.cns.TMetric;
import br.cns.experiments.ComplexNetwork;
import br.cns.experiments.nodePositions.CircularNetwork;
import br.cns.metrics.AveragePathLength;
import br.cns.metrics.Density;
import br.cns.model.GmlData;
import br.cns.model.GmlNode;
import br.cns.models.TModel;
import br.cns.persistence.GmlDao;
import br.cns.transformations.DegreeMatrix;
import br.cns.transformations.LinkClosenessSequence;
import br.grna.bp.RedeNeural;
import br.upe.jol.base.Interval;
import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.base.StatsSummary;
import br.upe.jol.problems.simon.entity.CallSchedulerNonUniformHub;
import br.upe.jol.problems.simon.entity.Link;
import br.upe.jol.problems.simon.entity.NetworkProfile;
import br.upe.jol.problems.simon.entity.Node;
import br.upe.jol.problems.simon.rwa.SimpleDijkstraSimulator;
import br.upe.jol.problems.simton.GeolocationConverter;
import br.upe.jol.problems.simton.GmlSimton;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo Araujo
 * @since 28/05/2015
 */
public class EaCsm extends Problem<Integer> {
	private static final int INSIDE_BAND = 1;
	private static final int NOT_INITIALIZED = 0;
	private static final int OUTSIDE_RANGE_LEFT = 3;
	private static final int OUTSIDE_RANGE_RIGHT = 2;
	public static final int NUMBER_OF_AMPLIFIER_LABELS = 10;
	public static final int NUMBER_OF_SWITCH_LABELS = 5;
	public static final int MAX_NUMBER_OF_WAVELENGHTS = 40;
	public static final int MIN_NUMBER_OF_WAVELENGHTS = 4;
	public int numNodes;
	public static final double RAND_MAX = 0x7FFF;
	public int probAvals = 0;
	public double minProbability = 1;
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

	private static StringBuffer sbNetworks = new StringBuffer();
	private static final int INTERVAL_SAVE = 1000;
	private static int evals = 0;
	private static int seq = 0;
	private static Set<String> setNets = new HashSet<>();

	public static final NumberFormat format = DecimalFormat.getInstance();

	private String pathFiles = "";

	private Map<Interval, StatsSummary> mapCosts;

	/**
	 * Fator de amortecimento no cálculo da média
	 */
	private static final double ALPHA = 0.6; 

	/**
	 * Número de vezes acima do desvio padrão para aceite
	 */
	private static final double RANGE_STD_DVT = 2.0; 

	private RedeNeural redeNeural;

	static {
		format.setMinimumFractionDigits(8);
		format.setMaximumFractionDigits(8);
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
	public static final double MIN_CALLS = 1000;
	public static final double PRECISION = 0.00001;

	public static final double CALLS = 1000000;

	private static final GmlDao dao = new GmlDao();

	private double networkLoad;

	private GmlData data;

	private double[] limits = new double[] { 0.10, 0.01, 0.001, 0.0001, 0.00001 };
//
	private int[] limitsMinCalls = new int[] { 0, 500, 500, 250, 250 };
	
//	private double[] limits = new double[] { 0.01, 0.001, 0.0001, 0.00001 };

//	private int[] limitsMinCalls = new int[] { 0, 500, 250, 250 };

	private double[] limitsMaxCalls = new double[] { 0, 2000, 10000, 100000, 1000000 };

//	private double[] limitsMaxCalls = new double[] { 0, 10000, 100000, 1000000 };

	public static long[] evaluations = new long[5];

	public static double numEvaluations = 0;

	public EaCsm(int numberOfNodes_ppr, int numberOfObjectives, String gmlFile, double networkLoad) {
		this(numberOfNodes_ppr, numberOfObjectives, dao.loadGmlData(gmlFile), networkLoad, null);
	}

	public EaCsm(int numberOfNodes_ppr, int numberOfObjectives, GmlData data, double networkLoad, RedeNeural rna) {
		super(((numberOfNodes_ppr * numberOfNodes_ppr - numberOfNodes_ppr) / 2) + 2, numberOfObjectives,
				"Projeto de Redes Ópticas");
		this.redeNeural = rna;
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

		upperLimitObjective[0] = 1;
		upperLimitObjective[1] = 20141.7028;
		lowerLimitObjective[0] = 0;
		lowerLimitObjective[1] = 420;

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
		mapCosts = getMap(this);
	}

	protected double computeDistance(Geolocation startCoords, Geolocation destCoords) {
		double startLatRads = startCoords.getLatitude();
		double startLongRads = startCoords.getLongitude();
		double destLatRads = destCoords.getLatitude();
		double destLongRads = destCoords.getLongitude();
		double radius = 6371; // raio da Terra em km
		double distance = Math.acos(Math.sin(startLatRads) * Math.sin(destLatRads) + Math.cos(startLatRads)
				* Math.cos(destLatRads) * Math.cos(startLongRads - destLongRads))
				* radius;
		return distance;
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

	protected double computeDistance(Geolocation startCoords, Geolocation destCoords, boolean convertToRadians) {
		if (convertToRadians) {
			return computeDistance(
					new Geolocation(degreesToRadians(startCoords.getLatitude()),
							degreesToRadians(startCoords.getLongitude())),
					new Geolocation(degreesToRadians(destCoords.getLatitude()), degreesToRadians(destCoords
							.getLongitude())));
		}
		return computeDistance(startCoords, destCoords);
	}

	protected double degreesToRadians(double degrees) {
		return (degrees * Math.PI) / 180;
	}

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
			double[] metrics = calculateMetricsWithSimulation((SolutionONTD) solution, redeNeural.getCamadaEntrada().length);
			metrics[7] = networkLoad;
			double pb = redeNeural.obterSaida(metrics, true);
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
			evaluations[i]++;
			numEvaluations++;
			for (int e = 0; e < evaluations.length; e++) {
				System.out.printf("%d: %.4f\t", e, evaluations[e] / numEvaluations);
			}
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

		for (double d : input) {
			System.out.printf("%.4f ", d);
		}

		String hash = am.toString() + input[0] + input[1];
		if (!setNets.contains(hash) && cn.getMetricValues().get(TMetric.ALGEBRAIC_CONNECTIVITY) > 1e-5) {
			evals++;

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

		double[] input = new double[] {
				solution.getDecisionVariables()[solution.getDecisionVariables().length - 1],
				SWITCHES_COSTS_AND_LABELS[1][solution.getDecisionVariables()[solution.getDecisionVariables().length - 2]],
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
		fileContent.append(nf.format(SWITCHES_COSTS_AND_LABELS[1][solution.getDecisionVariables()[solution
				.getDecisionVariables().length - 2]])); // 3 - oxc
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

	public synchronized double evaluateNetworkCost(SolutionONTD solution, List<Integer> networkRepresentation_ppr,
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
					// adjacencyMatrix_loc.get(i).set(j,
					// distanceBetweenNodes_mpr(i, j));
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
			Vector<Vector<Double>> adjacencyMatrixDistances, int minCalls) {
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
		// double[] metrics =
		// simulator.calculate12MetricsWithSimulation(profile);
		//
		// for (double d : metrics) {
		// System.out.printf("%.4f ", d);
		// }
		// System.out.println();
		simulator.simulate(profile, new CallSchedulerNonUniformHub(numberOfNodes, networkLoad), minCalls);
		// simulator.simulate(profile);

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
			Vector<Vector<Double>> adjacencyMatrixLabels, Vector<Vector<Double>> adjacencyMatrixDistances, int numParams) {
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

		profile.setOxcIsolationFactor(switchCostsAndTypes.get(1).get(networkRepresentation_ppr.get(vectorSize_loc - 2)));
		profile.setCompleteDistances(getDistances());
		profile.setEpsilon(pow(10, -0.1 * profile.getOxcIsolationFactor()));
		profile.setLinks(links);
		profile.cleanBp();
		if (numParams == 5) {
			long t0 = System.nanoTime();
			metrics = simulator.calculate5MetricsWithSimulation(profile);
			totalTime += (System.nanoTime() - t0);
			// for (double d : metrics) {
			// System.out.printf("%.4f ", d);
			// }
			// System.out.println();
			totalEvals++;
		} else if (numParams == 8) {
			long t0 = System.nanoTime();
			metrics = simulator.calculate8MetricsWithSimulation(profile);
			totalTime += (System.nanoTime() - t0);
			totalEvals++;
		} else if (numParams == 9) {
			metrics = simulator.calculate9MetricsWithSimulation(profile);
		} else if (numParams == 10) {
//			long t0 = System.nanoTime();
			metrics = simulator.calculate10MetricsWithSimulation(profile);
//			totalTime += (System.nanoTime() - t0);
//			totalEvals++;
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

		profile.setEpsilon(pow(10,
				-0.1 * switchCostsAndTypes.get(1).get(networkRepresentation_ppr.get(vectorSize_loc - 2))));
		profile.setLinks(links);
		profile.cleanBp();

		simulator.calculateMinimumLoad(profile);

		distancias = null;
		matrizGanho = null;
		links = null;

		return profile.getBp().getTotal();
	}

	public static void main(String[] args) {
		changeLoad("C:/workspace_phd/Icton15/results/nsfnet_100e/", "C:/workspace_phd/Icton15/results/nsfnet_150m/",
				150);
		// revalidadeFile(new String[] {
		// "C:/BACKUP_PENDRIVE_AZUL/Mestrado/Experimentos/2o/spea2/perfil4/r01/_spea2_50_100_100_001_1000_var.txt"
		// });
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
