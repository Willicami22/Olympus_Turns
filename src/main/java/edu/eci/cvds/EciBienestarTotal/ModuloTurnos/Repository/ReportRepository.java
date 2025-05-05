package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository extends MongoRepository<Report, String> {
}
