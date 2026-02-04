package mt.gov.seplag.backend.security.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mt.gov.seplag.backend.security.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final JwtService jwtService;

    public RateLimitingFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Pular rate limiting para endpoints públicos
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        if (path.startsWith("/api/v1/auth/") || 
            path.startsWith("/actuator/") || 
            path.startsWith("/swagger-ui/") ||
            path.startsWith("/v3/api-docs/") ||
            path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Rate limit mais brando para GET (consultas)
        String key = getClientKey(request);
        Bucket bucket = resolveBucket(key, method);

        if (bucket.tryConsume(1)) {
            long remaining = bucket.getAvailableTokens();
            response.addHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"message\": \"Limite de requisições excedido. Tente novamente em alguns segundos.\", " +
                "\"status\": 429}"
            );
        }
    }

    private Bucket resolveBucket(String key, String method) {
        String bucketKey = key + ":" + (method.equals("GET") ? "READ" : "WRITE");
        return cache.computeIfAbsent(bucketKey, k -> createNewBucket(method));
    }

    private Bucket createNewBucket(String method) {
        // GET: 200 req/min, outros: 50 req/min
        int limit = method.equals("GET") ? 100 : 50;
        Bandwidth bandwidthLimit = Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(bandwidthLimit)
                .build();
    }

    private String getClientKey(HttpServletRequest request) {
        // Tentar extrair username do JWT
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String username = jwtService.extractUsername(token);
                if (username != null) {
                    return "user:" + username;
                }
            } catch (Exception e) {
                // Token inválido, usar IP
            }
        }
        
        // Fallback para IP do cliente
        String clientIp = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            clientIp = forwarded.split(",")[0];
        }
        return "ip:" + clientIp;
    }
}
