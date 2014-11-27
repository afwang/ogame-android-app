package com.wikaba.ogapp.agent;

/**
 * This exception should be thrown when the OgameAgent is forcibly logged out.
 * This can occur either due to the 0300 server time kick or the user logging
 * into the account from another access point (e.g. from a browser on a desktop)
 *
 */

public class LoggedOutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 732313175539478004L;

	public LoggedOutException() {
	}

	public LoggedOutException(String detailMessage) {
		super(detailMessage);
	}

	public LoggedOutException(Throwable throwable) {
		super(throwable);
	}

	public LoggedOutException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
