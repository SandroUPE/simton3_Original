package br.upe.jol.problems.simon.rwa;

import static br.upe.jol.problems.simon.entity.Call.BIDIRECIONAL;
import static br.upe.jol.problems.simon.rwa.Dijkstra.dijkstra_fnb;
import static br.upe.jol.problems.simon.rwa.Funcoes.INF;
import static br.upe.jol.problems.simon.rwa.Funcoes.calculoPmd_fnb;
import static br.upe.jol.problems.simon.rwa.Funcoes.calculoRd_fnb;
import static br.upe.jol.problems.simon.rwa.Funcoes.somatorioPotSwitch;
import static br.upe.jol.problems.simon.util.SimonUtil.B0;
import static br.upe.jol.problems.simon.util.SimonUtil.PLANCK;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import br.cns.MetricHelper;
import br.cns.TMetric;
import br.cns.transformations.Laplacian;
import br.upe.jol.problems.simon.entity.Call;
import br.upe.jol.problems.simon.entity.CallList;
import br.upe.jol.problems.simon.entity.CallScheduler;
import br.upe.jol.problems.simon.entity.Fiber;
import br.upe.jol.problems.simon.entity.Link;
import br.upe.jol.problems.simon.entity.NetworkProfile;
import br.upe.jol.problems.simton.GmlSimton;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class SimpleDijkstraSimulator extends OpticalNetworkSimulatorAbstract {

	public static final double D_PMD = 0.05e-12 / sqrt(1000); // 0.04

	public static final double D_PMD_D_CF = 0;

	public static final double DELTA = 10;

	public static final double TIME_PULSE_BROADENING_PMD_P1 = (0.01) * (1 / TAXA_BITS);

	public static final double DELTA_PMD_P1 = (100) * (TAXA_BITS);

	private static final boolean ignorePhysicalImpairments = true;

	@Override
	public void simulate(NetworkProfile network, CallScheduler scheduler, int minCalls) {
		GmlSimton.totalEvals++;
		long t0 = System.nanoTime();
		int x = 0, y = 0;
		int nLambdaMax = 0, usarLambda = 0;
		int numCallsBlockedLackOfWaveLenght = 0;
		int numCallsBlockedUnacceptableBer = 0;
		int numCallsBlockedDispersion = 0;
		Vector<Link> rotaUplink = new Vector<Link>(), rotaDownlink = new Vector<Link>();
		Vector<Fiber> fibersUpLink = new Vector<Fiber>(), fibersDownLink = new Vector<Fiber>();
		double numOfCalls = network.getNumOfCalls();
		CallList listOfCalls = new CallList();
		Call tempCall = new Call();

		// of minumum acceptatle snr from dB to linear
		nLambdaMax = 0;
		HashMap<Integer, List<Link>> cacheRotas = new HashMap<Integer, List<Link>>();
		// Procura qual a fibra que possui mais comprimentos de onda
		for (int k = 0; k < network.getNodes().size(); k++) {
			for (int i = 0; i < network.getNodes().size(); i++) {
				Vector<Link> rota = new Vector<Link>();
				dijkstra_fnb(network.getLinks(), k, i, rota, network.getNodes(), ignorePhysicalImpairments);
				cacheRotas.put(k * 1000 + i, rota);
				if (nLambdaMax < network.getLinks()[k][i].getFiber(0).getLambda())
					nLambdaMax = network.getLinks()[k][i].getFiber(0).getLambda();
			}
		}
		network.setnLambdaMax(nLambdaMax);
		MapaComprimentosOnda mapa = new MapaComprimentosOnda(network.getnLambdaMax(), network.getNodes().size());

		// CallSchedulerPareto scheduler = new
		// CallSchedulerPareto(network.getMeanRateBetweenCalls(),
		// network.getMeanRateOfCallsDuration(), network.getNodes().size());

		// ExpParetoScheduler scheduler = new
		// ExpParetoScheduler(network.getMeanRateBetweenCalls(),
		// network.getMeanRateOfCallsDuration(), network.getNodes().size());
		for (int i = 1; i <= numOfCalls; i++) {
			if (scheduler.getCurrentTime() >= MAX_TIME) {
				listOfCalls.zerachamadas_mpu(scheduler.getCurrentTime());
				scheduler.resetTime_mpu();
			}
			scheduler.generateCallRequisition();
			x = scheduler.getNextSourceNode();
			y = scheduler.getNextDestinationNode();
			// remove ended calls
			listOfCalls.retirarChamada(scheduler.getCurrentTime(), network.getNodes(), mapa);

			tempCall = new Call();
			tempCall.setup(x, y, scheduler.getCurrentTime() + scheduler.getDuration(), scheduler.getDuration(),
					BIDIRECIONAL);

			CallList tempListOfCalls = new CallList(); // creates a vector of
														// call to represent
														// current processing
														// call
			tempListOfCalls.addChamada(tempCall, mapa); // this is required due
														// to
			// possible use of
			// regenerators

			int[] lambdaEncontrado_loc = { 0 };
			usarLambda = getStatusRota(network, x, y, rotaUplink, rotaDownlink, cacheRotas, lambdaEncontrado_loc, mapa);
			// ve se realmente existe uma rota
			if (usarLambda == BLOQ_WAVELENGTH) {
				numCallsBlockedLackOfWaveLenght++;
			} else if (usarLambda == BLOQ_BER) {
				numCallsBlockedUnacceptableBer++;
			} else if (usarLambda == BLOQ_DISPERSION) {
				numCallsBlockedDispersion++;
			} else { // the call has been accepted no regenerators were used
				tempCall.setWavelengthUp(usarLambda);
				tempCall.setWavelengthDown(usarLambda);

				fibersUpLink.clear();
				fibersDownLink.clear();

				for (int j = 0; j < rotaUplink.size(); j++) {
					// scanning all links in in found rote ja ï¿½
					// implementaï¿½ï¿½o p
					// multi-fibra
					fibersUpLink.add(rotaUplink.get(j).getFiber(rotaUplink.get(j).getAvailableFiber(usarLambda)));
					fibersDownLink.add(rotaDownlink.get(j).getFiber(rotaDownlink.get(j).getAvailableFiber(usarLambda)));
				}

				tempCall.alloc(fibersUpLink, fibersDownLink, network.getNodes());
				listOfCalls.addChamada(tempCall, mapa); // add actual tempCall
														// to the
				// active list of Calls
			}
			if ((numCallsBlockedLackOfWaveLenght + numCallsBlockedUnacceptableBer
					+ numCallsBlockedDispersion) >= minCalls) {
				numOfCalls = i;
				// System.out.println("Stopping simulation with " + numOfCalls +
				// " calls...");
				break;
			}
		}
		// System.out.println("numCallsBlockedUnacceptableBer=" +
		// numCallsBlockedUnacceptableBer);
		// System.out.println("numCallsBlockedLackOfWaveLenght=" +
		// numCallsBlockedLackOfWaveLenght);
		// System.out.println("numCallsBlockedDispersion=" +
		// numCallsBlockedDispersion);
		network.getBp().setBer(numCallsBlockedUnacceptableBer / numOfCalls);
		network.getBp().setLambda(numCallsBlockedLackOfWaveLenght / numOfCalls);
		network.getBp().setDispersion(numCallsBlockedDispersion / numOfCalls);
		network.getBp().setMeanDist(listOfCalls.getDistanciaMedia());
		network.getBp()
				.setTotal((numCallsBlockedLackOfWaveLenght + numCallsBlockedUnacceptableBer + numCallsBlockedDispersion)
						/ numOfCalls);
		GmlSimton.totalTime += (System.nanoTime() - t0);
	}

	public double[] calculate8MetricsWithSimulation(NetworkProfile network) {
		double[] metrics = null;
		int nLambdaMax = 0;
		int n = network.getNodes().size();
		// of minumum acceptatle snr from dB to linear
		nLambdaMax = 0;
		HashMap<Integer, List<Link>> cacheRotas = new HashMap<Integer, List<Link>>();
		double averagePathLengthPhysical = 0;
		double averagePathLengthPhysicalAux = 0;
		Double[][] linkCloseness = new Double[n][n];
		MapaComprimentosOnda mapa = new MapaComprimentosOnda(network.getnLambdaMax(), n);
		int numTotalLinks = 0;
		double[] apls = new double[(int) (n * (n - 1))];
		int count = 0;
		double parcialCoefficient = 0.0;
		double localCC = 0.0;
		Integer[][] matrix = new Integer[n][n];
		// Procura qual a fibra que possui mais comprimentos de onda
		int numRotas = 0;
		for (int k = 0; k < network.getNodes().size(); k++) {
			linkCloseness[k][k] = 0.0;
			matrix[k][k] = 0;

			for (int i = k + 1; i < network.getNodes().size(); i++) {
				if (i == k) {
					continue;
				}
				matrix[k][i] = 0;
				matrix[i][k] = 0;

				if (network.getLinks()[k][i] != null && network.getLinks()[k][i].getLength() < INF) {
					// Calcular densidade
					numTotalLinks++;
					matrix[k][i] = 1;
					matrix[i][k] = 1;
				}

				Vector<Link> rota = new Vector<Link>();
				dijkstra_fnb(network.getLinks(), k, i, rota, network.getNodes());

				if (rota != null && rota.size() > 1) {
					boolean validade = true;
					for (Link link : rota) {
						if (linkCloseness[link.getSource()][link.getDestination()] == null) {
							linkCloseness[link.getSource()][link.getDestination()] = 0.0;
							linkCloseness[link.getDestination()][link.getSource()] = 0.0;
						}
						if (link.getLength() == INF) {
							validade = false;
							break;
						}
					}
					if (validade) {
						for (Link link : rota) {
							linkCloseness[link.getSource()][link.getDestination()] += 1;
							linkCloseness[link.getDestination()][link.getSource()] += 1;
						}
					}
				}

				cacheRotas.put(k * 1000 + i, rota);
				cacheRotas.put(i * 1000 + k, rota);
				if (nLambdaMax < network.getLinks()[k][i].getFiber(0).getLambda())
					nLambdaMax = network.getLinks()[k][i].getFiber(0).getLambda();
				averagePathLengthPhysicalAux = 0;
				int countAPL = 0;
				for (Link link : rota) {
					if (link.getLength() < INF) {
						averagePathLengthPhysicalAux += link.getLength();
						countAPL++;
					}
				}
				if (countAPL != 0) {
					apls[count] = averagePathLengthPhysicalAux / countAPL;
					averagePathLengthPhysical += apls[count];
					numRotas++;
				}
				count++;

			}
		}
		averagePathLengthPhysical /= numRotas;
		network.setnLambdaMax(nLambdaMax);

		double plstddev = 0;
		count = 0;
		double maxLc = Double.MIN_VALUE;
		double minLc = Double.MAX_VALUE;
		double countLc = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (linkCloseness[i][j] != null) {
					if (linkCloseness[i][j] > maxLc) {
						maxLc = linkCloseness[i][j];
					}
					if (linkCloseness[i][j] > 0) {
						if (linkCloseness[i][j] < minLc) {
							minLc = linkCloseness[i][j];
						}
						countLc++;
					}
				}
				plstddev += (apls[count] - averagePathLengthPhysical) * (apls[count] - averagePathLengthPhysical);
				count++;
			}
		}
		plstddev = Math.sqrt(plstddev);
		plstddev /= count;

		Map<Integer, Integer[]> mapCalls = new HashMap<Integer, Integer[]>();
		Integer counter = 0;
		double connections = 0.0;
		double neighborhoodConnections = 0.0;
		for (int k = 0; k < network.getNodes().size(); k++) {
			connections = 0.0;
			neighborhoodConnections = 0.0;
			for (int i = 0; i < network.getNodes().size(); i++) {
				if (k != i) {
					mapCalls.put(counter, new Integer[] { k, i });
					counter++;
				}

				// cálculo de CC
				if (i != k && matrix[k][i] == 1) {
					connections++;
					for (int j = i + 1; j < n; j++) {
						if (j == k) {
							continue;
						}
						if ((matrix[i][j] == 1) && (matrix[k][j]) == 1) {
							neighborhoodConnections++;
						}
					}
				}
			}
			if (connections == 0 || neighborhoodConnections == 0) {
				localCC = 0;
			} else {
				localCC = 2.0 * neighborhoodConnections / (connections * (connections - 1));
			}
			parcialCoefficient += localCC;
		}

		network.getMinimumBp().setLambda(0 / counter);
		network.getMinimumBp().setMeanDist(0);
		network.getMinimumBp().setTotal(0.0);

		metrics = new double[] { network.getnLambdaMax(), network.getOxcIsolationFactor(), parcialCoefficient / n,
				numTotalLinks / (0.5 * n * (n - 1)), 2 * averagePathLengthPhysical,
				calculateDftLaplacianEntropy(matrix), (maxLc - minLc) / countLc, 100 };

		// for (double d : metrics) {
		// System.out.printf("%.4f ", d);
		// }

		return metrics;
	}

	public double[] calculate10MetricsWithSimulation(NetworkProfile network) {
		double[] metrics = null;
		int x = 0, y = 0;
		int nLambdaMax = 0;
		Vector<Link> rotaUplink = new Vector<Link>(), rotaDownlink = new Vector<Link>();
		List<Double> ber = new Vector<Double>();
		int n = network.getNodes().size();
		// of minumum acceptatle snr from dB to linear
		nLambdaMax = 0;
		HashMap<Integer, List<Link>> cacheRotas = new HashMap<Integer, List<Link>>();
		double averagePathLengthPhysical = 0;
		double averagePathLengthPhysicalAux = 0;
		Double[][] linkCloseness = new Double[n][n];
		MapaComprimentosOnda mapa = new MapaComprimentosOnda(network.getnLambdaMax(), n);
		double totalLinksLength = 0;
		double totalPossibleLinksLength = 0;
		int numTotalLinks = 0;
		double[] apls = new double[(int) (n * (n - 1))];
		int count = 0;
		double parcialCoefficient = 0.0;
		double localCC = 0.0;
		Integer[][] matrix = new Integer[n][n];
		// Procura qual a fibra que possui mais comprimentos de onda
		int numRotas = 0;
		for (int k = 0; k < network.getNodes().size(); k++) {
			linkCloseness[k][k] = 0.0;
			matrix[k][k] = 0;

			for (int i = k + 1; i < network.getNodes().size(); i++) {
				if (i == k) {
					continue;
				}
				matrix[k][i] = 0;
				matrix[i][k] = 0;

				totalPossibleLinksLength += network.getCompleteDistances()[k][i];

				if (network.getLinks()[k][i] != null && network.getLinks()[k][i].getLength() < INF) {
					// Calcular densidade
					totalLinksLength += network.getLinks()[k][i].getLength();
					numTotalLinks++;
					matrix[k][i] = 1;
					matrix[i][k] = 1;
				}

				Vector<Link> rota = new Vector<Link>();
				dijkstra_fnb(network.getLinks(), k, i, rota, network.getNodes());

				if (rota != null && rota.size() > 1) {
					boolean validade = true;
					for (Link link : rota) {
						if (linkCloseness[link.getSource()][link.getDestination()] == null) {
							linkCloseness[link.getSource()][link.getDestination()] = 0.0;
							linkCloseness[link.getDestination()][link.getSource()] = 0.0;
						}
						if (link.getLength() == INF) {
							validade = false;
							break;
						}
					}
					if (validade) {
						for (Link link : rota) {
							linkCloseness[link.getSource()][link.getDestination()] += 1;
							linkCloseness[link.getDestination()][link.getSource()] += 1;
						}
					}
				}

				cacheRotas.put(k * 1000 + i, rota);
				cacheRotas.put(i * 1000 + k, rota);
				if (nLambdaMax < network.getLinks()[k][i].getFiber(0).getLambda())
					nLambdaMax = network.getLinks()[k][i].getFiber(0).getLambda();
				averagePathLengthPhysicalAux = 0;
				int countAPL = 0;
				for (Link link : rota) {
					if (link.getLength() < INF) {
						averagePathLengthPhysicalAux += link.getLength();
						countAPL++;
					}
				}
				if (countAPL != 0) {
					apls[count] = averagePathLengthPhysicalAux / countAPL;
					averagePathLengthPhysical += apls[count];
					numRotas++;
				}
				count++;

			}
		}
		averagePathLengthPhysical /= numRotas;
		network.setnLambdaMax(nLambdaMax);

		count = 0;
		double maxLc = Double.MIN_VALUE;
		double minLc = Double.MAX_VALUE;
		double countLc = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (linkCloseness[i][j] != null) {
					if (linkCloseness[i][j] > maxLc) {
						maxLc = linkCloseness[i][j];
					}
					if (linkCloseness[i][j] > 0) {
						if (linkCloseness[i][j] < minLc) {
							minLc = linkCloseness[i][j];
						}
						countLc++;
					}
				}
				count++;
			}
		}

		Map<Integer, Integer[]> mapCalls = new HashMap<Integer, Integer[]>();
		Integer counter = 0;
		double connections = 0.0;
		double neighborhoodConnections = 0.0;
		for (int k = 0; k < network.getNodes().size(); k++) {
			connections = 0.0;
			neighborhoodConnections = 0.0;
			for (int i = 0; i < network.getNodes().size(); i++) {
				if (k != i) {
					mapCalls.put(counter, new Integer[] { k, i });
					counter++;
				}

				// cálculo de CC
				if (i != k && matrix[k][i] == 1) {
					connections++;
					for (int j = i + 1; j < n; j++) {
						if (j == k) {
							continue;
						}
						if ((matrix[i][j] == 1) && (matrix[k][j]) == 1) {
							neighborhoodConnections++;
						}
					}
				}
			}
			if (connections == 0 || neighborhoodConnections == 0) {
				localCC = 0;
			} else {
				localCC = 2.0 * neighborhoodConnections / (connections * (connections - 1));
			}
			parcialCoefficient += localCC;
		}
		double[] acumul = new double[2];
		acumul[0] = 0;
		acumul[1] = 0;
		for (int i = 0; i < counter; i++) {
			x = mapCalls.get(i)[0];
			y = mapCalls.get(i)[1];

			int[] lambdaEncontrado_loc = { 0 };
			double[] result = getSummary(network, x, y, rotaUplink, rotaDownlink, cacheRotas, lambdaEncontrado_loc,
					mapa);
			acumul[0] += result[0];
			acumul[1] += result[1];
			ber.add(result[1]);
		}
		double berMean = acumul[1] / counter;

		network.getMinimumBp().setBer(berMean);
		network.getMinimumBp().setLambda(0 / counter);
		network.getMinimumBp().setDispersion(acumul[0] / counter);
		network.getMinimumBp().setMeanDist(0);
		network.getMinimumBp().setTotal(0.0);

		metrics = new double[] { network.getnLambdaMax(), network.getOxcIsolationFactor(), parcialCoefficient / n,
				numTotalLinks / (0.5 * n * (n - 1)), 2 * averagePathLengthPhysical,
				calculateDftLaplacianEntropy(matrix), (maxLc - minLc) / countLc, 100,
				totalLinksLength / totalPossibleLinksLength, network.getMinimumBp().getBer() };

		// for (double d : metrics) {
		// System.out.printf("%.4f ", d);
		// }

		return metrics;
	}

	public double[] calculate9MetricsWithSimulation(NetworkProfile network) {
		double[] metrics = null;
		int x = 0, y = 0;
		int nLambdaMax = 0;
		Vector<Link> rotaUplink = new Vector<Link>(), rotaDownlink = new Vector<Link>();
		List<Double> ber = new Vector<Double>();
		int n = network.getNodes().size();
		// of minumum acceptatle snr from dB to linear
		nLambdaMax = 0;
		HashMap<Integer, List<Link>> cacheRotas = new HashMap<Integer, List<Link>>();
		double averagePathLengthPhysical = 0;
		double averagePathLengthPhysicalAux = 0;
		Double[][] linkCloseness = new Double[n][n];
		MapaComprimentosOnda mapa = new MapaComprimentosOnda(network.getnLambdaMax(), n);
		int numTotalLinks = 0;
		double[] apls = new double[(int) (n * (n - 1))];
		int count = 0;
		double parcialCoefficient = 0.0;
		double localCC = 0.0;
		Integer[][] matrix = new Integer[n][n];
		// Procura qual a fibra que possui mais comprimentos de onda
		int numRotas = 0;
		for (int k = 0; k < network.getNodes().size(); k++) {
			linkCloseness[k][k] = 0.0;
			matrix[k][k] = 0;

			for (int i = k + 1; i < network.getNodes().size(); i++) {
				if (i == k) {
					continue;
				}
				matrix[k][i] = 0;
				matrix[i][k] = 0;

				if (network.getLinks()[k][i] != null && network.getLinks()[k][i].getLength() < INF) {
					// Calcular densidade
					numTotalLinks++;
					matrix[k][i] = 1;
					matrix[i][k] = 1;
				}

				Vector<Link> rota = new Vector<Link>();
				dijkstra_fnb(network.getLinks(), k, i, rota, network.getNodes());

				if (rota != null && rota.size() > 1) {
					boolean validade = true;
					for (Link link : rota) {
						if (linkCloseness[link.getSource()][link.getDestination()] == null) {
							linkCloseness[link.getSource()][link.getDestination()] = 0.0;
							linkCloseness[link.getDestination()][link.getSource()] = 0.0;
						}
						if (link.getLength() == INF) {
							validade = false;
							break;
						}
					}
					if (validade) {
						for (Link link : rota) {
							linkCloseness[link.getSource()][link.getDestination()] += 1;
							linkCloseness[link.getDestination()][link.getSource()] += 1;
						}
					}
				}

				cacheRotas.put(k * 1000 + i, rota);
				cacheRotas.put(i * 1000 + k, rota);
				if (nLambdaMax < network.getLinks()[k][i].getFiber(0).getLambda())
					nLambdaMax = network.getLinks()[k][i].getFiber(0).getLambda();
				averagePathLengthPhysicalAux = 0;
				int countAPL = 0;
				for (Link link : rota) {
					if (link.getLength() < INF) {
						averagePathLengthPhysicalAux += link.getLength();
						countAPL++;
					}
				}
				if (countAPL != 0) {
					apls[count] = averagePathLengthPhysicalAux / countAPL;
					averagePathLengthPhysical += apls[count];
					numRotas++;
				}
				count++;

			}
		}
		averagePathLengthPhysical /= numRotas;
		network.setnLambdaMax(nLambdaMax);

		count = 0;
		double maxLc = Double.MIN_VALUE;
		double minLc = Double.MAX_VALUE;
		double countLc = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (linkCloseness[i][j] != null) {
					if (linkCloseness[i][j] > maxLc) {
						maxLc = linkCloseness[i][j];
					}
					if (linkCloseness[i][j] > 0) {
						if (linkCloseness[i][j] < minLc) {
							minLc = linkCloseness[i][j];
						}
						countLc++;
					}
				}
				count++;
			}
		}

		Map<Integer, Integer[]> mapCalls = new HashMap<Integer, Integer[]>();
		Integer counter = 0;
		double connections = 0.0;
		double neighborhoodConnections = 0.0;
		for (int k = 0; k < network.getNodes().size(); k++) {
			connections = 0.0;
			neighborhoodConnections = 0.0;
			for (int i = 0; i < network.getNodes().size(); i++) {
				if (k != i) {
					mapCalls.put(counter, new Integer[] { k, i });
					counter++;
				}

				// cálculo de CC
				if (i != k && matrix[k][i] == 1) {
					connections++;
					for (int j = i + 1; j < n; j++) {
						if (j == k) {
							continue;
						}
						if ((matrix[i][j] == 1) && (matrix[k][j]) == 1) {
							neighborhoodConnections++;
						}
					}
				}
			}
			if (connections == 0 || neighborhoodConnections == 0) {
				localCC = 0;
			} else {
				localCC = 2.0 * neighborhoodConnections / (connections * (connections - 1));
			}
			parcialCoefficient += localCC;
		}
		double[] acumul = new double[2];
		acumul[0] = 0;
		acumul[1] = 0;
		for (int i = 0; i < counter; i++) {
			x = mapCalls.get(i)[0];
			y = mapCalls.get(i)[1];

			int[] lambdaEncontrado_loc = { 0 };
			double[] result = getSummary(network, x, y, rotaUplink, rotaDownlink, cacheRotas, lambdaEncontrado_loc,
					mapa);
			acumul[0] += result[0];
			acumul[1] += result[1];
			ber.add(result[1]);
		}
		double berMean = acumul[1] / counter;

		network.getMinimumBp().setBer(berMean);
		network.getMinimumBp().setLambda(0 / counter);
		network.getMinimumBp().setDispersion(acumul[0] / counter);
		network.getMinimumBp().setMeanDist(0);
		network.getMinimumBp().setTotal(0.0);

		metrics = new double[] { network.getnLambdaMax(), network.getOxcIsolationFactor(), parcialCoefficient / n,
				numTotalLinks / (0.5 * n * (n - 1)), 2 * averagePathLengthPhysical,
				calculateDftLaplacianEntropy(matrix), (maxLc - minLc) / countLc, 100, network.getMinimumBp().getBer() };

		// for (double d : metrics) {
		// System.out.printf("%.4f ", d);
		// }

		return metrics;
	}

	public double[] calculate12MetricsWithSimulation(NetworkProfile network) {
		double[] metrics = null;
		int x = 0, y = 0;
		int nLambdaMax = 0;
		Vector<Link> rotaUplink = new Vector<Link>(), rotaDownlink = new Vector<Link>();
		List<Double> ber = new Vector<Double>();
		int n = network.getNodes().size();
		// of minumum acceptatle snr from dB to linear
		nLambdaMax = 0;
		HashMap<Integer, List<Link>> cacheRotas = new HashMap<Integer, List<Link>>();
		double averagePathLengthPhysical = 0;
		double averagePathLengthPhysicalAux = 0;
		Double[][] linkCloseness = new Double[n][n];
		MapaComprimentosOnda mapa = new MapaComprimentosOnda(network.getnLambdaMax(), n);
		double totalLinksLength = 0;
		double totalPossibleLinksLength = 0;
		int numTotalLinks = 0;
		double[] apls = new double[(int) (n * (n - 1))];
		int count = 0;
		double parcialCoefficient = 0.0;
		double localCC = 0.0;
		Integer[][] matrix = new Integer[n][n];
		// Procura qual a fibra que possui mais comprimentos de onda
		int numRotas = 0;
		for (int k = 0; k < network.getNodes().size(); k++) {
			linkCloseness[k][k] = 0.0;
			matrix[k][k] = 0;

			for (int i = k + 1; i < network.getNodes().size(); i++) {
				if (i == k) {
					continue;
				}
				matrix[k][i] = 0;
				matrix[i][k] = 0;

				totalPossibleLinksLength += network.getCompleteDistances()[k][i];

				if (network.getLinks()[k][i] != null && network.getLinks()[k][i].getLength() < INF) {
					// Calcular densidade
					totalLinksLength += network.getLinks()[k][i].getLength();
					numTotalLinks++;
					matrix[k][i] = 1;
					matrix[i][k] = 1;
				}

				Vector<Link> rota = new Vector<Link>();
				dijkstra_fnb(network.getLinks(), k, i, rota, network.getNodes());

				if (rota != null && rota.size() > 1) {
					boolean validade = true;
					for (Link link : rota) {
						if (linkCloseness[link.getSource()][link.getDestination()] == null) {
							linkCloseness[link.getSource()][link.getDestination()] = 0.0;
							linkCloseness[link.getDestination()][link.getSource()] = 0.0;
						}
						if (link.getLength() == INF) {
							validade = false;
							break;
						}
					}
					if (validade) {
						for (Link link : rota) {
							linkCloseness[link.getSource()][link.getDestination()] += 1;
							linkCloseness[link.getDestination()][link.getSource()] += 1;
						}
					}
				}

				cacheRotas.put(k * 1000 + i, rota);
				cacheRotas.put(i * 1000 + k, rota);
				if (nLambdaMax < network.getLinks()[k][i].getFiber(0).getLambda())
					nLambdaMax = network.getLinks()[k][i].getFiber(0).getLambda();
				averagePathLengthPhysicalAux = 0;
				int countAPL = 0;
				for (Link link : rota) {
					if (link.getLength() < INF) {
						averagePathLengthPhysicalAux += link.getLength();
						countAPL++;
					}
				}
				if (countAPL != 0) {
					apls[count] = averagePathLengthPhysicalAux / countAPL;
					averagePathLengthPhysical += apls[count];
					numRotas++;
				}
				count++;

			}
		}
		averagePathLengthPhysical /= numRotas;
		network.setnLambdaMax(nLambdaMax);

		double plstddev = 0;
		count = 0;
		double maxLc = Double.MIN_VALUE;
		double minLc = Double.MAX_VALUE;
		double countLc = 0;
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (linkCloseness[i][j] != null) {
					if (linkCloseness[i][j] > maxLc) {
						maxLc = linkCloseness[i][j];
					}
					if (linkCloseness[i][j] > 0) {
						if (linkCloseness[i][j] < minLc) {
							minLc = linkCloseness[i][j];
						}
						countLc++;
					}
				}
				plstddev += (apls[count] - averagePathLengthPhysical) * (apls[count] - averagePathLengthPhysical);
				count++;
			}
		}
		plstddev = Math.sqrt(plstddev);
		plstddev /= count;

		Map<Integer, Integer[]> mapCalls = new HashMap<Integer, Integer[]>();
		Integer counter = 0;
		double connections = 0.0;
		double neighborhoodConnections = 0.0;
		for (int k = 0; k < network.getNodes().size(); k++) {
			connections = 0.0;
			neighborhoodConnections = 0.0;
			for (int i = 0; i < network.getNodes().size(); i++) {
				if (k != i) {
					mapCalls.put(counter, new Integer[] { k, i });
					counter++;
				}

				// cálculo de CC
				if (i != k && matrix[k][i] == 1) {
					connections++;
					for (int j = i + 1; j < n; j++) {
						if (j == k) {
							continue;
						}
						if ((matrix[i][j] == 1) && (matrix[k][j]) == 1) {
							neighborhoodConnections++;
						}
					}
				}
			}
			if (connections == 0 || neighborhoodConnections == 0) {
				localCC = 0;
			} else {
				localCC = 2.0 * neighborhoodConnections / (connections * (connections - 1));
			}
			parcialCoefficient += localCC;
		}
		double[] acumul = new double[2];
		acumul[0] = 0;
		acumul[1] = 0;
		for (int i = 0; i < counter; i++) {
			x = mapCalls.get(i)[0];
			y = mapCalls.get(i)[1];

			int[] lambdaEncontrado_loc = { 0 };
			double[] result = getSummary(network, x, y, rotaUplink, rotaDownlink, cacheRotas, lambdaEncontrado_loc,
					mapa);
			acumul[0] += result[0];
			acumul[1] += result[1];
			ber.add(result[1]);
		}
		double berMean = acumul[1] / counter;
		double stdDevBer = 0;
		for (int i = 0; i < ber.size(); i++) {
			stdDevBer += (ber.get(i) - berMean) * (ber.get(i) - berMean);
		}
		stdDevBer /= ber.size();
		stdDevBer = Math.sqrt(stdDevBer);

		network.getMinimumBp().setStdDevBer(stdDevBer);
		network.getMinimumBp().setBer(berMean);
		network.getMinimumBp().setLambda(0 / counter);
		network.getMinimumBp().setDispersion(acumul[0] / counter);
		network.getMinimumBp().setMeanDist(0);
		network.getMinimumBp().setTotal(0.0);

		metrics = new double[] { network.getnLambdaMax(), network.getOxcIsolationFactor(), parcialCoefficient / n,
				numTotalLinks / (0.5 * n * (n - 1)), 2 * averagePathLengthPhysical,
				calculateDftLaplacianEntropy(matrix), (maxLc - minLc) / countLc, 100,
				totalLinksLength / totalPossibleLinksLength, network.getMinimumBp().getBer(), plstddev,
				network.getMinimumBp().getStdDevBer() };

		// for (double d : metrics) {
		// System.out.printf("%.4f ", d);
		// }

		return metrics;
	}

	public double[] calculate5MetricsWithSimulation(NetworkProfile network) {
		double[] metrics = null;
		int n = network.getNodes().size();
		double averagePathLengthPhysical = 0;
		Double[][] linkCloseness = new Double[n][n];
		int numTotalLinks = 0;
		// Procura qual a fibra que possui mais comprimentos de onda
		int numRotas = 0;
		double maxLc = Double.MIN_VALUE;
		double countLc = 0;
		for (int k = 0; k < n; k++) {
			linkCloseness[k][k] = 0.0;

			for (int i = k + 1; i < n; i++) {
				if (linkCloseness[k][i] == null) {
					linkCloseness[i][k] = 0.0;
					linkCloseness[k][i] = 0.0;
				}
				if (network.getLinks()[k][i] != null && network.getLinks()[k][i].getLength() < INF) {
					// Calcular densidade
					numTotalLinks++;
				}

				Vector<Link> rota = new Vector<Link>();
				dijkstra_fnb(network.getLinks(), k, i, rota, network.getNodes());
				int countAPL = 0;
				if (rota != null && rota.size() > 0) {
					for (Link link : rota) {
						if (linkCloseness[link.getSource()][link.getDestination()] == null) {
							linkCloseness[link.getSource()][link.getDestination()] = 0.0;
							linkCloseness[link.getDestination()][link.getSource()] = 0.0;
						}
						if (link.getLength() < INF) {
							if (linkCloseness[link.getSource()][link.getDestination()] == 0) {
								countLc++;
							}
							linkCloseness[link.getSource()][link.getDestination()] += 1;
							linkCloseness[link.getDestination()][link.getSource()] += 1;
							if (linkCloseness[link.getSource()][link.getDestination()] > maxLc) {
								maxLc = linkCloseness[link.getSource()][link.getDestination()];
							}
							countAPL++;
						}
					}
				}

				if (countAPL != 0) {
					averagePathLengthPhysical += countAPL;
					numRotas++;
				}
			}
		}
		double min = Double.MAX_VALUE;
		for (int k = 0; k < n; k++) {
			for (int i = k + 1; i < n; i++) {
				if (linkCloseness[k][i] != 0 && linkCloseness[k][i] < min) {
					min = linkCloseness[k][i];
				}
			}
		}
		averagePathLengthPhysical /= numRotas;

		metrics = new double[] { network.getnLambdaMax(), numTotalLinks / (0.5 * n * (n - 1)),
				averagePathLengthPhysical, (maxLc - min) / countLc, 100 };

		// for (double d : metrics) {
		// System.out.printf("%.4f ", d);
		// }

		return metrics;
	}

	public void calculateMinimumLoad(NetworkProfile network) {
		int x = 0, y = 0;
		int nLambdaMax = 0;
		Vector<Link> rotaUplink = new Vector<Link>(), rotaDownlink = new Vector<Link>();
		List<Double> ber = new Vector<Double>();
		// of minumum acceptatle snr from dB to linear
		nLambdaMax = 0;
		HashMap<Integer, List<Link>> cacheRotas = new HashMap<Integer, List<Link>>();
		MapaComprimentosOnda mapa = new MapaComprimentosOnda(network.getnLambdaMax(), network.getNodes().size());
		// Procura qual a fibra que possui mais comprimentos de onda
		for (int k = 0; k < network.getNodes().size(); k++) {
			for (int i = 0; i < network.getNodes().size(); i++) {
				if (i == k) {
					continue;
				}

				Vector<Link> rota = new Vector<Link>();
				dijkstra_fnb(network.getLinks(), k, i, rota, network.getNodes());

				cacheRotas.put(k * 1000 + i, rota);
				if (nLambdaMax < network.getLinks()[k][i].getFiber(0).getLambda())
					nLambdaMax = network.getLinks()[k][i].getFiber(0).getLambda();
			}
		}
		network.setnLambdaMax(nLambdaMax);

		Map<Integer, Integer[]> mapCalls = new HashMap<Integer, Integer[]>();
		Integer counter = 0;
		for (int k = 0; k < network.getNodes().size(); k++) {
			for (int i = 0; i < network.getNodes().size(); i++) {
				if (k != i) {
					mapCalls.put(counter, new Integer[] { k, i });
					counter++;
				}
			}
		}
		double[] acumul = new double[2];
		acumul[0] = 0;
		acumul[1] = 0;
		for (int i = 0; i < counter; i++) {
			x = mapCalls.get(i)[0];
			y = mapCalls.get(i)[1];

			int[] lambdaEncontrado_loc = { 0 };
			double[] result = getSummary(network, x, y, rotaUplink, rotaDownlink, cacheRotas, lambdaEncontrado_loc,
					mapa);
			acumul[0] += result[0];
			acumul[1] += result[1];
			ber.add(result[1]);
		}
		double berMean = acumul[1] / counter;
		double stdDevBer = 0;
		for (int i = 0; i < ber.size(); i++) {
			stdDevBer += (ber.get(i) - berMean) * (ber.get(i) - berMean);
		}
		stdDevBer /= ber.size();
		stdDevBer = Math.sqrt(stdDevBer);

		network.getMinimumBp().setStdDevBer(stdDevBer);
		network.getMinimumBp().setBer(berMean);
		network.getMinimumBp().setLambda(0 / counter);
		network.getMinimumBp().setDispersion(acumul[0] / counter);
		network.getMinimumBp().setMeanDist(0);
		network.getMinimumBp().setTotal(0.0);
	}

	public double calculateDftLaplacianEntropy(Integer[][] matrix) {
		DoubleFFT_1D fft = null;
		double[] realEigenvalues = null;
		double[] fftValues = null;
		Integer[][] laplacian = Laplacian.getInstance().transform(matrix);
		double[][] realValues = new double[matrix.length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				realValues[i][j] = laplacian[i][j];
			}
		}
		RealMatrix rm = new Array2DRowRealMatrix(realValues);
		try {
			EigenDecomposition solver = new EigenDecomposition(rm, 0);
			realEigenvalues = solver.getRealEigenvalues();
			double aux;
			for (int i = 0; i < realEigenvalues.length; i++) {
				for (int j = i; j < realEigenvalues.length; j++) {
					if (realEigenvalues[j] < realEigenvalues[i]) {
						aux = realEigenvalues[i];
						realEigenvalues[i] = realEigenvalues[j];
						realEigenvalues[j] = aux;
					}
				}
			}
			fft = new DoubleFFT_1D(realValues.length);
			fftValues = new double[realValues.length];
			for (int i = 0; i < realValues.length; i++) {
				fftValues[i] = realEigenvalues[i];
			}
			fft.realForward(fftValues);

		} catch (Exception e) {
			System.out.println("Failed to calculate matrix eigenvalues...");
			e.printStackTrace();
			return 0;
		}

		double sum = 0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (double d : fftValues) {
			if (d < min) {
				min = d;
			}
			if (d > max) {
				max = d;
			}
		}
		for (double fftValue : fftValues) {
			fftValue += min;
			fftValue /= (max - min);
			if (fftValue > 0) {
				sum += fftValue * (Math.log10(fftValue) / Math.log10(2));
			}
		}
		return -sum;
	}

	/**
	 * Retorna um lambda para uso ou um código de erro.
	 * 
	 * @param network
	 * @param origem
	 * @param destino
	 * @param rotaUplink
	 * @param rotaDownlink
	 * @param cacheRotas
	 * @param lambdaEncontrado
	 * @return
	 */
	protected int getStatusRota(NetworkProfile network, int origem, int destino, Vector<Link> rotaUplink,
			Vector<Link> rotaDownlink, Map<Integer, List<Link>> cacheRotas, int[] lambdaEncontrado,
			MapaComprimentosOnda mapa) {
		int retorno = -1;
		rotaUplink.clear(); // limpa a variï¿½vel q calcula a rota direta
		rotaDownlink.clear(); // limpa a variï¿½vel q calcula a rota de volta
		boolean lambdaDisponivel_loc = true;
		rotaUplink.addAll(cacheRotas.get(origem * 1000 + destino));
		// encontra o comprimento de onda por First Fit para os casos de minhops
		// e menor distancia (dijkstra) reverte a rota encontrada
		for (int j = rotaUplink.size() - 1; j >= 0; j--) {
			int destination_loc = rotaUplink.get(j).getSource();
			int source_loc = rotaUplink.get(j).getDestination();
			rotaDownlink.add(network.getLinks()[source_loc][destination_loc]);
		}
		// procura para cada lambda
		retorno = firstFit(network, rotaUplink, rotaDownlink, lambdaEncontrado, retorno, mapa);
		// retorno = newMostUsed(network, rotaUplink, rotaDownlink,
		// lambdaEncontrado, retorno, mapa);

		if (retorno == -1) // nao hï¿½ lambda disponivel
		{
			return BLOQ_WAVELENGTH;
		}

		if (!ignorePhysicalImpairments) {
			// calcula o alargamento temporal devido ï¿½ PMD da fibra de
			// transmissï¿½o
			double timePulseBroadeningPmd_loc = TIME_PULSE_BROADENING_PMD_P1
					* calculoPmd_fnb(rotaUplink, TAXA_BITS, D_PMD);
			// calcula o alargamento temporal resultante devido ï¿½ PMD
			double pulseBroadeningPmd_loc = sqrt(timePulseBroadeningPmd_loc * timePulseBroadeningPmd_loc);
			// calcula o delta t(%) resultante
			double totalDeltaPulseBroadening = abs(DELTA_PMD_P1 * pulseBroadeningPmd_loc
					+ calculoRd_fnb(rotaUplink, TAXA_BITS, LARGURA_LINHA, GAMA, retorno));

			if (totalDeltaPulseBroadening > DELTA) {
				return BLOQ_DISPERSION;
			}
			// a qualidade do serviï¿½o ï¿½ insuficiente, retorna valor
			// apropriado a
			// ser
			// tratado
			if (getSNR(network, rotaUplink, retorno) < SNR_THRESHOLD
					|| getSNR(network, rotaDownlink, retorno) < SNR_THRESHOLD) {
				return BLOQ_BER;
			}
		}
		double latency = 0;
		double latencyAux = 0;
		// TODO: gravar dados de latência do trasmissor
		// delay de interfaces de rede
		latencyAux = 2 * 51.2e-9;
		latency += latencyAux;
		// delay de transponders
		latencyAux = 2 * 10e-6;
		latency += latencyAux;
		// delay de booster e pre
		latencyAux = 2 * 0.15e-6;
		latency += latencyAux;
		for (int j = rotaUplink.size() - 1; j >= 0; j--) {
			// TODO: gravar dados de latência da rota
			latencyAux = 4.9e-6 * rotaUplink.get(j).getLength();
			latency += latencyAux;
			// delay de compensadores de dispersão
			latencyAux = latencyAux * 0.25;
			latency += latencyAux;

		}
		// TODO: gravar dados de latência no receptor
//		System.out.printf("Latência = %.4f\n", latency * 1e6);

		return retorno;
	}

	/**
	 * Retorna um lambda para uso ou um código de erro.
	 * 
	 * @param network
	 * @param origem
	 * @param destino
	 * @param rotaUplink
	 * @param rotaDownlink
	 * @param cacheRotas
	 * @param lambdaEncontrado
	 * @return
	 */
	protected int getStatusRota(NetworkProfile network, int origem, int destino, Vector<Link> rotaUplink,
			Vector<Link> rotaDownlink, Map<Integer, List<Link>> cacheRotas, int[] lambdaEncontrado) {
		return getStatusRota(network, origem, destino, rotaUplink, rotaDownlink, cacheRotas, lambdaEncontrado, null);
	}

	/**
	 * Retorna um lambda para uso ou um código de erro.
	 * 
	 * @param network
	 * @param origem
	 * @param destino
	 * @param rotaUplink
	 * @param rotaDownlink
	 * @param cacheRotas
	 * @param lambdaEncontrado
	 * @return
	 */
	protected int getStatusRota(NetworkProfile network, int origem, int destino, Vector<Link> rotaUplink,
			Vector<Link> rotaDownlink, int[] lambdaEncontrado) {
		int retorno = -1;
		// encontra o comprimento de onda por First Fit para os casos de minhops
		// e menor distancia (dijkstra) reverte a rota encontrada
		for (int j = rotaUplink.size() - 1; j >= 0; j--) {
			int destination_loc = rotaUplink.get(j).getSource();
			int source_loc = rotaUplink.get(j).getDestination();
			rotaDownlink.add(network.getLinks()[source_loc][destination_loc]);
		}
		// procura para cada lambda
		retorno = firstFit(network, rotaUplink, rotaDownlink, lambdaEncontrado, retorno, null);
		// retorno = newMostUsed(network, rotaUplink, rotaDownlink,
		// lambdaEncontrado, retorno, mapa);

		if (retorno == -1) // nao hï¿½ lambda disponivel
		{
			return BLOQ_WAVELENGTH;
		}

		if (!ignorePhysicalImpairments) {
			// calcula o alargamento temporal devido ï¿½ PMD da fibra de
			// transmissï¿½o
			double timePulseBroadeningPmd_loc = TIME_PULSE_BROADENING_PMD_P1
					* calculoPmd_fnb(rotaUplink, TAXA_BITS, D_PMD);
			// calcula o alargamento temporal resultante devido ï¿½ PMD
			double pulseBroadeningPmd_loc = sqrt(timePulseBroadeningPmd_loc * timePulseBroadeningPmd_loc);
			// calcula o delta t(%) resultante
			double totalDeltaPulseBroadening = abs(DELTA_PMD_P1 * pulseBroadeningPmd_loc
					+ calculoRd_fnb(rotaUplink, TAXA_BITS, LARGURA_LINHA, GAMA, retorno));

			if (totalDeltaPulseBroadening > DELTA) {
				return BLOQ_DISPERSION;
			}
			// a qualidade do serviï¿½o ï¿½ insuficiente, retorna valor
			// apropriado a
			// ser
			// tratado
			if (getSNR(network, rotaUplink, retorno) < SNR_THRESHOLD
					|| getSNR(network, rotaDownlink, retorno) < SNR_THRESHOLD) {
				return BLOQ_BER;
			}
		}

		return retorno;
	}

	/**
	 * @param network
	 * @param rotaUplink
	 * @param rotaDownlink
	 * @param lambdaEncontrado
	 * @param retorno
	 * @return
	 */
	public int firstFit(NetworkProfile network, Vector<Link> rotaUplink, Vector<Link> rotaDownlink,
			int[] lambdaEncontrado, int retorno, MapaComprimentosOnda mapa) {
		boolean lambdaDisponivel_loc;
		for (int nLambda_loc = 0; (nLambda_loc < network.getnLambdaMax()); nLambda_loc++) {
			lambdaDisponivel_loc = true;
			for (int i = 0; i < rotaUplink.size(); i++) {
				// se lambda nao disponivel
				if ((!rotaUplink.get(i).getFibers().isEmpty()
						&& rotaUplink.get(i).getFiber(0).isLambdaAvailable(nLambda_loc) == false)
						|| (!rotaDownlink.get(i).getFibers().isEmpty()
								&& rotaDownlink.get(i).getFiber(0).isLambdaAvailable(nLambda_loc) == false)) {
					lambdaDisponivel_loc = false;
					break;
				}
			}
			if (lambdaDisponivel_loc) {
				retorno = nLambda_loc;
				lambdaEncontrado[0] = retorno;
				break;
			}
		}
		return retorno;
	}

	/**
	 * @param network
	 * @param rotaUplink
	 * @param rotaDownlink
	 * @param lambdaEncontrado
	 * @param retorno
	 * @return
	 */
	public int maxValue(NetworkProfile network, Vector<Link> rotaUplink, Vector<Link> rotaDownlink,
			int[] lambdaEncontrado, int retorno, MapaComprimentosOnda mapa) {
		boolean continuidade;
		List<Integer> possibles = new Vector<Integer>();
		for (int lambda = 0; (lambda < network.getnLambdaMax()); lambda++) {
			continuidade = true;
			for (int i = 0; i < rotaUplink.size(); i++) {
				// se lambda nao disponivel
				if ((rotaUplink.get(i).getFiber(0).isLambdaAvailable(lambda) == false)
						|| (rotaDownlink.get(i).getFiber(0).isLambdaAvailable(lambda) == false)) {
					continuidade = false;
					break;
				}
			}
			if (continuidade) {
				possibles.add(lambda);
			}
		}
		// verificar dentre os possíveis qual é o que maximiniza a métrica
		// double decisionValue = Double.MIN_VALUE;
		double secDecisionValue = Double.MIN_VALUE;
		double decisionValue = Double.MAX_VALUE;
		TMetric principal = TMetric.NUMBER_OF_COMPONENTS;
		TMetric secundary = TMetric.ALGEBRAIC_CONNECTIVITY;
		// TMetric principal = TMetric.ALGEBRAIC_CONNECTIVITY;
		// TMetric secundary = TMetric.NATURAL_CONNECTIVITY;
		if (possibles.size() > 0) {
			retorno = possibles.get(0);
			for (int lambda : possibles) {
				Integer[][] planoW = mapa.getAvailableMap(lambda);
				// double oldValue = mapa.getEntry(lambda).getMetricValue();
				// double secOldValue =
				// mapa.getEntry(lambda).getSecundaryMetricValue();
				// if (mapa.getEntry(lambda).isNotInitialized()) {
				// oldValue = MetricHelper.getInstance().calculate(principal,
				// planoW);
				// secOldValue = MetricHelper.getInstance().calculate(secundary,
				// planoW);
				// mapa.getEntry(lambda).setMetricValue(oldValue);
				// mapa.getEntry(lambda).setSecundaryMetricValue(secOldValue);
				// }
				// simular a implementação no lambda dado para avaliar o novo
				// valor da métrica
				for (Link link : rotaUplink) {
					planoW[link.getSource()][link.getDestination()] = 0;
				}
				for (Link link : rotaDownlink) {
					planoW[link.getSource()][link.getDestination()] = 0;
				}
				double newValue = MetricHelper.getInstance().calculate(principal, planoW);
				double newSecValue = MetricHelper.getInstance().calculate(secundary, planoW);
				// System.out.printf(
				// "Lambda = %d; P antes = %.4f; P depois = %.4f; Sec antes =
				// %.4f; Sec depois = %.4f \n",
				// lambda,
				// oldValue, newValue, secOldValue, newSecValue);
				// if (oldValue == newValue) {
				// // comportamento do first fit
				// retorno = lambda;
				// break;
				// }
				if (newValue < decisionValue) {
					decisionValue = newValue;
					secDecisionValue = newSecValue;
					retorno = lambda;
				} else if (newValue == decisionValue) {
					if (newSecValue > secDecisionValue) {
						secDecisionValue = newSecValue;
						decisionValue = newValue;
						retorno = lambda;
					}
				}
			}
			mapa.getEntry(retorno).setMetricValue(decisionValue);
			mapa.getEntry(retorno).setSecundaryMetricValue(secDecisionValue);
			// System.out.printf("Lambda = %d; P atual = %.4f; Sec atual = %.4f
			// \n",
			// retorno, mapa.getEntry(retorno)
			// .getMetricValue(),
			// mapa.getEntry(retorno).getSecundaryMetricValue());
			lambdaEncontrado[0] = retorno;
		}
		return retorno;
	}

	public int newMostUsed(NetworkProfile network, Vector<Link> rotaUplink, Vector<Link> rotaDownlink,
			int[] lambdaEncontrado, int retorno, MapaComprimentosOnda mapa) {
		boolean continuidade;
		List<Integer> possibles = new Vector<Integer>();
		for (int lambda = 0; (lambda < network.getnLambdaMax()); lambda++) {
			continuidade = true;
			for (int i = 0; i < rotaUplink.size(); i++) {
				// se lambda nao disponivel
				if ((rotaUplink.get(i).getFiber(0).isLambdaAvailable(lambda) == false)
						|| (rotaDownlink.get(i).getFiber(0).isLambdaAvailable(lambda) == false)) {
					continuidade = false;
					break;
				}
			}
			if (continuidade) {
				possibles.add(lambda);
			}
		}
		// verificar dentre os possíveis qual é o que maximiniza a métrica
		double decisionValue = Double.MIN_VALUE;
		TMetric principal = TMetric.NUMBER_OF_COMPONENTS;
		if (possibles.size() > 0) {
			retorno = possibles.get(0);
			for (int lambda : possibles) {
				Integer[][] planoW = mapa.getAvailableMap(lambda);
				if (MetricHelper.getInstance().calculate(principal, planoW) > decisionValue) {
					decisionValue = MetricHelper.getInstance().calculate(principal, planoW);
					retorno = lambda;
				}
			}
			mapa.getEntry(retorno).setMetricValue(decisionValue);
			lambdaEncontrado[0] = retorno;
		}
		return retorno;
	}

	protected double[] getSummary(NetworkProfile network, int origem, int destino, Vector<Link> rotaUplink,
			Vector<Link> rotaDownlink, Map<Integer, List<Link>> cacheRotas, int[] lambdaEncontrado,
			MapaComprimentosOnda mapa) {
		int lambdaEncontrado_loc = -1;
		double[] ret = new double[2];
		rotaUplink.clear(); // limpa a variï¿½vel q calcula a rota direta
		rotaDownlink.clear(); // limpa a variï¿½vel q calcula a rota de volta
		boolean lambdaDisponivel_loc = true;
		rotaUplink.addAll(cacheRotas.get(origem * 1000 + destino));
		// encontra o comprimento de onda por First Fit para os casos de minhops
		// e menor distancia (dijkstra) reverte a rota encontrada
		for (int j = rotaUplink.size() - 1; j >= 0; j--) {
			int destination_loc = rotaUplink.get(j).getSource();
			int source_loc = rotaUplink.get(j).getDestination();
			rotaDownlink.add(network.getLinks()[source_loc][destination_loc]);
		}
		lambdaEncontrado_loc = firstFit(network, rotaUplink, rotaDownlink, lambdaEncontrado, lambdaEncontrado_loc,
				mapa);

		// calcula o alargamento temporal devido ï¿½ PMD da fibra de
		// transmissï¿½o
		double timePulseBroadeningPmd_loc = TIME_PULSE_BROADENING_PMD_P1 * calculoPmd_fnb(rotaUplink, TAXA_BITS, D_PMD);
		// calcula o alargamento temporal resultante devido ï¿½ PMD
		double pulseBroadeningPmd_loc = sqrt(timePulseBroadeningPmd_loc * timePulseBroadeningPmd_loc);
		// calcula o delta t(%) resultante
		double totalDeltaPulseBroadening = abs(DELTA_PMD_P1 * pulseBroadeningPmd_loc
				+ calculoRd_fnb(rotaUplink, TAXA_BITS, LARGURA_LINHA, GAMA, lambdaEncontrado_loc));

		ret[0] = DELTA - totalDeltaPulseBroadening;

		ret[1] = getSNRAse(network, rotaUplink, lambdaEncontrado_loc) - SNR_THRESHOLD;
		double snrDown = getSNRAse(network, rotaDownlink, lambdaEncontrado_loc) - SNR_THRESHOLD;
		if (snrDown < ret[1]) {
			ret[1] = snrDown;
		}

		return ret;
	}

	public static double getSNR(NetworkProfile network, Vector<Link> path, int lambda) {
		int source, destino;
		double sIn, nIn, G1G2, LMux2Exp;
		double sInPart1, sInPart2, nInPart1, nInPart2, nInPart3, nInPartFwm, nInPartCrTalk;
		double potFwm;
		double somatorioPotencias = 0.0, epsilon_loc;
		double swAtenuation, muxDemuxGain, fiberGain;

		if (path.isEmpty()) {
			return INF;
		}
		source = path.get(0).getSource();
		epsilon_loc = network.getEpsilon();

		somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, source, lambda);
		somatorioPotencias *= epsilon_loc;
		nIn = (network.getNodes().get(source).getLaserPower() / network.getNodes().get(source).getLaserSNR())
				+ somatorioPotencias;
		sIn = network.getNodes().get(source).getLaserPower();

		for (int j = 0; j < path.size(); j++) {
			nInPartFwm = 0.0;
			nInPartCrTalk = 0.0;
			nInPart3 = 0.0;
			source = path.get(j).getSource();
			destino = path.get(j).getDestination();

			swAtenuation = network.getNodes().get(destino).getSwitchAtenuation();
			muxDemuxGain = path.get(j).getFiber(0).getMuxDemuxGain();
			fiberGain = path.get(j).getFiber(0).getGain();
			LMux2Exp = muxDemuxGain * muxDemuxGain * path.get(j).getFiber(0).getGain();

			G1G2 = path.get(j).getFiber(0).getG0Booster(path.get(j).getFiber(0).getSumPowerB())
					* path.get(j).getFiber(0).getG0PreAmp(path.get(j).getFiber(0).getSumPowerD());

			sInPart1 = (sIn * G1G2);
			sInPart2 = LMux2Exp * swAtenuation;

			sIn = sInPart1 * sInPart2;

			nInPart1 = G1G2 * LMux2Exp * swAtenuation;
			nInPart2 = nIn;
			nInPart3 = ((PLANCK * path.get(j).getFiber(0).getFrequency(lambda) * B0) / (2 * muxDemuxGain))
					* (path.get(j)
							.getFiber(
									0)
							.getBoosterF(path.get(j).getFiber(0).getSumPowerB())
							+ (path.get(j).getFiber(0).getPreF(path.get(j).getFiber(0).getSumPowerD())
									/ (path.get(j).getFiber(0).getG0Booster(path.get(j).getFiber(0).getSumPowerB())
											* fiberGain)));

			// desconsiderando FWM por enquanto
			// potFwm = path.get(j).getFiber(0).getSumPowerFWM(lambda,
			// network.getNodes());
			// nInPartFwm = potFwm
			// / (muxDemuxGain * fiberGain * path.get(j).getFiber(0)
			// .getG0Booster(path.get(j).getFiber(0).getSumPowerB()));

			somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, destino, lambda);
			nInPartCrTalk = (somatorioPotencias * epsilon_loc)
					/ (muxDemuxGain * muxDemuxGain * swAtenuation * fiberGain * G1G2);

			nIn = nInPart1 * (nInPart2 + nInPartFwm + nInPart3 + nInPartCrTalk);
		}

		return (sIn / nIn);
	}

	public static double getSNRAse(NetworkProfile network, Vector<Link> path, int lambda) {
		int source, destino;
		double sIn, nIn, G1G2, LMux2Exp;
		double sInPart1, sInPart2, nInPart1, nInPart2, nInPart3;
		double somatorioPotencias = 0.0, epsilon_loc;
		double swAtenuation, muxDemuxGain, fiberGain;

		if (path.isEmpty()) {
			return INF;
		}
		source = path.get(0).getSource();
		epsilon_loc = network.getEpsilon();

		somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, source, lambda);
		somatorioPotencias *= epsilon_loc;
		nIn = (network.getNodes().get(source).getLaserPower() / network.getNodes().get(source).getLaserSNR())
				+ somatorioPotencias;
		sIn = network.getNodes().get(source).getLaserPower();

		for (int j = 0; j < path.size(); j++) {
			nInPart3 = 0.0;
			source = path.get(j).getSource();
			destino = path.get(j).getDestination();

			swAtenuation = network.getNodes().get(destino).getSwitchAtenuation();
			muxDemuxGain = path.get(j).getFiber(0).getMuxDemuxGain();
			fiberGain = path.get(j).getFiber(0).getGain();
			LMux2Exp = muxDemuxGain * muxDemuxGain * path.get(j).getFiber(0).getGain();

			G1G2 = path.get(j).getFiber(0).getG0Booster(path.get(j).getFiber(0).getSumPowerB())
					* path.get(j).getFiber(0).getG0PreAmp(path.get(j).getFiber(0).getSumPowerD());

			sInPart1 = (sIn * G1G2);
			sInPart2 = LMux2Exp * swAtenuation;

			sIn = sInPart1 * sInPart2;

			nInPart1 = G1G2 * LMux2Exp * swAtenuation;
			nInPart2 = nIn;
			nInPart3 = ((PLANCK * path.get(j).getFiber(0).getFrequency(lambda) * B0) / (2 * muxDemuxGain))
					* (path.get(j)
							.getFiber(
									0)
							.getBoosterF(path.get(j).getFiber(0).getSumPowerB())
							+ (path.get(j).getFiber(0).getPreF(path.get(j).getFiber(0).getSumPowerD())
									/ (path.get(j).getFiber(0).getG0Booster(path.get(j).getFiber(0).getSumPowerB())
											* fiberGain)));

			somatorioPotencias = somatorioPotSwitch(network.getLinks(), network.getNodes(), path, destino, lambda);

			nIn = nInPart1 * (nInPart2 + nInPart3);
		}

		return (sIn / nIn);
	}

}
