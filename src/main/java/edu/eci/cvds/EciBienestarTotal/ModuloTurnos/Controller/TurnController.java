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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/turns")
@CrossOrigin(origins = "*")
@Tag(name = "Turnos", description = "Gestión de turnos")
public class TurnController {

    @Autowired
    private TurnService turnService;

    /**
     * Endpoint to create a new shift
     * @param turnDTO DTO with the data of the shift to create
     * @return Code of the shift created
     */
    @PostMapping("/create")
    @Operation(summary = "Crear Turno", description = "Crear un nuevo turno")
    @ApiResponse(responseCode = "201", description = "Turno creado exitosamente")
    public ResponseEntity<Map<String, String>> CreateTurn(@RequestBody TurnDTO turnDTO) {
        try {
            String code = turnService.createTurn(
                    turnDTO.getUserName(),
                    turnDTO.getIdentityDocument(),
                    turnDTO.getRole(),
                    turnDTO.isPriority(),
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

    /**
     * Endpoint to move to the next shift
     * @param specialization Specialization of the turn to be passed
     * @return Confirmation message
     */
    @PutMapping("/pass")
    @Operation(summary = "Pass  Turn", description = "Pasa el turno")
    @ApiResponse(responseCode = "20o", description = "Turno pasado exitosamente")
    @ApiResponse(responseCode = "404", description = "Turno no pasado")
    public ResponseEntity<Map<String, String>> PassTurn(@RequestParam String specialization) {
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

    /**
     * Endpoint to disable all shifts of a specialization
     * @param specialization Specialization for which shifts are to be disabled
     * @return Confirmation message
     */
    @PutMapping("/disable")
    @Operation(summary = "Deshabilitar turno", description = "Deshabilita un turno por su especialización")
    @ApiResponse(responseCode = "200", description = "Turno deshabilitado")
    @ApiResponse(responseCode = "404", description = "Turno no encontrado")
    public ResponseEntity<Map<String, String>> DisableTurn(@RequestParam String specialization) {
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

    /**
     * Endpoint to obtain the list of shifts sorted by priority and time
     * @param specialization Specialization for filtering shifts
     * @return List of active shifts sorted by priority and start time
     */
    @GetMapping("/list")
    @Operation(summary = "Listar turnos", description = "Lista todos los turnos registrados")
    @ApiResponse(responseCode = "200", description = "Lista de turnos obtenida exitosamente")
    public ResponseEntity<?> getTurns(@RequestParam String specialization) {
        try {
            List<Turn> turnos = turnService.getNextTurns(specialization);

            List<TurnDTO> turnosDTO = turnos.stream().map(turn -> {
                TurnDTO dto = new TurnDTO();
                dto.setCode(turn.getCode());
                dto.setUserName(turn.getPatient());
                dto.setSpecialization(turn.getSpecialization().toString());
                dto.setState(turn.getStatus());
                dto.setPriority(turn.getPriority());
                dto.setIdentityDocument(turn.getIdentityDocument());
                dto.setRole(turn.getRole().toString());
                return dto;
            }).toList();

            return new ResponseEntity<>(turnosDTO, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener la lista de turnos: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * Endpoint to generate a report based on date range and user role
     * @param reportDTO Contains the parameters for report generation
     * @return ResponseEntity with the generated report
     */
    @PostMapping("/generate")
    @Operation(summary = "Crear reporte", description = "Crear un nuevo reporte")
    @ApiResponse(responseCode = "201", description = "Reporte creado exitosamente")
    public ResponseEntity<Report> generateReport(@RequestBody ReportDTO reportDTO) {
        // Validate required parameters
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
    @Operation(summary = "Buscar especializaciones", description = "Busca las especializaciones que halla")
    @ApiResponse(responseCode = "200", description = "Especializaciones encontradas exitosamente")
    public ResponseEntity<Specialization[]> getSpecializations(){
        Specialization[] specializations = turnService.getSpecializations();
        return ResponseEntity.ok(specializations);
    }

    @GetMapping("/disabilities")
    @Operation(summary = "Buscar discapacidades", description = "Busca las discapacidades que halla")
    @ApiResponse(responseCode = "200", description = "Discapacidades encontradas exitosamente")
    public ResponseEntity<Disabilitie[]> getDisabilities(){
        Disabilitie[] disabilities = turnService.getDisabilities();
        return ResponseEntity.ok(disabilities);
    }

    @GetMapping("/specialization/{id}")
    @Operation(summary = "Buscar especializacion por id", description = "Busca la especialidad que tiene asignada un usuario buscandolo por el id")
    @ApiResponse(responseCode = "200", description = "Especialidad encontrada exitosamente")
    @ApiResponse(responseCode = "404", description = "No se encontro el turno")
    public ResponseEntity<Specialization>getTurnSpecializationById(String id){
        Specialization specialization = turnService.getTurnSpecialization(id);
        return ResponseEntity.ok(specialization);
    }

}
