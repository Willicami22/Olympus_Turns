package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Controller;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.ReportRepository;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "*")
@Tag(name = "Exportación", description = "API para exportar reportes en diferentes formatos")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @Autowired
    private ReportRepository reportRepository;

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

    @GetMapping("/excel/{reportId}")
    @Operation(
            summary = "Exportar reporte a Excel",
            description = "Exporta un reporte específico a formato Excel (.xlsx)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Archivo Excel generado exitosamente",
                    content = @Content(
                            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado - Token inválido",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso prohibido - Rol insuficiente",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reporte no encontrado",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    public ResponseEntity<?> exportToExcel(
            @Parameter(
                    description = "ID del reporte a exportar",
                    required = true,
                    example = "663c9f5d98a432000a4e567d"
            )
            @PathVariable String reportId,
            @RequestHeader(value = "Authorization", required = true) String authHeader
    ) {
        try {
            // Solo los usuarios con rol Medical_Secretary pueden exportar reportes
            checkAuthorization(authHeader, "Medical_Secretary");

            Optional<Report> reportOpt = reportRepository.findById(reportId);
            if (reportOpt.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Reporte no encontrado");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            Report report = reportOpt.get();
            byte[] excelData = exportService.exportReportToExcel(report);

            String filename = generateFilename(report, "xlsx");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

        } catch (JWTVerificationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No autorizado: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al exportar a Excel: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/pdf/{reportId}")
    @Operation(
            summary = "Exportar reporte a PDF",
            description = "Exporta un reporte específico a formato PDF"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Archivo PDF generado exitosamente",
                    content = @Content(
                            mediaType = "application/pdf"
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autorizado - Token inválido",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acceso prohibido - Rol insuficiente",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reporte no encontrado",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    public ResponseEntity<?> exportToPdf(
            @Parameter(
                    description = "ID del reporte a exportar",
                    required = true,
                    example = "663c9f5d98a432000a4e567d"
            )
            @PathVariable String reportId,
            @RequestHeader(value = "Authorization", required = true) String authHeader
    ) {
        try {
            // Solo los usuarios con rol Medical_Secretary pueden exportar reportes
            checkAuthorization(authHeader, "Medical_Secretary");

            Optional<Report> reportOpt = reportRepository.findById(reportId);
            if (reportOpt.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Reporte no encontrado");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }

            Report report = reportOpt.get();
            byte[] pdfData = exportService.exportReportToPdf(report);

            String filename = generateFilename(report, "pdf");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfData.length);

            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);

        } catch (JWTVerificationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No autorizado: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al exportar a PDF: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Genera un nombre de archivo descriptivo basado en la información del reporte
     */
    private String generateFilename(Report report, String extension) {
        String dateFormat = report.getInitialDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDateFormat = report.getFinalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        StringBuilder filename = new StringBuilder("reporte_turnos_");
        filename.append(dateFormat);
        filename.append("_a_");
        filename.append(endDateFormat);

        if (report.getUserRole() != null) {
            filename.append("_").append(report.getUserRole().toString().toLowerCase());
        }

        filename.append(".").append(extension);

        return filename.toString();
    }
}