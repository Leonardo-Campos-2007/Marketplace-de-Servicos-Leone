package com.br.leone.repository;

import com.br.leone.entity.Carrinho;
import com.br.leone.enums.StatusCarrinho;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarrinhoRepository extends ListCrudRepository<Carrinho, Long> {

    Optional<Carrinho> findByUsuarioIdAndStatus(Long usuarioId, StatusCarrinho status);

    List<Carrinho> findByUsuarioId(Long usuarioId);
}
