package senac.tsi.starwars.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import senac.tsi.starwars.filter.ApiKeyFilter;
import senac.tsi.starwars.filter.IdempotencyFilter;
import senac.tsi.starwars.filter.RateLimitFilter;
import senac.tsi.starwars.repository.IdempotencyRecordRepository;
import senac.tsi.starwars.service.ApiKeyService;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilter(ApiKeyService apiKeyService) {
        FilterRegistrationBean<ApiKeyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ApiKeyFilter(apiKeyService));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        registration.setName("apiKeyFilter");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(2);
        registration.setName("rateLimitFilter");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilter(
            IdempotencyRecordRepository repository) {
        FilterRegistrationBean<IdempotencyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new IdempotencyFilter(repository));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(3);
        registration.setName("idempotencyFilter");
        return registration;
    }
}
