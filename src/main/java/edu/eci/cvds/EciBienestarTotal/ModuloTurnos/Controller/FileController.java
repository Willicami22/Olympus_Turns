package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Controller;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.FileModel;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.FileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileRepository fileRepo;

    @Operation(summary = "Subir un archivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo subido exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error al subir archivo", content = @Content)
    })
    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @Parameter(description = "Archivo a subir", required = true)
            @RequestParam("file") MultipartFile file) throws IOException {

        FileModel newFile = new FileModel();
        newFile.setName(file.getOriginalFilename());
        newFile.setContentType(file.getContentType());
        newFile.setData(file.getBytes());
        fileRepo.save(newFile);
        return ResponseEntity.ok("File uploaded successfully: " + newFile.getId());
    }

    @Operation(summary = "Obtener un archivo por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo encontrado"),
            @ApiResponse(responseCode = "404", description = "Archivo no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFile(
            @Parameter(description = "ID del archivo") @PathVariable String id) {
        Optional<FileModel> file = fileRepo.findById(id);
        return file.map(f -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, f.getContentType())
                        .body(f.getData()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los archivos (sin datos binarios)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de archivos obtenida exitosamente")
    })
    @GetMapping("/")
    public List<FileModel> getAllFiles() {
        return fileRepo.findAll().stream()
                .map(file -> {
                    FileModel f = new FileModel();
                    f.setId(file.getId());
                    f.setName(file.getName());
                    f.setContentType(file.getContentType());
                    return f;
                })
                .collect(Collectors.toList());
    }

    @Operation(summary = "Eliminar un archivo por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Archivo no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(
            @Parameter(description = "ID del archivo a eliminar") @PathVariable String id) {
        if (fileRepo.existsById(id)) {
            fileRepo.deleteById(id);
            return ResponseEntity.ok("Archivo eliminado correctamente: " + id);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

