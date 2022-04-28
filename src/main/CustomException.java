package main;

public class CustomException extends Exception {

	private static final long serialVersionUID = 1L;
	
	// Parameterless Constructor
    public CustomException() {}

    // Constructor that accepts a message
    public CustomException(String message)
    {
       super(message);
    }

} // End class
