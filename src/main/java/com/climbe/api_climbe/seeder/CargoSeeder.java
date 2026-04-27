package com.climbe.api_climbe.seeder;

import com.climbe.api_climbe.model.Cargo;
import com.climbe.api_climbe.repository.CargoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class CargoSeeder implements ApplicationRunner {

    private final CargoRepository cargoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Iniciando CargoSeeder - garantindo cargos padrão da Climbe");

        String[] cargosPadrao = {
            "CEO",
            "Compliance",
            "CSO",
            "CMO",
            "CFO",
            "Analista VI Trainee",
            "Analista VI Jr",
            "Analista VI Pl",
            "Analista VI Sr",
            "Analista BPO Financeiro",
            "Contador",
            "Membro do Conselho"
        };

        for (String nomeCargo : cargosPadrao) {
            Optional<Cargo> cargoExistente = cargoRepository.findByNomeCargo(nomeCargo);

            if (cargoExistente.isEmpty()) {
                Cargo novoCargo = new Cargo();
                novoCargo.setNomeCargo(nomeCargo);
                cargoRepository.save(novoCargo);
                log.info("Criado cargo: {}", nomeCargo);
            } else {
                log.debug("Cargo já existe: {}", nomeCargo);
            }
        }

        log.info("CargoSeeder concluído com sucesso");
    }
}
