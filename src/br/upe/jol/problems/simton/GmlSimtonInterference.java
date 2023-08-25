package br.upe.jol.problems.simton;

import br.cns.Geolocation;
import br.cns.model.GmlData;
import br.cns.model.GmlEdge;
import br.cns.persistence.GmlDao;
import br.upe.jol.problems.simon.entity.CallSchedulerNonUniformHub;
import br.upe.jol.problems.simon.rwa.InterferenceYenSimulator;

public class GmlSimtonInterference extends GmlSimton {

	public GmlSimtonInterference(int numberOfNodes_ppr, int numberOfObjectives,
			String gmlFile, double networkLoad) {
		super(numberOfNodes_ppr, numberOfObjectives, gmlFile, networkLoad);
		simulator = new InterferenceYenSimulator();
	}

	public GmlSimtonInterference(int numberOfNodes_ppr, int numberOfObjectives,
			GmlData data, double networkLoad) {
		super(numberOfNodes_ppr, numberOfObjectives, data, networkLoad);
		simulator = new InterferenceYenSimulator();
	}

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
//		String strNet = "hse_medianet_hessen_germany.gml";
		String strNet = "arnes.gml";
		String basePath = "C:/workspace_phd/Icton15/results/";

		GmlDao dao = new GmlDao();
		GmlData data = dao.loadGmlData(basePath + strNet);
		int numNodes = data.getNodes().size();
		Double[][] traffic = createUniformMatrix(numNodes);
		CallSchedulerNonUniformHub.TRAFFICMATRIX = traffic;
		Geolocation[] locations = new Geolocation[numNodes];
		double meanLat = 0;
		for (int i = 0; i < locations.length; i++) {
			locations[i] = new Geolocation(
					data.getNodes().get(i).getLatitude(), data.getNodes()
							.get(i).getLongitude());
			meanLat += data.getNodes().get(i).getLatitude();
		}
		meanLat /= locations.length;
		System.out.println("Grupo 1:");
		for (int i = 0; i < locations.length; i++) {
			if (locations[i].getLatitude() < meanLat) {
				System.out.print(i + ", ");
			}
		}
		System.out.println("\nGrupo 2:");
		for (int i = 0; i < locations.length; i++) {
			if (locations[i].getLatitude() >= meanLat) {
				System.out.print(i + ", ");
			}
		}
		System.out.println();
		int load = 110;
		GmlSimton problem = new GmlSimton(numNodes, 2, data, load);
//		GmlSimtonInterference problem = new GmlSimtonInterference(numNodes, 2, data, load);

		Integer[] dv = new Integer[numNodes * (numNodes - 1) / 2 + 2];
		for (int i = 0; i < dv.length; i++) {
			dv[i] = 0;
		}
		for (GmlEdge edge : data.getEdges()) {
			int count = 0;
			for (int i = 0; i < numNodes; i++) {
				for (int j = i + 1; j < numNodes; j++) {
					if ((edge.getSource().getId() == i && edge.getTarget().getId() == j) || (edge.getSource().getId() == j && edge.getTarget().getId() == i) ) {
						dv[count] = 3;
					}
					count++;
				}
			}
		}
		dv[dv.length-1] = 40;
		dv[dv.length-2] = 4;

		SolutionONTD sol = new SolutionONTD(problem, dv);

		for (int r = 0; r < 10; r++) {
			problem.evaluate(sol);
			System.out.printf("%.6f\n", sol.getObjective(0));
		}
	}
}
