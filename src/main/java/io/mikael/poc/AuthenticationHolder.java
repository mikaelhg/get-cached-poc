package io.mikael.poc;

/**
 * Emulating authentication and principal availability.
 * Instead of principals, we'll just pass around strings.
 */
public class AuthenticationHolder {

	public static ThreadLocal<String> AUTH = new ThreadLocal<>();

}
