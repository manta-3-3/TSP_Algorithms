package algorithms;

import java.util.ArrayList;
import java.util.Arrays;

import main.Main;
import processing.core.PApplet;

public class TSP_minimum_spanning_tree_heuristic extends PApplet {
	
	//Name dieser Klasse für Konsolen-Output
	public final String programTag = "[TSP_minimum_spanning_tree_heuristic] ";

	public final boolean twoOptActive = false; //TODO: out of order now

	// Der vollständig ungerichteter kantengewichteter Graph, repräsentiert durch Liste aus Kanten
	public Edge[] completeEdgeGraph;
	// Der minimaler Spannbaum, repräsentiert durch Liste aus Kanten
	public Edge[] MSTedgeGraph;
	// Der minimaler Spannbaum, repräsentiert durch Liste aus Knoten
	public Vertex[] MSTvertexGraph;
	
	// Beste bis jetzt gefundene Tour
	public int[] BEST_EVER_TOUR;
	// Tour-Kosten von der bis jetzt besten gefundenen Tour
	public float BEST_EVER_COST;

	public void settings() {
		size(Main.width_ALL,Main.height_ALL);
	}
	
	public void setup(){
		surface.setLocation(-10, 0);
		frameRate(100);
		
		Main.algoLogger1.info(programTag);
		
		BEST_EVER_TOUR = Main.generateRandomTour();
		BEST_EVER_COST = Main.calcTourCost(BEST_EVER_TOUR);
		Main.algoLogger1.info("Initial Tour-Cost: {"+millis()+","+BEST_EVER_COST+"}");
		Main.algoLogger1.info("-------------------------");
		
		thread("solveThread");
	} // End Methode

	public void draw(){
		background(0);
		drawField();
		drawCities(color(0,255,0),false);
		drawTour(BEST_EVER_TOUR, 1.5f, color(255));
		drawAllText();
	} // End Methode

	public void solveThread() {
		// Erzeuge den vollständig ungerichteter kantengewichteter Graph, repräsentiert durch Liste aus Kanten
		completeEdgeGraph = createCompleteEdgeGraph();
		
		//Step 1: erstelle MST von completeEdgeGraph
		MSTedgeGraph = KruskalMST(completeEdgeGraph);
		//printEdgeGraph(MSTedgeGraph); // print MST to console in edge format
		
		//Step 1.1: konvertiere MSTedgeGraph to an MSTvertexGraph
		MSTvertexGraph = convertEdgeToVertex(MSTedgeGraph);
		//printVertexGraph(MSTvertexGraph); // print MST to console in vertex format
		
		//Step 2: verdoppeln der Kanten im MST, kann ausgelassen werden
		
		//Step 3: Anwenden des Depth First Search (DFS) Algorithmus auf den doppelten MST
		
		//Mache einen DFS von jedem Knoten aus
		for(int i = 0; i < Main.countNodes_ALL; i++) {
			DFS dfsOnMSTvertexGraph = new DFS(MSTvertexGraph);
			dfsOnMSTvertexGraph.dfs(i);
			checkBEST_EVER_TOUR(dfsOnMSTvertexGraph.visited.stream().mapToInt(Integer::intValue).toArray());
			dfsOnMSTvertexGraph = null;
		}
		
		/*
		println(programTag+"Do 2-opt Improvement");
		BEST_EVER_TOUR = TSP_Only_2opt.completeTwoOptSwap(BEST_EVER_TOUR, BEST_EVER_COST);
		BEST_EVER_COST = Main.calcTourCost(BEST_EVER_TOUR);
		*/
		
		println(programTag+"Finished!");
		
		/* Alternative: mache DFS nur einmal mit zufälligen Startknoten auf MST
		// create new DFS search Object
		DFS dfsOnMSTvertexGraph = new DFS(MSTvertexGraph);
		// start DFS an einem zufälligen Start-Knoten
		dfsOnMSTvertexGraph.dfs(Main.randInt(0,dfsOnMSTvertexGraph.vertexGraph.length));
		//print(dfsOnMSTvertexGraph.visited); //print path after DFS to console
		
		BEST_EVER_TOUR = dfsOnMSTvertexGraph.visited.stream().mapToInt(Integer::intValue).toArray();
		BEST_EVER_COST = Main.calcTourCost(BEST_EVER_TOUR);
		*/
		
	} // End Methode
	
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
	
	public void drawAllText(){ 
		textSize(20);
		  
		fill(255);
		text("# Knoten/Cities: "+Main.countNodes_ALL,0,20);
		text("TwoOptActive: "+twoOptActive,0,40);
		text("Best Distance: "+BEST_EVER_COST+" km",0,80);
		  
		fill(255,0,0);
		text("FPS: "+frameRate,width-170,20);
		text("Run-Time: "+(float)millis() / 1000+" s",width-500,20);
	} //End Methode

	// check if input tour/cost is better than BEST EVER TOUR/COST if yes set it to input
	public void checkBEST_EVER_TOUR(int[] tour) {
		float tourCost = Main.calcTourCost(tour);
		if (tourCost < BEST_EVER_COST) {
			BEST_EVER_TOUR = tour.clone();
			BEST_EVER_COST = tourCost;
			Main.algoLogger1.info("{"+millis()+","+BEST_EVER_COST+"},");
			println(programTag+"New Best Tour found with Cost: "+BEST_EVER_COST);
		}
	}// End Methode
	
	// ---------------------------------------------------------------------//
	
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

	public class Vertex {

		// The Vertex Number of this Object
		private int vertexNumb;
		// contains all Neighbours vertexNumbs of this vertexNumb
		private ArrayList<Integer> connections = new ArrayList<Integer>();

		public Vertex(int vertexNumb) {
			this.vertexNumb = vertexNumb;
		}

		private void addConnectionTo(int vertexNumb) {
			this.connections.add(vertexNumb);
		} // End Methode

		public String toString() {
			this.connections.trimToSize();
			return vertexNumb + ": " + this.connections.toString();
		} // End Methode
  	  
  	} //End Class
	
	//----------------------------------------------------------------------//
	
	public Edge[] createCompleteEdgeGraph() {
		Edge[] res = new Edge[Main.binomialCoeff(Main.countNodes_ALL, 2)];
		int counter = 0;

		for (int i = 0; i < Main.countNodes_ALL; i++) {
			for (int j = i + 1; j < Main.countNodes_ALL; j++) {
				res[counter] = new Edge(i, j);
				counter++;
			}
		}
		return res;
	} // End Methode

	public Vertex[] convertEdgeToVertex(Edge[] inputEdgeGraph) {
		Vertex[] res = new Vertex[Main.countNodes_ALL];
		for (int i = 0; i < Main.countNodes_ALL; i++) {
			res[i] = new Vertex(i);
		}
		for (Edge ele : inputEdgeGraph) {
			res[ele.startVertexNumb].addConnectionTo(ele.endVertexNumb);
			res[ele.endVertexNumb].addConnectionTo(ele.startVertexNumb);
		}
		return res;
	} // End Methode
	
	public void drawEdgeGraph(Edge[] graph, float strokeWeight, int color) {
		strokeWeight(strokeWeight);
		stroke(color);
		for (Edge ele : graph) {
			line(Main.randomPointCords_ALL[ele.startVertexNumb].x, Main.randomPointCords_ALL[ele.startVertexNumb].y,
					Main.randomPointCords_ALL[ele.endVertexNumb].x, Main.randomPointCords_ALL[ele.endVertexNumb].y);
		}
	} // End Methode
	
	public void printEdgeGraph(Edge[] graph) {
		for (Edge ele : graph) {
			print(ele.toString());
		}
	} // End Methode

	public void printVertexGraph(Vertex[] graph) {
		print("\n");
		for (Vertex ele : graph) {
			print(ele.toString());
			print("\n");
		}
	} // End Methode

	//-----------------------------------2.TAB: DFS_class -------------------//
		
	public class DFS {

		Vertex[] vertexGraph;
		boolean[] marked;
		ArrayList<Integer> visited;

		public DFS(Vertex[] vertexGraph) {

			this.vertexGraph = vertexGraph;
			this.marked = new boolean[this.vertexGraph.length];
			this.visited = new ArrayList<Integer>(this.vertexGraph.length);

			for (int i = 0; i < this.marked.length; i++) {
				this.marked[i] = false;
			}
		}

		// apply DFS on vertexIndex (recursive!!!)
		public void dfs(int vertexIndex) {

			visited.add(vertexIndex);
			marked[vertexIndex] = true;

			for (int ele : vertexGraph[vertexIndex].connections) {
				if (!marked[ele]) {
					dfs(ele);
				}
			}

		}// End Methode

	} // End Class
		
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
