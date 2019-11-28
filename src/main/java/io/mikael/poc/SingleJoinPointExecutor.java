package io.mikael.poc;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We only want to run the JoinPoint once, for the first caller, and every subsequent
 * caller who wants to access the same JoinPoint while the call is still under way, will
 * just have to wait for the first caller, and reuse the result which they produced.
 *
 * If the execution of the JoinPoint threw an exception, we'll reuse that exception,
 * which means that the stack trace of that execution will not be correct for this thread.
 */
public class SingleJoinPointExecutor {

	private static final Logger logger = LoggerFactory.getLogger(SingleJoinPointExecutor.class);

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private volatile Object value;

	private volatile Throwable throwable;

	public Object run(final ProceedingJoinPoint pjp,
			final Consumer<ProceedingJoinPoint> afterWrite)
	{
		if (lock.writeLock().tryLock()) {
			try {
				try {
					this.value = pjp.proceed(pjp.getArgs());
					return this.value;
				} catch (final Throwable throwable) {
					this.throwable = throwable;
					throw new RuntimeException(this.throwable);
				}
			} finally {
				lock.writeLock().unlock();
				afterWrite.accept(pjp);
			}
		} else {
			lock.readLock().lock();
			try {
				if (null != this.throwable) {
					throw new RuntimeException(this.throwable);
				} else {
					return this.value;
				}
			} finally {
				lock.readLock().unlock();
			}
		}
	}

}
