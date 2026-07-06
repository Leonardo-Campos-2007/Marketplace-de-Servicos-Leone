package com.br.leone.repository;

import com.br.leone.entity.SolicitacaoServico;
import com.br.leone.enums.StatusSolicitacao;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoServicoRepository extends ListCrudRepository<SolicitacaoServico, Long> {

    List<SolicitacaoServico> findByCompradorId(Long compradorId);

    List<SolicitacaoServico> findByPerfilPrestadorId(Long perfilPrestadorId);

    List<SolicitacaoServico> findByCompradorIdAndStatus(Long compradorId, StatusSolicitacao status);

    List<SolicitacaoServico> findByPerfilPrestadorIdAndStatus(Long perfilPrestadorId, StatusSolicitacao status);
}
