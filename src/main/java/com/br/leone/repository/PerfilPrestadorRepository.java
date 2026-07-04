package com.br.leone.repository;

import com.br.leone.entity.PerfilPrestador;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilPrestadorRepository extends ListCrudRepository<PerfilPrestador, Long> {
}
