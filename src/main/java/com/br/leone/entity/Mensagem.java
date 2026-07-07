package com.br.leone.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("mensagem")
public class Mensagem {

    @Id
    private Long id;

    @NotNull(message = "Chat é obrigatório")
    @Column("chat_id")
    private Long chatId;

    @NotNull(message = "Remetente é obrigatório")
    @Column("remetente_id")
    private Long remetenteId;

    @NotBlank(message = "Conteúdo da mensagem não pode ser vazio")
    @Column("conteudo")
    private String conteudo;

    @Column("data_envio")
    private LocalDateTime dataEnvio = LocalDateTime.now();

    @Column("lida")
    private Boolean lida = false;

    protected Mensagem() {}

    public Mensagem(Long chatId, Long remetenteId, String conteudo) {
        this.chatId = chatId;
        this.remetenteId = remetenteId;
        this.conteudo = conteudo;
        this.dataEnvio = LocalDateTime.now();
        this.lida = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public Long getRemetenteId() { return remetenteId; }
    public void setRemetenteId(Long remetenteId) { this.remetenteId = remetenteId; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public Boolean getLida() { return lida; }
    public void setLida(Boolean lida) { this.lida = lida; }
}