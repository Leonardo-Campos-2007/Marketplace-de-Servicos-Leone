package com.br.leone.repository;

import com.br.leone.entity.ItemSolicitacao;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemSolicitacaoRepository extends ListCrudRepository<ItemSolicitacao, Long> {

    List<ItemSolicitacao> findBySolicitacaoId(Long solicitacaoId);

    List<ItemSolicitacao> findBySolicitacaoIdIn(List<Long> solicitacaoIds);
}
