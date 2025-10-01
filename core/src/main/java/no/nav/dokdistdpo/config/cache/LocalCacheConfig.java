package no.nav.dokdistdpo.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;

@Configuration
@EnableCaching
@Profile("nais")
public class LocalCacheConfig {

	public static final String MASKINPORTEN_CACHE = "maskinportenCache";

	@Bean
	@Primary
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(List.of(
				new CaffeineCache(MASKINPORTEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, MINUTES)
						.recordStats()
						.build())
		));
		return cacheManager;
	}

}