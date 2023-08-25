package br.upe.jol.base;

import java.util.Comparator;
import java.util.Iterator;

import br.upe.jol.base.comparators.DominanceComparator;
import br.upe.jol.base.comparators.DominanceComparator2;

/**
 * Classe adaptada a partir da classe AdaptiveGridArchive do JMetal.
 * 
 * @author Erick Barboza
 */
public class ArchiveGrid<T> extends SolutionSet<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Grid<T> grid;
	
	private int maxSize;
	
	private Comparator<Solution<T>> dominance;
	
	public ArchiveGrid(int maxSize, int bisections, int objectives){
		super(maxSize);
		this.maxSize = maxSize;
		this.grid = new Grid<T>(bisections, objectives);
		this.dominance = new DominanceComparator<T>();
	}
	
	public boolean add(Solution<T> solution) {
	    //Iterator of individuals over the list
	    Iterator<Solution<T>> iterator = solutionsList.iterator();
	        
	    while (iterator.hasNext()){
	      Solution<T> element = iterator.next();
	      int flag = dominance.compare(solution,element);
	      if (flag == -1) { // The Individual to insert dominates other 
	    	                // individuals in  the archive
	        iterator.remove(); //Delete it from the archive
	        int location = grid.location(element);
	        if (location > 0 && grid.getLocationDensity(location) > 1) {//The hypercube contains 
	          grid.removeSolution(location);            //more than one individual
	        } else {
	          grid.updateGrid(this);
	        } // else
	      } // if 
	      else if (flag == 1) { // An Individual into the file dominates the 
	    	                      // solution to insert
	        return false; // The solution will not be inserted
	      } // else if           
	    } // while
	        
	    // At this point, the solution may be inserted
	    if (size() == 0){ //The archive is empty
	      solutionsList.add(solution);
	      grid.updateGrid(this);        
	      return true;
	    } //
	        
	    if (size() < maxSize){ //The archive is not full              
	      grid.updateGrid(solution,this); // Update the grid if applicable
	      int location;
	      location= grid.location(solution); // Get the location of the solution
	      grid.addSolution(location); // Increment the density of the hypercube
	      solutionsList.add(solution); // Add the solution to the list
	      return true;
	    } // if
	        
	    // At this point, the solution has to be inserted and the archive is full
	    grid.updateGrid(solution,this);
	    int location = grid.location(solution);
	    if (location == grid.getMostPopulated()) { // The solution is in the 
	    	                                        // most populated hypercube
	      return false; // Not inserted
	    } else {
	      // Remove an solution from most poblated area
	      iterator = solutionsList.iterator();
	      boolean removed = false;
	      while (iterator.hasNext()) {
	        if (!removed) {
	          Solution<T> element = iterator.next();
	          int location2 = grid.location(element);
	          if (location2 == grid.getMostPopulated()) {
	            iterator.remove();
	            grid.removeSolution(location2);
	          } // if
	        } // if
	      } // while
	      
	      // A solution from most populated hypercube has been removed, 
	      // insert now the solution
	      grid.addSolution(location);
	      solutionsList.add(solution);            
	    }
	    return true;
	  } // add
		
		public Solution<T> getIndividual(int location){
			for(Solution<T> s : this.solutionsList){
				if(this.grid.location(s) == location)
					return s;
			}
			return null;
		}
	
		
	    
	  /**
	   * Returns the AdaptativeGrid used
	   * @return the AdaptativeGrid
	   */
	  public Grid<T> getGrid() {
	    return grid;
	  }
}
