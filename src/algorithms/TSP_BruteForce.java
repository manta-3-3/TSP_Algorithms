package algorithms;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.stream.IntStream;

import main.Main;
import processing.core.PApplet;

public class TSP_BruteForce extends PApplet {

	//Name dieser Klasse für Konsolen-Output
	public final String programTag = "[TSP_BruteForce]";

	public boolean hidePathFinding;
	
	public int[] toPermIntegers;
	// Beste bis jetzt gefundene Tour
	public int[] BEST_EVER_TOUR;
	// Tour-Kosten von der bis jetzt besten gefundenen Tour
	public float BEST_EVER_COST;

	// Anzahl möglicher Touren mit Knoten 1 als Startpunkt, entspricht aTSP = (n-1)!
	public BigInteger possiblePermutations;
	public BigInteger generatedPermutations;
	//Anzahl möglicher Touren mit Knoten 1 als Startpunkt und alle identischen Touren nur einmal gezählt
	//entspricht sTSP = 1/2*(n-1)!
	public BigInteger toTestPermutations;
	public BigInteger testedPermutations;
	public float progress;

	public void settings() {
		size(Main.width_ALL, Main.height_ALL);
	}

	public void setup() {
		surface.setLocation(-10, 0);
		frameRate(100);
		
		Main.algoLogger1.info(programTag);
		
		hidePathFinding = false;
		
		toPermIntegers = IntStream.range(0,Main.countNodes_ALL).toArray();
		BEST_EVER_TOUR = toPermIntegers.clone();
		BEST_EVER_COST = Main.calcTourCost(BEST_EVER_TOUR);
		Main.algoLogger1.info("Initial Tour-Cost: {"+millis()+","+BEST_EVER_COST+"}");

		possiblePermutations = Main.factorialBigNum(new BigInteger(Integer.toString(Main.countNodes_ALL-1)));
		generatedPermutations = new BigInteger("0");
		toTestPermutations = Main.factorialBigNum(new BigInteger(Integer.toString(Main.countNodes_ALL-1))).divide(new BigInteger("2"));
		testedPermutations = new BigInteger("0");
		progress = 0f;

		Main.algoLogger1.info("-------------------------");
		
		// starte in separaten Thread: generiere alle möglichen Permutationen mit Backtracking oder Heap-Algorithmus und messe Distanzen
		thread("solveThread");					
	}

	public void draw() {
		
		background(0);
		
		drawField();
		drawCities(color(0,255,0),false);
		if (!hidePathFinding) {
			drawTour(toPermIntegers, 1f,color(255));
		}
		drawTour(BEST_EVER_TOUR, 1.5f,color(255));

		drawAllText();
		
	}

	// Inhalt dieser Methode wird in einem separatem Thread ausgeführt
	public void solveThread() {
		
		// generatePerms_heaps(toPermIntegers.length-1, toPermIntegers); // length-1, da ein Punkt fix ist
		generatePerms_backtracking(1, toPermIntegers.length - 1, toPermIntegers); // Knoten 0 fix gewählt

		hidePathFinding = true;
		fill(255);
		println(programTag + " Finished after " + (float) millis() / 1000f + " s "+"("+testedPermutations+" path's calc)");
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
		text("To Test Permutations for sTSP: " + toTestPermutations, 0, 40);
		text("Generated Permutations: " + generatedPermutations + " / " + possiblePermutations, 0, 60);
		try {
			MathContext mc = new MathContext(5, RoundingMode.HALF_UP);
			progress = new BigDecimal(generatedPermutations).divide(new BigDecimal(possiblePermutations),mc).floatValue()*100f;
			mc = null;
		}catch(ArithmeticException e) {
			progress = 0f;
		}
		text("Progress: " + progress + " %", 0, 80);

		text("Best Distance: " + BEST_EVER_COST + " km", 0, 120);

		fill(255, 0, 0);
		text("FPS: " + frameRate, width - 170, 20);
		text("Run-Time: " + (float) millis() / 1000 + " s", width - 500, 20);
	} // End Methode

	// -------------------------------------------------------------------------------//
	
	public void checkBEST_EVER_TOUR(int[] tour) {

		// schaue mit dieser If-Bedingung ob es nicht einfach ein umgekehrter  Path ist //TODO ist das nötig??
		// nur gültig wenn generatePerms_backtracking() verwendet wird und nicht generatePerms_heaps()
		if (!(tour[1] < tour[tour.length - 1])) { 
			return;
		}
		/*
		 * if(!(path[0] < path[path.length-2])){ // schaue mit dieser If-Bedingung ob es
		 * nicht einfach ein umgekehrter Path ist //TODO ist das nötig?? return; } //
		 * nur gültig wenn generatePerms_heaps() verwendet wird und nicht
		 * generatePerms_backtracking()
		 */
		float tourCost = Main.calcTourCost(tour); 
		testedPermutations = testedPermutations.add(new BigInteger("1"));
		
		if (tourCost < BEST_EVER_COST) {
			BEST_EVER_TOUR = tour.clone();
			BEST_EVER_COST = tourCost;
			Main.algoLogger1.info("{"+millis()+","+BEST_EVER_COST+"},");
			println(programTag + " After " + millis() + " msec new BEST_EVER found!");
		}
	}// End Methode

	// -----------------------------------2.TAB: Heap_Algorithmus --------------------//

	public void generatePerms_heaps(int n, int[] perm) {

		int[] c = new int[n];
		for (int i = 0; i < n; i++) {
			c[i] = 0;
		}

		generatedPermutations = generatedPermutations.add(new BigInteger("1"));
		checkBEST_EVER_TOUR(perm);

		int I = 1;
		while (I < n) {
			if (c[I] < I) {
				if (I % 2 == 0) {
					// swap(perm[0], perm[i])
					int temp = perm[I];
					perm[I] = perm[0];
					perm[0] = temp;
				} else {
					// swap(perm[c[i]], perm[i])
					int temp = perm[I];
					perm[I] = perm[c[I]];
					perm[c[I]] = temp;
				}

				generatedPermutations = generatedPermutations.add(new BigInteger("1"));
				checkBEST_EVER_TOUR(perm);
				
				c[I] += 1;
				I = 1;
			} else {
				c[I] = 0;
				I += 1;
			}
		} // End of While
	}// End Methode

	// modified Version of -> https://www.geeksforgeeks.org/write-a-c-program-to-print-all-permutations-of-a-given-string/
	public void generatePerms_backtracking(int s, int e, int[] perm) {
		/**
		 * permutation function
		 * 
		 * @param str string to calculate permutation for
		 * @param l   starting index
		 * @param r   end index
		 */
		if (s == e) {
			generatedPermutations = generatedPermutations.add(new BigInteger("1"));
			checkBEST_EVER_TOUR(perm);
		} else {
			for (int i = s; i <= e; i++) {
				int temp;
				// swap int at index A with index B in array
				temp = perm[s];
				perm[s] = perm[i];
				perm[i] = temp;

				generatePerms_backtracking(s + 1, e, perm);

				// swap (back) int at index A with index B in array
				temp = perm[s];
				perm[s] = perm[i];
				perm[i] = temp;
			}
		}
	}// End Methode

} // End Algorithm
