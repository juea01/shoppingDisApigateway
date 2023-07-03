package com.shoppingdistrict.microservices.shoppingdistrictapigateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import java.util.Arrays;
import java.util.Collections;

@Configuration
public class ApiGatewayConfiguration{
	
	@Value("${keycloak.url}")
	private String keyCloakUrl;
	
	@Value("${frontend.url}")
	private String frontEndUrl;
	
	@Value("${gateway.url}")
	private String gatewayUrl;
	
	@Bean
	public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {

		return builder.routes()
				.route(p -> p.path("/user-service/customers/**")
						 .uri("lb://USER-SERVICE"))
				.route(p -> p.path("/user-service/subscription/**")
						 .uri("lb://USER-SERVICE"))
				.route(p -> p.path("/product-listing-service/**")
						 .uri("lb://PRODUCT-LISTING-SERVICE"))
				.route(p -> p.path("/customer-order-fulfillment-service/**")
						 .uri("lb://CUSTOMER-ORDER-FULFILLMENT-SERVICE"))				
				.build();
	}
	
	/**
	 * Currently validation for credential is not done here even though there
	 * is a configuration for key cloak oauth2.
	 * Down stream component has validation.
	 * They way of the application flow (from UI) to back end, for example, user will have to login first to access
	 * restricted resources make needing a bit more consideration for position of API gate way in oauth2 flow.
	 * For example should api gateway act as a resource server or resource client.
	 * 
	 * @param http
	 * @return
	 */
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
		.cors().configurationSource((org.springframework.web.cors.reactive.CorsConfigurationSource) new CorsConfigurationSource() {
			@Override
			public CorsConfiguration getCorsConfiguration(ServerWebExchange exchange) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				CorsConfiguration config = new CorsConfiguration();
				config.setAllowedOrigins(Arrays.asList( keyCloakUrl, frontEndUrl, gatewayUrl));
				//config.setAllowedOrigins(Collections.singletonList("*"));
				config.setAllowedMethods(Collections.singletonList("*"));
				config.setAllowCredentials(true);
				config.setAllowedHeaders(Collections.singletonList("*"));
				config.setMaxAge(3600L);
				return config;
			}
	}).and()
		.csrf().disable()
		.authorizeExchange()
				.pathMatchers("/user-service/customers/**","/user-service/subscription/**","/customer-order-fulfillment-service/**", "/product-listing-service/**")
				.permitAll()
			.and()
				.authorizeExchange()
				.anyExchange()
				.authenticated()
			.and()
				.oauth2Login(); // to redirect to oauth2 login page.

		return http.build();
	}
	

}
