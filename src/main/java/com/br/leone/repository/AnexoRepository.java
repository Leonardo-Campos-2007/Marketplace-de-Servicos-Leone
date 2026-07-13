package com.br.leone.repository;

import com.br.leone.entity.Anexo;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnexoRepository extends ListCrudRepository<Anexo, Long> {

    Optional<Anexo> findByMensagemId(Long mensagemId);

    boolean existsByMensagemId(Long mensagemId);
}
