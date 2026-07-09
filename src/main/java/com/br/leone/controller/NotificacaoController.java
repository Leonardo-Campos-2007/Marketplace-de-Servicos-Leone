package com.br.leone.controller;

import com.br.leone.dto.NotificacaoPageResponseDTO;
import com.br.leone.dto.NotificacaoResponseDTO;
import com.br.leone.entity.Notificacao;
import com.br.leone.service.NotificacaoService;
import com.br.leone.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final UserService userService;

    public NotificacaoController(NotificacaoService notificacaoService, UserService userService) {
        this.notificacaoService = notificacaoService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<NotificacaoPageResponseDTO> listar(
            @RequestParam(defaultValue = "false") boolean apenasNaoLidas,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = obterUsuarioId(userDetails);
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("dataCriacao").descending());

        Page<Notificacao> resultado = notificacaoService.listar(usuarioId, apenasNaoLidas, pageable);
        Page<NotificacaoResponseDTO> resultadoDTO = resultado.map(NotificacaoResponseDTO::new);

        return ResponseEntity.ok(new NotificacaoPageResponseDTO(resultadoDTO));
    }

    @PatchMapping("/{id}/marcar-lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = obterUsuarioId(userDetails);
        notificacaoService.marcarComoLida(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/marcar-todas-lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(@AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioId = obterUsuarioId(userDetails);
        notificacaoService.marcarTodasComoLidas(usuarioId);
        return ResponseEntity.noContent().build();
    }

    private Long obterUsuarioId(UserDetails userDetails) {
        return userService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."))
                .getId();
    }
}