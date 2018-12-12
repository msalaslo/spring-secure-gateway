package com.verisure.vcp.securegateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests().anyRequest().authenticated().and().x509().subjectPrincipalRegex("CN=(.*?)(?:,|$)")
				.userDetailsService(userDetailsService());
	}

	@Bean
	public UserDetailsService userDetailsService() {
		// TODO apply actual authentication: one certificate will be associated to a
		// Oauth2 credential of APIManager
		return (username -> {
			if (username.equals("apimanager-secure-client") || username.equals("apimanager-secure-client-2")) {
				return new User(username, "", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
			} else {
				return new User(username, "", false, false, true, true,
						AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
			}
		});
	}
}
