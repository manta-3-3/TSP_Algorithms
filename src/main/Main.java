package main;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import processing.core.PApplet;
import processing.core.PVector;
import processing.data.IntList;

public class Main {

	//Name dieser Klasse für Konsolen-Output
	public final static String programTag = "[MAIN] ";
	
	public static Logger algoLogger1 = Logger.getLogger("TSP_Log1");
	public static Logger algoLogger2 = Logger.getLogger("TSP_Log2");

	//Höhe und Breite von jedem Frame
	public final static int width_ALL = 1680; // 1260
	public final static int height_ALL = 980; // 840
	//Position und Höhe & Breite des Bereiches in dem die Knoten gezeichnet werden können
	public final static PVector[] fieldDimensions_ALL = { new PVector(20, 150), new PVector(width_ALL - 2 * 20 - 390, height_ALL - 150 - 20) }; // sollten Ints sein
	//Alternatives kleineres Feld: public final static PVector[] fieldDimensions_ALL = { new PVector(20, 150), new PVector(700,500) };

	//Anzahl der Knoten
	public static int countNodes_ALL;
	//Koordinaten der Knoten, wobei die Koordinaten von Knoten k an Index k im Array gespeichert sind
	public static PVector[] randomPointCords_ALL;
	
	//Enthält optimal gelöste Tour, wenn verfügbar
	public static int[] OPTIMAL_TOUR;
	//Enthält Kosten der optimal gelöste Tour, wenn verfügbar
	public static float OPTIMAL_COST;
	
	
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main_GUI setup_frame = new Main_GUI();
					setup_frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		initLogger();
		
	} // End of MAIN Methode

	public static void startAlgos(ArrayList<String> algoNames, int countCities,String mode) {

		if (algoNames.isEmpty()) {
			CustomException e = new CustomException("No Algos selected");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return;
		}
		if (countCities < 5 || countCities > 250) {
			CustomException e = new CustomException("No valid Count of Vertices");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return;
		}
		
	
		if(mode.equals("RANDOM")) {
			countNodes_ALL = countCities;
			randomPointCords_ALL = generateRandomPointCords(countNodes_ALL);
			System.out.println(programTag + countNodes_ALL+"-city TSP instance with random Integer Cords are successfull generated!");
			
		}else if(mode.equals("ch130.txt")) {
			PVector[] randomPointCords_ch150 = createPointCordsFromFile(new File("TSP_Data/"+mode));
			countNodes_ALL = randomPointCords_ch150.length;
			randomPointCords_ALL = scaleTransformPointCords(randomPointCords_ch150,new PVector(300,900),1);
			OPTIMAL_TOUR = createOptimalTourFromFile(new File("TSP_Data/ch130.opt.txt"));
			OPTIMAL_COST = calcTourCost(OPTIMAL_TOUR);
			System.out.println(programTag+"Optimal Tour-Cost: "+OPTIMAL_COST);
			System.out.println(programTag + countNodes_ALL+"-city TSP instance from file: "+mode+" successfull loaded!");
			
		}else if(mode.equals("ch150.txt")) {
			PVector[] randomPointCords_ch150 = createPointCordsFromFile(new File("TSP_Data/"+mode));
			countNodes_ALL = randomPointCords_ch150.length;
			randomPointCords_ALL = scaleTransformPointCords(randomPointCords_ch150,new PVector(300,900),1);
			System.out.println(programTag + countNodes_ALL+"-city TSP instance from file: "+mode+" successfull loaded!");
			
		}else if(mode.equals("dantzig42.txt")) {//TODO: meine verwendete Metrik passt nicht zu dieser Instanz
			PVector[] randomPointCords_dantzig42 = createPointCordsFromFile(new File("TSP_Data/"+mode));
			countNodes_ALL = randomPointCords_dantzig42.length;
			randomPointCords_ALL = scaleTransformPointCords(randomPointCords_dantzig42,new PVector(100,850),6);
			System.out.println(programTag + countNodes_ALL+"-city TSP instance from file: "+mode+" successfull loaded!");
			
		}else {
			CustomException e = new CustomException("Invalid Mode selected");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return;
		}
		
		System.out.println(programTag + "Selected Programs started!");
		System.out.println("-------------------------Setup completed-----------------------------");
		
		for (String name : algoNames) {
			PApplet.main("algorithms." + name);
		}
		
		//Kommentar-// entfernen um diese einzeln aufzurufen
		//PApplet.main("algorithms.Graph_Algorithms");
		//PApplet.main("algorithms.LinearProgramming_SimplexAlgorithmExample");
		//PApplet.main("algorithms.TSP_SmallestEdges");
		//PApplet.main("algorithms.TSP_HeldKarp_lowerBound");

	} // End Methode

	public static void initLogger() {
		
		class MyCustomFormatter extends Formatter {	 
	        @Override
	        public String format(LogRecord record) {
	            StringBuffer sb = new StringBuffer();
	            sb.append(record.getMessage());
	            sb.append("\n");
	            return sb.toString();
	        }   
	    }
		
		// This block configure the algoLogger1 with handler and formatter
		try {  
			FileHandler fh = new FileHandler("TSP_Data/localTSPlogFile1.log");  
			algoLogger1.addHandler(fh);  
	        fh.setFormatter(new MyCustomFormatter());
	        algoLogger1.setUseParentHandlers(false);
	        
	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }
		System.out.println(programTag + "Logger1 created!");
		
		
		// This block configure the algoLogger2 with handler and formatter
		try {  
			FileHandler fh = new FileHandler("TSP_Data/localTSPlogFile2.log");  
			algoLogger2.addHandler(fh);  
	        fh.setFormatter(new MyCustomFormatter());
	        algoLogger2.setUseParentHandlers(false);
    
	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }
		System.out.println(programTag + "Logger2 created!");
		
	} // End Methode
	
	// --------------------------------------------------//

	// Erzeuge n zufällige Punkte im Feld mit ganzzahligen Koordinaten
	public static PVector[] generateRandomPointCords(int n) {															
		PVector[] res = new PVector[n];
		for (int i = 0; i < n; i++) {
			int x = randInt((int) fieldDimensions_ALL[0].x,(int) (fieldDimensions_ALL[0].x + fieldDimensions_ALL[1].x));
			int y = randInt((int) fieldDimensions_ALL[0].y,(int) (fieldDimensions_ALL[0].y + fieldDimensions_ALL[1].y));
			res[i] = new PVector(x, y);
		}
		return res;
	} // End Methode

	// Definiert die Kostenfunktion zwischen zwei Knoten A und B mittels euklidischer Metrik
	public static float costFunc(int nodeA, int nodeB) {
		if (nodeA < 0 || nodeB < 0) {
			CustomException e = new CustomException("NodeNumber must be >= 0");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return -1;
		}
		return PApplet.dist(randomPointCords_ALL[nodeA].x, randomPointCords_ALL[nodeA].y, randomPointCords_ALL[nodeB].x,randomPointCords_ALL[nodeB].y);	
	} // End Methode

	// Berechnet die gesamt Kosten einer Tour
	public static float calcTourCost(int[] tour) {
		float totalCost = 0;
		for (int i = 0; i < tour.length; i++) {
			int nextIndex = (i + 1) % tour.length;
			totalCost += costFunc(tour[i], tour[nextIndex]);
		}
		return totalCost;
	} // End Methode
	
	// Erzeuge eine zufällige Tour indem alle Integers im Intervall [0,countNodes_ALL] zufällig angeordnet werden
	public static int[] generateRandomTour() {
		IntList shuffledIntegers = new IntList() {
			{
				resize(countNodes_ALL);
				IntStream.range(0, countNodes_ALL).forEach(n -> add(n, n));
			}
		};
		shuffledIntegers.shuffle();
		return shuffledIntegers.array();
	}// End Methode
	
	// --------------- Other Utility Methodes -----------//

	// Gibt eine züfälliges Float zwischen min und max zurück
	public static float randFloat(float min, float max) {
		Random rand = new Random();
		return rand.nextFloat() * (max - min) + min;
	} // End Methode

	// Gibt eine züfälliges positives Int zwischen min(inclusive) und max(exclusive) zurück
	public static int randInt(int min, int max) { 
													
		if (min < 0 || max < 0) {
			CustomException e = new CustomException("min and max value must be positive or 0");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return -1;
		} else if (min > max) {
			CustomException e = new CustomException("min value must be smaller than max value");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return -1;
		} else if (min == max) {
			/*//TODO
			CustomException e = new CustomException("min and max value should be unequal");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			*/
			// return then the value either
			return min;
		}
		Random rand = new Random();
		return rand.nextInt(max - min) + min;
		
		//Alternativ:
		//new Random().ints(min, max).findFirst().getAsInt(); min(inclusive) und max(exclusive)
		
	} // End Methode

	// Berechne die Fakultät von num (Achtung: es gibt fehler wenn max Value von long überschritten wird)
	public static final long factorial(int num) {
		return num == 1 ? 1 : factorial(num - 1) * num;
	}
	
	// Berechnet die Fakultät von num bei sehr grossen Resultaten die nicht mehr von long gespeichert werden könne
	public static final BigInteger factorialBigNum(BigInteger num) {
		return num.intValue() == 1 ? new BigInteger("1") : factorialBigNum(num.add(new BigInteger("-1"))).multiply(num);
	}

	// Berechnet den Binominal Koeffizienten
	// Code von: https://www.geeksforgeeks.org/binomial-coefficient-dp-9/
	public static int binomialCoeff(int n, int k) {
		int C[][] = new int[n + 1][k + 1];
		int i, j;

		// Calculate value of Binomial
		// Coefficient in bottom up manner
		for (i = 0; i <= n; i++) {
			for (j = 0; j <= PApplet.min(i, k); j++) {
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
	} // End Methode
	
	// Gibt den Index zurück der von der weightItems mit gewichtetem Zufall bestimmt wurde
	public static int computeWeightedRandomPick(float[] weightItems) {

		int res = 0;
		// Compute the total weight of all items together.
		// This can be skipped of course if sum is already 1.
		double totalWeight = 0d;
		for (float i : weightItems) {
			totalWeight += (double) i;
		}

		// check if not all probabilitys are 0, if yes throw exeption
		if (totalWeight == 0d) {
			CustomException e = new CustomException("All probabilities are zero!");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return -1;
		}

		// Now choose a random item.
		for (double r = Math.random() * totalWeight; res < weightItems.length - 1; res++) {
			r -= (double) weightItems[res];
			if (r <= 0d)
				break;
		}

		// check before returning if picked prob is really not zero
		if (weightItems[res] == 0d) {
			CustomException e = new CustomException("Probability of zero picked!");
			System.err.println("Exception: " + e.getMessage() + " at " + e.getStackTrace()[0]);
			e = null;
			return -1;
		}

		return res;

	}// End Methode
	
	// ---------------------------------------------------//
	
	public static PVector[] createPointCordsFromFile(File inputFile) {

		ArrayList<PVector> res = new ArrayList<PVector>();

		// We need to provide file path as the parameter:
		// double backquote is to avoid compiler interpret words
		// like \test as \t (ie. as a escape sequence)

		try {

			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String st;

			findStart: while ((st = br.readLine()) != null) {
				if (st.equals("DISPLAY_DATA_SECTION")) {
					break findStart;
				}
			}

			lineReading: while ((st = br.readLine()) != null) {

				if (st.equals("EOF")) {
					break lineReading;
				}

				Scanner sc = new Scanner(st);
				ArrayList<Float> li = new ArrayList<Float>();
				while (sc.hasNextFloat()) {
					li.add(sc.nextFloat());
				}
				li.remove(0);
				res.add(new PVector(li.get(0), li.get(1))); // create new PointCord via PVector
				li = null;
				sc.close();

			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return res.toArray(new PVector[0]);
	} // End Methode

	public static PVector[] scaleTransformPointCords(PVector[] input, PVector transformVek, float scaleVal) {
		// float yRotationCord = fieldDimensions_ALL[0].y + fieldDimensions_ALL[0].y/2;
		for (int i = 0; i < input.length; i++) {
			input[i].mult(scaleVal);
			input[i].y = input[i].y * -1;
			input[i].add(transformVek);
		}
		return input;
	} // End Methode

	public static int[] createOptimalTourFromFile(File inputFile){
		ArrayList<Integer> tour = new ArrayList<Integer>();

		// We need to provide file path as the parameter:
		// double backquote is to avoid compiler interpret words
		// like \test as \t (ie. as a escape sequence)

		try {

			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String st;

			findStart: while ((st = br.readLine()) != null) {
				if (st.equals("TOUR_SECTION")) {
					break findStart;
				}
			}

			lineReading: while ((st = br.readLine()) != null) {

				if (st.equals("EOF")) {
					break lineReading;
				}

				Scanner sc = new Scanner(st);
				int readNumb = sc.nextInt();
				if(readNumb == -1) {
					break lineReading;
				}
				tour.add(readNumb-1);
				sc.close();

			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return tour.stream().mapToInt(Integer::intValue).toArray();
	} // End Methode
	
} // End Main
