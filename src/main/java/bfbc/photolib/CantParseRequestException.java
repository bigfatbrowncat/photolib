package bfbc.photolib;

import com.google.gson.annotations.Expose;

import bfbc.photolib.Heap.ChangeRequest;

public class CantParseRequestException extends Exception {
	
	private static final String MESSAGE = "Can't parse request: ";

	@Expose
	private String message;
	
	@Expose
	private String requestJson;
	
	public CantParseRequestException(String requestJson) {
		super(MESSAGE + requestJson);
		message = MESSAGE + requestJson;
		this.requestJson = requestJson;
	}
	
	public CantParseRequestException(String requestJson, Throwable t) {
		super(MESSAGE + requestJson, t);
		message = MESSAGE + requestJson;
		this.requestJson = requestJson;
	}
	
	public String getRequestJson() {
		return requestJson;
	}
}
