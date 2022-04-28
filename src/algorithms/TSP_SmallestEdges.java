package algorithms;

import java.util.Arrays;

import main.Main;
import processing.core.PApplet;

public class TSP_SmallestEdges extends PApplet {
	
	//Name dieser Klasse für Konsolen-Output
	public final String programTag = "[TSP_SmallestEdges] ";
	
	// Der vollständig ungerichteter kantengewichteter Graph, repräsentiert durch Liste aus Kanten
	public Edge[] completeEdgeGraph;
	
	// Der Graph mit den n kleinsten Kanten
	public Edge[] smallestEdgeGraph;
	
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
		drawEdgeGraph(smallestEdgeGraph,1.5f,color(255));
	}
	
	public void solveThread() {
		
		//Step 0:
		completeEdgeGraph = createCompleteEdgeGraph();
		
		//Step 1:
		Arrays.sort(completeEdgeGraph);
		
		//Step 2:
		smallestEdgeGraph = (Edge[]) subset(completeEdgeGraph,0,Main.countNodes_ALL);
		
		//Step 3:
		lowerBound = calcTotalCostOfEdgeGraph(smallestEdgeGraph);
		println(programTag+"Finished with lowerBound of: " +lowerBound);
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
	
	public void drawAllText(){
		
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
	
} // End Algorithm
