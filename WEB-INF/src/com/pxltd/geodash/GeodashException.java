package com.pxltd.geodash;

public class GeodashException extends Exception {
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Throwable cause;
	    /**
	     * Constructs a GeoReportException with an explanatory message.
	     * @param message Detail about the reason for the exception.
	     */
	    public GeodashException(String message) {
	        super(message);
	    }
	    
	    public GeodashException(Throwable t) {
	        super(t.getMessage());
	        this.cause = t;
	    }

	    public Throwable getCause() {
	        return this.cause;
	    }
}
