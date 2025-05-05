package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.FileModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileRepository extends MongoRepository<FileModel, String> {
}
