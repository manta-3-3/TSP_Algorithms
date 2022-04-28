package algorithms;

import java.util.Random;

import main.Main;
import processing.core.*;

public class TSP_BruteForce extends PApplet {

	public final String programTag = "[TSP_BruteForce]";
	
	public final int countPoints = Main.countCities_ALL;
	public final PVector[] randomPointCords = Main.randomPointCords_ALL;// List contains coordinates of city at cityIndex k
	public final PVector[] fieldDimensions = Main.fieldDimensions_ALL; // Feld in dem die Punkte/Cities gezeichnet also liegen können

	public int[] toPermIntegers; 
	
	public long possibleSolutions; // realToTestSolutions/2, da gleiche Reihenfolge von Punkten gleiche Streckeist
	public long testedSolutions;
	public long realToTestSolutions;
	public long realTestedSolutions;
	public float progress;

	public float BEST_EVER_DIST;
	public int[] BEST_EVER_PATH;
	
	public boolean hidePathFinding;

	public void settings() {
		size(Main.width_ALL,Main.height_ALL);
	}
	
	public void setup() {
		 surface.setLocation(-10, 0);
		 frameRate(100);
	/*	
	  size(1260,840,P2D);
	  fieldDimensions[0] = new PVector(20, 150); //Vector top left corner cord of rect
	  fieldDimensions[1] = new PVector(width-2*20, height-150-20); //Vector width & height of rect
	    
	  frameRate(200);
	*/   
	  toPermIntegers = generateToPermIntegers(countPoints);
	  //randomPointCords = generateRandomPointCords(countPoints); // erzeuge n zufällige Punkte
	  
	  hidePathFinding = false;
	  
	  BEST_EVER_DIST = Float.POSITIVE_INFINITY;
	  BEST_EVER_PATH = toPermIntegers;
	  
	  possibleSolutions = ( factorial(countPoints-1) ) / 2l;
	  realToTestSolutions = ( factorial(countPoints-1) );
	  testedSolutions = 0;
	  realTestedSolutions = 0;
	  progress = ( (float)testedSolutions / (float)realToTestSolutions ) * 100f;
	  
	  thread("calcThread"); //starte in separaten Thread: generiere alle möglichen Permutationen mit Heap-Algorithmus und messe Distanzen
	 
	}

	public void draw() {
		background(0);
		drawField();
		drawCities();
	  
		if(!hidePathFinding){
			drawPath(toPermIntegers,1f);
		}
	  
		drawPath(BEST_EVER_PATH,1.5f);
		
		drawAllText();
	}

	public void calcThread(){// Inhalt dieser Methode wird in einem separatem Thread ausgeführt
		//generatePerms_heaps(toPermIntegers.length-1, toPermIntegers); // length-1, da ein Punkt fix ist);
		generatePerms_backtracking(1,toPermIntegers.length-1,toPermIntegers); // Knoten 0 fix gewählt
		  
		hidePathFinding = true;
		fill(255);
		println(programTag+" Finished after "+(float)millis() / 1000f+" s");
		println(programTag+realTestedSolutions);
	}
	
	//-------------------------------------------------------------------------------//

	public void drawField(){ 
		noFill();
		strokeWeight(1);
		stroke(255, 217, 0);
		rect(fieldDimensions[0].x,fieldDimensions[0].y,fieldDimensions[1].x,fieldDimensions[1].y);
		  
	}//End Methode

	public void drawCities(){
		  
		  for(int i = 0; i < randomPointCords.length; i++){
		    
		    noFill();
		    strokeWeight(1);
		    stroke(0,255,0);
		    circle(randomPointCords[i].x,randomPointCords[i].y,10);
		  }
		  
	}//End Methode

	public void drawPath(int[] path,float strokeWeight){
		  
		  strokeWeight(strokeWeight);
		  stroke(255);
		    
		  beginShape();
		  for(int i = 0; i < path.length; i++){      
		    vertex(randomPointCords[path[i]].x,randomPointCords[path[i]].y);      
		  }
		  endShape(CLOSE); 
		         
	}//End Methode

	public void drawAllText(){
		  
		  textSize(20);
		  
		  fill(255);
		  text("# Knoten/Cities: "+countPoints,0,20);
		  text("Possible Solutions: " + possibleSolutions,0,40);
		  text("Tested Solutions: "+testedSolutions+" / "+realToTestSolutions,0,60);
		  progress = ( (float)testedSolutions / (float)realToTestSolutions ) * 100;
		  text("Progress: " + progress +" %",0,80);
		  
		  text("Best Distance: " + round(BEST_EVER_DIST)+" km",0,120);
		  
		  fill(255,0,0);
		  text("FPS: "+frameRate,width-170,20);
		  text("Run-Time: "+(float)millis() / 1000+" s",width-500,20);
	} //End Methode

	public PVector[] generateRandomPointCords(int anzahl){ // erzeuge n zufällige Punkte im Feld mit ganzzahligen Koordinaten   
		  PVector[] res = new PVector[anzahl];
		  
		  for(int i = 0; i < anzahl; i++){    
		        res[i] = new PVector((float)Math.rint(randFloat(fieldDimensions[0].x,fieldDimensions[0].x+fieldDimensions[1].x)),(float)Math.rint(randFloat(fieldDimensions[0].y,fieldDimensions[0].y+fieldDimensions[1].y)));    
		  }  
		  return res;    
	} //End Methode
		  
	public float randFloat(float min, float max) { //gibt eine züfälliges Float zwischen min und max zurück
		  Random rand = new Random();
		  return rand.nextFloat() * (max - min) + min;
	} //End Methode

	/*
	public PVector[] generateRandomPointCords_old(int anzahl){
		  PVector[] res = new PVector[anzahl];
		  
		  for(int i = 0; i < anzahl; i++){    
		    res[i] = new PVector(random(fieldDimensions[0].x,fieldDimensions[0].x+fieldDimensions[1].x),random(fieldDimensions[0].y,fieldDimensions[0].y+fieldDimensions[1].y));    
		  }
		  
		  return res;
		}//End Methode

		public void calcDist(int[] array){
		  
		  PVector[] temp = new PVector[array.length];
		  float tourDist = 0;
		    
		    if(array[0] < array[array.length-2]){
		                    
		       for(int i = 0; i < array.length; i++){
		         
		         temp[i] = randomPointCords[array[i]];
		         
		       }
		       
		       currentPath = temp;
		         
		       for(int i = 0; i < temp.length; i++){
		           
		           int nextIndex = (i+1) % temp.length;
		           tourDist += dist(temp[i].x, temp[i].y, temp[nextIndex].x, temp[nextIndex].y); 
		           
		       }
		       
		       if(tourDist < BEST_EVER_DIST){
		    
		           BEST_EVER_DIST = tourDist;
		           currentBestPath = temp;
		    
		       }     
		         
		    }
		    
		}//End Methode 
		*/

	private void calcTotalPathDist(int[] path){
		
		if(!(path[1] < path[path.length-1])){ // schaue mit dieser If-Bedingung ob es nicht einfach ein umgekehrter Path ist //TODO ist das nötig??
			return;
		} // nur gültig wenn generatePerms_backtracking() verwendet wird und nicht generatePerms_heaps()
		
		/*
		if(!(path[0] < path[path.length-2])){ // schaue mit dieser If-Bedingung ob es nicht einfach ein umgekehrter Path ist //TODO ist das nötig??
			return;
		} // nur gültig wenn generatePerms_heaps() verwendet wird und nicht generatePerms_backtracking()
		*/
		
		realTestedSolutions++;
		float totalDist = 0;
		for(int i = 0; i < path.length; i++){
		    int nextIndex = (i+1) % path.length;
		    totalDist += Main.costFunc(path[i],path[nextIndex]);
		}		    
		checkBEST_EVER_PATH_DIST(path,totalDist);
		
		/* old
		if(path[0] < path[path.length-2]){ // schaue mit dieser If-Bedingung ob es nicht einfach ein umgekehrter Path ist
			   
			float totalDist = 0;
			   
			  for(int i = 0; i < path.length; i++){      
			     int nextIndex = (i+1) % path.length;
			     totalDist += dist(randomPointCords[path[i]].x,randomPointCords[path[i]].y,randomPointCords[path[nextIndex]].x,randomPointCords[path[nextIndex]].y);;
			  }
			    
			checkBEST_EVER_PATH_DIST(path,totalDist);
			   
		}
		*/	
	}//End Methode

	public void checkBEST_EVER_PATH_DIST(int[] inputPath,float inputDist){ // check inpute Path/Distance ist better than BEST EVER PATH/DISTANCE if yes set it to input
		  
		   if(inputDist < BEST_EVER_DIST){
		     println(programTag+" After "+round((float)millis()/1000f)+" s new BEST_EVER found!");
		     BEST_EVER_PATH = inputPath.clone();
		     BEST_EVER_DIST = inputDist;
		   }
		      
		}//End Methode

	static final long factorial(int num) {
		  return num == 1? 1 : factorial(num - 1)*num;
	}
	
	//-----------------------------------2.TAB: Heap_Algorithmus -----------------------//

	public int[] generateToPermIntegers(int anzahl) {
		  int[] res = new int[anzahl];
		  
		  for(int i = 0; i < anzahl; i++){
		    res[i] = i; 
		  }
		  return res;
		}//End Methode

	public void generatePerms_heaps(int n, int[] perm) {
		     
		    int[] c = new int[n];
		    
		    for(int i = 0; i < n; i++) {
		      
		      c[i]= 0;
		    }
		    
		    testedSolutions++;
		    calcTotalPathDist(perm);  
		       
		    int I = 1;
		    
		    while(I < n) {
		      
		      if(c[I]< I) {
		        
		        if (I % 2 == 0) {
		        
		          //swap(perm[0], perm[i])
		          int temp = perm[I];
		          perm[I] = perm[0];
		          perm[0] = temp;
		        }else {
		        
		          //swap(perm[c[i]], perm[i])
		          int temp = perm[I];
		          perm[I] = perm[c[I]];
		          perm[c[I]] = temp;      
		        }
		        
		        testedSolutions++;
		        calcTotalPathDist(perm);
		        
		  
		        c[I] += 1;
		        I = 1;
		        
		      }else {
		        
		        c[I] = 0;
		          I += 1;
		        
		      }
		        
		    }//End of While
		    
	}//End Methode

	//modified Version of -> https://www.geeksforgeeks.org/write-a-c-program-to-print-all-permutations-of-a-given-string/
	public void generatePerms_backtracking (int s, int e, int[] perm) {
		   /**
		    * permutation function
		    * @param str string to calculate permutation for
		    * @param l starting index
		    * @param r end index
		    */
		    if (s == e){
		      testedSolutions++;
		      calcTotalPathDist(perm); 
		    }else{
		      for (int i = s; i <= e; i++){
		        int temp;
		        //swap int at index A with index B in array
		        temp = perm[s];
		        perm[s] = perm[i];
		        perm[i] = temp;
		        
		        generatePerms_backtracking(s+1, e, perm);
		        
		        //swap (back) int at index A with index B in array
		        temp = perm[s];
		        perm[s] = perm[i];
		        perm[i] = temp;
		      }
		    }
		    
	}//End Methode
		  
} // End Algorithm
