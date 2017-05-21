package com.ilya.ivanov.security.registration;

/**
 * Contract password generating strategy type.
 */
public interface PasswordGenerator {
	/**
	 * Generate password.
	 * @return
	 */
	String generate();
}
