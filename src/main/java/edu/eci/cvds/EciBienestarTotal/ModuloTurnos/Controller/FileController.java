package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Controller;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.FileModel;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.FileRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/files")
@Tag(name = "Contenido Multimedia", description = "API para la gestion de archivos")
public class FileController {

    @Autowired
    private FileRepository fileRepo;

    private final String SECRET_KEY = "supersecretpassword1234567891011121314";

    /**
     * Método para validar el token JWT usando com.auth0.jwt
     * @param token Token JWT a validar
     * @return DecodedJWT del token si es válido
     * @throws JWTVerificationException Si el token no es válido
     */
    private DecodedJWT validateToken(String token) throws JWTVerificationException {
        if (token == null || token.isEmpty()) {
            throw new JWTVerificationException("Token no proporcionado");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT jwt = verifier.verify(token);

        String[] requiredClaims = {"id", "userName", "email", "name", "role"};

        for (String claim : requiredClaims) {
            if (jwt.getClaim(claim).isNull() || jwt.getClaim(claim).asString() == null || jwt.getClaim(claim).asString().isEmpty()) {
                throw new JWTVerificationException("El token no contiene el campo requerido: " + claim);
            }
        }

        return jwt;
    }


    /**
     * Método de verificación de autorización
     * @param authHeader Header de autorización con el token JWT
     * @param requiredRole Rol requerido para acceder al recurso (opcional)
     * @return true si el token es válido y tiene el rol requerido
     * @throws JWTVerificationException Si el token no es válido o no tiene el rol requerido
     */
    private boolean checkAuthorization(String authHeader, String requiredRole) throws JWTVerificationException {
        DecodedJWT jwt = validateToken(authHeader);

        if (requiredRole != null && !requiredRole.isEmpty()) {
            String userRole = jwt.getClaim("role").asString();
            if (!requiredRole.equals(userRole)) {
                throw new JWTVerificationException("No tiene permisos suficientes para esta operación");
            }
        }

        return true;
    }

    @Operation(summary = "Subir un archivo (requiere autenticación)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo subido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token inválido", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error al subir archivo", content = @Content)
    })
    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @Parameter(description = "Token JWT", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Archivo a subir", required = true)
            @RequestParam("file") MultipartFile file) throws IOException {

        try {
            checkAuthorization(authHeader, null);

            FileModel newFile = new FileModel();
            newFile.setName(file.getOriginalFilename());
            newFile.setContentType(file.getContentType());
            newFile.setData(file.getBytes()); // Aquí puede lanzar IOException
            fileRepo.save(newFile);

            return ResponseEntity.ok("File uploaded successfully: " + newFile.getId());
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(401).body("Error de autenticación: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al leer archivo");
        }
    }

    @Operation(summary = "Obtener un archivo por su ID (requiere autenticación)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Archivo no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFile(
            @Parameter(description = "Token JWT", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "ID del archivo") @PathVariable String id) {

        try {
            // Validar token JWT (sin rol específico requerido)
            checkAuthorization(authHeader, null);

            Optional<FileModel> file = fileRepo.findById(id);
            return file.map(f -> ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, f.getContentType())
                            .body(f.getData()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Archivo no encontrado".getBytes(StandardCharsets.UTF_8));
        }
    }

    @Operation(summary = "Listar todos los archivos - sin datos binarios (requiere autenticación)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de archivos obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token inválido", content = @Content)
    })
    @GetMapping("/")
    public ResponseEntity<?> getAllFiles(
            @Parameter(description = "Token JWT", required = true)
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Validar token JWT (sin rol específico requerido)
            checkAuthorization(authHeader, null);

            List<FileModel> files = fileRepo.findAll().stream()
                    .map(file -> {
                        FileModel f = new FileModel();
                        f.setId(file.getId());
                        f.setName(file.getName());
                        f.setContentType(file.getContentType());
                        return f;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(files);
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(401).body("Error de autenticación: " + e.getMessage());
        }
    }

    @Operation(summary = "Eliminar un archivo por ID (requiere autenticación)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo eliminado correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Archivo no encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(
            @Parameter(description = "Token JWT", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "ID del archivo a eliminar") @PathVariable String id) {

        try {
            // Validar token JWT (sin rol específico requerido)
            checkAuthorization(authHeader, null);

            if (fileRepo.existsById(id)) {
                fileRepo.deleteById(id);
                return ResponseEntity.ok("Archivo eliminado correctamente: " + id);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(401).body("Error de autenticación: " + e.getMessage());
        }
    }
}

