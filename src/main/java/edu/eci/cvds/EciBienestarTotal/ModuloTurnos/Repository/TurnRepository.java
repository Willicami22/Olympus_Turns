package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Turn;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TurnRepository extends MongoRepository<Turn, String> {
    long countBySpecializationAndDate(Specialization specEnum, LocalDate now);
    List<Turn> findBySpecializationAndDateAndStatus(Specialization specEnum, LocalDate now, String active);
    Optional<Turn> findFirstBySpecializationAndDateAndPriorityIsTrueAndStatusOrderByInitialTimeAsc(Specialization specEnum, LocalDate now, String active);
    Optional<Turn> findFirstBySpecializationAndDateAndPriorityIsFalseAndStatusOrderByInitialTimeAsc(Specialization specEnum, LocalDate now, String active);
    Optional<Turn> findFirstBySpecializationAndStatus(Specialization specEnum, String attending);
    List<Turn> findByDateBetween(LocalDate from, LocalDate to);
    Optional<Turn> findById(String id);
}
