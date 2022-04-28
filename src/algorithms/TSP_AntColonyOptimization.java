package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

import main.CustomException;
import main.Main;
import processing.core.PApplet;

public class TSP_AntColonyOptimization extends PApplet {

	//Name dieser Klasse für Konsolen-Output
	public final String programTag = "[TSP_AntColonyOptimization] ";																							
	
	public final boolean twoOptActive = false; //TODO: out of order now
	
	// 80% von countCities, AnzallAnzahl Ants pro Generation -> countAntsPerGeneration <= countCities
	public final int countAntsPerGeneration = Math.round(Main.countNodes_ALL * 0.8f);
	
	public final boolean acsActive = true; // wenn true mit Erweiterung zu Ant Colony System (ACS)
	
	public final float initialPheromon = 1f;
	public final float pheromonePower_alpha = 1f; // sollte >= 0 sein
	public final float distancePower_beta = 4f; // sollte >= 1 sein
	public final float globalEvaporationRate = 0.25f; // sollte ]0,1[ sein
	public final float localEvaporationRate = 0.05f; // sollte ]0,1] sein (nur bei ACS verwendet)
	public final float Q = 2f;
	
	public int antGenerationNumber = 0;
																																		
	// List were all ant objects are saved
	public ant[] ants;
	// List were all city objects are saved, city at cityIndex k is at index k of this array
	public city[] cities;				

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

		Main.algoLogger1.info(programTag);
		
		cities = generateCities(Main.countNodes_ALL);
		
		BEST_EVER_TOUR = Main.generateRandomTour();
		BEST_EVER_COST = Main.calcTourCost(BEST_EVER_TOUR);
		Main.algoLogger1.info("Initial Tour-Cost: {"+millis()+","+BEST_EVER_COST+"}");
		
		Main.algoLogger1.info("-------------------------");
		
		readyForNextIter = true;
		
		println(programTag+"Setup complete!: Initial Pheromon-Level: "+initialPheromon);
	}

	public void draw() {

		if(readyForNextIter) {
			thread("solveThread");
			readyForNextIter = false;
		}

		background(0);
		drawField();
		drawCities(color(0,255,0),false);
		drawTour(BEST_EVER_TOUR,2,color(255));
		drawAllText();
	}

	// 1 execution of this thread is 1 iteration (new Ant Gen.)
	public void solveThread() {

		//Each ant is positioned on a starting node
		ants = generateAnts(countAntsPerGeneration);
		
		// Ants are building the path, as long as not all cities have been visited
		for(int i = 0; i < Main.countNodes_ALL-1; i++) {
			moveAntsOneStep();
		}
		
		// A pheromone updating rule is applied
		evaporatePheromones();
		scoreAntTrailsWithPheromone();
		
		antGenerationNumber++;
		
		readyForNextIter = true;
	}// Ende Methode

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
	
	public void drawAllText() {

		textSize(20);
		fill(255);
		text("Anzahl Punkte/Cities: " + Main.countNodes_ALL, 0, 20);
		text("Ants per Generation: " + countAntsPerGeneration, 0, 40);
		text("Ant Generation " + antGenerationNumber, 0, 60);
		text("Best Distance: " + BEST_EVER_COST+ " km", 0, 100);
		
		textSize(15);
		fill(220);
		text("ACS active: "+acsActive,400,15);
		text("TwoOpt active: " + twoOptActive, 400, 30);
		text("initial Pheromon: " + initialPheromon, 400, 45);
		text("pheromonePower_alpha: " + pheromonePower_alpha, 400, 60);
		text("distancePower_beta: " + distancePower_beta, 400, 75);
		text("global Evaporation Rate: " + globalEvaporationRate, 400, 90);
		text("local Evaporation Rate: " + localEvaporationRate, 400, 105);
		text("Q: " + Q, 400, 120);
		
		textSize(20);
		fill(255, 0, 0);
		text("FPS: " + frameRate, width - 170, 20);
		text("Run-Time: " + (float) millis() / 1000f + " s", width - 500, 20);

	}// End Methode

	//----------------------------------------------------------------------//
	
	public city[] generateCities(int anzahl) {
		city[] res = new city[anzahl];
		for (int i = 0; i < res.length; i++) {
			res[i] = new city(i);
		}
		return res;
	}// End Methode

	// places ants at random startCity (but never more than one ant on same city)
	public ant[] generateAnts(int anzahl) {
		ant[] res = new ant[anzahl];

		ArrayList<Integer> shuffledIntegers = new ArrayList<Integer>() {
			private static final long serialVersionUID = 1L;
			{
				IntStream.range(0, Main.countNodes_ALL).forEach(n -> add(n));
			}
		};
		Collections.shuffle(shuffledIntegers);

		for (int i = 0; i < res.length; i++) {
			res[i] = new ant(shuffledIntegers.get(i));
		}
		return res;
	}// End Methode

	public void moveAntsOneStep() {
		for (ant ele : ants) {
			ele.calcNextMove();
		}
	}// End Methode

	// (1-globalEvaporationRate)*initPheromone
	public void evaporatePheromones() {
		for (city ele : cities) {
			IntStream.range(0, Main.countNodes_ALL).forEach(n -> ele.changePheromoneValue(n, (1f - globalEvaporationRate)*ele.getPheromoneValue(n)));
		}
	}// End Methode
	
	// updates pheromone Level of the path which the ants took
	public void scoreAntTrailsWithPheromone() {
		for (ant ele : ants) {
			ele.updatePheromoneOnTakenTour();
		}
	}// End Methode

	// check if input tour/cost is better than BEST EVER TOUR/COST if yes set it to input
	public void checkBEST_EVER_TOUR(int[] tour, float tourCost) {
		if(tourCost < BEST_EVER_COST) {
			
			if(twoOptActive) {
				BEST_EVER_TOUR = TSP_Only_2opt.completeTwoOptSwap(tour, tourCost);
				BEST_EVER_COST = Main.calcTourCost(BEST_EVER_TOUR);
			}else {
				BEST_EVER_TOUR = tour.clone();
				BEST_EVER_COST = tourCost;
			}
			Main.algoLogger1.info("{"+millis()+","+BEST_EVER_COST+"},");
			println(programTag + " new Best Tour found at Ant-Generation " + antGenerationNumber);
		}
	}// End Methode

	// --------------------2.TAB: ant_class---------------------------------------//

	public class ant {
		
		//cointains the current CityIndex where the ant is at the moment
		int currentNode;
		// bei Index k ist der Vorrgängerknoten von k gespeichert
		Integer[] currentPath = new Integer[Main.countNodes_ALL];

		// Counstructor of ant Class
		public ant(int startingNode) { 
			this.currentNode = startingNode;
			this.currentPath[this.currentNode] = -1; // set at the starting-Node index -1 as root
		}

		// calcs next move to another not visided city based on their pick-probability
		// and performs local-pheromone update if ACS is turn on
		private void calcNextMove() {

			// List contains the desirability to another city at cityIndex k (if city is already visided its 0)
			float[] notNormedProbabilityToOtherCities = new float[Main.countNodes_ALL];

			for (int i = 0; i < currentPath.length; i++) {
				if (currentPath[i] == null) {
					//not visited yet
					float distStrength = 1f/(Main.costFunc(currentNode,i));
					float pheromoneStrength = cities[currentNode].getPheromoneValue(i);
					
					float probability = pow(pheromoneStrength,pheromonePower_alpha)*pow(distStrength,distancePower_beta);
					
					if(probability < Float.MIN_NORMAL) {
						probability = Float.MIN_NORMAL; // if prob is to small for float set it automatically to MIN_NORMAL
						//println("ZERO PROB FOUND! Phero: "+pheromoneStrength+" Dist: "+distStrength+probability );
					}
					notNormedProbabilityToOtherCities[i] = probability;
				} else {
					//already visited
					notNormedProbabilityToOtherCities[i] = 0f;
				}
			}
			int pickedNode = Main.computeWeightedRandomPick(notNormedProbabilityToOtherCities);
			
			if(this.currentPath[pickedNode] != null) { //throws error if tries to pick already picked node
				CustomException e = new CustomException("Tries to Override already visited Node!");
				System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
				e = null;
				return;
			}
			
			// performs local-pheromone update if ACS is turn on
			if(acsActive) {
				cities[currentNode].changePheromoneValue(pickedNode, (1f-localEvaporationRate)*cities[currentNode].getPheromoneValue(pickedNode)+(localEvaporationRate*initialPheromon));
				cities[pickedNode].changePheromoneValue(currentNode, (1f-localEvaporationRate)*cities[pickedNode].getPheromoneValue(currentNode)+(localEvaporationRate*initialPheromon)); // Also to revers-connection when symmetric TSP
			}
			
			this.currentPath[pickedNode] = this.currentNode; // set at the pickedNode Index the last currentNode
			this.currentNode = pickedNode; // moves the ant to the the calculated city
		
		}// End Methode
		
		// updates the pheromone Level of the path witch this ant took
		private void updatePheromoneOnTakenTour() { 

			//check first if ant has a complete Tour, if not throw error
			if(Arrays.asList(currentPath).contains(null)) {
				CustomException e = new CustomException("Incomplete Tour of ant found!");
				System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
				e = null;
				return;
			}
			
			//build the tour by backtracking and calc the total Tour-Cost
			ArrayList<Integer> tourPath = new ArrayList<Integer>();
			double totalTourCost = 0d;
			int currentIndex = currentNode;
			while(currentPath[currentIndex] != -1) {
				tourPath.add(0,currentIndex);
				totalTourCost += Main.costFunc(currentPath[currentIndex], currentIndex);			
				currentIndex = currentPath[currentIndex];
			}
			tourPath.add(0,currentIndex);
			totalTourCost += Main.costFunc(currentNode,currentIndex);
			
			// updates pheromone Level of used edges in the Tour
			float pheromoneToAdd = Q / (float)totalTourCost;
			for (int i = 0; i < tourPath.size(); i++) {
				int nextIndex = (i + 1) % tourPath.size();

				int cityIndexA = tourPath.get(i);
				int cityIndexB = tourPath.get(nextIndex);

				cities[cityIndexA].changePheromoneValue(cityIndexB,cities[cityIndexA].getPheromoneValue(cityIndexB)+pheromoneToAdd);
				cities[cityIndexB].changePheromoneValue(cityIndexA,cities[cityIndexB].getPheromoneValue(cityIndexA)+pheromoneToAdd); // Also to revers-connection when symmetric TSP
			}
			// schaue ob neue BEST EVER PATH/DISTANCE gefunden wurde	
			checkBEST_EVER_TOUR(tourPath.stream().mapToInt(Integer::valueOf).toArray(), (float)totalTourCost);																				
		}// End Methode		
		
	}// End Class

	// --------------------3.TAB: city_class--------------------------------------//

	public class city {
		int cityIndex; // of this city
		float[] pheremoneTrailsToOtherCities; // when calling Array with index k, it gives you pheremoneTrail Strength from this City to the City witch has k as cityIndex
		
		// Counstructor of city Class
		public city(int cityIndex) {

			this.cityIndex = cityIndex;
			this.pheremoneTrailsToOtherCities = initPheremoneTrailsToOtherCities();

		}// End Counstructor

		// returns PheromoneValue at index
		private float getPheromoneValue(int index) {
			return this.pheremoneTrailsToOtherCities[index];
		}// End Methode
		
		private void changePheromoneValue(int index,float pheromoneValue) {
			
			//can't change Pheromone Value to its own city, is then set to 0 automaticlly
			if(index == this.cityIndex) {
				this.pheremoneTrailsToOtherCities[index] = 0f;
				return;
			}
			
			// can't represent smaller float without risking it gets zero, is set then automaticlly to MIN_NORMAL
			if(pheromoneValue < Float.MIN_NORMAL) {
				this.pheremoneTrailsToOtherCities[index] = Float.MIN_NORMAL;
				return;
			}
			
			this.pheremoneTrailsToOtherCities[index] = pheromoneValue;
			
		}// End Methode
		
		// sets all PheremoneTrails to Strength of initialPheromon at the Beginning
		private float[] initPheremoneTrailsToOtherCities() {																
			float[] res = new float[Main.countNodes_ALL];
			for (int i = 0; i < res.length; i++) {
				res[i] = initialPheromon;
			}
			res[cityIndex] = 0;
			return res;
		}// End Methode

	}// End Class

} // End Algorithm
