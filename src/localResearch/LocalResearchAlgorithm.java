package localResearch;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import genetic.Bridge;
import graph.WeightedGraph;
import util.HandyTools;

public class LocalResearchAlgorithm {

	public enum solutionGeneration{RANDOM, MSP, SP, ALL}	

	private	WeightedGraph 	base;			// Original problem
	private WeightedGraph	currentSol;		// current Solution
	private String			instance;		// 

	private int 			solValue;		// Valeur de la solution 
	private int 			opt;			// Valeur de la solution optimale

	private int				movementCount;
	private int				researchCount;
	private boolean			generateNewSolution;	// Doit l'algorithme partir d'une autre solution
	private boolean 		debug;

	private long 			startTime;			
	private long 			executionDuration;	 // Temps pour accomplir l'algo		

	private TreeMap<Integer, WeightedGraph> solutionsFound;	 //Contient les différentes solutions
	private ArrayList<Integer>	optimalValueLog; 			// Sauvegarde des valeurs

	// ======================
	// ===== PARAMETERS =====
	// ======================
	public static final int 	TIMEOUT 					= 1; 		// Temps maximal alloué pour finir l'algo (en minutes) 
	public static final double 	WEIGHT_RANDOM_OFFSET_MIN 	= 0.05; 	// Variation minimale d'un poids de sa valeur initial 
	public static final double 	WEIGHT_RANDOM_OFFSET_MAX 	= 0.40; 	// Variation maximale d'un poids  de sa valeur initial 
	public static boolean		LIMIT_NEIGHBOURS			= true;		// Ignorer les sommets candidats selon certains critères (voir sujet, partie III)

	public static final solutionGeneration 	GENERATION 		= solutionGeneration.ALL; 	// Type de génération pour les solutions

	public LocalResearchAlgorithm(String filename, boolean ignore, boolean debug) {
		this.instance 				= filename;
		this.opt					= HandyTools.getInstanceOptimal(filename);
		this.base					= new WeightedGraph(filename);
		LocalResearchAlgorithm.LIMIT_NEIGHBOURS = ignore;
		Bridge.setBase(base);
		this.generateNewSolution 	= true;
		this.solutionsFound			= new TreeMap<Integer, WeightedGraph>();
		this.optimalValueLog 		= new ArrayList<Integer>();
		this.movementCount			= 0;
		this.researchCount			= 0;
		this.debug 					= debug;
	} 
	
	public void compute() {
		startTime = System.nanoTime();
		
		generateSolution();
		solutionsFound.put(this.solValue, this.currentSol);
		addOptimalValueLog(solValue);

		while( (System.nanoTime() - startTime)/1000000 < TIMEOUT*60000 && solValue != opt ) {

			if(generateNewSolution) { 
				this.movementCount = 0;
				this.researchCount += 1;
				generateSolution(); 
			}

			TreeMap<Integer, WeightedGraph> candidates = getCandidates();
			if(!candidates.isEmpty()) {
				int value = candidates.firstKey();
				if(candidates.firstKey() < solValue) {
					setCurrentSolution(candidates.get(value), value);
					this.movementCount +=1;
				}else {
					generateNewSolution = true;
				}
			}else {
				generateNewSolution = true;
			}

			this.solutionsFound.put(solValue, currentSol);
			addOptimalValueLog(solValue);

			executionDuration = (System.nanoTime() - startTime)/1000000;
			if (debug) overview(true);
		}
		if (debug) overview(false);
	}

	public void overview(boolean bMinimal) {

		System.out.println(" ===== Local Research Algorithm =====");
		System.out.println(" Instance       : " + instance);
		System.out.println(" Research count : " + researchCount);
		System.out.println(" Movement count : " + movementCount);
		System.out.println(" Solution value : " + solValue);
		System.out.println(" Best sol value : " + getBestSolution().getKey());
		System.out.println(" Optimal value  : " + opt);
		System.out.println(" Exec. Duration : " + executionDuration + " ms");
		if(!bMinimal) {
			base.display();
			getBestSolution().getValue().display();
		}
	}

	// =====================
	// ===== FUNCTIONS =====
	// =====================
	
	private TreeMap<Integer, WeightedGraph> getCandidates() {
		TreeMap<Integer, WeightedGraph> candidates = new TreeMap<Integer, WeightedGraph>();
		boolean saveCandidate = false;

		for(int vertex:base.getVertices()) {
			if(!base.isVertexTerminal(vertex)) {
				WeightedGraph candidate = new WeightedGraph(currentSol);
				if(currentSol.containsVertex(vertex)) {
					candidate.removeVertex(vertex);
					if(candidate.isConnected() || !LocalResearchAlgorithm.LIMIT_NEIGHBOURS) {
						saveCandidate = true;
					}
				}else {
					candidate.addVertex(vertex);
					if(candidate.getVertexDegree(vertex) > 1 || !LocalResearchAlgorithm.LIMIT_NEIGHBOURS) {
						saveCandidate = true;
					}
				}
				if(saveCandidate) {
					candidate = candidate.kruskal();
					candidates.put(Bridge.computeFitness(candidate), candidate);
				}
			}
		}
		return candidates;
	}
	
	private void generateSolution() {
		generateNewSolution = false;
		WeightedGraph g;

		boolean randomize = (this.movementCount == 0 ) ? false : true; // La première tentative n'est pas randomisée
		switch(GENERATION) {
			case ALL:
				int choice = HandyTools.randInt(0, 3);
				if(choice==0) {
					g = (base.generateMinimalSpanningTreeGraphHeuristic(randomize));
					setCurrentSolution(g, 0);
				}else if(choice==1) {
					g = (base.generateShortestPathGraphHeuristic(randomize));
					setCurrentSolution(g, 0);
				}else if(choice==2) {
					boolean[] genes = new boolean[Bridge.getGenesLength()];
					double p =  HandyTools.randDouble(0, 1);
					for(int i=0; i<Bridge.getGenesLength(); i++) {
						if(HandyTools.randProb(p)) { genes[i] = true; }
					}
					g = new WeightedGraph(base, genes);
					setCurrentSolution(g, 0);
				}
				break;
			
			case RANDOM:
				boolean[] genes = new boolean[Bridge.getGenesLength()];
				double p =  HandyTools.randDouble(0, 1);
				for(int i=0; i<Bridge.getGenesLength(); i++) {
					if(HandyTools.randProb(p)) { genes[i] = true; }
				}
				g = new WeightedGraph(base, genes);
				setCurrentSolution(g, 0);
				break;

			case MSP:
				g = (base.generateMinimalSpanningTreeGraphHeuristic(randomize));
				setCurrentSolution(g, 0);
				break;

			case SP:
				g = (base.generateShortestPathGraphHeuristic(randomize));
				setCurrentSolution(g, 0);
				break;
		}
	}

	// =============================
	// ===== GETTERS & SETTERS =====
	// =============================

	public TreeMap<Integer, WeightedGraph> getSolutionsFound(){
		return this.solutionsFound;
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
	
	public Map.Entry<Integer, WeightedGraph> getBestSolution(){
		return this.solutionsFound.firstEntry();
	}
	
	public void setCurrentSolution(WeightedGraph g, int value) {
		this.currentSol = g;
		this.solValue = (value==0) ? Bridge.computeFitness(g) : value;
	}

	public ArrayList<Integer> getOptimalValueLog() {
		return optimalValueLog;
	}

	public void addOptimalValueLog(int value) {
		this.optimalValueLog.add(value);
	}

	public WeightedGraph getCurrentSol() {
		return currentSol;
	}

	public void setCurrentSol(WeightedGraph currentSol) {
		this.currentSol = currentSol;
	}

	public int getSolValue() {
		return solValue;
	}

	public void setSolValue(int solValue) {
		this.solValue = solValue;
	}

	public int getMovementCount() {
		return movementCount;
	}

	public void setMovementCount(int movementCount) {
		this.movementCount = movementCount;
	}

	public int getResearchCount() {
		return researchCount;
	}

	public void setResearchCount(int researchCount) {
		this.researchCount = researchCount;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public void setOpt(int opt) {
		this.opt = opt;
	}

	public void setOptimalValueLog(ArrayList<Integer> optimalValueLog) {
		this.optimalValueLog = optimalValueLog;
	}

}
