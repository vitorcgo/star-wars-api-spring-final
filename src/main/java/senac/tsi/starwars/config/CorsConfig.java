package senac.tsi.starwars.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
                return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns(
                                "http://localhost:3000",
                                "http://localhost:4173",
                                "http://localhost:5173",
                                "http://localhost:8080",
                                "https://*.netlify.app",
                                "https://*.netlify.com",
                                "https://*.vercel.app",
                                "https://*.vercel.com"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("X-RateLimit-Limit", "X-RateLimit-Remaining", "Retry-After",
                                "X-Idempotency-Key", "X-Idempotency-Replayed", "X-API-Version")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
