package senac.tsi.starwars.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_MS = 60_000;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        long now = System.currentTimeMillis();

        ConcurrentLinkedDeque<Long> timestamps = requestCounts.computeIfAbsent(clientIp,
                k -> new ConcurrentLinkedDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < now - WINDOW_MS) {
                timestamps.pollFirst();
            }

            int currentCount = timestamps.size();

            response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, MAX_REQUESTS - currentCount - 1)));

            if (currentCount >= MAX_REQUESTS) {
                Long oldest = timestamps.peekFirst();
                long retryAfterSeconds = (oldest != null)
                        ? Math.max(1, (oldest + WINDOW_MS - now) / 1000)
                        : 1;

                response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("timestamp", LocalDateTime.now().toString());
                body.put("status", 429);
                body.put("mensagem", "Limite de requisições excedido. Tente novamente em " + retryAfterSeconds + " segundos.");
                response.getWriter().write(MAPPER.writeValueAsString(body));
                return;
            }

            timestamps.addLast(now);
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
