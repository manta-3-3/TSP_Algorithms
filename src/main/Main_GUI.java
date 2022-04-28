package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class Main_GUI extends JFrame {
	
	public final static String programTag = "[Main_GUI] ";
	
	private static final long serialVersionUID = 1L;
	
	private JLabel title_TSP;
	private JLabel strich1_JLabel;
	private JLabel strich2_JLabel;
	private JSpinner countKnoten_JSpinner;
	private JLabel countKnoten_JLabel;
	private JComboBox<Object> comboBox_mode;
	
	private JCheckBox chckbxNewCheckBox_1;
	private JCheckBox chckbxNewCheckBox_2;
	
	private JCheckBox chckbxNewCheckBox_3;
	private JCheckBox chckbxNewCheckBox_4;
	private JCheckBox chckbxNewCheckBox_5;
	private JCheckBox chckbxNewCheckBox_6;
	private JCheckBox chckbxNewCheckBox_7;
	
	//private JCheckBox chckbxNewCheckBox_8;
	//private JCheckBox chckbxNewCheckBox_9;
	
	private JCheckBox chckbxNewCheckBox_10;
	
	
	private JToggleButton start_stop_JToggleButton;

	/**
	 * Launch the application via the Main class
	 */

	/**
	 * Create the application.
	 */
	public Main_GUI() {
		setType(Type.POPUP);
		setBounds(1290, 180, 400, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		setTitle("Setup for TSP");
		setResizable(false);
		setAlwaysOnTop(true);
		initialize();
		
		System.out.println(programTag+"successfull init!");
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
	
		title_TSP = new JLabel("Travelling Salesman Problem");
		title_TSP.setBounds(0, 0, 384, 63);
		title_TSP.setFont(new Font("Century", Font.BOLD, 23));
		title_TSP.setHorizontalAlignment(SwingConstants.CENTER);
		this.getContentPane().add(title_TSP);
		
		strich1_JLabel = new JLabel("");
		strich1_JLabel.setOpaque(true);
		strich1_JLabel.setBackground(new Color(0, 0, 0));
		strich1_JLabel.setEnabled(false);
		strich1_JLabel.setBounds(0, 57, 384, 6);
		this.getContentPane().add(strich1_JLabel);
		
		strich2_JLabel = new JLabel("");
		strich2_JLabel.setOpaque(true);
		strich2_JLabel.setEnabled(false);
		strich2_JLabel.setBackground(Color.BLACK);
		strich2_JLabel.setBounds(0, 107, 384, 6);
		this.getContentPane().add(strich2_JLabel);
		
		countKnoten_JSpinner = new JSpinner();
		countKnoten_JSpinner.setBackground(Color.LIGHT_GRAY);
		countKnoten_JSpinner.setModel(new SpinnerNumberModel(20, 5, 250, 5));
		countKnoten_JSpinner.setName("");
		countKnoten_JSpinner.setBounds(10, 70, 40, 30);
		this.getContentPane().add(countKnoten_JSpinner);
		
		countKnoten_JLabel = new JLabel("Anzahl Knoten/Punkte");
		countKnoten_JLabel.setFont(new Font("Arial", Font.BOLD, 12));
		countKnoten_JLabel.setBounds(60, 70, 130, 30);
		this.getContentPane().add(countKnoten_JLabel);
		
		comboBox_mode = new JComboBox<Object>();
		comboBox_mode.setFont(new Font("Arial", Font.PLAIN, 12));
		comboBox_mode.setModel(new DefaultComboBoxModel<Object>(new String[] {"RANDOM", "ch130.txt", "ch150.txt", "dantzig42.txt"}));
		comboBox_mode.setBounds(221, 70, 153, 30);
		getContentPane().add(comboBox_mode);
		
		chckbxNewCheckBox_1 = new JCheckBox("TSP_BruteForce");
		chckbxNewCheckBox_1.setFont(new Font("Arial", Font.PLAIN, 12));
		chckbxNewCheckBox_1.setBounds(10, 120, 364, 23);
		this.getContentPane().add(chckbxNewCheckBox_1);
		
		chckbxNewCheckBox_2 = new JCheckBox("TSP_cutting_plane");
		chckbxNewCheckBox_2.setFont(new Font("Arial", Font.PLAIN, 12));
		chckbxNewCheckBox_2.setBounds(10, 146, 245, 23);
		getContentPane().add(chckbxNewCheckBox_2);
		
		chckbxNewCheckBox_3 = new JCheckBox("TSP_AntColonyOptimization");
		chckbxNewCheckBox_3.setFont(new Font("Arial", Font.PLAIN, 12));
		chckbxNewCheckBox_3.setBounds(10, 184, 364, 23);
		this.getContentPane().add(chckbxNewCheckBox_3);
		
		chckbxNewCheckBox_4 = new JCheckBox("TSP_Genetic_Algorithem");
		chckbxNewCheckBox_4.setFont(new Font("Arial", Font.PLAIN, 12));
		chckbxNewCheckBox_4.setBounds(10, 210, 364, 23);
		this.getContentPane().add(chckbxNewCheckBox_4);
		
		chckbxNewCheckBox_5 = new JCheckBox("TSP_minimum_spanning_tree_heuristic");
		chckbxNewCheckBox_5.setFont(new Font("Arial", Font.PLAIN, 12));
		chckbxNewCheckBox_5.setBounds(10, 236, 364, 23);
		this.getContentPane().add(chckbxNewCheckBox_5);
		
		chckbxNewCheckBox_6 = new JCheckBox("Christofides\u2013Serdyukov");
		chckbxNewCheckBox_6.setFont(new Font("Arial", Font.PLAIN, 12));
		chckbxNewCheckBox_6.setBounds(10, 262, 245, 23);
		getContentPane().add(chckbxNewCheckBox_6);
		
		chckbxNewCheckBox_7 = new JCheckBox("TSP_Only_2opt");
		chckbxNewCheckBox_7.setFont(new Font("Arial", Font.PLAIN, 12));
		chckbxNewCheckBox_7.setBounds(10, 288, 364, 23);
		this.getContentPane().add(chckbxNewCheckBox_7);
		
		chckbxNewCheckBox_10 = new JCheckBox("TSP_Random");
		chckbxNewCheckBox_10.setFont(new Font("Arial", Font.PLAIN, 12));
		chckbxNewCheckBox_10.setBounds(10, 349, 364, 23);
		getContentPane().add(chckbxNewCheckBox_10);
		
		start_stop_JToggleButton = new JToggleButton("Starte Programme");
		start_stop_JToggleButton.addItemListener(new ItemListener() {
			   public void itemStateChanged(ItemEvent ev) {
			      if(ev.getStateChange()==ItemEvent.SELECTED){
			    	  start_stop_JToggleButton.setText("Beende Programme");
			    	  startButtonPressed();
			      } else if(ev.getStateChange()==ItemEvent.DESELECTED){
			    	  start_stop_JToggleButton.setText("Starte Programme");
			    	  System.out.println(programTag+"Whole Program terminated!");
			    	  System.exit(0);
			      }
			   }
			});
		start_stop_JToggleButton.setHideActionText(true);
		start_stop_JToggleButton.setFont(new Font("Arial", Font.BOLD, 18));
		start_stop_JToggleButton.setBounds(10, 512, 364, 38);
		this.getContentPane().add(start_stop_JToggleButton);
	} // End init Methode
	
	
	//Get called when Start Button Pressed
	private void startButtonPressed() {
  	  ArrayList<String> toExeStrings = new ArrayList<String>();
  	  String mode = String.valueOf(comboBox_mode.getSelectedItem());

  	  if(chckbxNewCheckBox_1.isSelected()) {
  		toExeStrings.add("TSP_BruteForce");
  	  }
  	  
  	 if(chckbxNewCheckBox_2.isSelected()) {
   		toExeStrings.add("TSP_cutting_plane");
   	  }
  	  
  	  if(chckbxNewCheckBox_3.isSelected()) {
  		toExeStrings.add("TSP_AntColonyOptimization");
  	  }
  	  
  	  if(chckbxNewCheckBox_4.isSelected()) {
  		toExeStrings.add("TSP_Genetic_Algorithem");
  	  }
  	  
  	  if(chckbxNewCheckBox_5.isSelected()) {
  		toExeStrings.add("TSP_minimum_spanning_tree_heuristic");
  	  }
  	  
  	  if(chckbxNewCheckBox_6.isSelected()) {
  		//TODO:
  		//toExeStrings.add();
  	  }
  	  
  	  if(chckbxNewCheckBox_7.isSelected()) {
  		toExeStrings.add("TSP_Only_2opt");
  	  }
  	  
  	  if(chckbxNewCheckBox_10.isSelected()) {
  		toExeStrings.add("TSP_Random");
  	  }

  	  Main.startAlgos(toExeStrings,(int) countKnoten_JSpinner.getValue(), mode);
	}
} // End Class
