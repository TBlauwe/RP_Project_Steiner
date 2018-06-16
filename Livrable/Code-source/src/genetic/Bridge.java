package genetic;

import graph.WeightedGraph;

/**
 * @author tristan
 * 	Classe utilitaire pour faire le pont entra l'algorithme génétique et les graphes
 */
public class Bridge {

	private static WeightedGraph base;

	public static final int PENALITY = 1000; 	// Pénalité utilisé dans la formule de fitness lorsque la solution n'est pas connexe

	public static void setBase(WeightedGraph graph) {
		base = graph;
	}

	public static WeightedGraph getBase() {
		return base;
	}
	
	public static int getGenesLength() {
		return base.getNumberOfVertices() - base.getNumberOfTerminals();
	}
	
	public static int computeFitness(Individual ind) {
		int fitness = 0;

		WeightedGraph graph	= new WeightedGraph(base, ind.getGenes());
		WeightedGraph mst 	= graph.kruskal();

		if(mst.isConnected()) {
			fitness = mst.sumWeight();
		}else {
			int forestWeight 	= mst.sumWeight();
			int M				= PENALITY;
			int nbVerticesMst	= mst.getNumberOfVertices();
			int nbEdgesMst		= mst.getNumberOfEdges();

			fitness = forestWeight + M * (nbVerticesMst - 1 - nbEdgesMst);
		}
		ind.setFitness(fitness);
		return fitness;
	}

	public static int computeFitness(WeightedGraph g) {
		int fitness = 0;

		WeightedGraph mst 	= g.kruskal();

		if(mst.isConnected()) {
			fitness = mst.sumWeight();
		}else {
			int forestWeight 	= mst.sumWeight();
			int M				= PENALITY;
			int nbVerticesMst	= mst.getNumberOfVertices();
			int nbEdgesMst		= mst.getNumberOfEdges();

			fitness = forestWeight + M * (nbVerticesMst - 1 - nbEdgesMst);
		}
		return fitness;
	}
}
