package senac.tsi.starwars.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import senac.tsi.starwars.service.ApiKeyService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Component
@Order(1)
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private static final Set<String> PROTECTED_METHODS = Set.of("POST", "PUT", "DELETE");

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/keys",
            "/swagger-ui",
            "/api-docs",
            "/h2-console",
            "/v3/api-docs"
    );

    private final ApiKeyService apiKeyService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ApiKeyFilter(ApiKeyService apiKeyService, ObjectMapper objectMapper) {
        this.apiKeyService = apiKeyService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method) || isPublicPath(path) || !PROTECTED_METHODS.contains(method.toUpperCase())) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            sendError(response, HttpStatus.UNAUTHORIZED, "Header X-API-Key é obrigatório para operações de escrita.");
            return;
        }

        if (!apiKeyService.isValidKey(apiKey)) {
            sendError(response, HttpStatus.FORBIDDEN, "API Key inválida ou revogada.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private void sendError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "mensagem", message
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
