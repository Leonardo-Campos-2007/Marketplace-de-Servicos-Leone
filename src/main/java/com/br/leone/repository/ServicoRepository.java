package com.br.leone.repository;

import com.br.leone.entity.Servico;
import com.br.leone.enums.StatusPublicacao;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends ListCrudRepository<Servico, Long> {

    List<Servico> findByPerfilPrestadorId(Long perfilPrestadorId);

    List<Servico> findByStatusPublicacao(StatusPublicacao statusPublicacao);

    List<Servico> findByPerfilPrestadorIdAndStatusPublicacao(Long perfilPrestadorId, StatusPublicacao statusPublicacao);

    List<Servico> findByCategoriaServicoId(Long categoriaServicoId);

    boolean existsByPerfilPrestadorId(Long perfilPrestadorId);
}
