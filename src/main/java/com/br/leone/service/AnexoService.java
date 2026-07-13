package com.br.leone.service;

import com.br.leone.entity.Anexo;
import com.br.leone.entity.Chat;
import com.br.leone.exception.AnexoJaExisteException;
import com.br.leone.exception.ArquivoInvalidoException;
import com.br.leone.exception.ChatEncerradoException;
import com.br.leone.exception.ChatNaoEncontradoException;
import com.br.leone.repository.AnexoRepository;
import com.br.leone.repository.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AnexoService {

    private static final long TAMANHO_MAXIMO_BYTES = 5L * 1024 * 1024; // 5MB

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final AnexoRepository anexoRepository;
    private final ChatRepository chatRepository;
    private final ChatService chatService;
    private final ArmazenamentoService armazenamentoService;

    public AnexoService(AnexoRepository anexoRepository,
                        ChatRepository chatRepository,
                        ChatService chatService,
                        ArmazenamentoService armazenamentoService) {
        this.anexoRepository = anexoRepository;
        this.chatRepository = chatRepository;
        this.chatService = chatService;
        this.armazenamentoService = armazenamentoService;
    }

    @Transactional
    public Anexo anexar(Long solicitacaoId, Long mensagemId, MultipartFile arquivo, Long usuarioId) {
        Chat chat = chatRepository.findBySolicitacaoId(solicitacaoId)
                .orElseThrow(() -> new ChatNaoEncontradoException(solicitacaoId));

        chatService.validarParticipante(chat, usuarioId);

        if (!chat.getAtivo()) {
            throw new ChatEncerradoException();
        }

        if (anexoRepository.existsByMensagemId(mensagemId)) {
            throw new AnexoJaExisteException();
        }

        validarTamanho(arquivo);
        String tipoReal = validarEDetectarTipoReal(arquivo);

        String extensao = extensaoParaTipo(tipoReal);
        String nomeArmazenado = UUID.randomUUID() + extensao;

        String urlArquivo;
        try {
            urlArquivo = armazenamentoService.salvar(arquivo, nomeArmazenado);
        } catch (Exception e) {
            throw new UncheckedIOException("Falha ao salvar o arquivo enviado.", new IOException(e));
        }

        try {
            Anexo anexo = new Anexo(mensagemId, arquivo.getOriginalFilename(), nomeArmazenado,
                    urlArquivo, tipoReal, arquivo.getSize());
            return anexoRepository.save(anexo);
        } catch (Exception e) {
            // Rollback manual: o disco não participa da transação do banco,
            // então se o save falhar depois do arquivo já gravado, o arquivo fica órfão.
            armazenamentoService.deletar(urlArquivo);
            throw e;
        }
    }

    public Optional<Anexo> buscarPorMensagem(Long solicitacaoId, Long mensagemId, Long usuarioId, boolean isAdmin) {
        Chat chat = chatRepository.findBySolicitacaoId(solicitacaoId)
                .orElseThrow(() -> new ChatNaoEncontradoException(solicitacaoId));

        if (!isAdmin) {
            chatService.validarParticipante(chat, usuarioId);
        }

        return anexoRepository.findByMensagemId(mensagemId);
    }

    private void validarTamanho(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("Arquivo vazio não é permitido.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new ArquivoInvalidoException("Arquivo excede o tamanho máximo permitido de 5MB.");
        }
    }

    /**
     * Valida o tipo real do arquivo pelos primeiros bytes (magic bytes),
     * ignorando o Content-Type declarado pelo cliente, que pode ser forjado.
     */
    private String validarEDetectarTipoReal(MultipartFile arquivo) {
        byte[] cabecalho;
        try {
            cabecalho = arquivo.getInputStream().readNBytes(12);
        } catch (IOException e) {
            throw new ArquivoInvalidoException("Não foi possível ler o arquivo enviado.");
        }

        if (correspondeAssinatura(cabecalho, new int[]{0xFF, 0xD8, 0xFF})) {
            return "image/jpeg";
        }
        if (correspondeAssinatura(cabecalho, new int[]{0x89, 0x50, 0x4E, 0x47})) {
            return "image/png";
        }
        if (cabecalho.length >= 12
                && correspondeAssinatura(Arrays.copyOfRange(cabecalho, 0, 4), new int[]{0x52, 0x49, 0x46, 0x46})
                && correspondeAssinatura(Arrays.copyOfRange(cabecalho, 8, 12), new int[]{0x57, 0x45, 0x42, 0x50})) {
            return "image/webp";
        }

        throw new ArquivoInvalidoException(
                "Tipo de arquivo não permitido. Apenas JPEG, PNG e WebP são aceitos.");
    }

    private boolean correspondeAssinatura(byte[] cabecalho, int[] assinatura) {
        if (cabecalho.length < assinatura.length) {
            return false;
        }
        for (int i = 0; i < assinatura.length; i++) {
            if ((cabecalho[i] & 0xFF) != assinatura[i]) {
                return false;
            }
        }
        return true;
    }

    private String extensaoParaTipo(String tipoConteudo) {
        return switch (tipoConteudo) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new ArquivoInvalidoException("Tipo de arquivo não suportado.");
        };
    }
}