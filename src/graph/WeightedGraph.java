package graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import genetic.SimpleGeneticAlgorithm;
import grph.algo.distance.DistanceMatrix;
import grph.algo.distance.PredecessorMatrix;
import grph.algo.distance.StackBasedBellmanFordWeightedMatrixAlgorithm;
import grph.algo.distance.WeightedPredecessorMatrixAlgorithm;
import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;
import util.HandyTools;

public class WeightedGraph extends InMemoryGrph {

	// ===================
	// ===== MEMBERS =====
	// ===================

	private static final long serialVersionUID = 6730725586287191272L;
	
	private NumericalProperty weights = new NumericalProperty("weigths",8,1);	// Propriété permettant de stocker les poids des arcs

	private static KruskalAlgorithm	kruskal		= new KruskalAlgorithm();				// Les algorithmes sont considérés comme des objets. Ils sont stockés pour être
	private CycleDetectionAlgorithm	cycle		= new CycleDetectionAlgorithm(this);	// ensuite appliqués à une instance

	private int				nbTerminals;		// Nombre de vertex terminaux (cache)
	private Set<Integer> 	terminalVertices;	// Contient les id des vertex qui sont terminaux

	// ========================
	// ===== CONSTRUCTORS =====
	// ========================
	
	public WeightedGraph() {
		setup();
	}
	
	public WeightedGraph(WeightedGraph base, boolean[] coding) {
		setup();
		createGraphFromGenes(base, coding);
	}

	public WeightedGraph(String filename) {
		setup();
		createGraphFromFile(filename);
	}
	
	public WeightedGraph(WeightedGraph g) {
		setup();
		for(int edge:g.getEdges().toIntArray()) {
			int vertexA = g.getOneVertex(edge);
			int vertexB = g.getTheOtherVertex(edge, vertexA);

			if(!containsVertex(vertexA)) { addVertex(vertexA); }
			if(!containsVertex(vertexB)) { addVertex(vertexB); }
			addEdge(edge, vertexA, vertexB, g.getEdgeWeight(edge));

			if(g.isVertexTerminal(vertexA)) { addTerminalVertex(vertexA); }
			if(g.isVertexTerminal(vertexB)) { addTerminalVertex(vertexB); }
		}
		for(int vertex:g.getInaccessibleVertices().toIntArray()) {
			if(!containsVertex(vertex)) { addVertex(vertex); }
			if(g.isVertexTerminal(vertex)) { addTerminalVertex(vertex); }
		}
	}

	private void setup() {
		terminalVertices = new HashSet<Integer>();
		setEdgesLabel(weights);
	}

	// =============================
	// ===== UTILITY FUNCTIONS =====
	// =============================

	/**
	 * @param filename - Doit être un fichier au formal .stp
	 */
	private void createGraphFromFile(String filename) {

		int edgeCounter = 0;	
		int nbVertices = 0;	

		try {
			File 			file 	 = new File(filename);
			BufferedReader 	br		 = new BufferedReader(new FileReader(file));
			String 			readLine = "";
			
			while((readLine = br.readLine()) != null) {

				if(readLine.startsWith("Nodes")) { 
					nbVertices = Integer.parseInt(readLine.split((" "))[1]);

				}else if(readLine.startsWith("E ")) {
					edgeCounter++;

					String[] 	parsedLine 	= readLine.split(" ");
					int			edge 		= edgeCounter + nbVertices;
					int 		vertexA 	= Integer.parseInt(parsedLine[1]);
					int 		vertexB 	= Integer.parseInt(parsedLine[2]);
					int 		weight 		= Integer.parseInt(parsedLine[3]);

					if(!containsVertex(vertexA)) { addVertex(vertexA); nbVertices++;}
					if(!containsVertex(vertexB)) { addVertex(vertexB); nbVertices++;}
					if(!containsEdge(edge)) {
						addEdge(edge, vertexA, vertexB, weight);
					}

				}else if(readLine.startsWith("Terminals")) {
					nbTerminals = Integer.parseInt(readLine.split((" "))[1]);

				}else if(readLine.startsWith("T ")) {
					int id = Integer.parseInt(readLine.split(" ")[1]);
					addTerminalVertex(id);
				}
			}
			br.close();
		}catch(IOException e) { e.printStackTrace(); }
	}
	
	/**
	 * @param base	- Graphe de base (contenant tous les noeuds)
	 * @param genes	- Genes qui détermine quels vertex non-terminaux appartiennent au graphe
	 */
	private void createGraphFromGenes(WeightedGraph base, boolean[] genes) {
		int i = 0;
	
		// Ajout des noeuds
		for(int vertex:base.getVertices().toIntArray()) {
			if(!base.isVertexTerminal(vertex)) {
				if(genes[i]) { 
					addVertex(vertex); 
				}
				i++;
			}else {
				addVertex(vertex); 
				addTerminalVertex(vertex);
			}
		}
		
		// Ajout des arcs
		for(int edge:base.getEdges()) {
			int vertexA = base.getOneVertex(edge);
			int vertexB = base.getTheOtherVertex(edge, vertexA);
			if(containsVertex(vertexA) && containsVertex(vertexB)) {
				addEdge(edge, vertexA, vertexB, base.getEdgeWeight(edge));
			}
		}
	}
	
	/**
	 * @param base	- Retourne le codage d'un graphe selon un graphe de base
	 * @return
	 */
	public boolean[] parseGraphToCoding(WeightedGraph base) {

		boolean[] coding = new boolean[base.getNumberOfVertices()-base.getNumberOfTerminals()];

		int i = 0;
		for(int id:base.getVertices().toIntArray()) {
			if(!base.isVertexTerminal(id)) {
				if(containsVertex(id)) 	{ coding[i] = true; }
				else 					{ coding[i] = false;}
				i++;
			}
		}
		return coding;
	}

	/**
	 * @return Somme de tous les arcs de l'arbre 
	 */
	public int sumWeight() {
		int sum = 0;
		for(int edge:getEdges()) {
			sum += getEdgeWeight(edge);
		}
		return sum;
	}
			
	/**
	 * @param id - Ajouter le vertex id comme étant ne vertex terminal.
	 */
	public void addTerminalVertex(int id) {
		terminalVertices.add(id);
		getVertexColorProperty().setValue(id, 12);
	}

	/**
	 * @param id 		- edge
	 * @param a			- vertex 
	 * @param b			- vertex
	 * @param weight	- poids de l'arc
	 */
	public void addEdge(int id, int a, int b, int weight) {
		addUndirectedSimpleEdge(id, a, b);
		setEdgeWeight(id, weight);
	}

	/**
	 * @return les poids des arcs du graphes ne sont pas modifiés. A la place, une nouvelle propriété est retournée (de loin plus efficage)
	 */
	public NumericalProperty getRandomizedWeights() {
		NumericalProperty _weights = new NumericalProperty("weigths",8,1);	

		double variation = HandyTools.randDouble(SimpleGeneticAlgorithm.WEIGHT_RANDOM_OFFSET_MIN, SimpleGeneticAlgorithm.WEIGHT_RANDOM_OFFSET_MAX);
		for (int edge : getEdges().toIntArray())
		{
			float weight = weights.getValue(edge);
			if(HandyTools.randInt(0, 2)==1) {
				weight += weight * variation;
			}else {
				weight -= weight * variation;
			}
			_weights.setValue(edge, (int) Math.floor(weight));
		}
		return _weights;
	}
	// =====================
	// ===== ALGORITHM =====
	// =====================

	public WeightedGraph kruskal() {
		return kruskal.compute(this, getWeightsProperty());
	}

	public static WeightedGraph kruskal(WeightedGraph g, NumericalProperty weights) {
		return kruskal.compute(g, weights);
	}

 	public boolean hasCycle() {
 		return cycle.compute();
 	}

 	/**
 	 * @param randomize - Est-ce que les poids doivent-être modifiés ? Utilisé pour générer des individus différents
 	 * @return Un arbre selon l'heuristique du plus court chemin
 	 */
 	public WeightedGraph generateShortestPathGraphHeuristic(boolean randomize) {

 		NumericalProperty weights = this.getWeightsProperty();
		if(randomize) { weights = getRandomizedWeights(); }

		int[] terminalVertices = this.terminalVertices.stream().mapToInt(Integer::intValue).toArray();
		DistanceMatrix 	distMatrix = new StackBasedBellmanFordWeightedMatrixAlgorithm(weights).compute(this);

		WeightedGraph g1 = new WeightedGraph();
		int edge = 0;
		for (int i = 0; i < terminalVertices.length; i++) {
		    for (int j = i + 1; j < terminalVertices.length; j++) {
				if(!g1.containsVertex(terminalVertices[i])) { g1.addVertex(terminalVertices[i]); }
				if(!g1.containsVertex(terminalVertices[j])) { g1.addVertex(terminalVertices[j]); }
				g1.addEdge(edge, terminalVertices[i], terminalVertices[j] , distMatrix.get(terminalVertices[i], terminalVertices[j]));
				edge++;
		    }
		}

		WeightedGraph g2 = g1.kruskal();

		PredecessorMatrix predMatrix = new WeightedPredecessorMatrixAlgorithm(weights).compute(this);
		WeightedGraph g3 = new WeightedGraph();
		for(int edgeId:g2.getEdges().toIntArray()) {
			int 	source 		= g2.getOneVertex(edgeId);
			int 	destination = g2.getTheOtherVertex(edgeId, source);
			if(!g3.containsVertex(source)) { g3.addVertex(source); }
			if(!g3.containsVertex(destination)) { g3.addVertex(destination); }

			int 	formerPred 	= destination;
			int 	pred 		= predMatrix.getPredecessor(source, destination);

			while(formerPred != source) {
				edgeId = getEdgesConnecting(pred, formerPred).toIntArray()[0];
				if(!g3.containsVertex(pred)) { g3.addVertex(pred); }
				if(!g3.containsEdge(edgeId)) { g3.addEdge(edgeId, pred, formerPred, getEdgeWeight(edgeId)); }
				formerPred = pred;
				pred = predMatrix.getPredecessor(source, pred);
			}
		}

		WeightedGraph g4 = g3.kruskal();
		for(int vertex:g4.getVertices().toIntArray()) {
			if(isVertexTerminal(vertex)) {
				g4.addTerminalVertex(vertex);
			}else {
				if(g4.getVertexDegree(vertex) <= 1) {
					g4.removeVertex(vertex);
				}
			}
		}
		return g4;	// G5 is built during the for loop
	}
 	
 	/**
 	 * @param randomize - Est-ce que les poids doivent-être modifiés ? Utilisé pour générer des individus différents
 	 * @return Un arbre selon l'heuristique de l'arbre couvrant de poids minimum
 	 */
	public WeightedGraph generateMinimalSpanningTreeGraphHeuristic(boolean randomize) {
 		NumericalProperty weights = this.getWeightsProperty();
		if(randomize) { weights = getRandomizedWeights(); }

		WeightedGraph g1 = WeightedGraph.kruskal(this, weights);

		boolean elimination = true;
		while(elimination) {
			elimination = false;
			for(int vertex:g1.getVertices().toIntArray()) {
				if(!isVertexTerminal(vertex) && (g1.getVertexDegree(vertex) <= 1)) {
						g1.removeVertex(vertex);
						elimination = true;
				}
			}
			if(elimination) { g1 = g1.kruskal(); }	
		}

		for(int vertex:terminalVertices) {
			g1.addTerminalVertex(vertex);
		}
		return g1;
	}
	
	// =============================
	// ===== GETTERS & SETTERS =====
	// =============================

	public NumericalProperty getWeightsProperty() {
		return this.weights;
	}

	public int getEdgeWeight(int id) {
		assert containsEdge(id);
		return weights.getValueAsInt(id);
	}
	
	public void setEdgeWeight(int id, int weight) {
		assert containsEdge(id);
		weights.setValue(id, weight);
	}

	public boolean isVertexTerminal(int id) {
		return (terminalVertices.contains(id)) ? true : false;
	}
	
	public int getNumberOfTerminals() {
		return nbTerminals;
	}
}
