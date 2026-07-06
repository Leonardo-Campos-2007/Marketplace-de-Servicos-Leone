package com.br.leone.service;

import com.br.leone.dto.CarrinhoResponseDTO;
import com.br.leone.dto.ItemCarrinhoRequestDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarrinhoServiceTest {

    @Mock
    private CarrinhoRepository carrinhoRepository;

    @Mock
    private ItemCarrinhoRepository itemCarrinhoRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private PerfilPrestadorRepository perfilPrestadorRepository;

    @InjectMocks
    private CarrinhoService carrinhoService;

    private Long usuarioClienteId;
    private Long usuarioPrestadorId;
    private Carrinho carrinhoAtivo;
    private Servico servicoAtivo;
    private PerfilPrestador prestadorAprovado;

    @BeforeEach
    void setUp() {
        usuarioClienteId = 1L;
        usuarioPrestadorId = 2L;

        carrinhoAtivo = new Carrinho(10L, usuarioClienteId, StatusCarrinho.ATIVO, LocalDateTime.now(), LocalDateTime.now());

        prestadorAprovado = new PerfilPrestador(20L, usuarioPrestadorId, "Prestador Teste", "Descrição",
                "Tecnologia", 5.0, 10, StatusAprovacao.APROVADO, LocalDateTime.now(), LocalDateTime.now());

        servicoAtivo = new Servico(20L, 30L, "Serviço Teste", "Descrição Serviço",
                BigDecimal.valueOf(100.0), 2, StatusPublicacao.ATIVO);
        servicoAtivo.setId(50L);
    }

    @Test
    void obterOuCriarCarrinhoAtivo_DeveRetornarExistente() {
        when(carrinhoRepository.findByUsuarioIdAndStatus(usuarioClienteId, StatusCarrinho.ATIVO))
                .thenReturn(Optional.of(carrinhoAtivo));

        Carrinho resultado = carrinhoService.obterOuCriarCarrinhoAtivo(usuarioClienteId);

        assertNotNull(resultado);
        assertEquals(carrinhoAtivo.getId(), resultado.getId());
        verify(carrinhoRepository, never()).save(any(Carrinho.class));
    }

    @Test
    void obterOuCriarCarrinhoAtivo_DeveCriarNovoSeNaoExistir() {
        when(carrinhoRepository.findByUsuarioIdAndStatus(usuarioClienteId, StatusCarrinho.ATIVO))
                .thenReturn(Optional.empty());
        when(carrinhoRepository.save(any(Carrinho.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrinho resultado = carrinhoService.obterOuCriarCarrinhoAtivo(usuarioClienteId);

        assertNotNull(resultado);
        assertEquals(usuarioClienteId, resultado.getUsuarioId());
        assertEquals(StatusCarrinho.ATIVO, resultado.getStatus());
        verify(carrinhoRepository, times(1)).save(any(Carrinho.class));
    }

    @Test
    void adicionarItem_ComSucesso() {
        ItemCarrinhoRequestDTO dto = new ItemCarrinhoRequestDTO(50L, 2);

        when(carrinhoRepository.findByUsuarioIdAndStatus(usuarioClienteId, StatusCarrinho.ATIVO))
                .thenReturn(Optional.of(carrinhoAtivo));
        when(servicoRepository.findById(50L)).thenReturn(Optional.of(servicoAtivo));
        when(perfilPrestadorRepository.findById(20L)).thenReturn(Optional.of(prestadorAprovado));
        when(itemCarrinhoRepository.findByCarrinhoIdAndServicoId(carrinhoAtivo.getId(), 50L))
                .thenReturn(Optional.empty());

        ItemCarrinho itemSalvo = new ItemCarrinho(100L, carrinhoAtivo.getId(), 50L, 2, BigDecimal.valueOf(100.0), LocalDateTime.now());
        when(itemCarrinhoRepository.save(any(ItemCarrinho.class))).thenReturn(itemSalvo);
        when(itemCarrinhoRepository.findByCarrinhoId(carrinhoAtivo.getId())).thenReturn(List.of(itemSalvo));
        when(servicoRepository.findAllById(List.of(50L))).thenReturn(List.of(servicoAtivo));

        CarrinhoResponseDTO response = carrinhoService.adicionarItem(usuarioClienteId, dto);

        assertNotNull(response);
        assertEquals(1, response.getItens().size());
        assertEquals(BigDecimal.valueOf(200.0), response.getValorTotal());
        verify(itemCarrinhoRepository, times(1)).save(any(ItemCarrinho.class));
    }

    @Test
    void adicionarItem_ProprioServicoLancaExcecao() {
        ItemCarrinhoRequestDTO dto = new ItemCarrinhoRequestDTO(50L, 1);

        when(carrinhoRepository.findByUsuarioIdAndStatus(usuarioPrestadorId, StatusCarrinho.ATIVO))
                .thenReturn(Optional.of(new Carrinho(11L, usuarioPrestadorId, StatusCarrinho.ATIVO, LocalDateTime.now(), LocalDateTime.now())));
        when(servicoRepository.findById(50L)).thenReturn(Optional.of(servicoAtivo));
        when(perfilPrestadorRepository.findById(20L)).thenReturn(Optional.of(prestadorAprovado));

        assertThrows(CompraProprioServicoException.class, () -> {
            carrinhoService.adicionarItem(usuarioPrestadorId, dto);
        });
    }

    @Test
    void adicionarItem_ServicoInativoLancaExcecao() {
        servicoAtivo.setStatusPublicacao(StatusPublicacao.SUSPENSO);
        ItemCarrinhoRequestDTO dto = new ItemCarrinhoRequestDTO(50L, 1);

        when(carrinhoRepository.findByUsuarioIdAndStatus(usuarioClienteId, StatusCarrinho.ATIVO))
                .thenReturn(Optional.of(carrinhoAtivo));
        when(servicoRepository.findById(50L)).thenReturn(Optional.of(servicoAtivo));

        assertThrows(ServicoInativoException.class, () -> {
            carrinhoService.adicionarItem(usuarioClienteId, dto);
        });
    }

    @Test
    void adicionarItem_PrestadorNaoAprovadoLancaExcecao() {
        prestadorAprovado.setStatusAprovacao(StatusAprovacao.PENDENTE);
        ItemCarrinhoRequestDTO dto = new ItemCarrinhoRequestDTO(50L, 1);

        when(carrinhoRepository.findByUsuarioIdAndStatus(usuarioClienteId, StatusCarrinho.ATIVO))
                .thenReturn(Optional.of(carrinhoAtivo));
        when(servicoRepository.findById(50L)).thenReturn(Optional.of(servicoAtivo));
        when(perfilPrestadorRepository.findById(20L)).thenReturn(Optional.of(prestadorAprovado));

        assertThrows(PerfilPrestadorNaoAprovadoException.class, () -> {
            carrinhoService.adicionarItem(usuarioClienteId, dto);
        });
    }

    @Test
    void atualizarQuantidade_ItemDeOutroUsuarioLancaExcecao() {
        ItemCarrinho itemOutro = new ItemCarrinho(100L, 99L, 50L, 2, BigDecimal.valueOf(100.0), LocalDateTime.now());

        when(itemCarrinhoRepository.findById(100L)).thenReturn(Optional.of(itemOutro));
        when(carrinhoRepository.findById(99L)).thenReturn(Optional.of(new Carrinho(99L, 999L, StatusCarrinho.ATIVO, LocalDateTime.now(), LocalDateTime.now())));

        assertThrows(ItemCarrinhoNaoEncontradoException.class, () -> {
            carrinhoService.atualizarQuantidade(usuarioClienteId, 100L, 5);
        });
    }

    @Test
    void removerItem_ItemDeOutroUsuarioLancaExcecao() {
        ItemCarrinho itemOutro = new ItemCarrinho(100L, 99L, 50L, 2, BigDecimal.valueOf(100.0), LocalDateTime.now());

        when(itemCarrinhoRepository.findById(100L)).thenReturn(Optional.of(itemOutro));
        when(carrinhoRepository.findById(99L)).thenReturn(Optional.of(new Carrinho(99L, 999L, StatusCarrinho.ATIVO, LocalDateTime.now(), LocalDateTime.now())));

        assertThrows(ItemCarrinhoNaoEncontradoException.class, () -> {
            carrinhoService.removerItem(usuarioClienteId, 100L);
        });
    }
}
