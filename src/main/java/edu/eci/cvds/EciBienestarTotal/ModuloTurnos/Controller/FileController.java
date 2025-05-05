package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Controller;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.FileModel;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.FileRepository;
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

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        FileModel newFile = new FileModel();
        newFile.setName(file.getOriginalFilename());
        newFile.setContentType(file.getContentType());
        newFile.setData(file.getBytes());
        fileRepo.save(newFile);
        return ResponseEntity.ok("File uploaded successfully: " + newFile.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable String id) {
        Optional<FileModel> file = fileRepo.findById(id);
        return file.map(f -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, f.getContentType())
                        .body(f.getData()))
                .orElse(ResponseEntity.notFound().build());
    }

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
}
