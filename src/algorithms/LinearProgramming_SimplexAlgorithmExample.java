package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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

// Dieses Programm nur hier in Eclipse programmiert nicht in Processing
public class LinearProgramming_SimplexAlgorithmExample extends PApplet {

	
	public final static String programTag = "[LinearProgramming_SimplexAlgorithmExample] ";
	
	public double[] controlZoneLP_radii;
	
	public double[] Fractional2MatchingLP_pointSolution;
	
	public static Set<LinearConstraint> Fractional2FactorLP_currentConstraints = new HashSet<LinearConstraint>();
	public static double[] Fractional2FactorLP_pointSolution;
	public static Edge[] Fractional2FactorLP_edgeGraph;
	public static Vertex[] Fractional2FactorLP_vertexGraph;
	public static ArrayList<int[]> Fractional2FactorLP_subTours;
 	
	public void settings() {
		size(Main.width_ALL,Main.height_ALL);
	}
	
	public void setup() {
		surface.setLocation(-10, 0);
		frameRate(100);
		
		//controlZoneLP_radii = solveControlZoneLP();
		//Fractional2MatchingLP_pointSolution = solveFractional2MatchingLP();
		//Fractional2FactorLP_pointSolution = solveFractional2FactorLP();
		
		Fractional2FactorLP_pointSolution = new double[0];
		addInitialConstrains();	
	}
	
	public void draw() {	
		//drawControlZones(controlZoneLP_radii);
		//drawFractional2MatchingLP_pointSolution(Fractional2MatchingLP_pointSolution);
		//drawFractionalLP(Fractional2MatchingLP_pointSolution);
		
		/*
		solveNextIteration();
		
		background(0);
		drawAllText();
		drawField();
		
		drawFractionalLP(Fractional2FactorLP_pointSolution);
		//drawEdges(Fractional2FactorLP_edgeGraph);
		drawCities();
		println("");
		noLoop();
		*/
		background(0);
		drawAllText();
		drawField();
		drawFractionalLP(minimalPerfectMatchingLP());
		drawCities();
		noLoop();
	}	
	
//-------------------------------------------------------------------------------------------------------//
	
	public static double[] minimalPerfectMatchingLP() {
		
		//Step 1: define objectiveFunction
		double[] objectiveFunctionArray = new double[binomialCoeff(Main.countNodes_ALL,2)];
		int counter = 0;
		for(int i = 0; i < Main.countNodes_ALL-1; i++) {
			for(int k = i+1; k < Main.countNodes_ALL; k++) {
				objectiveFunctionArray[counter] = dist(Main.randomPointCords_ALL[i].x,Main.randomPointCords_ALL[i].y,Main.randomPointCords_ALL[k].x,Main.randomPointCords_ALL[k].y);
				counter++;
			}
		}					
		LinearObjectiveFunction oFunc = new LinearObjectiveFunction(objectiveFunctionArray, 0);
		
		//Step 2: define constraints
		Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
		
		for(int i = 0; i < Main.countNodes_ALL; i++) {
			double[] doubleArray = new double[objectiveFunctionArray.length];
			
			for(int k = 0; k < Main.countNodes_ALL; k++) {
				if(k != i) {
					doubleArray[getIndex(i,k)] = 1;
				}
			} 
			constraints.add(new LinearConstraint(doubleArray, Relationship.EQ, 1));			
		}
		
		// add extra constraints: every edge variable will be >=0 and <=1
		for(int i = 0; i < objectiveFunctionArray.length; i++) {
			double[] doubleArray = new double[objectiveFunctionArray.length];
			doubleArray[i] = 1;
			
			constraints.add(new LinearConstraint(doubleArray, Relationship.LEQ, 1));
		}
		
		//Step 3: start solving
		SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(new MaxIter(3000),oFunc, new LinearConstraintSet(constraints), GoalType.MINIMIZE, new NonNegativeConstraint(true));
        solver = null; // delete solver
        
        System.out.println(programTag +"Fractional2FactorLP: "+solution.getSecond());
        //System.out.println(programTag +"Fractional2FactorLP: "+Arrays.toString(solution.getPoint()));
		
        return solution.getPoint();
	
	} // End Methode
	
	public static double[] solveControlZoneLP(){
		
		//Step 1: define objectiveFunction
		double[] objectiveFunctionArray = new double[Main.countNodes_ALL];
		for(int i = 0; i < objectiveFunctionArray.length;i++) {
			objectiveFunctionArray[i] = 2;
		}					
		LinearObjectiveFunction oFunc = new LinearObjectiveFunction(objectiveFunctionArray, 0);

		//Step 2: define constraints
		Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
		for(int i = 0; i < Main.countNodes_ALL-1 ; i++) {
			for(int k = i+1; k < Main.countNodes_ALL; k++) {				
				double[] doubleArray = new double[Main.countNodes_ALL];
				doubleArray[i] = 1;
				doubleArray[k] = 1;
				float dist = dist(Main.randomPointCords_ALL[i].x,Main.randomPointCords_ALL[i].y,Main.randomPointCords_ALL[k].x,Main.randomPointCords_ALL[k].y);
				constraints.add(new LinearConstraint(doubleArray, Relationship.LEQ, dist));
			}
		}
		
		//Step 3: start solving
        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(new MaxIter(3000), oFunc, new LinearConstraintSet(constraints), GoalType.MAXIMIZE, new NonNegativeConstraint(true));
        solver = null; // delete solver
        
        System.out.println(programTag + "ControlZoneLP: "+solution.getSecond());
        //System.out.println(programTag + "ControlZoneLP: "+Arrays.toString(solution.getPoint()));
        
        return solution.getPoint();
    } // End Methode
	
	public static double[] solveFractional2MatchingLP(){
		
		//Step 1: define objectiveFunction
		double[] objectiveFunctionArray = new double[binomialCoeff(Main.countNodes_ALL,2)];
		int counter = 0;
		for(int i = 0; i < Main.countNodes_ALL-1; i++) {
			for(int k = i+1; k < Main.countNodes_ALL; k++) {
				objectiveFunctionArray[counter] = dist(Main.randomPointCords_ALL[i].x,Main.randomPointCords_ALL[i].y,Main.randomPointCords_ALL[k].x,Main.randomPointCords_ALL[k].y);
				counter++;
			}
		}					
		LinearObjectiveFunction oFunc = new LinearObjectiveFunction(objectiveFunctionArray, 0);
		
		//Step 2: define constraints
		Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
		
		for(int i = 0; i < Main.countNodes_ALL; i++) {
			double[] doubleArray = new double[objectiveFunctionArray.length];
			
			for(int k = 0; k < Main.countNodes_ALL; k++) {
				if(k != i) {
					doubleArray[getIndex(i,k)] = 1;
				}
			} 
			constraints.add(new LinearConstraint(doubleArray, Relationship.GEQ, 2));			
		}
		
		//Step 3: start solving
		SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(new MaxIter(3000),oFunc, new LinearConstraintSet(constraints), GoalType.MINIMIZE, new NonNegativeConstraint(true));
        solver = null; // delete solver
        
        System.out.println(programTag +"Fractional2MatchingLP: "+solution.getSecond());
        //System.out.println(programTag +"Fractional2MatchingLP: "+Arrays.toString(solution.getPoint()));
		
        return solution.getPoint();
	} // End Methode

	public static double[] solveFractional2FactorLP(){
		
		//Step 1: define objectiveFunction
		double[] objectiveFunctionArray = new double[binomialCoeff(Main.countNodes_ALL,2)];
		int counter = 0;
		for(int i = 0; i < Main.countNodes_ALL-1; i++) {
			for(int k = i+1; k < Main.countNodes_ALL; k++) {
				objectiveFunctionArray[counter] = dist(Main.randomPointCords_ALL[i].x,Main.randomPointCords_ALL[i].y,Main.randomPointCords_ALL[k].x,Main.randomPointCords_ALL[k].y);
				counter++;
			}
		}					
		LinearObjectiveFunction oFunc = new LinearObjectiveFunction(objectiveFunctionArray, 0);
		
		//Step 2: define constraints
		Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
		
		for(int i = 0; i < Main.countNodes_ALL; i++) {
			double[] doubleArray = new double[objectiveFunctionArray.length];
			
			for(int k = 0; k < Main.countNodes_ALL; k++) {
				if(k != i) {
					doubleArray[getIndex(i,k)] = 1;
				}
			} 
			constraints.add(new LinearConstraint(doubleArray, Relationship.EQ, 2));			
		}
		
		// add extra constraints: every edge variable will be >=0 and <=1
		for(int i = 0; i < objectiveFunctionArray.length; i++) {
			double[] doubleArray = new double[objectiveFunctionArray.length];
			doubleArray[i] = 1;
			
			constraints.add(new LinearConstraint(doubleArray, Relationship.LEQ, 1));
		}
		
		//Step 3: start solving
		SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(new MaxIter(3000),oFunc, new LinearConstraintSet(constraints), GoalType.MINIMIZE, new NonNegativeConstraint(true));
        solver = null; // delete solver
        
        System.out.println(programTag +"Fractional2FactorLP: "+solution.getSecond());
        //System.out.println(programTag +"Fractional2FactorLP: "+Arrays.toString(solution.getPoint()));
		
        return solution.getPoint();
	} // End Methode
	
	public static void addInitialConstrains() {
		int countAdd = 0;
		
		int numbOfVars = binomialCoeff(Main.countNodes_ALL,2);
		
		for(int p1 = 0; p1 < Main.countNodes_ALL; p1++) {
			
			double[] doubleArray = new double[numbOfVars];
			
			for(int p2 = 0; p2 < Main.countNodes_ALL; p2++) {
				if(p1 != p2) {
					doubleArray[getIndex(p1,p2)] = 1;
				}
			} 
			Fractional2FactorLP_currentConstraints.add(new LinearConstraint(doubleArray, Relationship.EQ, 2));
			countAdd++;
		}
		
		println(programTag+" # constraintsOfexactely2Edges: "+countAdd);
		countAdd = 0;
		
		// add extra constraints: every edge variable will be >=0 and <=1
		for(int i = 0; i < numbOfVars; i++) {
			double[] doubleArray = new double[numbOfVars];
			doubleArray[i] = 1;
					
			Fractional2FactorLP_currentConstraints.add(new LinearConstraint(doubleArray, Relationship.LEQ, 1));
			countAdd++;
		}
		
		println(programTag+" # constraintsOfsmallerThan1: "+countAdd);
		
	
	} // End Methode
	
	public static void solveNextIteration() {
		
		//Step 1: define objectiveFunction
		double[] objectiveFunctionArray = new double[binomialCoeff(Main.countNodes_ALL,2)];
		int counter1 = 0;
		for(int p1 = 0; p1 < Main.countNodes_ALL-1; p1++) {
			for(int p2 = p1+1; p2 < Main.countNodes_ALL; p2++) {
				objectiveFunctionArray[counter1] = dist(Main.randomPointCords_ALL[p1].x,Main.randomPointCords_ALL[p1].y,Main.randomPointCords_ALL[p2].x,Main.randomPointCords_ALL[p2].y);
				counter1++;
			}
		}					
		LinearObjectiveFunction oFunc = new LinearObjectiveFunction(objectiveFunctionArray, 0);
		
		if(Fractional2FactorLP_pointSolution.length != 0) {
			
			//Step 2: define new constraints by first finding subTours and then add coresponding constrains
			Fractional2FactorLP_edgeGraph = convertFractionalLPSolutionToEdgeGraph(Fractional2FactorLP_pointSolution);
			Fractional2FactorLP_vertexGraph = convertEdgeToVertex(Fractional2FactorLP_edgeGraph);
			Fractional2FactorLP_subTours = findSubToursInVertexGraph(Fractional2FactorLP_vertexGraph);
			
			println(programTag+Fractional2FactorLP_subTours.size()+" SubTours found");
			
			if(Fractional2FactorLP_subTours.size() == 1) {
				println("optimality or error");
				return;
			}
			//add for each found subTours constrains
			int countAdd = 0;
			for(int[] subTour : Fractional2FactorLP_subTours) {
				
				double[] doubleArray = new double[objectiveFunctionArray.length];
				
				for(int v = 0; v < Main.countNodes_ALL; v++) {
					
					if( !Arrays.stream(subTour).boxed().collect(Collectors.toList()).contains(v) ) {
						
						for(int ele : subTour) {
							doubleArray[getIndex(ele,v)] = 1;
						}
						
					}
					
				}
				
				Fractional2FactorLP_currentConstraints.add(new LinearConstraint(doubleArray, Relationship.GEQ, 2));
				countAdd++;
			}
			println(programTag+countAdd+" constraints added");
		}		
		
		//Step 3: start solving
		SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(new MaxIter(3000),oFunc, new LinearConstraintSet(Fractional2FactorLP_currentConstraints), GoalType.MINIMIZE, new NonNegativeConstraint(true));
        solver = null; // delete solver
        
        System.out.println(programTag +"Fractional2FactorLP: "+solution.getSecond());
        //System.out.println(programTag +"Fractional2MatchingLP: "+Arrays.toString(solution.getPoint()));
		
        Fractional2FactorLP_pointSolution = solution.getPoint();
		
	} // End Methode	
	
	public static Edge[] convertFractionalLPSolutionToEdgeGraph(double[] solution) {
		ArrayList<Edge> res = new ArrayList<Edge>();
		
		int counter = 0;
		for(int i = 0; i < Main.countNodes_ALL-1; i++) {
			for(int k = i+1; k < Main.countNodes_ALL; k++) {
				if(solution[counter] == 1 | solution[counter] == 2) { // make a connection in the edgeGraph if solution contains value bigger than 0
					res.add(new Edge(i,k));							// change if value is 1 or 2
				}
				counter++;
			}
		}
		
		return res.toArray(new Edge[0]);
	} // End Methode

	public static int getIndex(int i, int k) {
		
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
	
	// Code von: https://www.geeksforgeeks.org/binomial-coefficient-dp-9/
	public static int binomialCoeff(int n, int k) {
		int C[][] = new int[n + 1][k + 1];
        int i, j;
 
        // Calculate  value of Binomial
        // Coefficient in bottom up manner
        for (i = 0; i <= n; i++) {
            for (j = 0; j <= min(i, k); j++) {
                // Base Cases
                if (j == 0 || j == i)
                    C[i][j] = 1;
 
                // Calculate value using
                // previously stored values
                else
                    C[i][j] = C[i - 1][j - 1] + C[i - 1][j];
            }
        }
        return C[n][k];
			        
	} //End Methode
		
	//-------------DFS class + Vertex class + Edge class + other utilitys -------------------------//
	
	public static class DFS{
		  
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
			
		for(int i = 0; i < sub.marked.length; i++) {
			if(sub.marked[i] == false) {
				
				sub.dfs(i);
				if(sub.visited.size() > 1) {
					subTours.add(sub.visited.stream().mapToInt(Integer::intValue).toArray());		
				}
				sub.visited.clear();
			}
		}
		return subTours;
	} // End Methode
	
	public void drawEdges(Edge[] graph){
		  
		  for(Edge ele : graph){
		    strokeWeight(1.5f);
		    stroke(255);
		    
		    line(Main.randomPointCords_ALL[ele.startVertexIndex].x,Main.randomPointCords_ALL[ele.startVertexIndex].y,Main.randomPointCords_ALL[ele.endVertexIndex].x,Main.randomPointCords_ALL[ele.endVertexIndex].y);
		  }
	} // End Methode
	
	//-------------------------Drawing Methodes --------------------------------------------------//
	
	public void drawAllText(){
		  
		  textSize(20);
		  
		  fill(255);
		  text("Anzahl Punkte/Cities/Vertices: "+Main.countNodes_ALL,0,20);
		  text("Constraints: "+Fractional2FactorLP_currentConstraints.size(),0,40);
		  
		  fill(255,0,0);
		  text("FPS: "+frameRate,width-170,20);
		  text("Run-Time: "+(float)millis() / 1000+" s",width-500,20);
		  
		} //End Methode

	public void drawField(){
		  
		  noFill();
		  strokeWeight(1);
		  stroke(255, 217, 0);
		  rect(Main.fieldDimensions_ALL[0].x,Main.fieldDimensions_ALL[0].y,Main.fieldDimensions_ALL[1].x,Main.fieldDimensions_ALL[1].y);
		  
		} //End Methode

	public void drawCities(){
		  
		  for(int i = 0; i < Main.randomPointCords_ALL.length; i++){
		    
		    noFill();
		    strokeWeight(1);
		    stroke(255,0,0);
		    circle(Main.randomPointCords_ALL[i].x,Main.randomPointCords_ALL[i].y,10);
		    
		    //textSize(15);
		    //text(i,Main.randomPointCords_ALL[i].x+5,Main.randomPointCords_ALL[i].y-5);
		  }
		  
	} //End Methode
		
	public void drawControlZones(double[] radii) {
			
			for(int i = 0; i < radii.length; i++) {
				fill(127, 224, 88);
				noStroke();
				circle(Main.randomPointCords_ALL[i].x,Main.randomPointCords_ALL[i].y,2*(float)radii[i]);
			}
			
	} //End Methode
		
	public void drawFractional2MatchingLP_pointSolution(double[] solution) {

			int fractionalValues = 0;
			
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
					}else {
						fractionalValues++;
					}
					counter++;
				}
			}
				System.out.println(programTag+"# Fractional Values: "+fractionalValues);
			
	} //End Methode

	public void drawFractionalLP(double[] solution) {
			
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
			System.out.println(programTag+"# Values not {0,1,0.5,2}: "+otherValues);
			
	} // End Methode
		
} // End Algorithm
