package com.climbe.api_climbe.seeder;

import com.climbe.api_climbe.model.Cargo;
import com.climbe.api_climbe.repository.CargoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargoSeederTest {

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private ApplicationArguments applicationArguments;

    @InjectMocks
    private CargoSeeder cargoSeeder;

    @BeforeEach
    void setUp() {
        reset(cargoRepository);
    }

    @Test
    void deveCriarCargoQuandoNaoExistir() {
        // Arrange
        when(cargoRepository.findByNomeCargo(anyString())).thenReturn(Optional.empty());
        when(cargoRepository.save(any(Cargo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        cargoSeeder.run(applicationArguments);

        // Assert - deve criar 12 cargos padrão
        verify(cargoRepository, times(12)).save(any(Cargo.class));
    }

    @Test
    void naoDeveCriarCargoDuplicado() {
        // Arrange
        Cargo cargoExistente = new Cargo();
        cargoExistente.setNomeCargo("CEO");
        
        when(cargoRepository.findByNomeCargo(anyString())).thenReturn(Optional.of(cargoExistente));

        // Act
        cargoSeeder.run(applicationArguments);

        // Assert
        verify(cargoRepository, never()).save(any(Cargo.class));
    }

    @Test
    void deveCriarTodosOsCargosPadrao() {
        // Arrange
        when(cargoRepository.findByNomeCargo(anyString())).thenReturn(Optional.empty());
        when(cargoRepository.save(any(Cargo.class))).thenAnswer(invocation -> {
            Cargo cargo = invocation.getArgument(0);
            return cargo;
        });

        // Act
        cargoSeeder.run(applicationArguments);

        // Assert - verifica que os 12 cargos foram criados
        verify(cargoRepository, times(12)).save(any(Cargo.class));
    }
}
