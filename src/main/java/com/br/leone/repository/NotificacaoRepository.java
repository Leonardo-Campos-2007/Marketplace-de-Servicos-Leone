package com.br.leone.repository;

import com.br.leone.entity.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacaoRepository extends ListCrudRepository<Notificacao, Long> {

    Page<Notificacao> findByUsuarioId(Long usuarioId, Pageable pageable);

    Page<Notificacao> findByUsuarioIdAndVisualizadaFalse(Long usuarioId, Pageable pageable);

    @Modifying
    @Query("UPDATE notificacao SET visualizada = true WHERE usuario_id = :usuarioId")
    void marcarTodasComoLidas(Long usuarioId);
}