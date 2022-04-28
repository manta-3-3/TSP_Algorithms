package algorithms;

import processing.core.*;
import processing.data.IntList;

import java.util.ArrayList;
import java.util.Collections;

import main.Main;

public class TSP_AntColonyOptimization extends PApplet {

	public final String programTag = "[TSP_AntColonyOptimization]";

	public final int countCities = Main.countCities_ALL; //Anzahl Cities
	public final int countAntsPerGeneration = Math.round(countCities*0.75f); //Anzahl Ants pro Generation -> countAntsPerGeneration <= countCities
	public int antGenerationNumber;

	public static int[] BEST_EVER_PATH;
	public static float BEST_EVER_DIST;

	public final PVector[] fieldDimensions = Main.fieldDimensions_ALL; // Feld in dem die Punkte/Cities gezeichnet also liegen können

	public final int[] colorTable = {color(255, 0, 0),color(0, 255, 0),color(0, 0, 255)}; // Hat Farben ROT, GRÜN und BLAU gespeichert

	public PVector[] randomPointCords = Main.randomPointCords_ALL;// List contains coordinates of city at cityIndex k
	public city[] cities; //List were all city objects are saved, city at cityIndex k is at index k of this array
	public ant[] ants; //List were all ant objects are saved

	public int PHASE_MODE; // definiert in welcher Phase man ist 0:Ants erzeugen Weg, 1:Auswerten Weg und Pheremone update
	public final boolean twoOptActive = true; // wenn true mit 2-opt wenn false ohne

	public void settings() {
		size(Main.width_ALL, Main.height_ALL);
	}
	
	public void setup(){
		surface.setLocation(-10, 0);
		frameRate(100);
	  /*
	  size(1260,840,P2D);
	  fieldDimensions[0] = new PVector(20, 150); //Vector top left corner cord of rect
	  fieldDimensions[1] = new PVector(width-2*20, height-150-20); //Vector width & height of rect
	  
	  frameRate(200);
	  
	  randomPointCords = generateRandomPointCords(countCities);
	  */
	  cities = generateCities(countCities);
	  ants = generateAnts(countAntsPerGeneration);
	  
	  BEST_EVER_PATH = generateRandomSolution();
	  BEST_EVER_DIST = Float.POSITIVE_INFINITY;
	  
	  antGenerationNumber = 1; // Generation 1 ist geboren
	  PHASE_MODE = 0; // setze Modus auf Weg Finden der Ameisen zu Beginn
	}

	public void draw(){
	  
	  background(0);
	  drawField();
	  drawCities();
	  //drawAnts();
	  //drawAntPaths();
	  drawBestEverPath();
	  
	  drawAllText();
	  calculationThread();
	}

	public void calculationThread(){
	  
	  if (PHASE_MODE == 0){
	    moveAntsOneStep();
	  }
	   
	  if (PHASE_MODE == 1){
	    
	    evaporatePheromones();
	    
	    scoreAntTrailsWithPheromone();   
	    
	    initNextGenerationAnt();
	    
	  }  
	  
	}//Ende Methode

	//------------------------------------------------------------//

	public void drawAllText(){
	  
	  textSize(20);
	  
	  fill(255);
	  text("Anzahl Punkte/Cities: "+countCities,0,20);
	  text("Ants per Generation: "+countAntsPerGeneration,0,40);
	  text("Ant Generation "+antGenerationNumber,0,60);
	  //text("Aktueller Modus: "+PHASE_MODE,10,80);
	  text("TwoOptActive: "+twoOptActive,0,80);
	  text("Best Distance: "+BEST_EVER_DIST+" km",0,120);
	  
	  fill(255,0,0);
	  text("FPS: "+frameRate,width-170,20);
	  text("Run-Time: "+(float)millis()/1000+" s",width-500,20);
	  
	}//End Methode

	public PVector[] generateRandomPointCords(int anzahl){
	  
	  PVector[] res = new PVector[anzahl];  
	  for(int i = 0; i < anzahl; i++){    
	    res[i] = new PVector(random(fieldDimensions[0].x,fieldDimensions[0].x+fieldDimensions[1].x),random(fieldDimensions[0].y,fieldDimensions[0].y+fieldDimensions[1].y));    
	  }  
	  return res;
	  
	}//End Methode

	public city[] generateCities(int anzahl){
	  
	  city[] res = new city[anzahl];
	  
	  for (int i = 0; i < res.length; i++){
	    
	    res[i] = new city(i);
	    
	  }  
	  return res;  
	}//End Methode

	public ant[] generateAnts(int anzahl){ //places ants at random startCity but never more than one ant on same city
	       
	  ArrayList<Integer> shuffledIntegers = new ArrayList<Integer>();
	  for(int i = 0; i < countCities; i++){
	    shuffledIntegers.add(i);
	  }
	  Collections.shuffle(shuffledIntegers);
	  
	  ant[] res = new ant[anzahl];    
	  for(int i = 0; i < res.length; i++){
	    
	    res[i] = new ant(shuffledIntegers.get(i),colorTable[0]);
	  }
	    
	  return res;
	}//End Methode

	public int[] generateRandomSolution(){
	  
	  IntList shuffledIntList = new IntList();
	  shuffledIntList.resize(countCities);
	  for(int i = 0; i < countCities; i++){
	    shuffledIntList.add(i,i);
	  }
	  shuffledIntList.shuffle();
	  
	  return shuffledIntList.array();
	}//End Methode

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

	public void drawAnts(){
	  
	  for(ant ele: ants){    
	    fill(255,0,0);
	    stroke(255,0,0);
	    circle(randomPointCords[ele.currentCityIndex].x,randomPointCords[ele.currentCityIndex].y,8); //zeichne ant mit ausgefülltem roten Kreis an aktuellem Standort
	    
	    noFill();
	    stroke(0,0,255);
	    strokeWeight(2);
	    circle(randomPointCords[ele.startCityIndex].x,randomPointCords[ele.startCityIndex].y,20); //zeichne nichtausgefüllter blauer Kreis von ant Startpunkt
	  }
	  
	}//End Methode

	public void drawAntPaths(){
	  
	  for(ant eleAnt:ants){
	    
	    strokeWeight(1);
	    stroke(eleAnt.pathColor);
	    
	    beginShape();
	    for(int eleInt: eleAnt.currentPath){      
	      vertex(randomPointCords[eleInt].x,randomPointCords[eleInt].y);      
	    }
	    endShape(CLOSE);
	    
	  }
	  
	}//End Methode

	public void drawBestEverPath(){
	  
	  strokeWeight(2);
	  stroke(255);
	    
	  beginShape();
	  for(int i = 0; i < BEST_EVER_PATH.length; i++){      
	    vertex(randomPointCords[BEST_EVER_PATH[i]].x,randomPointCords[BEST_EVER_PATH[i]].y);      
	  }
	  endShape(CLOSE); 
	         
	}//End Methode

	public void moveAntsOneStep(){
	  
	  for(ant ele:ants){    
	    ele.calcNextMove();    
	  }
	  
	}//End Methode

	public void evaporatePheromones(){
	  
	  for(city eleCity: cities){
	    
	    for(int i = 0; i < eleCity.pheremoneTrailsToOtherCities.length; i++ ){
	      eleCity.pheremoneTrailsToOtherCities[i] = (1-evaporationRate)*eleCity.pheremoneTrailsToOtherCities[i];
	    }    
	  }
	  
	}//End Methode

	public void scoreAntTrailsWithPheromone(){ //scoreAntTrails and BEST EVER PATH;
	  
	  //updates pheromone Level for each ant
	  for(ant ele:ants){
	    ele.updatePheromone();                    
	  }

	  /*
	  // updates pheromone Level of BEST_EVER_PATH
	  float pheromoneToAdd = 1/BEST_EVER_DIST;
	    
	  for(int i = 0; i < BEST_EVER_PATH.length ; i++){      
	    int nextIndex = (i+1) % BEST_EVER_PATH.length;
	      
	    int cityIndexA = BEST_EVER_PATH[i];
	    int cityIndexB = BEST_EVER_PATH[nextIndex];
	      
	    cities[cityIndexA].pheremoneTrailsToOtherCities[cityIndexB] += pheromoneToAdd;  
	    cities[cityIndexB].pheremoneTrailsToOtherCities[cityIndexA] += pheromoneToAdd; // Also to reverse connection when symmetric TSP      
	  }
	  */
	   
	}//End Methode

	public void initNextGenerationAnt(){
	  
	  ants = generateAnts(countAntsPerGeneration); // starte nexte Generation von Ants
	  antGenerationNumber++;
	  PHASE_MODE = 0; //starte wieder bei Phase 0
	  
	  
	}

	public void checkBEST_EVER_PATH_DISTANCE(int[] inputPath,float inputDistance){ // check inpute Path/Distance ist better than BEST EVER PATH/DISTANCE if yes set it to input
	    
	  if(inputDistance < BEST_EVER_DIST){
	    
	    int[] improvedPath = inputPath;
	    
	    if(twoOptActive){ // mache den 2-opt Improve nur wenn aktiv
	      
	     for(int i = 0; i < floor((countCities*0.7f)); i++){
	       improvedPath = completeTwoOpt(improvedPath,calcTotalPathDist(improvedPath)); // do (countCities*0.7) times an inprovement to inputPath with 2-opt Algorithmus
	     }
	    
	     println(programTag+" newBestImproved at Ant-Generation "+antGenerationNumber);
	    
	    }
	    
	    BEST_EVER_PATH = improvedPath;    
	    BEST_EVER_DIST = calcTotalPathDist(improvedPath);
	       
	    //BEST_EVER_PATH = inputPath;    
	    //BEST_EVER_DIST = inputDistance;
	  }
	  
	}//End Methode

	public float calcTotalPathDist(int[] path){
	  
	   float totalDist = 0;
	   
	   for(int i = 0; i < path.length; i++){      
	      int nextIndex = (i+1) % path.length;
	      totalDist += dist(randomPointCords[path[i]].x,randomPointCords[path[i]].y,randomPointCords[path[nextIndex]].x,randomPointCords[path[nextIndex]].y);
	    }
	   
	   return totalDist;   
	}//End Methode

	public int[] convertIntegerArrayListToArray(ArrayList<Integer> input){
	  
	  int[] res = new int[input.size()];
	  
	  for(int i = 0; i < res.length; i++){
	    res[i] = input.get(i);
	  }
	  
	  return res;
	}//End Methode

	public void improveBEST_EVER(){
	  
	  int[] newPath = completeTwoOpt(BEST_EVER_PATH,BEST_EVER_DIST);
	  
	  BEST_EVER_PATH = newPath;
	  BEST_EVER_DIST = calcTotalPathDist(BEST_EVER_PATH);
	}

	//--------------------2.TAB: ant_class---------------------------------------//
	
	public class ant{
		  int pathColor;
		  int startCityIndex; // the ant starts his way at this cityIndex
		  int currentCityIndex; // cointains the current CityIndex where the ant is at the moment
		  ArrayList<Integer> currentPath;
		  boolean[] visidedCities; // an Array of booleans witch shows if city at index k is already visited (True)
		  
		  ant(int startCityIndex, int pathColor){ //Counstructor of ant Class
		    
		    this.pathColor = pathColor; 
		    this.startCityIndex = startCityIndex;
		    this.currentCityIndex = this.startCityIndex;
		    this.visidedCities = defaultInitVisidedCities();
		    
		    this.currentPath = new ArrayList<Integer>(1);
		    this.currentPath.add(startCityIndex); //Add its starting Point to currentPath
		  }
		  
		  private boolean[] defaultInitVisidedCities(){ // inits all visidedCities to False exept the city where the ant starts (startCityIndex)
		    
		    boolean[] res = new boolean[countCities];
		    
		    for(int i = 0; i < res.length; i++){
		      
		      res[i] = false;
		      
		    }
		    
		    res[startCityIndex] = true; // setze den StartCityIndex auf schon besucht
		    
		    return res;
		  }//End Methode


		  private void calcNextMove(){ // calcs next move to another not visided city based on desirability
		    
		    float[] probabilityToOtherCities = new float[countCities]; // List contains the desirability to another city at cityIndex k (if city is already visided its 0)
		    
		    for(int i = 0; i < visidedCities.length; i++){
		      
		      if(!visidedCities[i]){        
		        float dst = cities[currentCityIndex].distancesToOtherCities[i];
		        float pheromoneStrength = cities[currentCityIndex].pheremoneTrailsToOtherCities[i];
		        
		        probabilityToOtherCities[i] = pow(1/dst,distancePower)*pow(pheromoneStrength,pheromonePower);        
		      }
		      else{
		        probabilityToOtherCities[i] = 0;       
		      }        
		    }
		                        
		    /*float maxDesirability = Float.NEGATIVE_INFINITY; //takes the city with the highest Desirability (alte Methode)
		    int indexMaxDesirability = 0;    
		    for(int i = 0; i < probabilityToOtherCities.length; i++){      
		      if (maxDesirability < probabilityToOtherCities[i]) 
		      {
		        maxDesirability = probabilityToOtherCities[i];
		        indexMaxDesirability = i;
		      }      
		    }
		    */
		    
		    int pickedDesirabilityIndex = pickRandomDesirability(probabilityToOtherCities);        
		    
		    boolean areAllVisited = false; //prüfe ob schon alle Cities besucht wurden
		    for(boolean b : visidedCities){
		      if(!b){
		        areAllVisited = false; // noch nicht alle Cities sind besucht worden
		        break;
		      }else{
		        areAllVisited = true; // alle Cities sind besucht worden 
		      }
		    }
		    
		    if(areAllVisited){ // wenn areAllVisited = true; Ameise hat alle Städe besucht beginne mit Phase 1
		    
		      //println("Solution finished");
		      //println(this.currentPath);
		      //this.currentCityIndex = this.startCityIndex;
		      //this.currentPath.add(this.startCityIndex);
		      //println(calcTotalPathDistance());
		      PHASE_MODE = 1; // leite Phase 1 ein
		      
		    }
		    else{      
		      this.currentCityIndex = pickedDesirabilityIndex; //moves the ant to the the calculated city
		      this.currentPath.add(pickedDesirabilityIndex); // adds this point to path
		      this.visidedCities[pickedDesirabilityIndex] = true; //sets the city witch its moved to to visided true
		      PHASE_MODE = 0; //setze auf Phase 0 um sicherzugehen
		    }    
		            
		  }//End Methode
		  
		  private int pickRandomDesirability(float[] weightItems){
		    
		    int res = 0;
		    
		    // Compute the total weight of all items together.
		    // This can be skipped of course if sum is already 1.
		    float totalWeight = 0;
		    for (float i : weightItems) {
		      totalWeight += i;
		    }
		    
		    // Now choose a random item.
		    for (double r = Math.random() * totalWeight; res < weightItems.length - 1; ++res) {
		      r -= weightItems[res];
		      if (r <= 0.0) break;
		    }
		    
		    return res;
		  }//End Methode
		     
		  private void updatePheromone(){ // updates the pheromone Level of the trail witch this ant took
		    
		    int[] finishedTourPath = convertIntegerArrayListToArray(this.currentPath);
		    float finishedTourDistance = calcTotalPathDist(finishedTourPath);
		    float pheromoneToAdd = Q/finishedTourDistance;
		    
		    for(int i = 0; i < this.currentPath.size(); i++){      
		      int nextIndex = (i+1) % this.currentPath.size();
		      
		      int cityIndexA = this.currentPath.get(i);
		      int cityIndexB = this.currentPath.get(nextIndex);
		      
		      cities[cityIndexA].pheremoneTrailsToOtherCities[cityIndexB] += pheromoneToAdd;  
		      cities[cityIndexB].pheremoneTrailsToOtherCities[cityIndexA] += pheromoneToAdd; // Also to reverse connection when symmetric TSP
		      
		    }// updates pheromone Level of visited edges
		    
		    checkBEST_EVER_PATH_DISTANCE(finishedTourPath,finishedTourDistance); //schaue ob neue BEST EVER PATH/DISTANCE gefunden wurde                   
		    
		  }//End Methode
		          
		}//End Class
	
	//--------------------3.TAB: city_class--------------------------------------//
	
	public final float pheromonePower = 1; // sollte >= 0 sein
	public final float distancePower = 2; // sollte >= 1 sein
	public final float evaporationRate = 0.5f;
	public final float Q = 2;

	public class city{

	int cityIndex; // of this city
	//PVector position; // x and y Cord of City (represented by Point) also saved in randomPoints[cityIndex]

	float[] distancesToOtherCities; // when calling Array with index k, it gives you Distance from this City to the City witch has k as cityIndex 
	float[] pheremoneTrailsToOtherCities; // when calling Array with index k, it gives you pheremoneTrail Strength from this City to the City witch has k as cityIndex
	  
	  city(int cityIndex){ //Counstructor of city Class
	    
	    this.cityIndex = cityIndex;
	    
	    this.distancesToOtherCities = calcDistancesToOtherCities();
	    this.pheremoneTrailsToOtherCities = initPheremoneTrailsToOtherCities();
	    
	  }//End Counstructor
	  
	  
	  
	  private float[] calcDistancesToOtherCities(){
	  
	    float[] res = new float[countCities];
	    
	    for(int i = 0; i < res.length; i++){
	      
	      res[i] = dist(randomPointCords[cityIndex].x,randomPointCords[cityIndex].y,randomPointCords[i].x,randomPointCords[i].y);
	      //calcDistance(cityIndex,i);
	      
	    }        
	    return res;
	  }//End Methode
	  
	  private float[] initPheremoneTrailsToOtherCities(){ // sets all PheremoneTrails to Strength of 1 at the Beginning
	  
	    float[] res = new float[countCities];
	    
	    for(int i = 0; i < res.length; i++){
	      
	      res[i] = 1;
	      
	    }
	    res[cityIndex] = 0;
	    
	    return res;
	  }//End Methode
	      
	}//End Class
	
	//--------------------4.TAB:two_opt_swap-------------------------------------//
	
	public int[] twoOptSwap(int[] route,int i,int k){
		  
		  int[] res = subset(route,0,i);
		  
		  res = concat(res,reverse(subset(route,i,k-i+1)));
		   
		  res = concat(res,subset(route,k+1,route.length-1-k));
		  
		  return res;  
	}//End Methode

	public int[] completeTwoOpt(int[] path, float pathDistance){ // gibt das beste Resultat nach 2-opt als path zurück
		  
		  float localBestDistance = pathDistance;
		  int[] localBestPath = path;
		  
		  for (int i = 1; i < path.length-1; i++){
		    for(int k = i+1; k < path.length; k++){        
		      
		      int[] newPath = twoOptSwap(path,i,k);
		      float newDistance = calcTotalPathDist(newPath);      
		      
		      if(newDistance < localBestDistance){      
		        localBestDistance = newDistance;
		        localBestPath = newPath; 
		      }      
		    }
		  }
		  
		  return localBestPath;
	}//End Methode
	
} // End Algorithm
