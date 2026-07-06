package com.br.leone.repository;

import com.br.leone.entity.ItemCarrinho;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCarrinhoRepository extends ListCrudRepository<ItemCarrinho, Long> {

    List<ItemCarrinho> findByCarrinhoId(Long carrinhoId);

    Optional<ItemCarrinho> findByCarrinhoIdAndServicoId(Long carrinhoId, Long servicoId);

    void deleteByCarrinhoId(Long carrinhoId);
}
