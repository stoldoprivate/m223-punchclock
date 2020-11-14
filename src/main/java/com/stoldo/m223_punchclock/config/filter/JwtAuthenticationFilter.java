package com.stoldo.m223_punchclock.config.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stoldo.m223_punchclock.model.api.UserLoginRequest;
import com.stoldo.m223_punchclock.shared.Constants;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
	private final String jwtSecret;
	private final Integer jwtTokenValidityInMinutes;


    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        try {
        	UserLoginRequest ulr = new ObjectMapper().readValue(req.getInputStream(), UserLoginRequest.class);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(ulr.getEmail(), ulr.getPassword(), Collections.emptyList()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    
    // TODO only admin should be able to register uswers...
    // TODO return user
    
    // TODO make itrfc conform and add emailmetc
    @Override
    public void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Authentication auth) throws IOException, ServletException {
    	User user = (User) auth.getPrincipal();
        
        String token = JWT
        		.create()
        		.withSubject(user.getUsername())
                .withClaim("email", user.getUsername())
                .withExpiresAt(DateUtils.addMinutes(new Date(), jwtTokenValidityInMinutes))
                .sign(Algorithm.HMAC256(jwtSecret.getBytes()));

		res.addHeader(Constants.AUTH_HEADER_NAME, Constants.JWT_TOKEN_PREFIX + token);
    }
}
