package com.auth.api.exception;

/** 일관된 예외 처리 */
public class AuthException extends RuntimeException {

	private final AuthFailureReason reason;

	public AuthException(AuthFailureReason reason) {
		super(reason.name());
		this.reason = reason;
	}
	public AuthException(AuthFailureReason reason, String message) {
		super(message);
		this.reason = reason;
	}
	public AuthException(AuthFailureReason reason, String message, Throwable cause) {
		super(message, cause);
		this.reason = reason;
	}
	public AuthException(AuthFailureReason reason, Throwable cause) {
		super(reason.name(), cause);
		this.reason = reason;
	}

	public AuthFailureReason getReason() {return reason;}

	@Override
	public String toString() {
		return "AuthException{" + "reason=" + reason + ", message=" + getMessage() + '}';
	}
}
