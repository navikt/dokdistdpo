package no.nav.dokdistdpo.qdist015.itest.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;
import static no.nav.dokdistdpo.config.cache.LocalCacheConfig.ALTINN3_TOKEN_CACHE;
import static no.nav.dokdistdpo.config.cache.LocalCacheConfig.MASKINPORTEN_CACHE;

@Configuration
@EnableCaching
@Profile({"itest"})
public class CacheTestConfig {

	@Bean
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(List.of(
				new CaffeineCache(MASKINPORTEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(0, MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(ALTINN3_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(0, MINUTES)
						.recordStats()
						.build())
		));
		return manager;
	}
}
