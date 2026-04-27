package com.climbe.api_climbe.repository;

import com.climbe.api_climbe.model.PropostaArquivo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropostaArquivoRepository extends JpaRepository<PropostaArquivo, Integer> {

    Optional<PropostaArquivo> findByPropostaIdProposta(Integer idProposta);

    boolean existsByPropostaIdProposta(Integer idProposta);
}
