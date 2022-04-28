package algorithms;

import main.Main;
import processing.core.PApplet;

public class TSP_Random extends PApplet {

	//Dieser Algo generiert nur zufällige Lösungen, ist eine Art Kontrolleinheit.
	long runTime = 183000; //msec
	
	// Name dieser Klasse für Konsolen-Output
	public final String programTag = "[TSP_Random]";

	public boolean hidePathFinding;
	public long testedSolutions;

	// Beste bis jetzt gefundene Tour
	public int[] BEST_EVER_TOUR;
	// Tour-Kosten von der bis jetzt besten gefundenen Tour
	public float BEST_EVER_COST;
	
	public boolean readyForNextIter;
	
	public void settings() {
		size(Main.width_ALL, Main.height_ALL);
	}
	
	public void setup() {
		surface.setLocation(-10, 0);
		frameRate(100);
		
		Main.algoLogger2.info(programTag);
		
		hidePathFinding = false;
		testedSolutions = 1;
		
		BEST_EVER_TOUR = Main.generateRandomTour();
		BEST_EVER_COST = Main.calcTourCost(BEST_EVER_TOUR);
		Main.algoLogger2.info("Initial Tour-Cost: {"+millis()+","+BEST_EVER_COST+"}");
		
		Main.algoLogger2.info("-------------------------");
		
		readyForNextIter = true;
	}
	
	public void draw() {
		
		if(millis() > runTime) {
			System.exit(0);//TODO: Achtung Terminating Kondition drin
		}
		
		if(readyForNextIter) {
			thread("solveThread");
			readyForNextIter = false;
		}
		
		background(0);
		drawField();
		drawCities(color(0,255,0),false);
		drawTour(BEST_EVER_TOUR,1.5f,color(255));
		drawAllText();
	}
	
	public void solveThread() {
		checkBEST_EVER_TOUR(Main.generateRandomTour());
		readyForNextIter = true;
	}

	// ---------------------------------Drawing Methodes ------------------------------//

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
		
		textSize(20);
		fill(255);
		text("# Knoten/Cities: " + Main.countNodes_ALL, 0, 20);

		text("Best Distance: " + BEST_EVER_COST + " km", 0, 120);

		fill(255, 0, 0);
		text("FPS: " + frameRate, width - 170, 20);
		text("Run-Time: " + (float) millis() / 1000 + " s", width - 500, 20);
	
	}// End Methode

	// -------------------------------------------------------------------------------//
	
	public void checkBEST_EVER_TOUR(int[] tour) {
		testedSolutions++;
		float tourCost = Main.calcTourCost(tour);
		if(tourCost < BEST_EVER_COST) {
			BEST_EVER_TOUR = tour.clone();
			BEST_EVER_COST = tourCost;
			
			Main.algoLogger2.info("{"+millis()+","+BEST_EVER_COST+"},");
		}
	}
	
} // End Algorithm
