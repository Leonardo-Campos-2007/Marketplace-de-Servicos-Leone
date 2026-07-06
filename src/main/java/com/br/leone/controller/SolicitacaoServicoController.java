package com.br.leone.controller;

import com.br.leone.dto.AlterarStatusRequestDTO;
import com.br.leone.dto.HistoricoStatusResponseDTO;
import com.br.leone.dto.ItemSolicitacaoResponseDTO;
import com.br.leone.dto.SolicitacaoResponseDTO;
import com.br.leone.entity.HistoricoStatusSolicitacao;
import com.br.leone.entity.ItemSolicitacao;
import com.br.leone.entity.SolicitacaoServico;
import com.br.leone.repository.HistoricoStatusSolicitacaoRepository;
import com.br.leone.repository.ItemSolicitacaoRepository;
import com.br.leone.service.SolicitacaoServicoService;
import com.br.leone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/solicitacoes")
public class SolicitacaoServicoController {

    private final SolicitacaoServicoService solicitacaoService;
    private final UserService userService;
    private final ItemSolicitacaoRepository itemSolicitacaoRepository;
    private final HistoricoStatusSolicitacaoRepository historicoRepository;

    public SolicitacaoServicoController(SolicitacaoServicoService solicitacaoService,
                                        UserService userService,
                                        ItemSolicitacaoRepository itemSolicitacaoRepository,
                                        HistoricoStatusSolicitacaoRepository historicoRepository) {
        this.solicitacaoService = solicitacaoService;
        this.userService = userService;
        this.itemSolicitacaoRepository = itemSolicitacaoRepository;
        this.historicoRepository = historicoRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<List<SolicitacaoResponseDTO>> checkout(@AuthenticationPrincipal UserDetails userDetails) {
        Long compradorId = obterUsuarioId(userDetails);
        List<SolicitacaoServico> criadas = solicitacaoService.checkout(compradorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(converterParaDTOs(criadas));
    }

    @GetMapping
    public ResponseEntity<List<SolicitacaoResponseDTO>> listarComoComprador(@AuthenticationPrincipal UserDetails userDetails) {
        Long compradorId = obterUsuarioId(userDetails);
        List<SolicitacaoServico> lista = solicitacaoService.listarComoComprador(compradorId);
        return ResponseEntity.ok(converterParaDTOs(lista));
    }

    @GetMapping("/prestador")
    public ResponseEntity<List<SolicitacaoResponseDTO>> listarComoPrestador(@AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = obterUsuarioId(userDetails);
        List<SolicitacaoServico> lista = solicitacaoService.listarComoPrestador(usuarioId);
        return ResponseEntity.ok(converterParaDTOs(lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoResponseDTO> buscarPorId(@PathVariable Long id,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = obterUsuarioId(userDetails);
        boolean isAdmin = isAdmin(userDetails);
        SolicitacaoServico s = solicitacaoService.buscarPorIdProtegido(id, usuarioId, isAdmin);
        return ResponseEntity.ok(converterParaDTOs(List.of(s)).get(0));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(@PathVariable Long id,
                                              @Valid @RequestBody AlterarStatusRequestDTO dto,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = obterUsuarioId(userDetails);
        boolean isAdmin = isAdmin(userDetails);
        solicitacaoService.alterarStatus(id, usuarioId, dto.status(), dto.observacao(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    private List<SolicitacaoResponseDTO> converterParaDTOs(List<SolicitacaoServico> solicitacoes) {
        if (solicitacoes.isEmpty()) {
            return List.of();
        }

        List<Long> ids = solicitacoes.stream().map(SolicitacaoServico::getId).toList();

        Map<Long, List<ItemSolicitacao>> itensPorSolicitacao = itemSolicitacaoRepository
                .findBySolicitacaoIdIn(ids).stream()
                .collect(Collectors.groupingBy(ItemSolicitacao::getSolicitacaoId));

        Map<Long, List<HistoricoStatusSolicitacao>> historicoPorSolicitacao = ids.stream()
                .collect(Collectors.toMap(id -> id, historicoRepository::findBySolicitacaoId));

        return solicitacoes.stream()
                .map(s -> new SolicitacaoResponseDTO(
                        s,
                        itensPorSolicitacao.getOrDefault(s.getId(), List.of()).stream()
                                .map(ItemSolicitacaoResponseDTO::new)
                                .toList(),
                        historicoPorSolicitacao.getOrDefault(s.getId(), List.of()).stream()
                                .map(HistoricoStatusResponseDTO::new)
                                .toList()
                ))
                .toList();
    }

    private Long obterUsuarioId(UserDetails userDetails) {
        return userService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."))
                .getId();
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
