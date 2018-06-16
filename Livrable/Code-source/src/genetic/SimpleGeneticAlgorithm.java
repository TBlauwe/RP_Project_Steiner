package genetic;

import java.util.ArrayList;

import graph.WeightedGraph;
import util.HandyTools;

public class SimpleGeneticAlgorithm {
	
	public enum ReplacementStrategy{ GENERATIONAL, ELITIST;}
	public enum PopulationGeneration{ RANDOM, IMPROVED, MINIMAL} 	// Random 	- Individu aléatoirement
																	// Improved - Individu selon les deux heuristiques + aléatoire (1/3 de chaque) ! Très couteux pour générer la pop° initiale
																	// Minimal 	- Individu selon les deux heuristiques + aléatoire (Pour les deux heuristiques, elles utilisent le taux spécifié, le reste => aléatoire)

	// ===================
	// ===== MEMBERS =====
	// ===================
	private	WeightedGraph 	base;				// Graphe du problème à résoudre
	private	Individual 		optimalSolution;	// Solution optimal
	private String			instance;	

	private int				solValue;					
	private int 			opt;				// Valeur de la solution optimale
	
	// ======================
	// ===== STATISTICS =====
	// ======================
	private int	generationCount;
	private boolean debug;

	private long startTime;			

	private long popGenDuration;	 // Temps pour générer la population
	private long executionDuration;	 // Temps pour accomplir l'algo		

	private ArrayList<Integer>	optimalValueLog; // Sauvegarde des valeurs

	// ======================
	// ===== PARAMETERS =====
	// ======================
	public static double 	MUTATION_RATE 				= 0.4; 		// Probabilité qu'un individu mute
	public static double 	TRUE_PROBABILITY_MIN 		= 0.20; 	// Valeur min de la probabilité qu'un chromosome soit à 1 (lors de la création d'un nouvel individu)
	public static double 	TRUE_PROBABILITY_MAX 		= 0.50; 	// Valeur max de la probabilité qu'un chromosome soit à 1 (lors de la création d'un nouvel individu)
	public static double 	WEIGHT_RANDOM_OFFSET_MIN 	= 0.05; 	// Variation minimale d'un poids de sa valeur initial 
	public static double 	WEIGHT_RANDOM_OFFSET_MAX 	= 0.40; 	// Variation maximale d'un poids  de sa valeur initial 
	public static int 	INITIAL_POP_SIZE 			= 50; 		// Taille de la population (Doit être paire (par simplicité))
	public static int 	MAX_GENERATIONS 			= 2000; 	// Nombre maximum de génération à produire
	public static int 	MAX_IND_FROM_SP_HEURISTIC 	= 3; 		// Nombre maximum d'individus à produire selon l'heuristique du plus court chemins
	public static int 	MAX_IND_FROM_MSP_HEURISTIC 	= 3; 		// Nombre maximum d'individus à produire selon l'heuristique de l'arbre couvrant de poids minimum

	public static int 	TIMEOUT 					= 5; 		// Temps maximal alloué pour finir l'algo (en minutes) 

	public static ReplacementStrategy replacementStrat 	= ReplacementStrategy.GENERATIONAL; 	// Selon quelle stratégie, l'algorithme doit-il former la nouvelle génération
	public static PopulationGeneration populationGen 	= PopulationGeneration.IMPROVED; 	// Selon quelle méthodes, l'alorithme doit-il générer la population initiale

	// ========================j
	// ===== CONSTRUCTORS =====
	// ========================
	public SimpleGeneticAlgorithm(String filename, ReplacementStrategy strat, PopulationGeneration popGen, boolean debug) {
		this.instance = filename;
		this.opt		= HandyTools.getInstanceOptimal(filename);
		this.optimalValueLog = new ArrayList<Integer>();
		SimpleGeneticAlgorithm.replacementStrat = strat;
		SimpleGeneticAlgorithm.populationGen = popGen;
		setBase(new WeightedGraph(filename));
		this.debug = debug;
	}
	
	public void compute() {
		startTime = System.nanoTime();
		
		Population pop = new Population(INITIAL_POP_SIZE, true); 	// Génération de la population initiale
		popGenDuration = (System.nanoTime() - startTime)/1000000;
		
		while( (generationCount < MAX_GENERATIONS && (System.nanoTime() - startTime)/1000000 < TIMEOUT*60000) && solValue != opt ) {
			pop = pop.evolve();										
			generationCount++;
			getOptimal(pop);										// Récupération de la meilleure solution actuelle
			setExecutionDuration((System.nanoTime() - startTime)/1000000);
			if (debug) overview(true);											// Affiche un résumé de l'éxécution
		}
		if (debug) overview(false);											// Affiche un résumé totale de l'éxécution
	}

	/**
	 * @param bMinimal - True => Ne pas afficher les graphes
	 */
	public void overview(boolean bMinimal) {
		System.out.println(" ===== Simple Genetic Algorithm =====");
		System.out.println(" Instance  : " + instance);
		System.out.println(" Gen count : " + generationCount);
		System.out.println(" Solution  : " + solValue);
		System.out.println(" Optimal   : " + opt);
		System.out.println(" Duration  : " + executionDuration + " ms ");
		System.out.println(" Duration  : " + executionDuration/1000 + " s ");

		if(!bMinimal) {
			base.display();
			new WeightedGraph(base, optimalSolution.getGenes()).display();
		}
	}
	
	private void getOptimal(Population pop) {
		optimalSolution = pop.getFittest(1)[0];
		solValue 	= optimalSolution.getFitness();
		optimalValueLog.add(solValue);
	}
	
	// =============================
	// ===== GETTERS & SETTERS =====
	// =============================
	public void setBase(WeightedGraph base) {
		this.base = base;
		Bridge.setBase(base);
	}
	
	public ArrayList<Integer> getOptimalValueLog(){
		return this.optimalValueLog;
	}

	public long getExecutionDuration() {
		return executionDuration;
	}

	public void setExecutionDuration(long executionDuration) {
		this.executionDuration = executionDuration;
	}

	public String getInstance() {
		return instance;
	}

	public int getOpt() {
		return opt;
	}

	public int getBestValue() {
		return solValue;
	}

	public long getPopGenDuration() {
		return popGenDuration;
	}
}
