package com.br.leone.controller;

import com.br.leone.dto.AlterarStatusRequestDTO;
import com.br.leone.dto.SolicitacaoResponseDTO;
import com.br.leone.entity.SolicitacaoServico;
import com.br.leone.enums.StatusSolicitacao;
import com.br.leone.service.SolicitacaoServicoService;
import com.br.leone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/solicitacoes")
public class SolicitacaoServicoController {

    private final SolicitacaoServicoService solicitacaoService;
    private final UserService userService;

    public SolicitacaoServicoController(SolicitacaoServicoService solicitacaoService, UserService userService) {
        this.solicitacaoService = solicitacaoService;
        this.userService = userService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<List<SolicitacaoResponseDTO>> checkout(@AuthenticationPrincipal UserDetails userDetails) {
        Long compradorId = userService.buscarPorEmail(userDetails.getUsername()).orElseThrow().getId();
        List<SolicitacaoServico> criadas = solicitacaoService.checkout(compradorId);
        // simples conversão sem itens/histórico detalhado para agora
        List<SolicitacaoResponseDTO> dtos = criadas.stream()
                .map(s -> new SolicitacaoResponseDTO(s, List.of(), List.of()))
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    @GetMapping
    public ResponseEntity<List<SolicitacaoResponseDTO>> listarComoComprador(@AuthenticationPrincipal UserDetails userDetails) {
        Long compradorId = userService.buscarPorEmail(userDetails.getUsername()).orElseThrow().getId();
        List<SolicitacaoServico> lista = solicitacaoService.listarComoComprador(compradorId);
        List<SolicitacaoResponseDTO> dtos = lista.stream().map(s -> new SolicitacaoResponseDTO(s, List.of(), List.of())).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/prestador")
    public ResponseEntity<List<SolicitacaoResponseDTO>> listarComoPrestador(@AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = userService.buscarPorEmail(userDetails.getUsername()).orElseThrow().getId();
        List<SolicitacaoServico> lista = solicitacaoService.listarComoPrestador(usuarioId);
        List<SolicitacaoResponseDTO> dtos = lista.stream().map(s -> new SolicitacaoResponseDTO(s, List.of(), List.of())).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoResponseDTO> buscarPorId(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = userService.buscarPorEmail(userDetails.getUsername()).orElseThrow().getId();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        var s = solicitacaoService.buscarPorIdProtegido(id, usuarioId, isAdmin);
        return ResponseEntity.ok(new SolicitacaoResponseDTO(s, List.of(), List.of()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(@PathVariable Long id, @Valid @RequestBody AlterarStatusRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = userService.buscarPorEmail(userDetails.getUsername()).orElseThrow().getId();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        solicitacaoService.alterarStatus(id, usuarioId, dto.status(), dto.observacao(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
