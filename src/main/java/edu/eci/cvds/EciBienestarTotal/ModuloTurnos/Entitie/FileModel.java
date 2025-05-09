package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Media")
@Schema(description = "Modelo de archivo almacenado en la base de datos")
public class FileModel {

    @Id
    @Schema(description = "Identificador único del archivo", example = "6621c5c4f3c2a9001cefa3d8")
    private String id;

    @Schema(description = "Nombre original del archivo", example = "documento.pdf")
    private String name;

    @Schema(description = "Tipo de contenido MIME del archivo", example = "application/pdf")
    private String contentType;

    @Schema(description = "Contenido binario del archivo (no se muestra en listados)")
    private byte[] data;


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public byte[] getData() {
        return data;
    }
    public void setData(byte[] data) {
        this.data = data;
    }
}
