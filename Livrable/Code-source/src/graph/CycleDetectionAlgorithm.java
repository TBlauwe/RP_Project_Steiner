package graph;

import java.util.HashSet;
import java.util.Set;

import grph.Grph;
import grph.GrphAlgorithm;

public class CycleDetectionAlgorithm extends GrphAlgorithm<Boolean> {

	private static final long serialVersionUID = 4841174321013663291L;
	
	private WeightedGraph g;
	
	public CycleDetectionAlgorithm(WeightedGraph g) {
		this.g = g;
	}

	@Override
	public Boolean compute(Grph g) {
		return compute((WeightedGraph) g);
	}
	
	public boolean compute() {
 		if(g.getNumberOfEdges() >= g.getNumberOfVertices()) {
 			return true;
 		}else {

 			Set<Integer> visited = new HashSet<Integer>(g.getNumberOfVertices());
 			for(int vertex:g.getVertices()) {
 				if(!visited.contains(vertex)) {
 					if(isCyclic(vertex, visited, -1)) { return true; }
 				}
 			}
 		}
 		return false;
		
	}
 	
 	private boolean isCyclic(int vertex, Set<Integer> visited, int parent) {
 		visited.add(vertex);
 		for(int v:g.getNeighbours(vertex)) {
 			if(!visited.contains(v)) {
 				if(isCyclic(v, visited, vertex)) { return true; }
 			}else if( v != parent ){
 				return true;
 			}
 		}
 		return false;
 	}

}
