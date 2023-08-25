package br.upe.jol.experiments;

import br.cns.model.GmlData;
import br.cns.model.GmlEdge;
import br.cns.persistence.GmlDao;
import br.upe.jol.problems.simon.entity.CallSchedulerNonUniformHub;
import br.upe.jol.problems.simton.GmlSimton;
import br.upe.jol.problems.simton.SolutionONTD;

public class LatencyAnalysis {
	private static final int NUM_RUNS = 1;
	
	public static void main(String[] args) {
		String[] networks = new String[] { "Nsfnet.gml", "arnes.gml", "hse_medianet_hessen_germany.gml",
				"restena.gml" };
		String basePath = "C:/workspace_research/simton3/results_security/";

		for (String strNet : networks) {
			for (int load = 100; load <= 100; load += 20) {
				simulate(strNet, basePath, load);
			}
		}

	}

	private static void simulate(String strNet, String basePath, int load) {
		GmlDao dao = new GmlDao();
		GmlData data = dao.loadGmlData(basePath + strNet);
		int numNodes = data.getNodes().size();
		Double[][] traffic = createUniformMatrix(numNodes);
		CallSchedulerNonUniformHub.TRAFFICMATRIX = traffic;
		GmlSimton problem = new GmlSimton(numNodes, 4, data, load);

		Integer[] dv = new Integer[numNodes * (numNodes - 1) / 2 + 2];
		for (int i = 0; i < dv.length; i++) {
			dv[i] = 0;
		}
		for (GmlEdge edge : data.getEdges()) {
			int count = 0;
			for (int i = 0; i < numNodes; i++) {
				for (int j = i + 1; j < numNodes; j++) {
					if ((edge.getSource().getId() == i && edge.getTarget().getId() == j)
							|| (edge.getSource().getId() == j && edge.getTarget().getId() == i)) {
						dv[count] = 3;
					}
					count++;
				}
			}
		}
		dv[dv.length - 1] = 40;
		dv[dv.length - 2] = 4;

		SolutionONTD sol = new SolutionONTD(problem, dv);

		for (int r = 0; r < NUM_RUNS; r++) {
			problem.evaluate(sol);
			System.out.printf("%s\t%d\t%.6f\n", strNet, load, sol.getObjective(0));
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
}
