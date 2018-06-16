package genetic;

import util.HandyTools;

public class Individual {
	// ===================
	// ===== MEMBERS =====
	// ===================

	private boolean[]		genes;
	private int				fitness;

	// ========================
	// ===== CONSTRUCTORS =====
	// ========================

	public Individual(boolean generate) {
		fitness = 0;
		genes 	= new boolean[Bridge.getGenesLength()];
		
		if(generate) {
			double p =  HandyTools.randDouble(SimpleGeneticAlgorithm.TRUE_PROBABILITY_MIN, SimpleGeneticAlgorithm.TRUE_PROBABILITY_MAX);
			for(int i=0; i<genes.length; i++) {
				if(HandyTools.randProb(p)) { genes[i] = true; }
			}
		}
	}

	public Individual(boolean[] genes) {
		this.fitness = 0;
		this.genes = genes;
	}

	// =====================
	// ===== FUNCTIONS =====
	// =====================
	
	/**
	 * Croissement à un point
	 * @return Retourne deux individus issues du croissement de A et de B
	 */
	public static Individual[] crossover(Individual a, Individual b) {

		Individual[] childs = new Individual[2];
		childs[0] = new Individual(false);
		childs[1] = new Individual(false);

		int rnd = HandyTools.randInt(1, a.getGenes().length-1);	// Exclusion des bornes pour avoir un crossover significatif
		
		for(int i=0; i<a.getGenes().length; i++) {
			if(i<rnd) {
				childs[0].setGene(i, a.getGene(i));
				childs[1].setGene(i, b.getGene(i));
			}else {
				childs[0].setGene(i, b.getGene(i));
				childs[1].setGene(i, a.getGene(i));
			}
		}

		return childs;
	}
	
	public void mutate() {
		int mutationIndex = HandyTools.randInt(0, genes.length);
		genes[mutationIndex] = !genes[mutationIndex];
		fitness = 0;	// Pour relancer le calcul de fitness
	}
	
	public String toString() {
		return "Genes : " + HandyTools.nicify(getGenes()) + " | Fitness = " + getFitness();
	}

	// =============================
	// ===== GETTERS & SETTERS =====
	// =============================


	public boolean[] getGenes() {
		return genes;
	}

	public void setGenes(boolean[] genes) {
		this.genes = genes;
	}

	public boolean getGene(int i) {
		return genes[i];
	}

	public void setGene(int i, boolean gene) {
		genes[i] = gene;
	}

	public int getFitness() {
		if(fitness==0) { Bridge.computeFitness(this); }
		return fitness;
	}

	public void setFitness(int fitness) {
		this.fitness = fitness;
	}
}
