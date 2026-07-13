package com.br.leone.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("anexo")
public class Anexo {

    @Id
    private Long id;

    @NotNull(message = "Mensagem é obrigatória")
    @Column("mensagem_id")
    private Long mensagemId;

    @NotBlank(message = "Nome do arquivo original é obrigatório")
    @Column("nome_arquivo_original")
    private String nomeArquivoOriginal;

    @NotBlank(message = "Nome do arquivo armazenado é obrigatório")
    @Column("nome_arquivo_armazenado")
    private String nomeArquivoArmazenado;

    @NotBlank(message = "URL do arquivo é obrigatória")
    @Column("url_arquivo")
    private String urlArquivo;

    @NotBlank(message = "Tipo de conteúdo é obrigatório")
    @Column("tipo_conteudo")
    private String tipoConteudo;

    @NotNull(message = "Tamanho do arquivo é obrigatório")
    @Positive(message = "Tamanho do arquivo deve ser maior que zero")
    @Column("tamanho_bytes")
    private Long tamanhoBytes;

    @Column("data_upload")
    private LocalDateTime dataUpload = LocalDateTime.now();

    protected Anexo() {}

    public Anexo(Long mensagemId, String nomeArquivoOriginal, String nomeArquivoArmazenado,
                 String urlArquivo, String tipoConteudo, Long tamanhoBytes) {
        this.mensagemId = mensagemId;
        this.nomeArquivoOriginal = nomeArquivoOriginal;
        this.nomeArquivoArmazenado = nomeArquivoArmazenado;
        this.urlArquivo = urlArquivo;
        this.tipoConteudo = tipoConteudo;
        this.tamanhoBytes = tamanhoBytes;
        this.dataUpload = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMensagemId() { return mensagemId; }
    public void setMensagemId(Long mensagemId) { this.mensagemId = mensagemId; }

    public String getNomeArquivoOriginal() { return nomeArquivoOriginal; }
    public void setNomeArquivoOriginal(String nomeArquivoOriginal) { this.nomeArquivoOriginal = nomeArquivoOriginal; }

    public String getNomeArquivoArmazenado() { return nomeArquivoArmazenado; }
    public void setNomeArquivoArmazenado(String nomeArquivoArmazenado) { this.nomeArquivoArmazenado = nomeArquivoArmazenado; }

    public String getUrlArquivo() { return urlArquivo; }
    public void setUrlArquivo(String urlArquivo) { this.urlArquivo = urlArquivo; }

    public String getTipoConteudo() { return tipoConteudo; }
    public void setTipoConteudo(String tipoConteudo) { this.tipoConteudo = tipoConteudo; }

    public Long getTamanhoBytes() { return tamanhoBytes; }
    public void setTamanhoBytes(Long tamanhoBytes) { this.tamanhoBytes = tamanhoBytes; }

    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
}