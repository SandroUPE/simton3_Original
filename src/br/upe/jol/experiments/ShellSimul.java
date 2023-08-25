/**
 * 
 */
package br.upe.jol.experiments;

import java.io.File;

import br.upe.jol.base.Problem;
import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.metaheuristics.dwga.DWGA;
import br.upe.jol.metaheuristics.modeii.IMODE2;
import br.upe.jol.metaheuristics.modeii.IMODEII;
import br.upe.jol.metaheuristics.nsgaii.INSGAII;
import br.upe.jol.metaheuristics.nsgaii.NSGAII_Busca_Gulosa;
import br.upe.jol.metaheuristics.paes.IPAES;
import br.upe.jol.metaheuristics.pesaii.IPesaII;
import br.upe.jol.metaheuristics.spea2.ISPEA2;
import br.upe.jol.problems.simton.SimonProblem_3Obj;

/**
 * .
 * 
 * @author Danilo Araújo
 */
public class ShellSimul {
	public static void main(String[] args) {
		boolean parOk = true;
//		Problem<Integer> ontd = new SimonProblem(14, 2);
		Problem<Integer> ontd = new SimonProblem_3Obj(14, 3);
//		NaiveConstSimonProblem ontd = new NaiveConstSimonProblem(14, 2);
		ISPEA2 spea2 = null;
		DWGA dwga = null;
		INSGAII nsgaii = null;
		NSGAII_Busca_Gulosa nsgaiibg = null;
		IPesaII pesaii = null;
		IPAES paes = null;
		IMODEII mode = null;
		IMODE2 mode2 = null;
		SolutionSet<Integer> ss = new SolutionSet<Integer>();
		boolean continuar = false;
		double numCalls = 20000;
		boolean usarCache = false;
		int ultIteracao = 1;
		String path = ".";
		if (args.length > 2){
			if (args[0].equalsIgnoreCase("c")){
				try {
					ss.readIntVariablesFromFile(args[2], ontd);
					continuar = true;
					String strIter = args[2].substring(0, args[2].lastIndexOf("_"));
					while (strIter.contains(".") || strIter.contains(",")){
						strIter = strIter.replace(".", "");
						strIter = strIter.replace(",", "");
					}
					ultIteracao = Integer.parseInt(strIter.substring(strIter.lastIndexOf("_")+1, strIter.length()));
					System.out.println("Continuando a partir da iteração: " + ultIteracao);
					path = args[2].substring(0, args[2].lastIndexOf(File.separator)+1) + ".";
				} catch (Exception e) {
					e.printStackTrace();
					parOk = false;
				}
			}else if (!args[0].equalsIgnoreCase("s")){
				path = args[2];
				parOk = false;
			}
			try {
				numCalls = Integer.parseInt(args[3]);
			} catch (Exception e) {}
			try {
				usarCache = args[4] == null || args[4].equals("N") ? false : true;
			} catch (Exception e) {}
			if (args[1].equalsIgnoreCase("nsgaii")){ 
				nsgaii = new INSGAII(50, 10000, ontd);
				nsgaii.setCrossoverProbability(1);
				nsgaii.setMutationProbability(1/93.0);
				nsgaii.setPathFiles(path);
			}else if (args[1].equalsIgnoreCase("nsgaiibg")){ 
				nsgaiibg = new NSGAII_Busca_Gulosa(50, 1000, ontd);
				nsgaiibg.setCrossoverProbability(1);
				nsgaiibg.setMutationProbability(0.03);
				nsgaiibg.setPathFiles(path);
			}else if (args[1].equalsIgnoreCase("dwga")){
				dwga = new DWGA(50, 1000, ontd);
				dwga.setCrossoverProbability(1);
				dwga.setMutationProbability(0.06);
				dwga.setPathFiles(path);
			}else if (args[1].equalsIgnoreCase("spea2")){
				spea2 = new ISPEA2(50, 50, 10000, ontd);
				spea2.setCrossoverProbability(1);
				spea2.setMutationProbability(1/93.0);
				spea2.setPathFiles(path);
			}else if (args[1].equalsIgnoreCase("pesaii")){
				pesaii = new IPesaII(50, 50, 10000, ontd);
				pesaii.setCrossoverProbability(1);
				pesaii.setMutationProbability(0.03);
				pesaii.setNumberOfDivisions(5);
				pesaii.setPathFiles(path);
			}else if (args[1].equalsIgnoreCase("paes")){
				paes = new IPAES(50, 10000 * 50, ontd);
				paes.setnDivs(5);
				paes.setPathFiles(path);
			}else if (args[1].equalsIgnoreCase("mode")){
				mode = new IMODEII(50, 50, 10000, ontd);
				mode.setCrossoverProbability(.3);
				mode.setPathFiles(path);
			}else if (args[1].equalsIgnoreCase("mode2")){
				mode2 = new IMODE2(50, 50, 10000, ontd);
				mode2.setCrossoverProbability(.3);
				mode2.setPathFiles(path);
			}else{
				parOk = false;
			}
		}else{
			parOk = false;
		}
		
		if (parOk){
			SolutionSet<Integer> solutions1 = null;
			if (args[1].equalsIgnoreCase("nsgaii")){
				if (continuar){
					for (Solution<Integer> sol : ss.getSolutionsList()){
						ontd.evaluate(sol);
					}
					solutions1 = nsgaii.execute(ss, ultIteracao);
				}else{
					solutions1 = nsgaii.execute();
				}
			}else if (args[1].equalsIgnoreCase("nsgaiibg")){
				if (continuar){
					for (Solution<Integer> sol : ss.getSolutionsList()){
						ontd.evaluate(sol);
					}
					solutions1 = nsgaiibg.execute(ss, ultIteracao);
				}else{
					solutions1 = nsgaiibg.execute();
				}
			}else if (args[1].equalsIgnoreCase("spea2")){
				if (continuar){
					for (Solution<Integer> sol : ss.getSolutionsList()){
						ontd.evaluate(sol);
					}
					solutions1 = spea2.execute(ss, ultIteracao);
				}else{
					solutions1 = spea2.execute();
				}
			}else if (args[1].equalsIgnoreCase("dwga")){
				if (continuar){
					for (Solution<Integer> sol : ss.getSolutionsList()){
						ontd.evaluate(sol);
					}
					solutions1 = dwga.execute(ss, ultIteracao);
				}else{
					solutions1 = dwga.execute();
				}
			}else if (args[1].equalsIgnoreCase("pesaii")){
				if (continuar){
					for (Solution<Integer> sol : ss.getSolutionsList()){
						ontd.evaluate(sol);
					}
					solutions1 = pesaii.execute(ss, ultIteracao);
				}else{
					solutions1 = pesaii.execute();
				}
			}else if (args[1].equalsIgnoreCase("paes")){
				if (continuar){
					for (Solution<Integer> sol : ss.getSolutionsList()){
						ontd.evaluate(sol);
					}
					solutions1 = paes.execute(ss, ultIteracao);
				}else{
					solutions1 = paes.execute();
				}
			}else if (args[1].equalsIgnoreCase("mode")){
				if (continuar){
					for (Solution<Integer> sol : ss.getSolutionsList()){
						ontd.evaluate(sol);
					}
					solutions1 = mode.execute(ss, ultIteracao);
				}else{
					solutions1 = mode.execute();
				}
			}else if (args[1].equalsIgnoreCase("mode2")){
				if (continuar){
					for (Solution<Integer> sol : ss.getSolutionsList()){
						ontd.evaluate(sol);
					}
					solutions1 = mode2.execute(ss, ultIteracao);
				}else{
					solutions1 = mode2.execute();
				}
			}
			System.out.println("============================================================");
			System.out.println("				FIM DA EXECUÇÃO. SOLUÇÕES:");
			System.out.println("============================================================");
			System.out.println(solutions1.toString());
		}else{
			System.out.println("============================================================");
			System.out.println("			PARÂMETROS INVÁLIDOS. SINTAXE:");
			System.out.println("============================================================");
			System.out.println(">java -jar ShellSimul s|c NSGAII|SPEA2|PESA2|PAES <PATH_VAR> <QTDE_CH> <USA_CACHE>");
			System.out.println("------------------------------------------------------------");
			System.out.println("	Opções:");
			System.out.println("------------------------------------------------------------");
			System.out.println("	s|c		s -> nova execução; c -> continuar execução;");
			System.out.println("	NSGAII|SPEA2|PESA2|PAES		algoritmo selecionado;");
			System.out.println("	<PATH_VAR>	caminho do arquivo de variáveis");
			System.out.println("============================================================");
		}
	}
}
