package com.br.leone.repository;

import com.br.leone.entity.User;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends ListCrudRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByCnpj(String cnpj);

    boolean existsByTelefone(String telefone);

    @Query("SELECT * FROM users WHERE cpf = :cpf")
    Optional<User> findByCpf(String cpf);
}
