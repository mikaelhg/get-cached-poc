package io.mikael.poc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@Aspect
public class RateLimitAspect {

	private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);

	private static final CacheLoader<Long, SingleJoinPointExecutor> LOADER = new CacheLoader<Long, SingleJoinPointExecutor>() {
		@Override
		public SingleJoinPointExecutor load(final Long key) {
			return new SingleJoinPointExecutor();
		}
	};

	private final LoadingCache<Long, SingleJoinPointExecutor> cache = CacheBuilder.newBuilder()
			.expireAfterWrite(5, TimeUnit.MINUTES)
			.build(LOADER);

	@Pointcut("execution(public * io.mikael.poc.*.*(..))")
	private void anyPublicOperation() {}

	@Pointcut("@annotation(io.mikael.poc.RateLimited)")
	public void rateLimited() {}

	@Around("anyPublicOperation() && rateLimited()")
	public Object aroundRateLimited(final ProceedingJoinPoint pjp)
			throws Throwable
	{
		final Long hash = hashJoinPoint(pjp);
		final SingleJoinPointExecutor executor = cache.get(hash);
		return executor.run(pjp);
	}

	public long hashJoinPoint(final JoinPoint joinPoint) {
		final Hasher hasher = Hashing.murmur3_128(1234).newHasher();
		final String auth = AuthenticationHolder.AUTH.get();
		if (null != auth) {
			hasher.putUnencodedChars(auth);
		}
		hasher.putUnencodedChars(joinPoint.toLongString());
		final Signature sig = joinPoint.getStaticPart().getSignature();
		if (sig instanceof MethodSignature) {
			final Method method = ((MethodSignature)sig).getMethod();
			final Annotation[][] annotations = method.getParameterAnnotations();
			final Object[] arguments = joinPoint.getArgs();
			for (int i = 0; i < annotations.length; i++) {
				if (isContentAnnotation(annotations[i])) {
					if (arguments[i] instanceof Long) {
						hasher.putLong((Long) arguments[i]);
					} else if (arguments[i] instanceof Integer) {
						hasher.putInt((Integer) arguments[i]);
					} else if (arguments[i] instanceof String) {
						hasher.putUnencodedChars((String) arguments[i]);
					}
				}
			}
		}
		return hasher.hash().asLong();
	}

	private static boolean isContentAnnotation(final Annotation[] annotations) {
		return Arrays.stream(annotations).anyMatch(RateLimitAspect::isContentAnnotation);
	}

	private static boolean isContentAnnotation(final Annotation annotation) {
		return annotation.annotationType() == PathVariable.class ||
				annotation.annotationType() == RequestParam.class;
	}

}
