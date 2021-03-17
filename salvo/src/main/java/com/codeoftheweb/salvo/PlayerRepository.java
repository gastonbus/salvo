package com.codeoftheweb.salvo;

import java.util.List;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.JpaRepository;

@RepositoryRestResource
public interface PlayerRepository extends JpaRepository<Player, Long> {
    String findByUserName(String userName);
}
