package br.upe.jol.problems.simon.rwa;

import java.util.List;
import java.util.Vector;

import br.upe.jol.problems.simon.entity.Call;
import br.upe.jol.problems.simon.entity.Link;
import br.upe.jol.problems.simon.entity.Node;
import edu.asu.emit.qyan.alg.control.YenTopKShortestPathsAlg;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.Vertex;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class Yen {
	public static final int K = 3;
	private static final List<Path> existing = new Vector<Path>();
	public static Double[][] distances;
	
	public static int[][] GROUPS_ARNES = new int[][]{{0, 1, 3, 6, 7, 9, 10, 14, 16, 17, 18, 19, 20, 22, 23},
		{2, 4, 5, 8, 11, 12, 13, 15, 21, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33}};
	
	public static List<List<Integer>> LIST_GROUPS_ARNES = new Vector<>();
	
	public Yen() {
		cretateListGroups();
	}

	private static void cretateListGroups() {
		List<Integer> g1 = new Vector<>();
		for (Integer i : GROUPS_ARNES[0]) {
			g1.add(i);
		}
		LIST_GROUPS_ARNES.add(g1);
		List<Integer> g2 = new Vector<>();
		for (Integer i : GROUPS_ARNES[1]) {
			g2.add(i);
		}
		LIST_GROUPS_ARNES.add(g2);
	}
	
	public static void removeCall(Call call) {
		Path toRemoval = new Path();
		Vertex vertex = new Vertex();
//		System.out.println("Removendo chamada");
		vertex.set_id(call.getFibersUpLink().get(0).getSourceNode());
		toRemoval.get_vertices().add(vertex);
		for (int i = 0; i < call.getFibersUpLink().size(); i++) {
			vertex = new Vertex();
			vertex.set_id(call.getFibersUpLink().get(i).getDestinationNode());
			toRemoval.get_vertices().add(vertex);
		}
//		System.out.println("Antes " + existing.size());
		existing.remove(toRemoval);
//		System.out.println("Depois " + existing.size());
	}

	public static void findRoute(Link[][] matAdj_par, int fonte_par,
			int dest_par, Vector<Link> caminho_par,
			Vector<Node> vectorOfNodes_par, boolean hops) {
		if (LIST_GROUPS_ARNES.isEmpty()) {
			cretateListGroups();
		}
		Graph graph = null;
//		System.out.println("Procurando rota de " + fonte_par + " para " + dest_par);
		// transformar a representação do SIMTON na representação da API
		graph = new Graph();
		for (int i = 0; i < vectorOfNodes_par.size(); i++) {
			Vertex vertex = new Vertex();
			vertex.set_id(i);
			graph.addVertex(vertex);
		}
		for (int i = 0; i < vectorOfNodes_par.size(); i++) {
			for (int j = 0; j < vectorOfNodes_par.size(); j++) {
				if (i != j) {
					graph.add_edge(i, j, matAdj_par[i][j].getLength());
				}
			}
		}
		BaseVertex source = graph.get_vertex(fonte_par);
		BaseVertex dest = graph.get_vertex(dest_par);

		YenTopKShortestPathsAlg yen = new YenTopKShortestPathsAlg(graph);
		Path best = getBestKMinInterference(existing, yen.get_shortest_paths(source, dest, K));

		// transformar a representação da API na representação do SIMTON
		for (int i = 1; i < best.get_vertices().size(); i++) {
			caminho_par
					.add(matAdj_par[best.get_vertices().get(i - 1).get_id()][best
							.get_vertices().get(i).get_id()]);
		}
		existing.add(best);
	}

	public static Path getBestKMinInterference(List<Path> existing, List<Path> trials) {
		double minInterference = Double.MAX_VALUE;
		int index = -1;

		for (int i = 0; i < trials.size(); i++) {
			double interference = interference(existing, trials.get(i));
//			System.out.println("Interferencia da rota " + i + " = " + interference);
			if (interference < minInterference) {
				minInterference = interference;
				index = i;
			}
		}

//		System.out.println("Selecionou a rota de interferencia " + minInterference);
		return trials.get(index);
	}
	
	public static Path getBestKMaxInterference(List<Path> existing, List<Path> trials) {
		double maxInterference = -1;
		int index = -1;

		for (int i = 0; i < trials.size(); i++) {
			double interference = interference(existing, trials.get(i));
//			System.out.println("Interferencia da rota " + i + " = " + interference);
			if (interference >= maxInterference) {
				maxInterference = interference;
				index = i;
			}
		}

//		System.out.println("Selecionou a rota de interferencia " + minInterference);
		return trials.get(index);
	}

	public static double interference(List<Path> existing, Path trial) {
		double totalinterference = 0;

		for (Path path : existing) {
			totalinterference += interference(path, trial);
		}

		return totalinterference / (existing.size()+1);
	}

	public static double interference(Path existing, Path trial) {
		double interferencex = 0;
		
		List<BaseVertex> group1 = new Vector<>();
		List<BaseVertex> group2 = new Vector<>();
		
//		int ge1 = 0;
//		int ge2 = 0;
//		
//		for (BaseVertex v : existing.get_vertices()) {
//			if (LIST_GROUPS_ARNES.get(0).contains(v.get_id())) {
//				ge1++;
//			} else {
//				ge2++;
//			}
//		}
//		
//		int ge = 1;
//		if (ge1 < ge2) {
//			ge = 2;
//		}
//		
//		int gt1 = 0;
//		int gt2 = 0;
//		
//		for (BaseVertex v : trial.get_vertices()) {
//			if (LIST_GROUPS_ARNES.get(0).contains(v.get_id())) {
//				gt1++;
//			} else {
//				gt2++;
//			}
//		}
//		
//		int gt = 1;
//		if (gt1 < gt2) {
//			gt = 2;
//		}
//		
//		if (gt != ge) {
//			return 0;
//		}
		
		for (BaseVertex vertex : trial.get_vertices()) {
			if (!existing.get_vertices().contains(vertex)) {
				group1.add(vertex);
			}
		}
		for (BaseVertex vertex : existing.get_vertices()) {
			if (!trial.get_vertices().contains(vertex)) {
				group2.add(vertex);
			}
		}
		for (int i = 0; i < group1.size(); i++) {
			for (int j = 0; j < group2.size(); j++) {
				interferencex += distances[i][j];
			}
		}

		interferencex = existing.get_vertices().size()
				* trial.get_vertices().size() / (1 + interferencex);

		return interferencex;
	}

	public static void findRoute(Link[][] matAdj_par, int fonte_par,
			int dest_par, Vector<Link> caminho_par,
			Vector<Node> vectorOfNodes_par) {
		findRoute(matAdj_par, fonte_par, dest_par, caminho_par,
				vectorOfNodes_par, false);
	}
}
