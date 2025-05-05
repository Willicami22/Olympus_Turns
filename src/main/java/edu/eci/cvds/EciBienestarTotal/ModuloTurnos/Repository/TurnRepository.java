package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Turn;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TurnRepository extends MongoRepository<Turn, String> {
}
