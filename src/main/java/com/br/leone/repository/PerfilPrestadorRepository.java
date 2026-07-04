package com.br.leone.repository;

import com.br.leone.entity.PerfilPrestador;
import com.br.leone.enums.StatusAprovacao;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilPrestadorRepository extends ListCrudRepository<PerfilPrestador, Long> {

    // Busca o perfil através do ID do usuário dono da conta
    Optional<PerfilPrestador> findByUsuarioId(Long usuarioId);

    // Lista os perfis filtrando pelo status (ex: buscar apenas os PENDENTES)
    List<PerfilPrestador> findByStatusAprovacao(StatusAprovacao statusAprovacao);
}
