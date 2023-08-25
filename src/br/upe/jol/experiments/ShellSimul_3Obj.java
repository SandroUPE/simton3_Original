/**
 * 
 */
package br.upe.jol.experiments;

import java.io.File;

import br.upe.jol.base.Solution;
import br.upe.jol.base.SolutionSet;
import br.upe.jol.metaheuristics.modeii.IMODEII;
import br.upe.jol.metaheuristics.nsgaii.SNSGAII;
import br.upe.jol.metaheuristics.paes.IPAES;
import br.upe.jol.metaheuristics.pesaii.IPesaII;
import br.upe.jol.metaheuristics.spea2.ISPEA2;
import br.upe.jol.problems.simton.SimonProblem_3Obj;

/**
 * .
 * 
 * @author Danilo Ara?jo
 */
public class ShellSimul_3Obj {
	public static void main(String[] args) {
		boolean parOk = true;
		SimonProblem_3Obj ontd = new SimonProblem_3Obj(14, 3);
//		NaiveConstSimonProblem ontd = new NaiveConstSimonProblem(14, 2);
		ISPEA2 spea2 = null;
		SNSGAII nsgaii = null;
		IPesaII pesaii = null;
		IPAES paes = null;
		IMODEII mode = null;
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
					ultIteracao = Integer.parseInt(args[2].substring(args[2].lastIndexOf("_")+1, args[2].lastIndexOf(".")));
					path = args[2].substring(0, args[2].lastIndexOf(File.separator)+1);
				} catch (Exception e) {
					parOk = false;
				}
			}else if (args[0].equalsIgnoreCase("s")){
				path = args[2];
				parOk = true;
			}
			try {
				numCalls = Integer.parseInt(args[3]);
			} catch (Exception e) {}
			try {
				usarCache = args[4] == null || args[4].equals("N") ? false : true;
			} catch (Exception e) {}
			ontd.setCALLS(numCalls);
			SimonProblem_3Obj.setUsarCache(usarCache);
			if (args[1].equalsIgnoreCase("nsgaii")){ 
				nsgaii = new SNSGAII(50, 2000, ontd);
				nsgaii.setCrossoverProbability(1);
				nsgaii.setMutationProbability(0.03);
				nsgaii.setPathFiles(path);
			}else if (args[1].equalsIgnoreCase("spea2")){
				spea2 = new ISPEA2(50, 50, 2000, ontd);
				spea2.setCrossoverProbability(1);
				spea2.setMutationProbability(0.03);
				spea2.setPathFiles(path);
			}else if (args[1].equalsIgnoreCase("pesaii")){
				pesaii = new IPesaII(50, 50, 2000, ontd);
				pesaii.setCrossoverProbability(1);
				pesaii.setMutationProbability(0.03);
				pesaii.setNumberOfDivisions(12);
				pesaii.setPathFiles(path);
			}else if (args[1].equalsIgnoreCase("paes")){
				paes = new IPAES(50, 2000 * 50, ontd);
				paes.setnDivs(12);
				paes.setPathFiles(path);
			}else if (args[1].equalsIgnoreCase("mode")){
				mode = new IMODEII(50, 50, 2000, ontd);
				mode.setCrossoverProbability(.3);
				mode.setPathFiles(path);
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
			}else if (args[1].equalsIgnoreCase("spea2")){
				if (continuar){
					solutions1 = spea2.execute(ss, ultIteracao);
				}else{
					solutions1 = spea2.execute();
				}
			}else if (args[1].equalsIgnoreCase("pesaii")){
				if (continuar){
					solutions1 = pesaii.execute(ss, ultIteracao);
				}else{
					solutions1 = pesaii.execute();
				}
			}else if (args[1].equalsIgnoreCase("paes")){
				if (continuar){
					solutions1 = paes.execute(ss, ultIteracao);
				}else{
					solutions1 = paes.execute();
				}
			}else if (args[1].equalsIgnoreCase("mode")){
				if (continuar){
					solutions1 = mode.execute(ss, ultIteracao);
				}else{
					solutions1 = mode.execute();
				}
			}
			System.out.println("============================================================");
			System.out.println("				FIM DA EXECU??O. SOLU??ES:");
			System.out.println("============================================================");
			System.out.println(solutions1.toString());
		}else{
			System.out.println("============================================================");
			System.out.println("			PAR?METROS INV?LIDOS. SINTAXE:");
			System.out.println("============================================================");
			System.out.println(">java -jar jol.jar s|c NSGAII|SPEA2|PESA2|PAES <PATH_VAR> <QTDE_CH> <USA_CACHE>");
			System.out.println("------------------------------------------------------------");
			System.out.println("	Op??es:");
			System.out.println("------------------------------------------------------------");
			System.out.println("	s|c		s -> nova execu??o; c -> continuar execu??o;");
			System.out.println("	NSGAII|SPEA2|PESA2|PAES		algoritmo selecionado;");
			System.out.println("	<PATH_VAR>	caminho do arquivo de vari?veis");
			System.out.println("============================================================");
		}
	}
}
