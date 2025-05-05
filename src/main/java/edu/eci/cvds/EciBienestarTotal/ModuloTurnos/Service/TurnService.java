package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.ReportRepository;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.TurnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TurnService {
    @Autowired
    private TurnRepository turnRepository;
    @Autowired
    private ReportRepository reportRepository;


}
