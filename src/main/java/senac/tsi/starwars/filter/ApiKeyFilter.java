package senac.tsi.starwars.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import senac.tsi.starwars.service.ApiKeyService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final Set<String> PROTECTED_METHODS = Set.of("POST", "PUT", "DELETE");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ApiKeyService apiKeyService;

    public ApiKeyFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isPublicEndpoint(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!PROTECTED_METHODS.contains(method.toUpperCase())) {
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

    private boolean isExcludedPath(String path) {
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/h2-console");
    }

    private boolean isPublicEndpoint(String path, String method) {
        return path.equals("/api/auth/keys") && "POST".equalsIgnoreCase(method);
    }

    private void sendError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("mensagem", message);
        response.getWriter().write(MAPPER.writeValueAsString(body));
    }
}
