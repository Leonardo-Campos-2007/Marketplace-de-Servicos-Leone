package com.br.leone.controller;

import com.br.leone.dto.ChatResponseDTO;
import com.br.leone.dto.MensagemRequestDTO;
import com.br.leone.dto.MensagemResponseDTO;
import com.br.leone.entity.Chat;
import com.br.leone.entity.Mensagem;
import com.br.leone.exception.ChatNaoEncontradoException;
import com.br.leone.repository.ChatRepository;
import com.br.leone.service.ChatService;
import com.br.leone.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/solicitacoes/{solicitacaoId}/chat")
public class ChatController {

    private final ChatService chatService;
    private final ChatRepository chatRepository;
    private final UserService userService;

    public ChatController(ChatService chatService, ChatRepository chatRepository, UserService userService) {
        this.chatService = chatService;
        this.chatRepository = chatRepository;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ChatResponseDTO> buscarChat(
            @PathVariable Long solicitacaoId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = obterUsuarioId(userDetails);
        boolean isAdmin = isAdmin(userDetails);

        Pageable pageable = PageRequest.of(pagina, tamanho);
        Page<Mensagem> mensagens = chatService.buscarMensagens(solicitacaoId, usuarioId, isAdmin, pageable);
        Page<MensagemResponseDTO> mensagensDTO = mensagens.map(MensagemResponseDTO::new);

        Chat chat = chatRepository.findBySolicitacaoId(solicitacaoId)
                .orElseThrow(() -> new ChatNaoEncontradoException(solicitacaoId));

        return ResponseEntity.ok(new ChatResponseDTO(chat, mensagensDTO));
    }

    @PostMapping("/mensagens")
    public ResponseEntity<MensagemResponseDTO> enviarMensagem(
            @PathVariable Long solicitacaoId,
            @Valid @RequestBody MensagemRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long usuarioId = obterUsuarioId(userDetails);
        Mensagem mensagem = chatService.enviarMensagem(solicitacaoId, usuarioId, dto.conteudo());
        return ResponseEntity.status(HttpStatus.CREATED).body(new MensagemResponseDTO(mensagem));
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