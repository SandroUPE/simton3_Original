package br.upe.jol.dataset.experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.cns.GravityModel;
import br.cns.TMetric;
import br.cns.experiments.ComplexNetwork;
import br.cns.experiments.nodePositions.CircularNetwork;
import br.cns.model.GmlData;
import br.cns.model.GmlEdge;
import br.cns.models.BarabasiDensity;
import br.cns.models.GenerativeProcedure;
import br.cns.models.TModel;
import br.cns.models.WattsStrogatzDensity;
import br.cns.persistence.GmlDao;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.problems.simon.entity.CallSchedulerNonUniformHub;
import br.upe.jol.problems.simton.GmlSimton;
import br.upe.jol.problems.simton.SolutionONTD;

public class DatasetBuilder {
	public static void main(String[] args) {
		evaluateFixedNetwork();
	}

	public static void evaluateFixedNetwork() {
		
		SimpleDateFormat df = new SimpleDateFormat("YYYY.dd.MM.HH.mm");
		GmlDao dao = new GmlDao();
		GmlData gmlData = null;
		String basePath  = "C:/UFRPE/pesquisas/dataset_otn/";
		for (int a = 2; a < 28; a++) {
			String strNet = "line" + a + ".gml";
			gmlData = dao.loadGmlData(basePath + strNet);
			int n = gmlData.getNodes().size();
			
			createGravityTraffic(basePath, n, strNet, gmlData);
			
			GmlSimton problem = new GmlSimton(n, 2, gmlData, 60);
			List<TMetric> metrics = TMetric.getDefaults();
			Integer[] variables = new Integer[(n * (n - 1)) / 2 + 2];
			variables[variables.length - 1] = 40;
			variables[variables.length - 2] = 4;
			int count = 0;
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					variables[count] = 0;
					for (GmlEdge edge : gmlData.getEdges()) {
						if ((edge.getSource().getId() == i && edge.getTarget().getId() == j)
								|| (edge.getSource().getId() == j && edge.getTarget().getId() == i)) {
							variables[count] = 3;
							break;
						}
					}
					count++;
				}
			}

			SolutionONTD sol = new SolutionONTD(problem, variables);
			problem.evaluate(sol);
			System.out.println("Rede " + a);
			System.out.printf("BP = %.6f\n", sol.getObjective(0));
			ComplexNetwork cn = gmlData.createComplexNetworkDistance();
			for (TMetric m : cn.getMetricValues().keySet()) {
				System.out.printf("%s = %.6f\n", m.toString(), cn.getMetricValues().get(m));
			}
		}

	}
	
	private static Double[][] createGravityTraffic(String basePath, int max, String strNet, GmlData data) {
		GravityModel gm = new GravityModel(data);
		Double[][] traffic = gm.getTrafficMatrix();

		StringBuffer sbTrafficMatrix = new StringBuffer();
		for (int i = 0; i < max; i++) {
			for (int j = 0; j < max; j++) {
				sbTrafficMatrix.append(String.format("%.4f ", traffic[i][j]));
			}
			sbTrafficMatrix.append("\n");
		}
		try {
			FileWriter fw = new FileWriter(basePath + "results/" + strNet + "_tm.txt");
			fw.write(sbTrafficMatrix.toString());
			fw.close();
		} catch (IOException e) {
		}
		CallSchedulerNonUniformHub.TRAFFICMATRIX = traffic;
		return traffic;
	}


}
