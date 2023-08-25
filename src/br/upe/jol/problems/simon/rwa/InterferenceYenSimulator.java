package br.upe.jol.problems.simon.rwa;

import static br.upe.jol.problems.simon.entity.Call.BIDIRECIONAL;
import static br.upe.jol.problems.simon.rwa.Yen.findRoute;

import java.util.Vector;

import br.upe.jol.problems.simon.entity.Call;
import br.upe.jol.problems.simon.entity.CallList;
import br.upe.jol.problems.simon.entity.CallScheduler;
import br.upe.jol.problems.simon.entity.Fiber;
import br.upe.jol.problems.simon.entity.Link;
import br.upe.jol.problems.simon.entity.NetworkProfile;
import br.upe.jol.problems.simton.GmlSimton;

public class InterferenceYenSimulator extends SimpleDijkstraSimulator {
	public void simulate(NetworkProfile network, CallScheduler scheduler,
			int minCalls) {
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
		nLambdaMax = network.getnLambdaMax();
		Yen.distances = network.getCompleteDistances();
		network.setnLambdaMax(nLambdaMax);
		MapaComprimentosOnda mapa = new MapaComprimentosOnda(
				network.getnLambdaMax(), network.getNodes().size());

		for (int i = 1; i <= numOfCalls; i++) {
			if (scheduler.getCurrentTime() >= MAX_TIME) {
				listOfCalls.zerachamadas_mpu(scheduler.getCurrentTime());
				scheduler.resetTime_mpu();
			}
			scheduler.generateCallRequisition();
			x = scheduler.getNextSourceNode();
			y = scheduler.getNextDestinationNode();
			// remove ended calls
			listOfCalls.retirarChamada(scheduler.getCurrentTime(),
					network.getNodes(), mapa);

			tempCall = new Call();
			tempCall.setup(x, y,
					scheduler.getCurrentTime() + scheduler.getDuration(),
					scheduler.getDuration(), BIDIRECIONAL);

			CallList tempListOfCalls = new CallList();
			tempListOfCalls.addChamada(tempCall, mapa);

			int[] lambdaEncontrado_loc = { 0 };

			Vector<Link> rota = new Vector<Link>();
			findRoute(network.getLinks(), x, y, rota, network.getNodes(), true);
			rotaUplink = rota;
			rotaDownlink = new Vector<Link>();
			usarLambda = getStatusRota(network, x, y, rotaUplink, rotaDownlink,
					lambdaEncontrado_loc);
			// ve se realmente existe uma rota
			if (usarLambda == BLOQ_WAVELENGTH) {
//				System.out.println("LAMBDA");
				numCallsBlockedLackOfWaveLenght++;
			} else if (usarLambda == BLOQ_BER) {
				numCallsBlockedUnacceptableBer++;
//				System.out.println("BER");
			} else if (usarLambda == BLOQ_DISPERSION) {
//				System.out.println("DC");
				numCallsBlockedDispersion++;
			} else { // the call has been accepted no regenerators were used
				tempCall.setWavelengthUp(usarLambda);
				tempCall.setWavelengthDown(usarLambda);

				fibersUpLink.clear();
				fibersDownLink.clear();

				for (int j = 0; j < rotaUplink.size(); j++) {
					// scanning all links in in found rote ja �
					// implementa��o p
					// multi-fibra
					fibersUpLink.add(rotaUplink.get(j).getFiber(
							rotaUplink.get(j).getAvailableFiber(usarLambda)));
					fibersDownLink.add(rotaDownlink.get(j).getFiber(
							rotaDownlink.get(j).getAvailableFiber(usarLambda)));
				}

				tempCall.alloc(fibersUpLink, fibersDownLink, network.getNodes());
				listOfCalls.addChamada(tempCall, mapa); // add actual tempCall
														// to the
				// active list of Calls
			}
			if ((numCallsBlockedLackOfWaveLenght
					+ numCallsBlockedUnacceptableBer + numCallsBlockedDispersion) >= minCalls) {
				numOfCalls = i;
				break;
			}
		}
		network.getBp().setBer(numCallsBlockedUnacceptableBer / numOfCalls);
		network.getBp().setLambda(numCallsBlockedLackOfWaveLenght / numOfCalls);
		network.getBp().setDispersion(numCallsBlockedDispersion / numOfCalls);
		network.getBp().setMeanDist(listOfCalls.getDistanciaMedia());
		network.getBp()
				.setTotal(
						(numCallsBlockedLackOfWaveLenght
								+ numCallsBlockedUnacceptableBer + numCallsBlockedDispersion)
								/ numOfCalls);
		GmlSimton.totalTime += (System.nanoTime() - t0);
	}
}
