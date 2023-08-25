/**
 * 
 */
package br.upe.jol.metaheuristics;

import java.util.HashMap;

import br.upe.jol.base.Algorithm;
import br.upe.jol.base.Problem;
import br.upe.jol.metaheuristics.cssmopso.CSSMOPSO;
import br.upe.jol.metaheuristics.dnpso.DNPSO;
import br.upe.jol.metaheuristics.dwga.DDWGA;
import br.upe.jol.metaheuristics.dwga.DWGA;
import br.upe.jol.metaheuristics.modeii.MODEII;
import br.upe.jol.metaheuristics.mopso.MOPSO;
import br.upe.jol.metaheuristics.mopsocdr.MOPSOCDR;
import br.upe.jol.metaheuristics.nsgaii.NSGAII;
import br.upe.jol.metaheuristics.paes.PAES;
import br.upe.jol.metaheuristics.pesaii.PESAII;
import br.upe.jol.metaheuristics.spea2.SPEA2;
import br.upe.jol.problems.TProblems;
import br.upe.jol.problems.dtlz.DTLZ1;
import br.upe.jol.problems.dtlz.DTLZ2;
import br.upe.jol.problems.dtlz.DTLZ4;
import br.upe.jol.problems.dtlz.DTLZ5;
import br.upe.jol.problems.dtlz.DTLZ6;
import br.upe.jol.problems.dtlz.DTLZ7;
import br.upe.jol.problems.zdt.ZDT1;
import br.upe.jol.problems.zdt.ZDT2;
import br.upe.jol.problems.zdt.ZDT3;
import br.upe.jol.problems.zdt.ZDT4;
import br.upe.jol.problems.zdt.ZDT6;

/**
 * .
 * 
 * @author Danilo Ara�jo
 */
public class AlgorithmFactory {
	private static final AlgorithmFactory instance = new AlgorithmFactory();
	
	private HashMap<String, Algorithm<Double>> map = new HashMap<String, Algorithm<Double>>();
	
	private AlgorithmFactory(){
	}

	public Algorithm<Double> getAlgorithm(TAlgorithm tAlgorithm, TProblems tProblem){
		Algorithm<Double> ret = map.get(tAlgorithm + "-" + tProblem);
		Problem<Double> problem = null;
		
		if (ret == null){
			switch (tProblem) {
			case ZDT1:
				problem = new ZDT1(30, 2);
				break;
			case ZDT2:
				problem = new ZDT2(30, 2);
				break;
			case ZDT3:
				problem = new ZDT3(30, 2);
				break;
			case ZDT4:
				problem = new ZDT4(10, 2);
				break;
			case ZDT6:
				problem = new ZDT6(10, 2);
				break;
			case DTLZ1:
				problem = new DTLZ1(7, 2);
				break;
			case DTLZ2:
				problem = new DTLZ2(11, 2);
				break;
			case DTLZ4:
				problem = new DTLZ4(11, 2);
				break;
			case DTLZ5:
				problem = new DTLZ5(11, 2);
				break;
			case DTLZ6:
				problem = new DTLZ6(11, 2);
				break;
			case DTLZ7:
				problem = new DTLZ7(21, 2);
				break;
			default:
				problem = new ZDT1(30, 2);
				break;
			}
			
			switch (tAlgorithm) {
			case NSGAII:
				ret = new NSGAII(100, 1000, problem);
				break;
			case SPEA2:
				ret = new SPEA2(50, 100, 3000, problem);
				break;
			case CSSMOPSO:
				ret = new CSSMOPSO(40, 100, problem);
				break;
			case DNPSO:
				ret = new DNPSO(40, 100, problem);
				break;
			case MOPSO:
				ret = new MOPSO(40, 100, problem);
				break;
			case MOPSOCDR:
				ret = new MOPSOCDR(40, 100, problem);
				break;
			case PAES:
				ret = new DDWGA(50, 1000, problem);
				break;
			case PESAII:
				ret = new PESAII(50, 100, 3000, problem);
				break;
			case MODEII:
				ret = new MODEII(50, 100, 1000, problem);
				break;
			default:
				ret = new NSGAII(40, 100, problem);
				break;
			}
			map.put(tAlgorithm + "-" + tProblem, ret);
		}
		
		return ret;
	}

	public static AlgorithmFactory getInstance() {
		return instance;
	}
}
