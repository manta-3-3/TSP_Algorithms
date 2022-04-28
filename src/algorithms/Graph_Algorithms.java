package algorithms;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.stream.IntStream;

import main.Main;
import processing.core.PApplet;

public class Graph_Algorithms extends PApplet {
	
	public final static String programTag = "[Graph_Algorithms] ";
	
	public static int[] squareDimension = new int[2];
	public static int countSquares;
	
	public static final HashMap<String, Integer> squareStateMap = new HashMap<String, Integer>(){
		private static final long serialVersionUID = 1L;
	{
	    put("SEARCH", 3);
	    put("VISITED", 2);
	    put("UNVISITED", 1);
	    put("OBSTAC", 0);
	    put("SRC", -1);
	    put("DEST", -2);
	    put("PATH",-3);
	    
	}};
	public static final HashMap<Integer, Color> squareColorMap = new HashMap<Integer, Color>(){
		private static final long serialVersionUID = 1L;
	{
		put(squareStateMap.get("SEARCH"),    new Color(255, 136, 0));
	    put(squareStateMap.get("VISITED"),   new Color(247, 244, 141));
	    put(squareStateMap.get("UNVISITED"), new Color(255, 255, 255));
	    put(squareStateMap.get("OBSTAC"),    new Color(69, 69, 69));
	    put(squareStateMap.get("SRC"),       new Color(0, 110, 255));
	    put(squareStateMap.get("DEST"),      new Color(255, 0, 0));
	    put(squareStateMap.get("PATH"),     new Color(0, 255, 0));
		
	}};
	
	public static int srcIndex;
	public static int destIndex;
	public static ArrayList<Square> squareGrid;
	public static ArrayList<Integer> path = new ArrayList<Integer>();
	
	public void settings() {
		size(Main.width_ALL,Main.height_ALL);
	}
	
	public void setup() {
		surface.setLocation(-10, 0);
		frameRate(100);
		
		squareDimension[0] = 107;
		squareDimension[1] = Math.round(Main.fieldDimensions_ALL[1].y/Main.fieldDimensions_ALL[1].x*squareDimension[0]);
		countSquares = squareDimension[0]*squareDimension[1];
		
		squareGrid = initKruskalMaze();
		//thread("calcThread_BFS");
		thread("calcThread_DFS");
	}
	
	public void draw() {
		background(0);
		drawSquareGrid(squareGrid);
		drawPath(path);
		drawField();
		drawAllText();
	}
	
	public void calcThread_BFS() {
		delay(500);
		BFS bfs = new BFS(squareGrid);
		bfs.bfs(srcIndex,destIndex);
		bfs.buildPath();
	}
	
	public void calcThread_DFS() {
		delay(500);
		DFS dfs = new DFS(squareGrid);
		dfs.dfs(srcIndex,destIndex);
		dfs.buildPath();
	}
	
	//---------------------------- utility methodes ---------------------------//
	
	public ArrayList<Square> initDefaultSquareGrid() {
		srcIndex = Math.round(Main.randFloat(0, countSquares-1));
		destIndex = Math.round(Main.randFloat(0, countSquares-1));
		
		ArrayList<Square> res = new ArrayList<Square>();
		//generate squares
		for(int i = 0; i < countSquares; i++) {
			
			if(Math.random() < 0.3d) { // 30% Chance Square is getting an obstacle
				res.add(new Square(i,squareStateMap.get("OBSTAC")));
			}else {
				res.add(new Square(i,squareStateMap.get("UNVISITED")));
			}
		}
		//set src and dest state
		res.get(srcIndex).state = squareStateMap.get("SRC");
		res.get(destIndex).state = squareStateMap.get("DEST");
		
		// Add connections to needed squares
		int[] dx = {0,1,0,-1};
		int[] dy = {-1,0,1,0};
		
		outerLoop: for(int i = 0; i < res.size(); i++) {
			if(res.get(i).state == squareStateMap.get("OBSTAC")) {
				continue outerLoop;
			}
				
			innerLoop: for(int n = 0; n < 4; n++) {
				
				int[] pos = getPositionInGrid(i);
				pos[0] += dx[n];
				pos[1] += dy[n];
				int index = getIndexInGrid(pos);
				
				if(index < 0 ) {
					continue innerLoop;
				}
				if(res.get(index).state == squareStateMap.get("OBSTAC")) {
					continue innerLoop;
				}
				res.get(i).addConnection(index);
			}
		}
		println(programTag+"Default Square Grid initialized: "+countSquares+" -> "+Arrays.toString(squareDimension));
		return res;		
	} // End Methode
	
	public ArrayList<Square> initKruskalMaze(){
		srcIndex = 0;
		destIndex = countSquares-1;
		
		ArrayList<Square> res = new ArrayList<Square>();
		
		//generate squares
		int columSwitcher = 0;
		int rowSwitcher = 0;
		for(int i = 0; i < countSquares; i++) {
			
			if(Math.floorMod(i, squareDimension[0]) == 0) {
				rowSwitcher = 1 - rowSwitcher;
			}
			columSwitcher = 1 - columSwitcher;
			
			if(rowSwitcher == 1) {
				if(columSwitcher == 1) {
					res.add(new Square(i,squareStateMap.get("UNVISITED")));
				}else {
					res.add(new Square(i,squareStateMap.get("OBSTAC")));
				}
			}else {
				res.add(new Square(i,squareStateMap.get("OBSTAC")));
			}
			
		}
		
		//set src and dest state
		res.get(srcIndex).state = squareStateMap.get("SRC");
		res.get(destIndex).state = squareStateMap.get("DEST");
		
		// make an Edge graph form all square with value "UNVISITED"
		ArrayList<int[]> gridIndexEdgeGraph = new ArrayList<int[]>();
		
		int[] dx = {0,2,0,-2};
		int[] dy = {-2,0,2,0};
		
		outerLoop: for(int i = 0; i < res.size(); i++) {
			if(res.get(i).state == squareStateMap.get("OBSTAC")) {
				continue outerLoop;
			}
				
			innerLoop: for(int n = 0; n < 4; n++) {
				
				int[] pos = getPositionInGrid(i);
				pos[0] += dx[n];
				pos[1] += dy[n];
				int index = getIndexInGrid(pos);
				
				if(index < 0 ) {
					continue innerLoop;
				}
				if(res.get(index).state == squareStateMap.get("OBSTAC")) {
					continue innerLoop;
				}
				if(index < i) {
					continue innerLoop;
				}
				gridIndexEdgeGraph.add(new int[] {i,index});
			}
		}
		
		// translate/encode the gridIndexEdgeGraph to a normIndexEdgeGraph were vertices are marked from 0 to V
		// save the mapping in both directions for later decoding
		ArrayList<int[]> normIndexEdgeGraph = new ArrayList<int[]>();
		HashMap<Integer,Integer> gridToNormMap = new HashMap<Integer,Integer>();
		HashMap<Integer,Integer> normToGridMap = new HashMap<Integer,Integer>();
		int keyMapCount = 0;
		
		for(int[] edge : gridIndexEdgeGraph) {
			
			int[] newEdge = new int[2];
			for(int i = 0; i < edge.length; i++) {
				
				if(gridToNormMap.get(edge[i]) != null) {
					newEdge[i] = gridToNormMap.get(edge[i]);
				}else {
					gridToNormMap.put(edge[i], keyMapCount);
					normToGridMap.put(keyMapCount, edge[i]); //add the reversed connection
					
					keyMapCount++;
					newEdge[i] = gridToNormMap.get(edge[i]);
				}
				
			}
			normIndexEdgeGraph.add(newEdge);
		}
		//normIndexEdgeGraph.stream().forEach(e -> print(Arrays.toString(e))); print("\n");
		
		// applay KruskalMaze on normIndexEdgeGraph
		ArrayList<int[]> treeNormIndexEdgeGraph = KruskalMaze(normIndexEdgeGraph);
		//treeNormIndexEdgeGraph.stream().forEach(e -> System.out.print(Arrays.toString(e))); print("\n");
		
		//decode result back to gridIndex
		ArrayList<int[]> treeGridIndexEdgeGraph = new ArrayList<int[]>();
		
		for(int[] edge : treeNormIndexEdgeGraph) {
			
			int[] newEdge = new int[2];
			for(int i = 0; i < edge.length; i++) {
				
				if(normToGridMap.get(edge[i]) != null) {
					newEdge[i] = normToGridMap.get(edge[i]);
				}else {
					println(programTag+"Error in Mapping!");
				}
			}
			treeGridIndexEdgeGraph.add(newEdge);
		}
		//treeGridIndexEdgeGraph.stream().forEach(e -> System.out.print(Arrays.toString(e))); print("\n");
		
		//for the square between the tow edges change state: OBSTAC -> UNVISITED and make connections
		for(int[] edge : treeGridIndexEdgeGraph) {
			
			int[] cordIndexA = getPositionInGrid(edge[0]);
			int[] cordIndexB = getPositionInGrid(edge[1]);
			int[] resCord = new int[2];
			
			resCord[0] = (cordIndexA[0]+cordIndexB[0])/2;
			resCord[1] = (cordIndexA[1]+cordIndexB[1])/2;
			
			res.get(getIndexInGrid(resCord)).state = squareStateMap.get("UNVISITED");
			//add connections
			res.get(getIndexInGrid(resCord)).addConnection(edge[0]);
			res.get(getIndexInGrid(resCord)).addConnection(edge[1]);
			
			res.get(edge[0]).addConnection(getIndexInGrid(resCord));
			res.get(edge[1]).addConnection(getIndexInGrid(resCord));
			
		}
		
		println(programTag+"Maze by Kruskal initialized: "+countSquares+" -> "+Arrays.toString(squareDimension));
		return res;
	} // End Method
	
	public void drawSquareGrid(ArrayList<Square> grid) {
		float rectWidth = Main.fieldDimensions_ALL[1].x/squareDimension[0];
		float rectHeight = Main.fieldDimensions_ALL[1].y/squareDimension[1];
		
		//stroke(194, 238, 255); //noStroke();
		for(int i = 0; i < squareDimension[1]; i++) {
			for(int j = 0; j < squareDimension[0]; j++) {
				int[] pos = {j,i};
				Color c = squareColorMap.get(grid.get(getIndexInGrid(pos)).state);
				stroke(c.getRed(),c.getGreen(),c.getBlue());
				fill(c.getRed(),c.getGreen(),c.getBlue());
				rect(Main.fieldDimensions_ALL[0].x+j*rectWidth,Main.fieldDimensions_ALL[0].y+i*rectHeight,rectWidth,rectHeight);
				
				//fill(255,0,0);
				//text(getIndexInGrid(pos),Main.fieldDimensions_ALL[0].x+j*rectWidth+20,Main.fieldDimensions_ALL[0].y+i*rectHeight+20);
			}
		}
		
	} // End Methode
	
	public void drawPath(ArrayList<Integer> path) {
		float rectWidth = Main.fieldDimensions_ALL[1].x/squareDimension[0];
		float rectHeight = Main.fieldDimensions_ALL[1].y/squareDimension[1];
		
		strokeWeight(1.5f);
		stroke(255,0,0);
		noFill();
		    
		  beginShape();
		  for(int i = 0; i < path.size(); i++){
			int[] gridCord = getPositionInGrid(path.get(i));
			float xCord = Main.fieldDimensions_ALL[0].x + gridCord[0]*rectWidth+(rectWidth/2);
			float yCord = Main.fieldDimensions_ALL[0].y + gridCord[1]*rectHeight+(rectHeight/2);
		    vertex(xCord,yCord);      
		  }
		  endShape(); 
	} // End Methode
	
	public int[] getPositionInGrid(int index){
		int[] res = new int[2];
		res[0] = Math.floorMod(index,squareDimension[0]);
		res[1] = Math.floorDiv(index,squareDimension[0]);
		return res;
	}
	
	public int getIndexInGrid(int[] cord) {
		if(cord[0] < 0 | cord[1] < 0 | cord[0] >= squareDimension[0]| cord[1] >= squareDimension[1]) {
			return -1;
		}
		return cord[1]*(squareDimension[0])+cord[0];
	}
	
	public void drawAllText(){
		  
		  textSize(20);
		  fill(255);
		  text("# Squares: "+countSquares,0,20);
		  text("",0,40);
		  text("",0,60);
		  
		  textSize(20);
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
	
	//---------------------------- utility classes ----------------------------//
	
	public class Square{
		public final int index;
		public int state;
		public HashSet<Integer> connections = new HashSet<Integer>(0); 
		
		public Square(int index, int state) {
			this.index = index;
			this.state = state;
		}
		
		public void addConnection(int index) {
			if(index < 0) {
				println(programTag+"Error detected caus of negativ index");
				return;
			}
			connections.add(index);
		} // End Methode
		
		public void removeConnection(int index) {
			if(index < 0) {
				println(programTag+"Error detected caus of negativ index");
				return;
			}
			connections.remove(index);			
		} // End Methode
		
	} // End Class Square

	public class DFS_rec{ // DFS-Algorithmen in recursive form
		  
		  ArrayList<Square> vertexGraph;
		  boolean[] marked;
		  ArrayList<Integer> visited;
		  
		  public DFS_rec(ArrayList<Square> vertexGraph){
		    
		    this.vertexGraph = vertexGraph;
		    this.marked = new boolean[this.vertexGraph.size()];
		    this.visited = new ArrayList<Integer>(this.vertexGraph.size());
		    
		    for(int i = 0; i < this.marked.length; i++){
		      this.marked[i] = false;
		    }    
		  }
		  
		  public void dfs(int startIndex){ // apply DFS on vertexIndex (recursive!!!)

		    vertexGraph.get(startIndex).state = 3;
		    delay(5);
			vertexGraph.get(startIndex).state = 2;
			
		    visited.add(startIndex);
		    marked[startIndex] = true;
		    
		    for(int ele : vertexGraph.get(startIndex).connections){
		      if(!marked[ele]){
		        dfs(ele);
		      }
		    }
		    
		  }//End Methode
		  
	} // End Class

	public class DFS{
		  
		  ArrayList<Square> vertexGraph;
		  boolean[] marked;
		  //ArrayList<Integer> visited;
		  Integer[] pred;
		  
		  public DFS(ArrayList<Square> vertexGraph) {
			  this.vertexGraph = vertexGraph;
			   this.marked = new boolean[this.vertexGraph.size()];
			   //this.visited = new ArrayList<Integer>(this.vertexGraph.size());
			   pred = new Integer[vertexGraph.size()];
			    
			   for(int i = 0; i < this.marked.length; i++){
			     this.marked[i] = false;
			   }  
		  }
		  
		  public void dfs(int srcIndex, int destIndex) {
			  Stack<Integer> stack = new Stack<Integer>();
			  stack.push(srcIndex);
			  pred[srcIndex] = -1;
			  
			  mainLoop: while(stack.size() > 0) {
				  
				  int v = stack.pop();
				  
				  if(!marked[v]) {
					  //visited.add(v);
					  marked[v] = true;
				  }else {
					  continue mainLoop;
				  }
				  vertexGraph.get(v).state = squareStateMap.get("SEARCH");
				  
				  for(int ele : vertexGraph.get(v).connections){
					  
					// check if dest found
					  if(ele == destIndex) {
						  pred[ele] = v;
						  break mainLoop;
					  }
					  
					  if(!marked[ele]) {
						  stack.push(ele);
						  pred[ele] = v;
					  }
				  }
				  delay(5);
				  vertexGraph.get(v).state = squareStateMap.get("VISITED");
			  }
		  } // End Methode
		
		  public void buildPath() {
			  Integer currentIndex = destIndex;
			  while(currentIndex != null && currentIndex != -1){		
				  vertexGraph.get(currentIndex).state = squareStateMap.get("PATH");
				  path.add(0, currentIndex);
				  currentIndex = pred[currentIndex];
			  }
			  vertexGraph.get(destIndex).state = squareStateMap.get("DEST");
			  vertexGraph.get(srcIndex).state = squareStateMap.get("SRC");			  
		  } // End Methode
		  
	} // End Class
	
	public class BFS{
		  
		  ArrayList<Square> vertexGraph;
		  boolean[] marked;
		  //ArrayList<Integer> visited;
		  Integer[] pred;
		  
		  public BFS(ArrayList<Square> vertexGraph){
		    
		    this.vertexGraph = vertexGraph;
		    this.marked = new boolean[this.vertexGraph.size()];
		    //this.visited = new ArrayList<Integer>(this.vertexGraph.size());
		    pred = new Integer[vertexGraph.size()];
		    
		    for(int i = 0; i < this.marked.length; i++){
		      this.marked[i] = false;
		    }    
		  }
		  
		  public void bfs(int srcIndex, int destIndex) {
			  LinkedList<Integer> queue = new LinkedList<Integer>();
			  queue.add(srcIndex);
			  pred[srcIndex] = -1;
			  
			  mainLoop: while(queue.size() > 0) {
				  
				  int v = queue.pop();
				  
				  if(!marked[v]) {
					  //visited.add(v);
					  marked[v] = true;
				  }else {
					  continue mainLoop;
				  }
				  vertexGraph.get(v).state = squareStateMap.get("SEARCH");
				  
				  for(int ele : vertexGraph.get(v).connections){
					  
					  // check if dest found
					  if(ele == destIndex) {
						  pred[ele] = v;
						  break mainLoop;
					  }
					  
					  if(!marked[ele]) {
						  queue.add(ele);
						  pred[ele] = v;
					  }
				  }
				  delay(5);
				  vertexGraph.get(v).state = squareStateMap.get("VISITED");
			  }  
		  } // End Methode

		  public void buildPath() {
			  Integer currentIndex = destIndex;
			  while(currentIndex != null && currentIndex != -1){		
				  vertexGraph.get(currentIndex).state = squareStateMap.get("PATH");
				  path.add(0, currentIndex);
				  currentIndex = pred[currentIndex];
			  }
			  vertexGraph.get(destIndex).state = squareStateMap.get("DEST");
			  vertexGraph.get(srcIndex).state = squareStateMap.get("SRC");			  
		  } // End Methode
		  
	} // End Class

	//-----------------------------------4.TAB: KruskalMST ----------------------------//
	
			// Code von: https://www.geeksforgeeks.org/kruskals-minimum-spanning-tree-algorithm-greedy-algo-2/

		    // A class to represent a subset for
		    // union-find
		    class subset
		    {
		        int parent, rank;
		    }

		    // A utility function to find set of an
		    // element i (uses path compression technique)
		    public int find(subset subsets[], int i)
		    {
		        // find root and make root as parent of i
		        // (path compression)
		        if (subsets[i].parent != i)
		            subsets[i].parent
		                = find(subsets, subsets[i].parent);
		 
		        return subsets[i].parent;
		    }
		 
		    // A function that does union of two sets
		    // of x and y (uses union by rank)
		    public void Union(subset subsets[], int x, int y)
		    {
		        int xroot = find(subsets, x);
		        int yroot = find(subsets, y);
		 
		        // Attach smaller rank tree under root
		        // of high rank tree (Union by Rank)
		        if (subsets[xroot].rank
		            < subsets[yroot].rank)
		            subsets[xroot].parent = yroot;
		        else if (subsets[xroot].rank
		                 > subsets[yroot].rank)
		            subsets[yroot].parent = xroot;
		 
		        // If ranks are same, then make one as
		        // root and increment its rank by one
		        else {
		            subsets[yroot].parent = xroot;
		            subsets[xroot].rank++;
		        }
		    }

		    // The main function to construct MST using Kruskal's
		    // algorithm
		    
		    public ArrayList<int[]> KruskalMaze(ArrayList<int[]> inputGraph)
		    {
				//*
				HashSet<Integer> vertexSet = new HashSet<Integer>();
				inputGraph.stream().forEach(e1 -> { 
					//System.out.print(Arrays.toString(e1));
					IntStream s = Arrays.stream(e1);
					s.forEach(e2 -> vertexSet.add(e2));
				});
				
				// This will store number of vertices
				int countVertex = vertexSet.size();
				
				//print(" and #Vertex: "+countVertex+"\n");
				//*/
				
		    	// This will store the resultant Maze-Connections
		    	ArrayList<int[]> result = new ArrayList<int[]>();
		       
		        // An counter variable, used for termination condition
		        int counter = 0;
		       
		        // Step 1:  Shuffle all the Edges random.If we are not allowed to
		        // change the given graph, we can create a copy of array of edges.
		        Collections.shuffle(inputGraph);
		 
		        // Allocate memory for creating V subsets
		        subset subsets[] = new subset[countVertex];
		        for (int i = 0; i < countVertex; ++i)
		            subsets[i] = new subset();
		 
		        // Create V subsets with single elements
		        for (int v = 0; v < countVertex; ++v)
		        {
		            subsets[v].parent = v;
		            subsets[v].rank = 0;
		        }
		 
		        int i = 0; // Index used to pick next edge
		 
		        // Number of edges to be taken is equal to V-1
		        while (counter < countVertex - 1)
		        {
		            // Step 2: Pick the next edge. And increment
		            // the index for next iteration
		            int[] next_edge = inputGraph.get(i++);
		 
		            int x = find(subsets, next_edge[0]);
		            int y = find(subsets, next_edge[1]);
		 
		            // If including this edge does't cause cycle,
		            // include it in result and increment the index
		            // of result for next edge
		            if (x != y) {
		                result.add(next_edge);
		                counter++;
		                Union(subsets, x, y);
		            }
		            // Else discard the next_edge
		        }                         
		        return result;
		    }
	
} // End Class
