package com.codeoftheweb.salvo;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.JpaRepository;

@RepositoryRestResource
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {
}