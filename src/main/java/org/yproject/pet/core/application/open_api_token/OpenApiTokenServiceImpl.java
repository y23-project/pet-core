package org.yproject.pet.core.application.open_api_token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yproject.pet.core.domain.open_api_token.OpenApiToken;
import org.yproject.pet.core.infrastructure.generator.identity.IdGenerator;
import org.yproject.pet.core.infrastructure.web.jwt.JwtService;

import java.util.List;

@Component
@RequiredArgsConstructor
class OpenApiTokenServiceImpl implements OpenApiTokenService {
    private final IdGenerator idGenerator;
    private final JwtService jwtService;
    private final OpenApiTokenStorage openApiTokenStorage;

    @Override
    public OpenApiTokenIdWithTokenDto create(String userId, String email, String name) {
        final var id = idGenerator.get();
        openApiTokenStorage.store(new OpenApiToken(
                id,
                userId,
                name
        ));
        final var oneTimeToken = jwtService.generateToken(email, id);
        return new OpenApiTokenIdWithTokenDto(id, oneTimeToken);
    }

    @Override
    public List<OpenApiTokenIdWithNameDto> retrieveAllByUserId(String userId) {
        return openApiTokenStorage.findByUserId(userId)
                .stream()
                .map(domain -> new OpenApiTokenIdWithNameDto(domain.id(), domain.name()))
                .toList();
    }

    @Override
    public void delete(String userId, String id) {
        openApiTokenStorage.deleteById(userId, id);
    }
}
