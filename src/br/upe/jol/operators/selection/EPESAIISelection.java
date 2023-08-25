package br.upe.jol.operators.selection;

import br.upe.jol.base.ArchiveGrid;
import br.upe.jol.base.PseudoRandom;
import br.upe.jol.base.Solution;

public class EPESAIISelection<T>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public Solution<T> execute(ArchiveGrid<T> archive) {
		 try {
		      int selected;        
		      int hc1 = archive.getGrid().randomOccupiedHypercube();
		      int hypercube2 = archive.getGrid().randomOccupiedHypercube();                                        
		        
		      if (hc1 != hypercube2){
		        if (archive.getGrid().getLocationDensity(hc1) < 
		            archive.getGrid().getLocationDensity(hypercube2)) {
		        
		          selected = hc1;
		        
		        } else if (archive.getGrid().getLocationDensity(hypercube2) <
		                   archive.getGrid().getLocationDensity(hc1)) {
		        
		          selected = hypercube2;
		        } else {
		          if (PseudoRandom.randDouble() < 0.5) {
		            selected = hypercube2;
		          } else {
		            selected = hc1;
		          }
		        }
		      } else { 
		        selected = hc1;
		      }
		      int base = PseudoRandom.randInt(0,archive.size()-1);
		      int cnt = 0;
		      while (cnt < archive.size()){   
		        Solution<T> individual = archive.get((base + cnt)% archive.size());        
		        if (archive.getGrid().location(individual) != selected){
		          cnt++;                
		        } else {
		          return individual;
		        }
		      }        
		      return archive.get((base + cnt) % archive.size());
		    } catch (ClassCastException e) {
		      /*Configuration.logger_.severe("PESA2Selection.execute: ClassCastException. " +
		          "Found" + object.getClass() + "Expected: AdaptativeGridArchive") ;*/
		      Class cls = java.lang.String.class;
		      String name = cls.getName(); 
		      throw new RuntimeException("Exception in " + name + ".execute()") ;
		    }
	}

}
