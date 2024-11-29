package api.mcnc.surveyresponsegateway.filter;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

  @LoadBalanced
  private final WebClient webClient;

  // TODO 2024-11-27 yhj : WebClient로 교체
  public AuthenticationFilter(ReactorLoadBalancerExchangeFilterFunction lbFunction) {
    super(Config.class);
    this.webClient = WebClient.builder()
      .filter(lbFunction)
      .baseUrl("http://survey-respondent-service")
      .build();
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

      if (authHeader == null) {
        return handleAuthenticationError(exchange);
      }

      if (authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);

        return validateToken(token)
          .flatMap(userId -> proceedWithUserId(userId, exchange, chain))
          .switchIfEmpty(chain.filter(exchange)) // If token is invalid, continue without setting userId
          .onErrorResume(e -> handleAuthenticationError(exchange)); // Handle errors
      }

      // Authorization 헤더가 유효하지 않은 경우
      return handleAuthenticationError(exchange);
    };
  }

  private Mono<Void> handleAuthenticationError(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }

  private Mono<String> validateToken(String token) {
    return webClient.post()
      .uri("/token-validation")
      .bodyValue("{\"accessToken\":\"" + token + "\"}")
      .header("Content-Type", "application/json")
      .retrieve()
      .bodyToMono(String.class);
  }

  private Mono<Void> proceedWithUserId(String userId, ServerWebExchange exchange, GatewayFilterChain chain) {
    exchange.getRequest().mutate().header("requested-by", userId);
    return chain.filter(exchange);
  }

  public static class Config {
    // 필터 구성을 위한 설정 클래스
  }
}
