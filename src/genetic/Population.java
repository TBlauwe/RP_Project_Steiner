package genetic;

import java.util.ArrayList;
import java.util.Comparator;

import graph.WeightedGraph;
import util.HandyTools;

public class Population {

	// ===================
	// ===== MEMBERS =====
	// ===================

	private ArrayList<Individual> 	population;

	// ========================
	// ===== CONSTRUCTORS =====
	// ========================

	public Population(int size, boolean generate) {
		this.population = new ArrayList<Individual>();
		if(generate) { 
			WeightedGraph base = Bridge.getBase();
			switch(SimpleGeneticAlgorithm.populationGen) {
				case IMPROVED:
					for(int i=0; i<size; i++) {
						boolean[] genes;
						int choice = HandyTools.randInt(0, 3);
						if(choice==0) {
							WeightedGraph g = (base.generateMinimalSpanningTreeGraphHeuristic(true));
							genes = g.parseGraphToCoding(base);
							saveIndividual(new Individual(genes));
						}else if(choice==1) {
							WeightedGraph g = (base.generateShortestPathGraphHeuristic(true));
							genes = g.parseGraphToCoding(base);
							saveIndividual(new Individual(genes));
						}else {
							saveIndividual(new Individual(true));
						}
					}
					break;

				case MINIMAL:
					boolean[] genes;
					for(int i=0; i<size; i++) {
						if(i<SimpleGeneticAlgorithm.MAX_IND_FROM_SP_HEURISTIC) {
							WeightedGraph g = (base.generateShortestPathGraphHeuristic(true));
							genes = g.parseGraphToCoding(base);
							saveIndividual(new Individual(genes));
						}else if(i<SimpleGeneticAlgorithm.MAX_IND_FROM_MSP_HEURISTIC) {
							WeightedGraph g = (base.generateMinimalSpanningTreeGraphHeuristic(true));
							genes = g.parseGraphToCoding(base);
							saveIndividual(new Individual(genes));
						}else {
							saveIndividual(new Individual(true));
						}
					}
					break;

				default:
					for(int i=0; i<size; i++) { saveIndividual(new Individual(true)); }
					break;
			}
		}
	}

	// =====================
	// ===== FUNCTIONS =====
	// =====================
	
	public Population evolve() {
		Population childrens = new Population(getPopulationSize(), false); // Population engendrée par les parents
		Population newGen 	= new Population(getPopulationSize(), false); // Population retenue

		// I/ - Sélection et Crossover et Mutation
		for(int i=0; i<getPopulationSize()/2; i++) {
			Individual[] childs = Individual.crossover(getIndividual(rouletteSelect()),getIndividual(rouletteSelect()));
			childrens.saveIndividual(childs[0]);
			childrens.saveIndividual(childs[1]);
		}
		
		// II/ - Replacement de la nouvelle génération
		switch(SimpleGeneticAlgorithm.replacementStrat) {
			case ELITIST:
				Individual[] fittestFromParents 	= getFittest(getPopulationSize()/2);
				Individual[] fittestFromChildrens 	= childrens.getFittest(getPopulationSize()/2);
				for(int i=0; i<getPopulationSize()/2; i++) {
					newGen.saveIndividual(fittestFromParents[i]);
					newGen.saveIndividual(fittestFromChildrens[i]);
				}
				break;

			default:
				newGen = childrens;
				break;
		}

		// III/ - Mutation
		for(int i=0; i<getPopulationSize(); i++) {
			if (HandyTools.randProb(SimpleGeneticAlgorithm.MUTATION_RATE)) { getIndividual(i).mutate();}
		}
		return newGen;
	}
	
	private int rouletteSelect() {
		int	leastFittest = getLeastFittest().getFitness();	
		int index;
		while (true) {
			index = HandyTools.randInt(0, getPopulationSize());
			if (HandyTools.randProb((1-getIndividual(index).getFitness() / leastFittest))) break;
		}
		return index;
	}
	
	// =============================
	// ===== GETTERS & SETTERS =====
	// =============================

	public ArrayList<Individual> getPopulation() {
		return population;
	}

	public int getPopulationSize() {
		return population.size();
	}
	
	public Individual getIndividual(int i) {
		return population.get(i);
	}

	public void saveIndividual(Individual ind) {
		population.add(ind);
	}

	public Individual[] getFittest(int size) {
		java.util.Collections.sort(population, new Comparator<Individual>() {
			@Override
			public int compare(Individual o1, Individual o2) {
				return o1.getFitness() - o2.getFitness();
			}
		}); 
		Individual[] fittest = new Individual[size];
		for(int i=0; i<size; i++) {
			fittest[i] = population.get(i);
		}
		assert fittest[0] != null;
		return fittest;
	}
	
	public Individual getLeastFittest() {
		Individual fittest = getIndividual(0);
		for(Individual ind:population) {
			if(ind.getFitness() > fittest.getFitness()) {
				fittest = ind;
			}
		}
		return fittest;
	}

	public String toString() {
		String s = "";
		for(int i=0; i<getPopulationSize(); i++) {
			s += " i = " + i + " | Individu = " + getIndividual(i) + "\n";
		}
		return s;
	}
}
