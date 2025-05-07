package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Controller;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO.ReportDTO;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO.TurnDTO;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Turn;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service.TurnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/turns")
@CrossOrigin(origins = "*")
public class TurnController {

    @Autowired
    private TurnService turnService;

    /**
     * Endpoint to create a new shift
     * @param turnDTO DTO with the data of the shift to create
     * @return Code of the shift created
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> CreateTurn(@RequestBody TurnDTO turnDTO) {
        try {
            String code = turnService.CreateTurn(
                    turnDTO.getUserName(),
                    turnDTO.getIdentityDocument(),
                    turnDTO.getRole(),
                    turnDTO.isPriority(),
                    turnDTO.getSpeciality()
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
    public ResponseEntity<?> getTurns(@RequestParam String specialization) {
        try {
            List<Turn> turnos = turnService.getNextTurns(specialization);

            // Convertir la lista de entidades Turn a TurnDTO
            List<TurnDTO> turnosDTO = turnos.stream().map(turn -> {
                TurnDTO dto = new TurnDTO();
                dto.setCode(turn.getCode());
                dto.setUserName(turn.getPatient());
                dto.setSpeciality(turn.getSpecialization().toString());
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
}