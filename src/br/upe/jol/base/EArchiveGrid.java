package br.upe.jol.base;

public class EArchiveGrid<T> extends SolutionSet<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HyperCubeGrid<T> grid;
	
	private int numberOfHypercubes;
		
	public EArchiveGrid(int maxSize, int objectives, int numberOfDivisions){
		super(maxSize);
		this.numberOfHypercubes = numberOfDivisions;
		this.grid = new HyperCubeGrid<T>(objectives, numberOfDivisions);
	}

	public void addSolution(Solution<T> solution){
		if(this.size() == 0){
			this.add(solution);
			this.grid.addSolution(0, this);
		}else{
			for(int i=0; i<this.size(); i++){
				Solution<T> aux = this.get(i);
				if(aux.dominates(solution)){
					//solução é dominada por alguma solução do arquivo
					break;
				}else if(solution.dominates(aux)){
					//solução domina uma solução do arquivo
					
					//remove a solução dominada da lista
					this.remove(i);
					//adiciona a nova solução na lista
					this.add(solution);
					
					//remove a solução do grid
					this.grid.removeSolution(i, this);
					//adiciona a nova solução no grid
					this.grid.addSolution(this.size()-1, this);
					
					break;
				}
				
				//Solução não domina nem é dominada
				if(this.size() < this.getCapacity()){
					//arquivo não esta cheio
					
					//adiciona a nova solução na lista
					this.add(solution);
					//adiciona a nova solução no grid
					this.grid.addSolution(this.size()-1, this);
				}else{
					//arquivo está cheio
					
					//solucao do hypercubo mais ocupado
					int toRemove = this.grid.getMostCrowded().getRandom();
					
					//remove a solução escolhida aleatoriamente da lista
					this.remove(toRemove);
					//adiciona a nova solução na lista
					this.add(solution);
					
					//remove a solução escolhida aleatoriamente do grid
					this.grid.removeSolution(toRemove, this);
					//adiciona a nova solução no grid
					this.grid.addSolution(this.size()-1, this);
					
				}
			}
		}
	}
	
	public int getNumberOfHypercubes() {
		return numberOfHypercubes;
	}
	
	public void setNumberOfHypercubes(int numberOfHypercubes) {
		this.numberOfHypercubes = numberOfHypercubes;
	}

	public HyperCubeGrid<T> getGrid() {
		return grid;
	}

	public void setGrid(HyperCubeGrid<T> grid) {
		this.grid = grid;
	}

}
