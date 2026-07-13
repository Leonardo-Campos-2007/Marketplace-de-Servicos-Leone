package com.br.leone.controller;

import com.br.leone.dto.AnexoResponseDTO;
import com.br.leone.entity.Anexo;
import com.br.leone.exception.AnexoJaExisteException;
import com.br.leone.exception.AnexoNaoEncontradoException;
import com.br.leone.service.AnexoService;
import com.br.leone.service.UserService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/solicitacoes/{solicitacaoId}/chat/mensagens/{mensagemId}/anexo")
public class AnexoController {

    private final AnexoService anexoService;
    private final UserService userService;

    public AnexoController(AnexoService anexoService, UserService userService) {
        this.anexoService = anexoService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnexoResponseDTO> anexar(
            @PathVariable Long solicitacaoId,
            @PathVariable Long mensagemId,
            @RequestParam("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = obterUsuarioId(userDetails);
        Anexo anexo = anexoService.anexar(solicitacaoId, mensagemId, arquivo, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AnexoResponseDTO(anexo));
    }

    @GetMapping
    public ResponseEntity<AnexoResponseDTO> buscarMetadados(
            @PathVariable Long solicitacaoId,
            @PathVariable Long mensagemId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = obterUsuarioId(userDetails);
        boolean isAdmin = isAdmin(userDetails);

        return anexoService.buscarPorMensagem(solicitacaoId, mensagemId, usuarioId, isAdmin)
                .map(anexo -> ResponseEntity.ok(new AnexoResponseDTO(anexo)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> baixar(
            @PathVariable Long solicitacaoId,
            @PathVariable Long mensagemId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = obterUsuarioId(userDetails);
        boolean isAdmin = isAdmin(userDetails);

        Anexo anexo = anexoService.buscarPorMensagem(solicitacaoId, mensagemId, usuarioId, isAdmin)
                .orElseThrow(AnexoNaoEncontradoException::new); // ver nota abaixo

        Resource recurso = new FileSystemResource(anexo.getUrlArquivo());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(anexo.getTipoConteudo()))
                .header("Content-Disposition", "inline; filename=\"" + anexo.getNomeArquivoOriginal() + "\"")
                .body(recurso);
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