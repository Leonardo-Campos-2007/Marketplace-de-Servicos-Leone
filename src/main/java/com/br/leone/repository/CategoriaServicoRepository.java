package com.br.leone.repository;

import com.br.leone.entity.CategoriaServico;
import com.br.leone.enums.StatusAprovacao;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaServicoRepository extends ListCrudRepository<CategoriaServico, Long> {

    // Traz subcategorias apenas se corresponderem ao status informado (ex: APROVADO)
    List<CategoriaServico> findByCategoriaPaiIdAndStatusAprovacao(Long categoriaPaiId, StatusAprovacao statusAprovacao);

    // Traz categorias raízes apenas se corresponderem ao status informado
    List<CategoriaServico> findByCategoriaPaiIdIsNullAndStatusAprovacao(StatusAprovacao statusAprovacao);

    boolean existsByNomeAndCategoriaPaiId(String nome, Long categoriaPaiId);

    List<CategoriaServico> findByStatusAprovacao(StatusAprovacao statusAprovacao);
}
