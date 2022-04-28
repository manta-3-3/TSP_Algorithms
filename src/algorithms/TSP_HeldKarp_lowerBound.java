package algorithms;

import java.util.Arrays;

import main.Main;
import processing.core.PApplet;

//nicht komplett fertig geschafft -> funktioniert nicht!
public class TSP_HeldKarp_lowerBound extends PApplet {
	
	//Name dieser Klasse für Konsolen-Output
	public final String programTag = "[TSP_HeldKarp_lowerBound] ";
	
	// Der vollständig ungerichteter kantengewichteter Graph ohne einen zufälligen Knoten v, repräsentiert durch Liste aus Kanten
	public Edge[] completeOneMissEdgeGraph;
	
	// Der minimaler Spannbaum ohne den Knoten v, repräsentiert durch Liste aus Kanten
	public Edge[] MSToneMissEdgeGraph;
	
	float lowerBound;
	
	
	public void settings() {
		size(Main.width_ALL,Main.height_ALL);
	}
	
	public void setup(){
		surface.setLocation(-10, 0);
		frameRate(100);

		thread("solveThread");
	} 
	
	public void draw(){
		background(0);
		drawField();
		drawCities(color(0,255,0),false);
		drawEdgeGraph(MSToneMissEdgeGraph,1.5f,color(255));
		//drawEdgeGraph(completeOneMissEdgeGraph,1.5f,color(255));
	}
	
	public void solveThread() {
		
		int missNodeNumb = Main.randInt(0, Main.countNodes_ALL);
		completeOneMissEdgeGraph = createCompleteOneMissEdgeGraph(missNodeNumb);
		
		MSToneMissEdgeGraph = KruskalMST(completeOneMissEdgeGraph);
		//TODO geht noch nicht!!!!
	}
	
	// ---------------------- Drawing Methodes-------------------------------//
	
	public void drawField() {
		noFill();
		strokeWeight(1);
		stroke(255, 217, 0);
		rect(Main.fieldDimensions_ALL[0].x, Main.fieldDimensions_ALL[0].y, Main.fieldDimensions_ALL[1].x, Main.fieldDimensions_ALL[1].y);
	}// End Methode
	
	public void drawCities(int color,boolean showNodeNumb) {
		noFill();
		strokeWeight(1);
		stroke(color);
		textSize(15);
		for (int i = 0; i < Main.randomPointCords_ALL.length; i++) {
			circle(Main.randomPointCords_ALL[i].x, Main.randomPointCords_ALL[i].y, 10);
			// draws the node number next to the vertex if showNodeNumb is set to true
		    if(showNodeNumb) {
			    text(i,Main.randomPointCords_ALL[i].x+5,Main.randomPointCords_ALL[i].y-5);
		    }
		}
	}// End Methode
	
	public void drawTour(int[] tour, float strokeWeight, int color) {
		noFill();
		strokeWeight(strokeWeight);
		stroke(color);
		beginShape();
		for (int i = 0; i < tour.length; i++) {
			vertex(Main.randomPointCords_ALL[tour[i]].x, Main.randomPointCords_ALL[tour[i]].y);
		}
		endShape(CLOSE);
	}// End Methode
	
	public void drawAllText() {
		
	}// End Methode
	
	public void drawEdgeGraph(Edge[] graph, float strokeWeight, int color) {
		strokeWeight(strokeWeight);
		stroke(color);
		for (Edge ele : graph) {
			line(Main.randomPointCords_ALL[ele.startVertexNumb].x, Main.randomPointCords_ALL[ele.startVertexNumb].y,
					Main.randomPointCords_ALL[ele.endVertexNumb].x, Main.randomPointCords_ALL[ele.endVertexNumb].y);
		}
	} // End Methode
	
	//----------------------------------------------------------------------//
	
	public Edge[] createCompleteOneMissEdgeGraph(int missNodeNumb) {
		
		Edge[] res = new Edge[Main.binomialCoeff(Main.countNodes_ALL-1, 2)];
		int counter = 0;

		for (int i = 0; i < Main.countNodes_ALL; i++) {
			for (int j = i + 1; j < Main.countNodes_ALL; j++) {
				if(i == missNodeNumb || j == missNodeNumb) {
					//continue;
				}else {
					res[counter] = new Edge(i, j);
					counter++;
				}
			}
		}
		return res;
	} // End Methode
	
	public float calcTotalCostOfEdgeGraph(Edge[] graph) {
		float totalCost = 0;
		
		for(Edge edge : graph) {
			totalCost += edge.cost;
		}
		
		return totalCost;
	} // End Methode
	
	public class Edge implements Comparable<Edge> {

		private int startVertexNumb;
		private int endVertexNumb;
		private float cost;

		public Edge(int startVertexNumb, int endVertexNumb) {
			this.startVertexNumb = startVertexNumb;
			this.endVertexNumb = endVertexNumb;
			this.cost = Main.costFunc(startVertexNumb, endVertexNumb);
		}

		// Comparator function used for sorting edges based on their costs
		public int compareTo(Edge compareEdge) {
			int res = 0;
			float diff = this.cost - compareEdge.cost;

			if (diff < 0) {
				res = -1;
			} else if (diff > 0) {
				res = 1;
			} else {
				res = 0;
			}
			return res;
		} // End Methode

		public String toString() {
			return "[" + startVertexNumb + "," + endVertexNumb + "]";
		} // End Methode

	} //End Class

	//-----------------------------------4.TAB: KruskalMST -------------------//

	// Code von: https://www.geeksforgeeks.org/kruskals-minimum-spanning-tree-algorithm-greedy-algo-2/

	// A class to represent a subset for union-find
	public class subset {
		int parent, rank;
	}

	// A utility function to find set of an element i (uses path compression technique)
	public int find(subset subsets[], int i) {
		// find root and make root as parent of i
		// (path compression)
		if (subsets[i].parent != i)
			subsets[i].parent = find(subsets, subsets[i].parent);

		return subsets[i].parent;
	}

	// A function that does union of two sets of x and y (uses union by rank)
	public void Union(subset subsets[], int x, int y) {
		int xroot = find(subsets, x);
		int yroot = find(subsets, y);

		// Attach smaller rank tree under root
		// of high rank tree (Union by Rank)
		if (subsets[xroot].rank < subsets[yroot].rank)
			subsets[xroot].parent = yroot;
		else if (subsets[xroot].rank > subsets[yroot].rank)
			subsets[yroot].parent = xroot;

		// If ranks are same, then make one as
		// root and increment its rank by one
		else {
			subsets[yroot].parent = xroot;
			subsets[xroot].rank++;
		}
	}

	// The main function to construct MST using Kruskal's algorithm
	public Edge[] KruskalMST(Edge[] inputGraph) {
	      // clone the inputGraph and use graph to process  
			inputGraph = inputGraph.clone();
	      
	      // Tnis will store the resultant MST
	        Edge result[] = new Edge[Main.countNodes_ALL-1];
	       
	        // An index variable, used for result[]
	        int e = 0;
	       
	        // Step 1:  Sort all the edges in non-decreasing
	        // order of their weight.  If we are not allowed to
	        // change the given graph, we can create a copy of
	        // array of edges
	        Arrays.sort(inputGraph);
	 
	        // Allocate memory for creating V subsets
	        subset subsets[] = new subset[Main.countNodes_ALL];
	        for (int i = 0; i < Main.countNodes_ALL; ++i)
	            subsets[i] = new subset();
	 
	        // Create V subsets with single elements
	        for (int v = 0; v < Main.countNodes_ALL; ++v)
	        {
	            subsets[v].parent = v;
	            subsets[v].rank = 0;
	        }
	 
	        int i = 0; // Index used to pick next edge
	 
	        // Number of edges to be taken is equal to V-1
	        while (e < Main.countNodes_ALL - 1)
	        {
	            // Step 2: Pick the smallest edge. And increment
	            // the index for next iteration
	            Edge next_edge = inputGraph[i++];
	 
	            int x = find(subsets, next_edge.startVertexNumb);
	            int y = find(subsets, next_edge.endVertexNumb);
	 
	            // If including this edge does't cause cycle,
	            // include it in result and increment the index
	            // of result for next edge
	            if (x != y) {
	                result[e++] = next_edge;
	                Union(subsets, x, y);
	            }
	            // Else discard the next_edge
	        }                         
	        return result;
	}
	
} // End Algorithm
