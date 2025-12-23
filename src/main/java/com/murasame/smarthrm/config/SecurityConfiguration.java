package com.murasame.smarthrm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.authorizeHttpRequests(auth -> {
					auth.requestMatchers("/static/**").permitAll();
					auth.anyRequest().permitAll();  //.authenticated();
				})
				.formLogin(conf -> {
//					conf.loginPage("/login");
//					conf.loginProcessingUrl("/doLogin");
					conf.defaultSuccessUrl("/");
					conf.permitAll();
				})
				.csrf(AbstractHttpConfigurer::disable)
				.build();
	}
}
