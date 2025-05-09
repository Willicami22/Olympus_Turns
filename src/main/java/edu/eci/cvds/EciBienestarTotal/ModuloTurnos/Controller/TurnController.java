package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Controller;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO.ReportDTO;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO.TurnDTO;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Turn;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Disabilitie;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service.TurnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/turns")
@CrossOrigin(origins = "*")
@Tag(name = "Turnos", description = "API para la gestión de turnos de atención")
public class TurnController {

    @Autowired
    private TurnService turnService;

    @PostMapping("/create")
    @Operation(
            summary = "Crear un nuevo turno",
            description = "Crea un nuevo turno de atención basado en los datos proporcionados")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Turno creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Map.class,
                                    example = "{\"code\": \"T001\", \"message\": \"Turno creado exitosamente\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Map.class,
                                    example = "{\"error\": \"Error al crear el turno: Datos inválidos\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Map.class,
                                    example = "{\"error\": \"Error al crear el turno: Error interno\"}"
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, String>> createTurn(
            @Parameter(
                    description = "Datos del turno a crear",
                    required = true,
                    schema = @Schema(implementation = TurnDTO.class)
            )
            @RequestBody TurnDTO turnDTO
    ) {
        try {
            String code = turnService.createTurn(
                    turnDTO.getUserName(),
                    turnDTO.getIdentityDocument(),
                    turnDTO.getRole(),
                    turnDTO.getSpecialization(),
                    turnDTO.getDisabilitie()
            );

            Map<String, String> response = new HashMap<>();
            response.put("code", code);
            response.put("message", "Turno creado exitosamente");

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al crear el turno: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/pass")
    @Operation(
            summary = "Pasar al siguiente turno",
            description = "Marca como 'pasado' el turno actual y avanza al siguiente en la especialización indicada"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Turno pasado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Map.class,
                                    example = "{\"message\": \"Turno pasado exitosamente\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No hay turnos disponibles para pasar",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Map.class,
                                    example = "{\"error\": \"No hay turnos disponibles para la especialización indicada\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Map.class
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, String>> passTurn(
            @Parameter(
                    description = "Especialización del turno a pasar",
                    required = true,
                    example = "MedicinaGeneral"
            )
            @RequestParam String specialization
    ) {
        try {
            turnService.PassTurn(specialization);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Turno pasado exitosamente");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al pasar el turno: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/disable")
    @Operation(
            summary = "Deshabilitar turnos",
            description = "Deshabilita todos los turnos de una especialización específica"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Turnos deshabilitados exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Map.class,
                                    example = "{\"message\": \"Turnos deshabilitados exitosamente\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontraron turnos para la especialización",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Map.class,
                                    example = "{\"error\": \"No se encontraron turnos para la especialización indicada\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Map.class
                            )
                    )
            )
    })
    public ResponseEntity<Map<String, String>> disableTurn(
            @Parameter(
                    description = "Especialización de los turnos a deshabilitar",
                    required = true,
                    example = "MedicinaGeneral"
            )
            @RequestParam String specialization
    ) {
        try {
            turnService.DisableTurns(specialization);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Turnos deshabilitados exitosamente");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al deshabilitar los turnos: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/list")
    @Operation(
            summary = "Listar turnos por especialización",
            description = "Obtiene la lista de turnos activos para una especialización específica, ordenados por prioridad y hora de inicio"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de turnos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TurnDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = Map.class
                            )
                    )
            )
    })
    public ResponseEntity<?> getTurns(
            @Parameter(
                    description = "Especialización para filtrar los turnos",
                    required = true,
                    example = "MedicinaGeneral"
            )
            @RequestParam String specialization
    ) {
        try {
            List<Turn> turnos = turnService.getNextTurns(specialization);

            List<TurnDTO> turnosDTO = turnos.stream().map(turn -> {
                TurnDTO dto = new TurnDTO();
                dto.setCode(turn.getCode());
                dto.setUserName(turn.getPatient());
                dto.setSpecialization(turn.getSpecialization());
                dto.setState(turn.getStatus());
                dto.setIdentityDocument(turn.getIdentityDocument());
                dto.setRole(turn.getRole());
                dto.setDisabilitie(turn.getDisabilitie());
                return dto;
            }).toList();

            return new ResponseEntity<>(turnosDTO, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener la lista de turnos: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/generate")
    @Operation(
            summary = "Generar reporte",
            description = "Genera un reporte basado en un rango de fechas y opcionalmente filtrado por rol de usuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte generado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Report.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros inválidos",
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
    public ResponseEntity<Report> generateReport(
            @Parameter(
                    description = "Datos para generar el reporte",
                    required = true,
                    schema = @Schema(implementation = ReportDTO.class)
            )
            @RequestBody ReportDTO reportDTO
    ) {
        if (reportDTO.getInitialDate() == null || reportDTO.getFinalDate() == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Report report = turnService.generateReport(
                    reportDTO.getInitialDate(),
                    reportDTO.getFinalDate(),
                    reportDTO.getUserRole()
            );

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/specializations")
    @Operation(
            summary = "Obtener especializaciones disponibles",
            description = "Retorna todas las especializaciones disponibles en el sistema"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Especializaciones encontradas exitosamente",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Specialization.class))
            )
    )
    public ResponseEntity<Specialization[]> getSpecializations() {
        Specialization[] specializations = turnService.getSpecializations();
        return ResponseEntity.ok(specializations);
    }

    @GetMapping("/disabilities")
    @Operation(
            summary = "Obtener tipos de discapacidades",
            description = "Retorna todos los tipos de discapacidades registrados en el sistema"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Discapacidades encontradas exitosamente",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Disabilitie.class))
            )
    )
    public ResponseEntity<Disabilitie[]> getDisabilities() {
        Disabilitie[] disabilities = turnService.getDisabilities();
        return ResponseEntity.ok(disabilities);
    }

    @GetMapping("/actualTurn")
    @Operation(
            summary = "Hallar el turno actual segun la especializacion",
            description = "Se obtendra la informacion del turno actual segun la especialidad, se obtendra el nombre y codigo "
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Turno encontrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Specialization.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No hay turno actualmente",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    public ResponseEntity<TurnDTO> getInfoActualTurn(
            @Parameter(
                    description = "Especializacion de la cual se requiera saber el turno actual",
                    required = true,
                    example = "Psicologia"
            )
            @RequestParam Specialization specialization
    ) {
        TurnDTO turn = turnService.getTurnActualTurn(specialization);
        return ResponseEntity.ok(turn);
    }
}
