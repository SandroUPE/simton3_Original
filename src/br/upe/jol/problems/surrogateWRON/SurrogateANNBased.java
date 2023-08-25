/*
 * ****************************************************************************
 * Copyright (c) 2013
 * Todos os direitos reservados, com base nas leis brasileiras de copyright
 * Este software é confidencial e de propriedade intelectual da UFPE
 * ****************************************************************************
 * Projeto: BONS - Brazilian Optical Network Simulator
 * Arquivo: SurrogateANNBased.java
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import br.upe.jol.base.Util;
import br.upe.jol.problems.simon.entity.Link;
import br.upe.jol.problems.simon.entity.NetworkProfile;
import br.upe.jol.problems.simon.entity.Node;
import br.upe.jol.problems.simon.rwa.SimpleDijkstraSimulator;
import br.upe.jol.problems.simton.ResultsEvaluation;
import br.upe.jol.problems.simton.SimtonProblem;
import br.upe.jol.problems.simton.SolutionONTD;

/**
 * 
 * @author Danilo
 * @since 21/08/2014
 */
public class SurrogateANNBased extends Problem<Integer> {
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
	public static final double MIN_CALLS = 500;
	public static final double PRECISION = 0.00001;
	public static final double CALLS = 100000;
	
	public static int NUM_EVALS = 0;

	public SurrogateANNBased(int numberOfNodes_ppr, int numberOfObjectives) {
		this(numberOfNodes_ppr, numberOfObjectives, 200);
	}

	public SurrogateANNBased(int numberOfNodes_ppr, int numberOfObjectives, int networkLoad) {
		super(((numberOfNodes_ppr * numberOfNodes_ppr - numberOfNodes_ppr) / 2) + 2, numberOfObjectives,
				"Projeto de Redes Ópticas");
		this.networkLoad = networkLoad;
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
	
	/* (non-Javadoc)
	 * @see br.upe.jol.base.Problem#getLogScale()
	 */
	@Override
	public boolean[] getLogScale() {
		return new boolean[]{true, false};
	}

	@Override
	public synchronized void evaluate(Solution<Integer> solution) {
		List<ResultsEvaluation> result = null;
		List<Integer> networkRepresentation_ppr = Arrays.asList(solution.getDecisionVariables());
		long t = System.currentTimeMillis();
		Vector<Vector<Double>> adjacencyMatrixLabels;

		adjacencyMatrixLabels = buildAdjacencyMatrixLabels(networkRepresentation_ppr);

		Vector<Vector<Double>> adjacencyMatrix = buildAdjacencyMatrixDistances(networkRepresentation_ppr,
				adjacencyMatrixLabels);
		ComplexNetwork cn = createComplexNetworkDistance((SolutionONTD) solution);
		double pbth_rna = 0.1;
		try {
			Map<ResultsEvaluation, Double> mapCost = evaluateNetworkCost((SolutionONTD) solution,
					networkRepresentation_ppr, adjacencyMatrixLabels, adjacencyMatrix);
			
			result = netwokSimulationRegression((SolutionONTD) solution, cn, mapCost);
			double rna = result.get(0).getPb();
			double costNew = result.get(0).getCost();
			int oxc = result.get(0).getOxc();
			int w = result.get(0).getW();

				// System.out.println("Setando RNA...");
				solution.setObjective(0, rna);
				if (rna > pbth_rna) {
					solution.setAccurateEvaluation(true);
				} else {
					solution.setAccurateEvaluation(false);
				}

				solution.setObjective(1, costNew);
				solution.setValue(solution.getDecisionVariables().length - 1, w);
				solution.setValue(solution.getDecisionVariables().length - 2, oxc);


			if (solution.getObjective(0) < 1e-5) {
				solution.setObjective(0, 1e-5);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Util.LOGGER.info("Avaliação (" + format.format(((SolutionONTD) solution).getObjective(0)) + ", "
				+ format.format(((SolutionONTD) solution).getObjective(1)) + "). Tempo de avaliação do indivíduo ("
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

	public synchronized Map<ResultsEvaluation, Double> evaluateNetworkCost(SolutionONTD solution,
			List<Integer> networkRepresentation_ppr, Vector<Vector<Double>> labelMatrix_loc,
			Vector<Vector<Double>> adjacencyMatrix) {
		Map<ResultsEvaluation, Double> result = new HashMap<ResultsEvaluation, Double>();

		int vectorSize_loc = networkRepresentation_ppr.size();
		double amplifierCost_loc = 0;
		double switchCost_loc = 0;
		double wavelengthCost_loc = 0;
		double totalCost_loc = 0;

		int numberOfWavelenghts_loc = networkRepresentation_ppr.get(vectorSize_loc - 1);
		int indexOxc = networkRepresentation_ppr.get(vectorSize_loc - 2);

		totalCost_loc = evaluateSimpleCost(networkRepresentation_ppr, adjacencyMatrix, vectorSize_loc,
				amplifierCost_loc, switchCost_loc, wavelengthCost_loc, numberOfWavelenghts_loc, indexOxc);
		result.put(new ResultsEvaluation(indexOxc, numberOfWavelenghts_loc), totalCost_loc);
		// assigns capital costs data
		solution.setNetwork(profile);
		solution.setObjective(1, totalCost_loc);

		return result;
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
		simulator.simulate(profile);

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

	public List<ResultsEvaluation> netwokSimulationRegression(SolutionONTD solution, ComplexNetwork cn,
			Map<ResultsEvaluation, Double> mapCost) {
		List<ResultsEvaluation> result = new Vector<ResultsEvaluation>();
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

		ResultsEvaluation best = new ResultsEvaluation(
				solution.getDecisionVariables()[solution.getDecisionVariables().length - 2],
				solution.getDecisionVariables()[solution.getDecisionVariables().length - 1]);
		double costBest = mapCost.get(best);
		double pbBest = redeNeural.obterSaida(input, true);
		best.setCost(costBest);
		best.setPb(pbBest);
		result.add(best);
		return result;

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
