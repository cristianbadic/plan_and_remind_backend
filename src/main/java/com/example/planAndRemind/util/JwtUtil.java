package com.example.planAndRemind.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

@Component
public class JwtUtil {

    @Value("${jwt.string.key}")
    private String STRING_KEY;

    private Date expirationDate = null;

    //one hour in milliseconds

    private static final int EXPIRATION_TIME= 3600 *1000;

    public String getEmailFromToken(String token){
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(STRING_KEY));
        Claims allClaims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return allClaims.getSubject();
    }

    public LocalDateTime getExpirationTime(){
        return LocalDateTime.ofInstant(this.expirationDate.toInstant(), ZoneId.systemDefault());
    }

    public String generateToken(UserDetails userDetails) {

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(STRING_KEY));

        this.expirationDate = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .signWith(key)
                .compact();
    }
}
