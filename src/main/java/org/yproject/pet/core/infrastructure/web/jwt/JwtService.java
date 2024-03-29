package org.yproject.pet.core.infrastructure.web.jwt;

import org.yproject.pet.core.infrastructure.web.security.UserInfo;

public interface JwtService {
    String extractEmail(String token);

    String generateToken(String email);

    String generateToken(String email, String tokenId);

    boolean isTokenValid(String token, UserInfo userInfo);

}
