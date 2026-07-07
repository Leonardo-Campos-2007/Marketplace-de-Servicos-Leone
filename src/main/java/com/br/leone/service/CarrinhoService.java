package com.br.leone.service;

import com.br.leone.dto.CarrinhoResponseDTO;
import com.br.leone.dto.ItemCarrinhoRequestDTO;
import com.br.leone.dto.ItemCarrinhoResponseDTO;
import com.br.leone.entity.Carrinho;
import com.br.leone.entity.ItemCarrinho;
import com.br.leone.entity.PerfilPrestador;
import com.br.leone.entity.Servico;
import com.br.leone.enums.StatusAprovacao;
import com.br.leone.enums.StatusCarrinho;
import com.br.leone.enums.StatusPublicacao;
import com.br.leone.exception.*;
import com.br.leone.repository.CarrinhoRepository;
import com.br.leone.repository.ItemCarrinhoRepository;
import com.br.leone.repository.PerfilPrestadorRepository;
import com.br.leone.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final ServicoRepository servicoRepository;
    private final PerfilPrestadorRepository perfilPrestadorRepository;

    public CarrinhoService(CarrinhoRepository carrinhoRepository,
                           ItemCarrinhoRepository itemCarrinhoRepository,
                           ServicoRepository servicoRepository,
                           PerfilPrestadorRepository perfilPrestadorRepository) {
        this.carrinhoRepository = carrinhoRepository;
        this.itemCarrinhoRepository = itemCarrinhoRepository;
        this.servicoRepository = servicoRepository;
        this.perfilPrestadorRepository = perfilPrestadorRepository;
    }

    @Transactional
    public Carrinho obterOuCriarCarrinhoAtivo(Long usuarioId) {
        return carrinhoRepository.findByUsuarioIdAndStatus(usuarioId, StatusCarrinho.ATIVO)
                .orElseGet(() -> {
                    Carrinho novo = new Carrinho(null, usuarioId, StatusCarrinho.ATIVO, LocalDateTime.now(), LocalDateTime.now());
                    return carrinhoRepository.save(novo);
                });
    }

    @Transactional
    public CarrinhoResponseDTO obterCarrinhoResponse(Long usuarioId) {
        Carrinho carrinho = obterOuCriarCarrinhoAtivo(usuarioId);
        return converterParaDTO(carrinho);
    }

    @Transactional
    public CarrinhoResponseDTO adicionarItem(Long usuarioId, ItemCarrinhoRequestDTO dto) {
        Carrinho carrinho = obterOuCriarCarrinhoAtivo(usuarioId);

        Servico servico = servicoRepository.findById(dto.servicoId())
                .orElseThrow(() -> new ServicoNaoEncontradoException(dto.servicoId()));

        // Validação: serviço ativo
        if (servico.getStatusPublicacao() != StatusPublicacao.ATIVO) {
            throw new ServicoInativoException("O serviço '" + servico.getNome() + "' não está ativo e não pode ser comprado.");
        }

        PerfilPrestador prestador = perfilPrestadorRepository.findById(servico.getPerfilPrestadorId())
                .orElseThrow(() -> new PerfilPrestadorNaoEncontradoException(servico.getPerfilPrestadorId()));

        // Validação: prestador aprovado
        if (prestador.getStatusAprovacao() != StatusAprovacao.APROVADO) {
            throw new PerfilPrestadorNaoAprovadoException("O prestador deste serviço não está com o perfil aprovado.");
        }

        // Validação: não comprar o próprio serviço
        if (Objects.equals(prestador.getUsuarioId(), usuarioId)) {
            throw new CompraProprioServicoException("Você não pode comprar seus próprios serviços.");
        }

        Optional<ItemCarrinho> itemExistente = itemCarrinhoRepository.findByCarrinhoIdAndServicoId(carrinho.getId(), dto.servicoId());

        if (itemExistente.isPresent()) {
            ItemCarrinho item = itemExistente.get();
            int novaQuantidade = item.getQuantidade() + dto.quantidade();
            if (novaQuantidade > 99) {
                throw new IllegalArgumentException("A quantidade máxima permitida para um mesmo serviço é 99.");
            }
            item.setQuantidade(novaQuantidade);
            item.setDataAdicionado(LocalDateTime.now());
            itemCarrinhoRepository.save(item);
        } else {
            if (dto.quantidade() > 99) {
                throw new IllegalArgumentException("A quantidade máxima permitida para um mesmo serviço é 99.");
            }
            ItemCarrinho novoItem = new ItemCarrinho(null, carrinho.getId(), dto.servicoId(), dto.quantidade(), servico.getPrecoBase(), LocalDateTime.now());
            itemCarrinhoRepository.save(novoItem);
        }

        carrinho.setDataAtualizacao(LocalDateTime.now());
        carrinhoRepository.save(carrinho);

        return converterParaDTO(carrinho);
    }

    @Transactional
    public CarrinhoResponseDTO atualizarQuantidade(Long usuarioId, Long itemId, int novaQuantidade) {
        ItemCarrinho item = itemCarrinhoRepository.findById(itemId)
                .orElseThrow(() -> new ItemCarrinhoNaoEncontradoException(itemId));

        Carrinho carrinho = carrinhoRepository.findById(item.getCarrinhoId())
                .orElseThrow(() -> new ItemCarrinhoNaoEncontradoException(itemId));

        // Validação de posse do carrinho ativo (mitigação IDOR com retorno 404 opaco)
        if (carrinho.getStatus() != StatusCarrinho.ATIVO || !Objects.equals(carrinho.getUsuarioId(), usuarioId)) {
            throw new ItemCarrinhoNaoEncontradoException(itemId);
        }

        if (novaQuantidade < 1 || novaQuantidade > 99) {
            throw new IllegalArgumentException("A quantidade deve estar entre 1 e 99.");
        }

        item.setQuantidade(novaQuantidade);
        itemCarrinhoRepository.save(item);

        carrinho.setDataAtualizacao(LocalDateTime.now());
        carrinhoRepository.save(carrinho);

        return converterParaDTO(carrinho);
    }

    @Transactional
    public CarrinhoResponseDTO removerItem(Long usuarioId, Long itemId) {
        ItemCarrinho item = itemCarrinhoRepository.findById(itemId)
                .orElseThrow(() -> new ItemCarrinhoNaoEncontradoException(itemId));

        Carrinho carrinho = carrinhoRepository.findById(item.getCarrinhoId())
                .orElseThrow(() -> new ItemCarrinhoNaoEncontradoException(itemId));

        // Validação de posse do carrinho ativo (mitigação IDOR)
        if (carrinho.getStatus() != StatusCarrinho.ATIVO || !Objects.equals(carrinho.getUsuarioId(), usuarioId)) {
            throw new ItemCarrinhoNaoEncontradoException(itemId);
        }

        itemCarrinhoRepository.deleteById(itemId);

        carrinho.setDataAtualizacao(LocalDateTime.now());
        carrinhoRepository.save(carrinho);

        return converterParaDTO(carrinho);
    }

    @Transactional
    public CarrinhoResponseDTO limparCarrinho(Long usuarioId) {
        Carrinho carrinho = obterOuCriarCarrinhoAtivo(usuarioId);

        itemCarrinhoRepository.deleteByCarrinhoId(carrinho.getId());

        carrinho.setDataAtualizacao(LocalDateTime.now());
        carrinhoRepository.save(carrinho);

        return converterParaDTO(carrinho);
    }

    private CarrinhoResponseDTO converterParaDTO(Carrinho carrinho) {
        List<ItemCarrinho> itens = itemCarrinhoRepository.findByCarrinhoId(carrinho.getId());
        if (itens.isEmpty()) {
            return new CarrinhoResponseDTO(carrinho, List.of());
        }

        List<Long> servicoIds = itens.stream().map(ItemCarrinho::getServicoId).toList();
        List<Servico> servicos = servicoRepository.findAllById(servicoIds);
        Map<Long, Servico> servicoMap = servicos.stream()
                .collect(Collectors.toMap(Servico::getId, Function.identity()));

        List<ItemCarrinhoResponseDTO> itemDTOs = itens.stream()
                .map(item -> new ItemCarrinhoResponseDTO(item, servicoMap.get(item.getServicoId())))
                .toList();

        return new CarrinhoResponseDTO(carrinho, itemDTOs);
    }
}
