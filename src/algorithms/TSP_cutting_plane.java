package algorithms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import main.Main;
import processing.core.PApplet;

public class TSP_cutting_plane extends PApplet{
	
	//Name dieser Klasse für Konsolen-Output
	public final static String programTag = "[TSP_cutting_plane] ";
	
	public static boolean readyForNextIter;
	
	public static boolean calcRunning;
	
	public static ArrayList<Float> scoreBoard = new ArrayList<Float>(6);
	
	public static boolean graphSwitch = true;
	
	//------------Variables for LP - system ---------------//
	
	public static int countEdgeVariables = Main.binomialCoeff(Main.countNodes_ALL,2);
	
	public static LinearObjectiveFunction objFunc;
	
	public static Collection<LinearConstraint> currentConstraints = new HashSet<LinearConstraint>();
	
	public static final MaxIter maxSimplexIters = new MaxIter(3000);
	
	public static double[] currentLpSolution;
	public static double currentObjFuncValue;
	
	public static int iterationStep;
	
	
	public void settings() {
		size(Main.width_ALL,Main.height_ALL);
	}
	
	public void setup() {
		surface.setLocation(-10, 0);
		frameRate(100);
		
		currentLpSolution = new double[countEdgeVariables];
		initInitialLP(true);
		println("---------------------------------------------------------------------");
		readyForNextIter = true;
		calcRunning = true;
	}
	
	public void draw() {
		
		if(readyForNextIter && calcRunning) {
			thread("calcThread");
			readyForNextIter = false;
			//calcRunning = false;
		}
		
		background(0);
		drawAllText();
		drawField();
		drawCities(color(255,0,0),true);
		
		if(graphSwitch) {
			drawLpSolution(currentLpSolution);
		}else {
			drawEdgeGraph(convertIntegerPartsOfLPSolutionToEdgeGraph(currentLpSolution));
		}
	}
	
	public void calcThread() {
		
		if(solveCurrentLP()) {
			println(programTag+"Problem was solved optimal!");
			calcRunning = false;
			return;
		}
		addCuttingPlanes();
		println("---------------------------------------------------------------------");
		readyForNextIter = true;
	}
	
	public void keyPressed() {
		
		if (key == 's' || key == 'S') { // to stop/start calculation
			
			if(calcRunning) {
				calcRunning = false;
			}else {
				calcRunning = true;
			}
		}
		
		if (key == 'g' || key == 'G') { // switch between diffrent graph views
			
			if(graphSwitch) {
				graphSwitch = false;
			}else {
				graphSwitch = true;
			}
		}
	}
	
	// ---------------------------------Drawing Methodes ------------------------------//

	public void drawField() {
		noFill();
		strokeWeight(1);
		stroke(255, 217, 0);
		rect(Main.fieldDimensions_ALL[0].x, Main.fieldDimensions_ALL[0].y, Main.fieldDimensions_ALL[1].x,Main.fieldDimensions_ALL[1].y);
	}// End Methode

	public void drawCities(int color, boolean showNodeNumb) {
		noFill();
		strokeWeight(1);
		stroke(color);
		textSize(15);
		for (int i = 0; i < Main.randomPointCords_ALL.length; i++) {
			circle(Main.randomPointCords_ALL[i].x, Main.randomPointCords_ALL[i].y, 10);
			// draws the node number next to the vertex if showNodeNumb is set to true
			if (showNodeNumb) {
				text(i, Main.randomPointCords_ALL[i].x + 5, Main.randomPointCords_ALL[i].y - 5);
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
		  textSize(20);
		  fill(255);
		  text("# Knoten/Cities: "+Main.countNodes_ALL,0,20);
		  text("Current LP Solution: "+currentObjFuncValue,0,40);
		  text("At Iteration: "+iterationStep,0,60);
		  text("# Edge-Variables: "+countEdgeVariables,0,100);
		  text("# Constraints: "+currentConstraints.size(),0,120);
		  
		  text("calcRunning: "+calcRunning,width-500,50);
		  text("readyForNextIter: "+readyForNextIter,width-500,80);
		  
		  drawScoreBoard();
		  
		  textSize(20);
		  fill(255,0,0);
		  text("FPS: "+frameRate,width-170,20);
		  text("Run-Time: "+(float)millis() / 1000+" s",width-500,20);	
} //End Methode
	
	
	public void drawLpSolution(double[] solution) {
		int otherValues = 0; // alle anderen Values neben 0, 1, 0.5 , 2
		
		int counter = 0;
		for(int i = 0; i < Main.countNodes_ALL-1; i++) {
			for(int k = i+1; k < Main.countNodes_ALL; k++) {
				
				if ((solution[counter] == Math.floor(solution[counter])) && !Double.isInfinite(solution[counter])) {
					switch((int)solution[counter]) {
					case 0: //do nothing
					break; 
					case 1: // draw with line
						stroke(255);
						strokeWeight(1.5f);
						line(Main.randomPointCords_ALL[i].x,Main.randomPointCords_ALL[i].y,Main.randomPointCords_ALL[k].x,Main.randomPointCords_ALL[k].y);
					break;
					case 2: // draw in violett
						stroke(79, 57, 219);
						strokeWeight(1.5f);
						line(Main.randomPointCords_ALL[i].x,Main.randomPointCords_ALL[i].y,Main.randomPointCords_ALL[k].x,Main.randomPointCords_ALL[k].y);
					break;
					
					default: System.out.println(programTag+"Edge more than 2-times used!");
					break;
					}
					
				}else if(solution[counter] == 0.5) {
					stroke(157, 255, 0); // draw in green
					strokeWeight(1.5f);
					line(Main.randomPointCords_ALL[i].x,Main.randomPointCords_ALL[i].y,Main.randomPointCords_ALL[k].x,Main.randomPointCords_ALL[k].y);
					 							
				}else {
					stroke(230, 66, 245);
					strokeWeight(1.5f);
					line(Main.randomPointCords_ALL[i].x,Main.randomPointCords_ALL[i].y,Main.randomPointCords_ALL[k].x,Main.randomPointCords_ALL[k].y);
					otherValues++;
				}
				
				counter++;
			}
		}
		//System.out.println(programTag+"# Values not {0,1,0.5,2}: "+otherValues);
	}
	
	public void drawEdgeGraph(Edge[] graph){
		strokeWeight(1.5f);
		stroke(255);
		for (Edge ele : graph) {
			line(Main.randomPointCords_ALL[ele.startVertexIndex].x, Main.randomPointCords_ALL[ele.startVertexIndex].y,Main.randomPointCords_ALL[ele.endVertexIndex].x, Main.randomPointCords_ALL[ele.endVertexIndex].y);
		}
	} // End Methode
	
	public void drawScoreBoard() {
		
		if(scoreBoard.size() > 6) {
			while(scoreBoard.size() > 6) {
				scoreBoard.remove(scoreBoard.size()-2);
			}
		}
		
		textSize(16);
		fill(255);
		for(int i = 0; i < scoreBoard.size()-1; i++) {
			text(scoreBoard.get(i), width-170, 50+(i*16) );
		}
		
		try{
			text(scoreBoard.get(scoreBoard.size()-1), width-170, 50 + (5*16)+ 10 );
		}
		catch(Exception e) {};
		
	} // End Methode
	
	//-------------------------------------------------------------------------------------------//
	
	public static void initInitialLP(boolean smallerThan1Constraints) {
		
		//Step 1: define objectiveFunction
		double[] objectiveFunctionArray = new double[countEdgeVariables];
		int counter = 0;
		for(int i = 0; i < Main.countNodes_ALL-1; i++) {
			for(int k = i+1; k < Main.countNodes_ALL; k++) {
				objectiveFunctionArray[counter] = dist(Main.randomPointCords_ALL[i].x,Main.randomPointCords_ALL[i].y,Main.randomPointCords_ALL[k].x,Main.randomPointCords_ALL[k].y);
				counter++;
			}
		}					
		objFunc = new LinearObjectiveFunction(objectiveFunctionArray, 0);
		
		objectiveFunctionArray = null;
		
		//Step 2: add initial constraints
		int countConstraintsAdded = 0;
		
		// add constraints of typ: every vertex shoud have exactely 2 edges
		for(int p1 = 0; p1 < Main.countNodes_ALL; p1++) {
			
			double[] doubleArray = new double[countEdgeVariables];
			
			for(int p2 = 0; p2 < Main.countNodes_ALL; p2++) {
				if(p1 != p2) {
					doubleArray[getIndex(p1,p2)] = 1;
				}
			} 
			currentConstraints.add(new LinearConstraint(doubleArray, Relationship.EQ, 2));
			countConstraintsAdded++;
		}
		
		println(programTag+" # constraintsOfexactely2Edges: "+countConstraintsAdded);
		countConstraintsAdded = 0;
		
		if(smallerThan1Constraints) {
			
			// add constraints of typ: every edge variable will be >=0 and <=1
			for(int i = 0; i <countEdgeVariables; i++) {
				double[] doubleArray = new double[countEdgeVariables];
				doubleArray[i] = 1;
						
				currentConstraints.add(new LinearConstraint(doubleArray, Relationship.LEQ, 1));
				countConstraintsAdded++;
			}
			
			println(programTag+" # constraintsOfsmallerThan1: "+countConstraintsAdded);
			
		}
		
		iterationStep = 0;
		println(programTag+"initial LP successful initialized!");
	
	} // End Methode
	
	public boolean solveCurrentLP() {
		
		int startTime = millis(); // for measuring the calculation time     
		
		SimplexSolver simplexSolver = new SimplexSolver();
        PointValuePair solution = simplexSolver.optimize(maxSimplexIters, objFunc, new LinearConstraintSet(currentConstraints), GoalType.MINIMIZE, new NonNegativeConstraint(true));
        simplexSolver = null; // delete solver
        
        println(programTag+"unrounded Variable-Sete: "+new HashSet<Double>(Arrays.asList(Arrays.stream(solution.getPoint()).boxed().toArray(Double[]::new))));
        for(int i = 0; i < solution.getPoint().length; i++) {
        	currentLpSolution[i] = round(solution.getPoint()[i],6);
        }
		println(programTag+"rounded Variable-Sete: "+new HashSet<Double>(Arrays.asList(Arrays.stream(currentLpSolution).boxed().toArray(Double[]::new))));
        
		solution = null;
        //currentObjFuncValue = solution.getSecond();
		currentObjFuncValue = new ArrayRealVector(currentLpSolution).dotProduct(objFunc.getCoefficients());
        scoreBoard.add(0, (float) (double) currentObjFuncValue);
        
        float elapsedTime = (millis() - startTime)/1000; //elapsedTime in seconds
        
        System.out.println(programTag+"New iteration Nr."+iterationStep+" solved after "+elapsedTime+" s : "+currentObjFuncValue); //make call to console
        iterationStep++;
        
        //check if founded solution is already a TSP tour, if yes Problem solved
        Vertex[] vertexGraph = convertEdgeToVertex(convertIntegerPartsOfLPSolutionToEdgeGraph(currentLpSolution));
        DFS checkOptimalSolved = new DFS(vertexGraph);
        checkOptimalSolved.dfs(0);
        if(checkOptimalSolved.visited.size() == Main.countNodes_ALL) {
        	return true;
        }
        vertexGraph = null;
        checkOptimalSolved = null;
        
        return false;
        
	} // End Methode
	
	public void addCuttingPlanes() { // Argument: how much new constraints(cuts) should be added??

		LinearConstraint cut = findNextSmallestSubTourCut();
		if(cut != null) {
			currentConstraints.add(cut);
			println(programTag+"New SubTour-Cut added!");
			cut = null;
			return;
		}
		cut = findNextHalfTriangleCut();
		if(cut != null) {
			currentConstraints.add(cut);
			println(programTag+"New HalfTriangle-Cut added!");
			return;
		}
		println(programTag+"No more cuts found!");
		calcRunning = false;
		//println(programTag+"Now searching for Random SubTour-Cut!");
		//currentConstraints.add(findNewRandomSubTourCut());
		//println(programTag+"New Random SubTour-Cut added!");	
		
		
	} // End Methode

	
	
	public LinearConstraint findNewRandomSubTourCut() {
		
		search: while(true) {
			// Step 1: pick random vertex Subset bigger than 1 and smaller than V-1
			int randCountVertexSubset = (int) random(2,Main.countNodes_ALL-1);
			int[] randVertexSubset = new int[randCountVertexSubset];
			
			for(int i = 0; i < randVertexSubset.length; i++) {
				randVertexSubset[i] = (int) random(0,Main.countNodes_ALL);
			}
			
			double[] constraintArray = new double[countEdgeVariables];
			
			for(int v = 0; v < Main.countNodes_ALL; v++) {
				
				if( !Arrays.stream(randVertexSubset).boxed().collect(Collectors.toList()).contains(v) ) {
					
					for(int ele : randVertexSubset) {
						constraintArray[getIndex(ele,v)] = 1;
					}
					
				}
				
			}
			
			// Step 2: check if this subTour constraint violates currentLpSolution, if not start with Step 1 again
			double checkValue = round(new ArrayRealVector(constraintArray).dotProduct(new ArrayRealVector(currentLpSolution)),5);
			if(checkValue < 2) {
				return new LinearConstraint(constraintArray, Relationship.GEQ, 2);
			}
			
		}
		
	} // End Methode
	
	public void findNewSubTourCuts() {
		
		Vertex[] vertexGraph = convertEdgeToVertex(convertIntegerPartsOfLPSolutionToEdgeGraph(currentLpSolution));
		ArrayList<int[]> subTours = findSubToursInVertexGraph(vertexGraph);
			
		for(int[] subTour: subTours) {
			
			double[] constraintArray = new double[countEdgeVariables];
			
			for(int v = 0; v < Main.countNodes_ALL; v++) {
				
				if( !Arrays.stream(subTour).boxed().collect(Collectors.toList()).contains(v) ) {
					
					for(int ele : subTour) {
						constraintArray[getIndex(ele,v)] = 1;
					}
					
				}
				
			}
			currentConstraints.add(new LinearConstraint(constraintArray, Relationship.GEQ, 2));
			println(programTag+"New Cut found!");
		}
		
	} // End Methode
	
	public LinearConstraint findNextSmallestSubTourCut() {
		
		Vertex[] vertexGraph = convertEdgeToVertex(convertIntegerPartsOfLPSolutionToEdgeGraph(currentLpSolution));
		ArrayList<int[]> subTours = findSubToursInVertexGraph(vertexGraph);
		
		Comparator<int[]> intArrayLengthComparator = new Comparator<int[]>()
	    {
	        @Override
	        public int compare(int[] o1, int[] o2)
	        {
	            return Integer.compare(o1.length, o2.length);
	        }
	    };
	    Collections.sort(subTours,intArrayLengthComparator);
	    
	    /*
	    if(subTours.isEmpty()) {
	    	println(programTag+"No Sub Tours found!");
	    	return null;
	    }else if(subTours.size() == 1 && subTours.get(0).length == Main.countNodes_ALL ) {
	    	println(programTag+"Problem was solved optimal!");
	    	calcRunning = false;
	    	return null;
	    }
	    */
	    
	    for(int[] subTour : subTours) {
	    	
	    	double[] constraintArray = new double[countEdgeVariables];
			for(int v = 0; v < Main.countNodes_ALL; v++) {
				
				if( !Arrays.stream(subTour).boxed().collect(Collectors.toList()).contains(v) ) {
					
					for(int ele : subTour) {
						constraintArray[getIndex(ele,v)] = 1;		
					}
				}			
			}
			
			// Step 2: check if this subTour constraint violates currentLpSolution, if yes return the LinearConstraint
			double edgeValues = new ArrayRealVector(constraintArray).dotProduct(new ArrayRealVector(currentLpSolution));
			if(round(edgeValues,5) < 2) {
				println(programTag+"New SubTour-Cut found: "+Arrays.toString(subTour)+" Value: "+edgeValues);
				return new LinearConstraint(constraintArray, Relationship.GEQ, 2);
			}
			
	    }
	    // every found subTour check, no violated found
	    println(programTag+"No SubTour-Cut found!");
    	return null;
		
	} // End Methode
	
	public LinearConstraint findNextHalfTriangleCut() {
		
		//Step 1: convert LP Solution to vertexGraph
		ArrayList<Edge> edgeGraph = new ArrayList<Edge>();
		int counter = 0;
		for(int i = 0; i < Main.countNodes_ALL-1; i++) {
			for(int k = i+1; k < Main.countNodes_ALL; k++) {
				if(currentLpSolution[counter] == 0.5) { // make a connection in the edgeGraph if currentLpSolution contains only value 0.5
					edgeGraph.add(new Edge(i,k));							
				}
				counter++;
			}
		}
		Vertex[] vertexGraph = convertEdgeToVertex(edgeGraph.toArray(new Edge[0]));
		edgeGraph = null;
		//Step 2: make DFS for finding threePointSub triangle
		int[] threePointSub = null;
		
		DFS search = new DFS(vertexGraph);
		vertexGraph = null;
		searchLoop: for(int i = 0; i < search.marked.length; i++) {
			if(search.marked[i] == false) {
				
				search.dfs(i);
				
				int minDegree = Integer.MAX_VALUE;
				for(int j = 0; j < search.visited.size(); j++) {
					if(search.vertexGraph[search.visited.get(j)].getDegree() < minDegree) {
						minDegree = search.vertexGraph[search.visited.get(j)].getDegree();
					}
				}
				
				
				if(search.visited.size() == 3 && minDegree >= 2) { //sub Tour only valid if exactely 3 nodes and contains cycle
					threePointSub = search.visited.stream().mapToInt(k -> k).toArray();
					println("lol");
					break searchLoop;
				}
				search.visited.clear();
			}
		}
		search = null;
		
		if(threePointSub == null) {
			println(programTag+"No HalfTriangle-Cut found!");
			return null;
			
		}else {
			println(programTag+"New HalfTriangle-Cut found: " + Arrays.toString(threePointSub));
		}
		
		//Step 3: find the subSets for this triangle
		ArrayList<int[]> triangleSubSets = new ArrayList<int[]>();
		
		triangleSubSets.add(threePointSub);
		
		vertexGraph = convertEdgeToVertex(convertIntegerPartsOfLPSolutionToEdgeGraph(currentLpSolution));
		for(int ele : threePointSub) {
			
			if(vertexGraph[ele].getDegree() == 1) {
				int[] temp = new int[2];
				temp[0] = ele;
				temp[1] = vertexGraph[ele].connections.get(0);
				triangleSubSets.add(temp);
			}else {
				println("Error found");
				return null;
			}
			
		}
		vertexGraph = null;
		println(Arrays.toString(triangleSubSets.get(0))+Arrays.toString(triangleSubSets.get(1))+Arrays.toString(triangleSubSets.get(2)));
		
		//Step 4: create cut and return
		ArrayRealVector resVek = new ArrayRealVector(countEdgeVariables);
		
		for(int[] subTour: triangleSubSets) {
			
			double[] constraintArray = new double[countEdgeVariables];
			
			for(int v = 0; v < Main.countNodes_ALL; v++) {
				
				if( !Arrays.stream(subTour).boxed().collect(Collectors.toList()).contains(v) ) {
					
					for(int ele : subTour) {
						constraintArray[getIndex(ele,v)] = 1;
					}
					
				}
				
			}
			resVek = resVek.add(new ArrayRealVector(constraintArray));
		}
		
		return new LinearConstraint(resVek.toArray(), Relationship.GEQ, 10);
	} // End Methode
	
	//------------------------- other utilitys Methodes--------------------------------------------------//
	
	public static class DFS{ // DFS-Algorithmen in recursive form
		  
		  Vertex[] vertexGraph;
		  boolean[] marked;
		  ArrayList<Integer> visited;
		  
		  public DFS(Vertex[] vertexGraph){
		    
		    this.vertexGraph = vertexGraph;
		    this.marked = new boolean[this.vertexGraph.length];
		    this.visited = new ArrayList<Integer>(this.vertexGraph.length);
		    
		    for(int i = 0; i < this.marked.length; i++){
		      this.marked[i] = false;
		    }    
		  }
		  
		  public void dfs(int vertexIndex){ // apply DFS on vertexIndex (recursive!!!)
		  
		    visited.add(vertexIndex);
		    marked[vertexIndex] = true;
		    
		    ArrayList<Integer> neighborsOfvertexIndex = vertexGraph[vertexIndex].connections;
		    
		    for(int ele : neighborsOfvertexIndex){
		      if(!marked[ele]){
		        dfs(ele);
		      }
		    }
		    
		  }//End Methode
		  
	} // End Class
	
	public static class Vertex{
		  
		  private int vertexIndex; // The VertexIndex of this Objekt
		  private ArrayList<Integer> connections = new ArrayList<Integer>(); // contains all Neighbours VertexIndices of this VertexIndex
		  
		  public Vertex(int vertexIndex){
		    this.vertexIndex = vertexIndex;
		  }
		  
		  private void addConnectionTo(int vertexIndex){
		    this.connections.add(vertexIndex);
		  } //End Methode
		  
		  private int getDegree() { 
			  connections.trimToSize();
			  return connections.size();
		  } //End Methode
		  
		  public String toString(){
		    
		    this.connections.trimToSize();
		    
		    return vertexIndex+": "+this.connections.toString();
		    
		  } //End Methode
		  
	} // End Class

	public static class Edge{ // modifed Edge class form processing
		  
		  private int startVertexIndex;
		  private int endVertexIndex;
		  
		  public Edge(int startVertexIndex,int endVertexIndex){
		    this.startVertexIndex = startVertexIndex;
		    this.endVertexIndex = endVertexIndex;
		  }
		  
		  public String toString(){
		    return "["+startVertexIndex+","+endVertexIndex+"]";       
		  } //End Methode
		  
		} // End Class
	
	public static Edge[] convertIntegerPartsOfLPSolutionToEdgeGraph(double[] solution) {
		ArrayList<Edge> res = new ArrayList<Edge>();
		
		int counter = 0;
		for(int i = 0; i < Main.countNodes_ALL-1; i++) {
			for(int k = i+1; k < Main.countNodes_ALL; k++) {
				if(solution[counter] == 1) { // make a connection in the edgeGraph if solution contains only value 1
					res.add(new Edge(i,k));							
				}
				counter++;
			}
		}
		
		return res.toArray(new Edge[0]);
	} // End Methode
	
	public static Vertex[] convertEdgeToVertex(Edge[] inputEdgeGraph){
		  Vertex[] res = new Vertex[Main.countNodes_ALL];
		  
		  for(int i = 0; i < res.length; i++){
		    res[i] = new Vertex(i);
		  }
		  
		  for(Edge ele : inputEdgeGraph){   
		    res[ele.startVertexIndex].addConnectionTo(ele.endVertexIndex);
		    res[ele.endVertexIndex].addConnectionTo(ele.startVertexIndex);   
		  }
		  
		  return res;
		} //End Methode
	
	public static ArrayList<int[]> findSubToursInVertexGraph(Vertex[] inputVertexGraph){
		
		ArrayList<int[]> subTours = new ArrayList<int[]>();
		
		DFS sub = new DFS(inputVertexGraph);
		
		//int removed = 0;
			
		for(int i = 0; i < sub.marked.length; i++) {
			if(sub.marked[i] == false) {
				
				sub.dfs(i);
				
				/*
				// check each found subTour if its really a subTour and contains a cycle(Kreis)
				// by find MinDeegree of this SubGraph and only contains Cycle if MinDegree >= 2 (n Anzahl Knoten)
				
				boolean containsCycle = true;
				int minDegree = Integer.MAX_VALUE;
				for(int j = 0; j < sub.visited.size(); j++) {
					if(inputVertexGraph[sub.visited.get(j)].getDegee() < minDegree) {
						minDegree = inputVertexGraph[sub.visited.get(j)].getDegee();
					}
				}
				if(minDegree < 2) {
					//containsCycle = false;
					//removed++;
				}
				*/
				
				if(sub.visited.size() > 1) { // only add the subTour if contains more than one vertex (and contains Cycle)
					subTours.add(sub.visited.stream().mapToInt(Integer::intValue).toArray());		
				}
				sub.visited.clear();
			}
		}
		sub = null;
		//println("Removed: "+removed);
		return subTours;
	} // End Methode
	
	
	public static int getIndex(int i, int k) { //returns the index of the edge variable from vertex i to k in the array
		
		int i1, i2;
		int n = Main.countNodes_ALL-1;
		
		if(i < k) {
			i1 = i;
			i2 = k;
		}else {
			i1 = k;
			i2 = i;
		}
		
		return (( n*(n+1) - (n-i1)*(n-i1+1) )/2)+(i2-i1)-1;
		
	} // End Methode
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(Double.toString(value));
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
} // End Algorithm
