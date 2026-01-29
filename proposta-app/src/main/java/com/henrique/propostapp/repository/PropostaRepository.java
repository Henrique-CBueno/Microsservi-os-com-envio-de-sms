package com.henrique.propostapp.repository;

import com.henrique.propostapp.entity.Proposta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PropostaRepository extends JpaRepository<Proposta, Long> {
    List<Proposta> findAllByIntegradaFalse(Pageable pageable);

    @Transactional
    @Modifying
    @Query("""
        UPDATE Proposta p
        SET p.aprovada = :aprovada,
            p.observacao = :observacao
        WHERE p.id = :id
    """)
    void atualizarProposta(Long id, boolean aprovada, String observacao);

    @Transactional
    @Modifying
    @Query("""
        UPDATE Proposta p
        SET p.integrada = :integrada
        WHERE p.id = :id
    """)
    void atualizarStatusIntegrada(Long id, boolean integrada);
}
