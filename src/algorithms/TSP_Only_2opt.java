package algorithms;

import main.Main;
import processing.core.PApplet;

public class TSP_Only_2opt extends PApplet {
	
	//Name dieser Klasse für Konsolen-Output
	public final String programTag = "[TSP_Only_2opt] ";
	
	// Beste bis jetzt gefundene Tour
	public int[] BEST_EVER_TOUR;
	// Tour-Kosten von der bis jetzt besten gefundenen Tour
	public float BEST_EVER_COST;

	public void settings() {
		size(Main.width_ALL, Main.height_ALL);
	}
	
	public void setup(){
		surface.setLocation(-10, 0);
		frameRate(100);

		Main.algoLogger1.info(programTag);
		
		BEST_EVER_TOUR = Main.generateRandomTour();
		BEST_EVER_COST = Main.calcTourCost(BEST_EVER_TOUR);
		Main.algoLogger1.info("Initial Tour-Cost: {"+millis()+","+BEST_EVER_COST+"}");
		Main.algoLogger1.info("-------------------------");
		
		println(programTag+"Initial Random Tour-Cost: "+BEST_EVER_COST);
	}

	public void draw(){
		
		BEST_EVER_TOUR = completeTwoOptSwap(BEST_EVER_TOUR,BEST_EVER_COST);
		BEST_EVER_COST = Main.calcTourCost(BEST_EVER_TOUR);
		println(programTag+"Finished after "+(float)millis() / 1000 +" s");
		
		background(0);
		drawField();
		drawCities(color(0,255,0),false);
		drawTour(BEST_EVER_TOUR, 1.5f, color(255));
		drawAllText();
		noLoop();
	}

	// ----------------------Drawing Methodes-------------------------------//

	public void drawField() {
		noFill();
		strokeWeight(1);
		stroke(255, 217, 0);
		rect(Main.fieldDimensions_ALL[0].x, Main.fieldDimensions_ALL[0].y, Main.fieldDimensions_ALL[1].x,Main.fieldDimensions_ALL[1].y);
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
		text("# Knoten/Cities: " + Main.countNodes_ALL, 0, 20);
		text("Frame: " + frameCount, 0, 40);
		text("Best Distance: " + BEST_EVER_COST + " km", 0, 80);

		fill(255, 0, 0);
		text("FPS: " + frameRate, width - 170, 20);
		text("Run-Time: " + (float) millis() / 1000 + " s", width - 500, 20);
	}//End Methode
	
	//checkBEST_EVER_TOUR() Methode fehlt, weil sie hier nicht gebraucht wird
	
	//----------------------------------------------------------------------//

	public static int[] twoOptSwap(int[] tour,int i,int k){
	  
	  int[] res = subset(tour,0,i);
	  
	  res = concat(res,reverse(subset(tour,i,k-i+1)));
	   
	  res = concat(res,subset(tour,k+1,tour.length-1-k));
	  
	  return res;  
	}//End Methode

	public static int[] completeTwoOptSwap(int[] tour, float tourCost) {
		while(true) {
			boolean finished = true;
			out:for (int i = 1; i < tour.length - 1; i++) {
				for (int k = i + 1; k < tour.length; k++) {					
					int[] newTour = twoOptSwap(tour, i, k);
					float newTourCost = Main.calcTourCost(newTour);
					if (newTourCost < tourCost) {
						tour = newTour;
						tourCost = newTourCost;		
						finished = false;
						break out;
					}
				}
			}
			if(finished) {
				return tour;
			}
		}
	}//End Methode
	 
} // End Algorithm
