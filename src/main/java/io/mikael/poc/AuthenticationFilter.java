package io.mikael.poc;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.stereotype.Component;

/**
 * Emulating authentication from JWT with simple IP address lookups.
 */
@Component
public class AuthenticationFilter implements Filter {

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response,
			final FilterChain chain) throws IOException, ServletException
	{
		try {
			AuthenticationHolder.AUTH.set(request.getRemoteAddr());
			chain.doFilter(request, response);
		} finally {
			AuthenticationHolder.AUTH.remove();
		}
	}

}
