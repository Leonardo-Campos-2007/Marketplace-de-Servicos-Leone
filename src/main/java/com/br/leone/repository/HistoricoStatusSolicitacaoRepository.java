package com.br.leone.repository;

import com.br.leone.entity.HistoricoStatusSolicitacao;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoStatusSolicitacaoRepository extends ListCrudRepository<HistoricoStatusSolicitacao, Long> {

    List<HistoricoStatusSolicitacao> findBySolicitacaoId(Long solicitacaoId);
}
