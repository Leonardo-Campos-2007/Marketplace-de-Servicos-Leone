package com.br.leone.repository;

import com.br.leone.entity.Mensagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemRepository extends ListCrudRepository<Mensagem, Long> {

    Page<Mensagem> findByChatId(Long chatId, Pageable pageable);

    @Modifying
    @Query("UPDATE mensagem SET lida = true WHERE chat_id = :chatId AND lida = false AND remetente_id <> :usuarioId")
    void marcarComoLidas(Long chatId, Long usuarioId);
}