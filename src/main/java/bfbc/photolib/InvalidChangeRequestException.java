package bfbc.photolib;

import com.google.gson.annotations.Expose;

public class InvalidChangeRequestException extends Exception {
	
	private static final String MESSAGE = "Invalid request: ";

	@Expose
	private String message;
	
	@Expose
	private ChangeRequest changeRequest;
	
	public InvalidChangeRequestException(ChangeRequest cr) {
		super(MESSAGE + cr.toString());
		message = MESSAGE + cr.toString();
		this.changeRequest = cr;
	}
	public InvalidChangeRequestException(ChangeRequest cr, Throwable t) {
		super(MESSAGE + cr.toString(), t);
		message = MESSAGE + cr.toString();
		this.changeRequest = cr;
	}
	public ChangeRequest getChangeRequest() {
		return changeRequest;
	}
}
