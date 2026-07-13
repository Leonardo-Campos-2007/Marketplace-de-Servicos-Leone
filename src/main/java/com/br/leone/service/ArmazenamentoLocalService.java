package com.br.leone.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ArmazenamentoLocalService implements ArmazenamentoService {

    private final Path diretorioBase;

    public ArmazenamentoLocalService(@Value("${leone.armazenamento.diretorio:uploads}") String diretorio) {
        this.diretorioBase = Paths.get(diretorio).toAbsolutePath().normalize();
        try {
            Files.createDirectories(diretorioBase);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar o diretório de uploads: " + diretorioBase, e);
        }
    }

    @Override
    public String salvar(MultipartFile arquivo, String nomeArmazenado) {
        Path destino = diretorioBase.resolve(nomeArmazenado).normalize();

        if (!destino.startsWith(diretorioBase)) {
            throw new IllegalArgumentException("Nome de arquivo armazenado inválido.");
        }

        try {
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar arquivo em: " + destino, e);
        }

        return destino.toString();
    }

    @Override
    public void deletar(String urlArquivo) {
        try {
            Files.deleteIfExists(Paths.get(urlArquivo));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao deletar arquivo em: " + urlArquivo, e);
        }
    }
}