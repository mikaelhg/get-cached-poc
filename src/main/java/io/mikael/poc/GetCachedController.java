package io.mikael.poc;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
public class GetCachedController {

	private static final Logger logger = LoggerFactory.getLogger(GetCachedController.class);

	private AtomicLong counter = new AtomicLong();

	@GetMapping("/foo/{id}")
	@RateLimited
	public ResponseEntity<String> foo(final WebRequest request,
			@PathVariable("id") final Long id)
	{
		logger.debug("starting foo");
		try {
			Thread.sleep(3000);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		logger.debug("done with foo");
		return ResponseEntity.ok("foo " + counter.incrementAndGet());
	}

	@GetMapping("/bar")
	@RateLimited
	public ResponseEntity<String> bar() {
		logger.debug("starting bar");
		try {
			Thread.sleep(3000);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		logger.debug("done with bar");
		throw new RuntimeException("bar! " + counter.incrementAndGet());
	}

}
