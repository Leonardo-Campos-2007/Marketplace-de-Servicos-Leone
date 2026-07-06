package com.br.leone.controller;

import com.br.leone.dto.PerfilPrestadorRequestDTO;
import com.br.leone.dto.PerfilPrestadorResponseDTO;
import com.br.leone.entity.PerfilPrestador;
import com.br.leone.service.PerfilPrestadorService;
import com.br.leone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/prestadores")
public class PerfilPrestadorController {

    private final PerfilPrestadorService perfilService;
    private final UserService userService;

    public PerfilPrestadorController(PerfilPrestadorService perfilService, UserService userService) {
        this.perfilService = perfilService;
        this.userService = userService;
    }

    // 1. SOLICITAR PERFIL (Qualquer usuário autenticado)
    @PostMapping
    public ResponseEntity<PerfilPrestadorResponseDTO> criar(@Valid @RequestBody PerfilPrestadorRequestDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioAutenticadoId = obterUsuarioId(userDetails);

        PerfilPrestador perfil = new PerfilPrestador();
        perfil.setUsuarioId(usuarioAutenticadoId); // Vincula o perfil automaticamente ao ID de quem está logado
        perfil.setNomeFantasia(dto.nomeFantasia());
        perfil.setDescricao(dto.descricao());
        perfil.setAreaAtuacao(dto.areaAtuacao());

        PerfilPrestador novoPerfil = perfilService.criar(perfil);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PerfilPrestadorResponseDTO(novoPerfil));
    }

    // 2. BUSCAR O PRÓPRIO PERFIL (Baseado no Token)
    @GetMapping("/meu-perfil")
    public ResponseEntity<PerfilPrestadorResponseDTO> buscarMeuPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioAutenticadoId = obterUsuarioId(userDetails);
        return perfilService.buscarPorUsuarioId(usuarioAutenticadoId)
                .map(perfil -> ResponseEntity.ok(new PerfilPrestadorResponseDTO(perfil)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. LISTAR TODOS OS PERFIS (Público/Clientes)
    @GetMapping
    public ResponseEntity<List<PerfilPrestadorResponseDTO>> listarTodos() {
        List<PerfilPrestadorResponseDTO> perfis = perfilService.listarTodos().stream()
                .map(PerfilPrestadorResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(perfis);
    }

    // 4. BUSCAR PERFIL POR ID (Público)
    @GetMapping("/{id}")
    public ResponseEntity<PerfilPrestadorResponseDTO> buscarPorId(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioLogadoId = null;
        boolean isAdmin = false;

        if (userDetails != null) {
            isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            usuarioLogadoId = obterUsuarioId(userDetails);
        }

        PerfilPrestador perfil = perfilService.buscarPorIdProtegido(id, usuarioLogadoId, isAdmin);
        return ResponseEntity.ok(new PerfilPrestadorResponseDTO(perfil));
    }

    // 5. ATUALIZAR PERFIL (Apenas o próprio dono)
    @PutMapping("/{id}")
    public ResponseEntity<PerfilPrestadorResponseDTO> atualizar(@PathVariable Long id,
                                                                @Valid @RequestBody PerfilPrestadorRequestDTO dto,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        Long usuarioAutenticadoId = obterUsuarioId(userDetails);

        PerfilPrestador dadosNovos = new PerfilPrestador();
        dadosNovos.setNomeFantasia(dto.nomeFantasia());
        dadosNovos.setDescricao(dto.descricao());
        dadosNovos.setAreaAtuacao(dto.areaAtuacao());

        PerfilPrestador perfilAtualizado = perfilService.atualizar(id, dadosNovos, usuarioAutenticadoId);
        return ResponseEntity.ok(new PerfilPrestadorResponseDTO(perfilAtualizado));
    }

    // ================= ROTAS ADMINISTRATIVAS (ROLE_ADMIN) =================

    // 6. LISTAR PERFIS PENDENTES DE APROVAÇÃO
    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PerfilPrestadorResponseDTO>> listarPendentes() {
        List<PerfilPrestadorResponseDTO> perfis = perfilService.listarPendentes().stream()
                .map(PerfilPrestadorResponseDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(perfis);
    }

    // 7. APROVAR SOLICITAÇÃO DE PERFIL
    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PerfilPrestadorResponseDTO> aprovar(@PathVariable Long id) {
        return ResponseEntity.ok(new PerfilPrestadorResponseDTO(perfilService.aprovarPerfil(id)));
    }

    // 8. REJEITAR / EXCLUIR PERFIL
    @DeleteMapping("/{id}/rejeitar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejeitar(@PathVariable Long id) {
        perfilService.rejeitarPerfil(id);
        return ResponseEntity.noContent().build();
    }

    // Método utilitário para extrair ID do usuário logado via Contexto de Segurança
    private Long obterUsuarioId(UserDetails userDetails) {
        return userService.buscarPorEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado."))
                .getId();
    }
}
