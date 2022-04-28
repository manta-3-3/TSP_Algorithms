package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import main.CustomException;
import main.Main;
import processing.core.PApplet;

public class TSP_Genetic_Algorithem extends PApplet {
	
	/*Programm braucht ca. 15 min also ca. 1000 Sekunden für gute bis sehr gute Lösung
	  ca. 300 Sekunden für mittlere bis gute Lösung
	*/

	//Name dieser Klasse für Konsolen-Output
	public final String programTag = "[TSP_Genetic_Algorithem]";
	
	public final boolean twoOptActive = false; //TODO: out of order now
	
	//Grösse der Population, hier 80% der zu lösenden Knotenanzahl
	public final int popSize = Math.round(Main.countNodes_ALL * 0.8f);
	// Wahrscheinlichkeit für Mutation sollte nicht höher als 0.1 sein zu starke Mutation schadet auch
	public final float mutationRate = 0.03f;
	
	//Aktuelle Population
	public Chromo[] currentPop;
	// durchschnittliche Fitness in der aktuellen Generation
	public float averageFitnessOfGen;
	// aktuelle Generation
	public int generationNumber;

	// Beste bis jetzt gefundene Tour
	public int[] BEST_EVER_TOUR;
	// Tour-Kosten von der bis jetzt besten gefundenen Tour
	public float BEST_EVER_COST;
	
	public boolean readyForNextIter;
	

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
		
		generationNumber = 1;
		averageFitnessOfGen = 0.0f;
		
		initFirstPop();
		readyForNextIter = true;
	}

	public void draw(){
		
		if(readyForNextIter) {
			thread("solveThread");
			readyForNextIter = false;
		}
		
		background(0);
		drawField();
		drawCities(color(0,255,0),false);
		drawTour(BEST_EVER_TOUR, 1.5f, color(255));
		drawAllText();
	}

	// Inhalt dieser Methode wird in einem separatem Thread ausgeführt
	public void solveThread() {
		// erzeuge leere neue Generation
		Chromo[] newPop = new Chromo[popSize];
		
		// Teil 1: ordne currentPop Array nach ihrer Fitness, wobei die beste Fitness an Index 0 ist.
		Arrays.sort(currentPop, new sortByFitness().reversed());
		
		// Teil 2.0: schaue ob Lösung mit bester Fitness besser als BEST_EVER_TOUR/COST ist
		checkBEST_EVER_TOUR(currentPop[0].tour,currentPop[0].tourCost);
		
		//Berechne die durchschnittliche Fitness und den FitnessPool 
		float[] fitnessPool = new float[popSize];
		float totalFitnessOfGen = 0f;
		for (int i = 0; i < fitnessPool.length; i++) {
			fitnessPool[i] = currentPop[i].fitness;
			totalFitnessOfGen += currentPop[i].fitness;
		}
		averageFitnessOfGen = totalFitnessOfGen / (float) fitnessPool.length;
		
		// Teil 2.1: nimm die 5 % der Besten von der alten Generation ohne Crossover, Mutation oder neue komplett zufällige Lösung
		int counter = 0;
		int amount =  0;
		while (counter < ceil(popSize * 0.05f)) {

			newPop[counter] = currentPop[counter];
			counter++;
			
		}
		amount = counter;
		// Teil 2.2: erzeuge 5 % neue komplett zufällige Lösungen
		while (counter < (amount + ceil(popSize * 0.05f))) {

			newPop[counter] = new Chromo(Main.generateRandomTour());
			counter++;
			
		}
		amount = counter;
		// Teil 2.3: erzeuge 10 % neue Individuuen über 2x Zufällige Selektion 
		// mit Nearest-Insertion-combined crossover und Block-type mutation
		while (counter < (amount + ceil(popSize * 0.1f))) {

			int[] parentA = randomSelect().tour;
			int[] parentB = randomSelect().tour;
			while(parentB.equals(parentA)){ //schau das nicht die selben zwei Eltern gezogen wurden
			    parentB = randomSelect().tour; // ziehe neu wenn es die gleichen waren
			}
			
			newPop[counter] = new Chromo(blockTypMut(nearInsertCombCross(parentA,parentB)));
			counter++;
		}
		amount = counter;
		// Teil 2.4: erzeuge 40 % neue Individuuen über 1x Tournament Selektion und 1x Proportional Roulette
		// mit Order Crossover Operator (OX) und Block-type mutation
		while (counter < (amount + ceil(popSize * 0.4f))) {
			
			int[] parentA = propWheelSelect(fitnessPool).tour;
			int[] parentB = binTournamentSelect().tour;
			while(parentB.equals(parentA)){ //schau das nicht die selben zwei Eltern gezogen wurden
			    parentB = binTournamentSelect().tour; // ziehe neu wenn es die gleichen waren
			}
			
			newPop[counter] = new Chromo(blockTypMut(orderCross(parentA,parentB)));
			counter++;
			
		}
		amount = counter;
		// Teil 2.5: erzeuge 40 % neue Individuuen über 1x Tournament Selektion und 1x Proportional Roulette
		// mit Nearest-Insertion-combined crossover und Block-type mutation
		while (counter < popSize) {
			
			int[] parentA = propWheelSelect(fitnessPool).tour;
			int[] parentB = binTournamentSelect().tour;
			while(parentB.equals(parentA)){ //schau das nicht die selben zwei Eltern gezogen wurden
			    parentB = binTournamentSelect().tour; // ziehe neu wenn es die gleichen waren
			}
			
			newPop[counter] = new Chromo(blockTypMut(nearInsertCombCross(parentA,parentB)));
			counter++;
			
		}
		// letzter Schritt ersetze alte Population mit neuer Population
		if (newPop.length != popSize) {
			CustomException e = new CustomException("Invalid new Population build!");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
		}
		currentPop = newPop;
		generationNumber++;
		
		readyForNextIter = true;
	} 
	
	// ----------------------Drawing Methodes-------------------------------//

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
		  text("Generation Size: "+popSize,0,40);
		  text("Mutation Rate: " + mutationRate,0,60);
		  text("Generation "+generationNumber,0,80);
		  text("durchschnittliche Fitness der Generation: "+averageFitnessOfGen,0,100);
		  text("Best Distance: " + BEST_EVER_COST+" km",0,140);
		  
		  fill(255,0,0);
		  text("FPS: "+frameRate,width-170,20);
		  text("Run-Time: "+(float)millis() / 1000+" s",width-500,20);
	}//End Methode
	
	//---------Schritt 0: initialisierung der ersten Population-------------// 
	public void initFirstPop(){
		
		int amount = ceil(popSize/3f);
		int counter = 0;
		currentPop = new Chromo[popSize];
		
		// 1/3 der Population wird mit der Zufällige Methode erzeugt
		while(counter < 1*amount){
			currentPop[counter] = new Chromo(Main.generateRandomTour());
			counter++;
		}
		// 1/3 der Population wird mit der Zufällige NI-Methode erzeugt
		while(counter < 2*amount){
			currentPop[counter] = new Chromo(randomInsertion());
			counter++;
		}
		// 1/3 der Population wird mit der Nearest neighbour-Heuristik (NN) erzeugt
		while(counter < popSize){
			currentPop[counter] = new Chromo(nearestNeighbour());
			counter++;
		}
		
	}//End Methode
	
	public static int[] nearestNeighbour() {
		ArrayList<Integer> tour = new ArrayList<Integer>();
		boolean[] visited = new boolean[Main.countNodes_ALL];
		
		int currentNode = Main.randInt(0,Main.countNodes_ALL);
		tour.add(currentNode);
		visited[currentNode] = true;
		
		while(tour.size() < Main.countNodes_ALL) {	
			int nearestNodeIndex = -1;
			float nearestEdgeCost = Float.POSITIVE_INFINITY;
			for(int i = 0; i < visited.length; i++) {
				if(visited[i]) {
					continue;
				}
				float edgeCost = Main.costFunc(currentNode, i);
				if(edgeCost < nearestEdgeCost) {
					nearestNodeIndex = i;
					nearestEdgeCost = edgeCost;
				}
			}
			currentNode = nearestNodeIndex;
			tour.add(currentNode);
			visited[currentNode] = true;
		}
		
		if (new HashSet<Integer>(tour).size() != Main.countNodes_ALL) {
			CustomException e = new CustomException("Invalid Tour by NN build!");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return null;
		}
		
		return tour.stream().mapToInt(Integer::intValue).toArray();
	} // End Methode
	
	public static int[] randomInsertion() {
		ArrayList<Integer> tour = (ArrayList<Integer>) IntStream.range(0,Main.countNodes_ALL).boxed().collect(Collectors.toList()); 
		Collections.shuffle(tour);
		int indK = 3;
		
		while(indK < tour.size()) {
			
			int firstInd = -1;
			float cheapestDeltaCost = Float.POSITIVE_INFINITY;
			
			for (int indI = 0; indI < indK; indI++) {
				int indJ = (indI + 1) % indK;
				float deltaCost = Main.costFunc(tour.get(indI), tour.get(indK)) + Main.costFunc(tour.get(indK), tour.get(indJ)) - Main.costFunc(tour.get(indI), tour.get(indJ));
				if(deltaCost < cheapestDeltaCost) {
					firstInd = indJ;
					cheapestDeltaCost = deltaCost;
				}
			}
			
			tour.add(firstInd,tour.get(indK));
			tour.remove(indK+1);
			
			indK++;
		}
		
		if (new HashSet<Integer>(tour).size() != Main.countNodes_ALL) {
			CustomException e = new CustomException("Invalid Tour by RI build!");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return null;
		}
		
		return tour.stream().mapToInt(Integer::intValue).toArray();
	}// End Methode
	
	//---------Schritt 1: Erzeugen einer neuen Generation-----------------//
	
	//-----Schritt 1b: Selektion-----//
	public Chromo randomSelect() {
		return currentPop[Main.randInt(0,popSize)];
	} // End Methode
	
	public Chromo binTournamentSelect() {
		Chromo compA = currentPop[Main.randInt(0,popSize)];
		Chromo compB = currentPop[Main.randInt(0,popSize)];
		while(compA.equals(compB)) {
			compB = currentPop[Main.randInt(0,popSize)];
		}
		if(compA.fitness > compB.fitness) {
			return compA;
		}else {
			return compB;
		}
	} // End Methode
	
	public Chromo propWheelSelect(float[] fitnessPool) {
		return currentPop[Main.computeWeightedRandomPick(fitnessPool)];
	} // End Methode
	
	//-----Schritt 1c: Cross-over-----//
	public int[] orderCross(int[] tourA, int[] tourB) {
		
		//Bestimme durch Zufall welches tourA und tourB ist
		if(0.5f > random(1)) {
			int[] temp = tourA;
			tourA = tourB;
			tourB = temp;
		}
		
		int firstCut = Main.randInt(1, tourA.length-1);
		int secondCut = Main.randInt(firstCut+1, tourA.length);
		
		ArrayList<Integer> childTour = (ArrayList<Integer>) Arrays.stream(Arrays.copyOfRange(tourA,firstCut,secondCut)).boxed().collect(Collectors.toList());

		int counterB = secondCut;
		while(secondCut < tourB.length) {
			if(!childTour.contains(tourB[counterB])) {
				childTour.add(tourB[counterB]);
				secondCut++;
			}
			if(!(counterB+1 < tourB.length)) {
				counterB = 0;
			}else {
				counterB++;
			}
		}
		secondCut = 0;
		while(secondCut < firstCut) {
			if(!childTour.contains(tourB[counterB])) {
				childTour.add(secondCut,tourB[counterB]);
				secondCut++;
			}
			counterB++;
		}
		
		if (new HashSet<Integer>(childTour).size() != Main.countNodes_ALL) {
			CustomException e = new CustomException("Invalid Tour by OX build!");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return null;
		}
		
		return childTour.stream().mapToInt(Integer::intValue).toArray();
	} // End Methode
	
	public int[] nearInsertCombCross(int[] tourA, int[] tourB) {
		
		//Bestimme durch Zufall welches tourA und tourB ist
		if (0.5f > random(1)) {
			int[] temp = tourA;
			tourA = tourB;
			tourB = temp;
		}
		
		int cut = Main.randInt(1, tourA.length);
		
		ArrayList<Integer> childTour = (ArrayList<Integer>) Arrays.stream(Arrays.copyOfRange(tourA,0,cut)).boxed().collect(Collectors.toList());
		
		ArrayList<Integer> toInsert = (ArrayList<Integer>) Arrays.stream(tourB).boxed().collect(Collectors.toList());
		toInsert.removeAll(childTour);
		
		for(Integer eleK : toInsert) {
			
			int firstInd = -1;
			float cheapestDeltaCost = Float.POSITIVE_INFINITY;
			
			for (int indI = 0; indI < childTour.size(); indI++) {
				int indJ = (indI + 1) % childTour.size();
				float deltaCost = Main.costFunc(childTour.get(indI), eleK) + Main.costFunc(eleK, childTour.get(indJ)) - Main.costFunc(childTour.get(indI), childTour.get(indJ));
				if(deltaCost < cheapestDeltaCost) {
					firstInd = indJ;
					cheapestDeltaCost = deltaCost;
				}
			}
			childTour.add(firstInd,eleK);
			
		}
		toInsert = null;
		
		if (new HashSet<Integer>(childTour).size() != Main.countNodes_ALL) {
			CustomException e = new CustomException("Invalid Tour by nearInsertCombCross build!");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return null;
		}
		
		return childTour.stream().mapToInt(Integer::intValue).toArray();
	} // End Methode
	
	//-----Schritt 1d: Mutation-------//
	public int[] blockTypMut(int[] tour) {
		
		if( mutationRate > random(1)) {
			return tour;
		}
		
		int randNodeInd = Main.randInt(0, tour.length);
		int r = Main.randInt(0, min(randNodeInd ,Main.countNodes_ALL-randNodeInd-1));
		
		int[] mutTour = subset(tour,0,randNodeInd-r);
		int[] toInsert = subset(tour,randNodeInd-r,2*r+1);
		mutTour = concat(mutTour,subset(tour,randNodeInd+r+1));
		
		for(int eleK : toInsert) {
			
			int firstInd = -1;
			float cheapestDeltaCost = Float.POSITIVE_INFINITY;
			
			for (int indI = 0; indI < mutTour.length; indI++) {
				int indJ = (indI + 1) % mutTour.length;
				float deltaCost = Main.costFunc(mutTour[indI], eleK) + Main.costFunc(eleK, mutTour[indJ]) - Main.costFunc(mutTour[indI], mutTour[indJ]);
				if(deltaCost < cheapestDeltaCost) {
					firstInd = indJ;
					cheapestDeltaCost = deltaCost;
				}
			}
			mutTour = splice(mutTour,eleK,firstInd);
		}
		
		if (new HashSet<Integer>((ArrayList<Integer>) Arrays.stream(mutTour).boxed().collect(Collectors.toList())).size() != Main.countNodes_ALL) {
			CustomException e = new CustomException("Invalid Tour by blockTypMut build!");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return null;
		}
		return mutTour;
	} // End Methode
	
	/*
	public Chromo crossOverAndMutation(){
	  
	  //Wähle parentA aus
	  Chromo parentA = pickParent();
	  int[] pathA = parentA.tour;
	  
	  //Wähle parentB aus
	  Chromo parentB = pickParent();
	  while(parentB.equals(parentA)){ //schau das nicht die selben zwei Eltern gezogen wurden
	    parentB = pickParent(); // ziehe neu wenn es die gleichen waren
	  }
	  int[] pathB = parentB.tour;
	  
	  
	  //pathA = subset(pathA,0,ceil(pathA.length/2)); // nimm ca. bis zur Hälfte von parentA
	  int min = ceil(pathA.length*0.2f);
	  int max = pathA.length - min;
	  pathA = subset( pathA, 0, (int)random(min, max+1) ); // nimm von index 0 bis zu einem zufälligen index von parentA, wobei es ein min und max gibt
	  
	  int[] newPath = concat(pathA,pathB);
	  newPath = removeDuplicates(newPath);
	  
	  // Mutiere neu entstandener Path
	  if(mutationRate > random(1)){// Mutiere nur wenn Wahrscheinlichkeit eintrifft
	    int i = (int)random(1,newPath.length-1);
	    int k = (int)random(i+1,newPath.length);
	    newPath = twoOptSwap(newPath,i,k); // Mutation mit 2-opt Swap
	  }
	  
	  // erzeuge das Kind und gib es zurück
	  Chromo child = new Chromo(newPath);
	  return child;
	}//End Methode
*/
	
	/*
	public int[] removeDuplicates(int[] array){// Function to remove duplicate from integer array
		  
		   LinkedHashSet<Integer> set = new LinkedHashSet<Integer>();
		 
		    // adding elements to LinkedHashSet
		    for (int i = 0; i < array.length; i++){
		      set.add(array[i]);
		    }
		   
		   ArrayList<Integer> arr = new ArrayList<Integer>(set);
		   int[] res = new int[arr.size()];
		   for (int i = 0; i < arr.size(); i++){
		     res[i] = arr.get(i);
		   }
		   
		   return res;     
	}//End Methode
	*/
	
	//------------------------------others-------------------------------//

	//check if input tour/cost is better than BEST EVER TOUR/COST if yes set it to input
	public void checkBEST_EVER_TOUR(int[] tour,float tourCost){
		if (tourCost < BEST_EVER_COST) {
			
			if(twoOptActive) {
				BEST_EVER_TOUR = TSP_Only_2opt.completeTwoOptSwap(tour, tourCost);
				BEST_EVER_COST = Main.calcTourCost(BEST_EVER_TOUR);
			}else {
				BEST_EVER_TOUR = tour.clone();
				BEST_EVER_COST = tourCost;
			}
			Main.algoLogger1.info("{"+millis()+","+BEST_EVER_COST+"},");
			println(programTag + " After " + round(millis() / 1000) + " s new BEST_EVER found at Generation "+ generationNumber);
		}
	}//End Methode
	
	//-----------------------------------2.TAB---------------------------------//
	
	// Das Chromosom das Objekt dieser Klasse stellt eine Lösung also ein Tour dar
	public class Chromo{
		public int[] tour;
		public float tourCost;
		public float fitness;
		  
		public Chromo(int[] tour){
		    this.tour = tour;
		    this.tourCost = Main.calcTourCost(this.tour);
		    this.fitness = this.calcFitness();
		}//Ende Construktor
		 
		// sogenannte Fitness Function, gibt Fitness Wert als float zurück
		private float calcFitness(){
			return (float)Main.countNodes_ALL * 0.1f / tourCost;
		}//End Methode
		
		public String toString(){
			return "f "+this.fitness;    
		}// Ende Methode  
	}//End Klasse combSolution

	// Used for sorting in ascending order of Fitness
	public class sortByFitness implements Comparator<Chromo>{
	    public int compare(Chromo a, Chromo b){
	      int res = 0;
	      float diff = a.fitness - b.fitness;
	      
	      if(diff < 0){
	        res = -1;
	      }else if(diff > 0){
	        res = 1;
	      }else{
	        res = 0;
	      }      
	      return res;      
	    }
	}//Ende Klasse sortByFitness
		
} // End Algorithm
