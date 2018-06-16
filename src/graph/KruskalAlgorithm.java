package graph;

import java.util.ArrayList;

import grph.Grph;
import grph.GrphAlgorithm;
import grph.algo.msp.EdgeProperty;
import grph.properties.NumericalProperty;

public class KruskalAlgorithm extends GrphAlgorithm<WeightedGraph> {

	private static final long serialVersionUID = 6362588679656418702L;

	@Override
	public WeightedGraph compute(Grph g) {
		return compute((WeightedGraph) g, null);
	}

	public WeightedGraph compute(WeightedGraph g, NumericalProperty weights) {
		// Récupération des arcs et de leur poids pour ensuite les trier
		ArrayList<EdgeProperty> sortedEdges = new ArrayList<EdgeProperty>(g.getNumberOfEdges());
		for(int edge:g.getEdges().toIntArray()){
			sortedEdges.add(new EdgeProperty(edge, weights.getValueAsInt(edge)));
		}
		java.util.Collections.sort(sortedEdges);

		// Creation du graphe couvrant de poids minimum
		WeightedGraph mst = new WeightedGraph();

		// Ajout des noeuds isolés
		for(int id:g.getInaccessibleVertices().toIntArray()) { mst.addVertex(id); }

		for(EdgeProperty edge : sortedEdges) {
			int id = edge.getId();
			int v1 = g.getOneVertex(id);
			int v2 = g.getTheOtherVertex(id, v1);

			if(!mst.containsVertex(v1)) { mst.addVertex(v1); }	
			if(!mst.containsVertex(v2)) { mst.addVertex(v2); }
			mst.addEdge(id, v1, v2, edge.getWeight());

			if (mst.hasCycle()) { mst.removeEdge(id); }
			if (mst.getNumberOfEdges() >= g.getNumberOfVertices() - 1) { break; }
		}
		return mst;
	}
}
