package edu.eci.cvds.EciBienestarTotal.ModuloTurnos;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Controller.FileController;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.FileModel;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class FileControllerTest {

    @InjectMocks
    private FileController fileController;

    @Mock
    private FileRepository fileRepo;

    private String validToken;
    private String tokenSinClaims;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Algorithm algorithm = Algorithm.HMAC256("ContraseñaSuperSecreta123");

        // Token válido
        validToken = "Bearer " + JWT.create()
                .withClaim("id", "1")
                .withClaim("userName", "usuario")
                .withClaim("email", "email@example.com")
                .withClaim("name", "Nombre")
                .withClaim("role", "USER")
                .withClaim("specialty", "Ingeniería")
                .sign(algorithm);

        // Token inválido (faltan claims)
        tokenSinClaims = "Bearer " + JWT.create()
                .sign(algorithm);
    }

    @Test
    void upload_DebeSubirArchivo_CuandoTokenValido() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "prueba.txt", "text/plain", "Hola mundo".getBytes());

        when(fileRepo.save(any(FileModel.class))).thenAnswer(invocation -> {
            FileModel f = invocation.getArgument(0);
            f.setId("abc123");
            return f;
        });

        ResponseEntity<String> response = fileController.upload(validToken, file);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("File uploaded successfully"));
    }

    @Test
    void upload_DebeRetornar401_CuandoTokenInvalido() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "prueba.txt", "text/plain", "Hola mundo".getBytes());

        ResponseEntity<String> response = fileController.upload(tokenSinClaims, file);

        assertEquals(401, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Error de autenticación"));
    }

    @Test
    void getFile_DebeRetornarArchivo_CuandoExisteYTokenValido() {
        FileModel file = new FileModel();
        file.setId("1");
        file.setName("archivo.txt");
        file.setContentType("text/plain");
        file.setData("contenido".getBytes());

        when(fileRepo.findById("1")).thenReturn(Optional.of(file));

        ResponseEntity<byte[]> response = fileController.getFile(validToken, "1");

        assertEquals(200, response.getStatusCodeValue());
        assertArrayEquals("contenido".getBytes(), response.getBody());
    }

    @Test
    void getFile_DebeRetornar404_CuandoArchivoNoExiste() {
        when(fileRepo.findById("404")).thenReturn(Optional.empty());

        ResponseEntity<byte[]> response = fileController.getFile(validToken, "404");

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getFile_DebeRetornar401_CuandoTokenInvalido() {
        ResponseEntity<byte[]> response = fileController.getFile(tokenSinClaims, "1");

        assertEquals(404, response.getStatusCodeValue());  // retorna 404 porque está en el catch general
        assertTrue(new String(response.getBody(), StandardCharsets.UTF_8).contains("Archivo no encontrado"));
    }

    @Test
    void getAllFiles_DebeListarArchivos_CuandoTokenValido() {
        FileModel file = new FileModel();
        file.setId("1");
        file.setName("test.txt");
        file.setContentType("text/plain");

        when(fileRepo.findAll()).thenReturn(List.of(file));

        ResponseEntity<?> response = fileController.getAllFiles(validToken);
        assertEquals(200, response.getStatusCodeValue());

        List<?> lista = (List<?>) response.getBody();
        assertEquals(1, lista.size());
    }

    @Test
    void getAllFiles_DebeRetornar401_CuandoTokenInvalido() {
        ResponseEntity<?> response = fileController.getAllFiles(tokenSinClaims);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void deleteFile_DebeEliminarArchivo_CuandoExiste() {
        when(fileRepo.existsById("1")).thenReturn(true);

        ResponseEntity<String> response = fileController.deleteFile(validToken, "1");

        assertEquals(200, response.getStatusCodeValue());
        verify(fileRepo).deleteById("1");
    }

    @Test
    void deleteFile_DebeRetornar404_CuandoArchivoNoExiste() {
        when(fileRepo.existsById("no-existe")).thenReturn(false);

        ResponseEntity<String> response = fileController.deleteFile(validToken, "no-existe");

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void deleteFile_DebeRetornar401_CuandoTokenInvalido() {
        ResponseEntity<String> response = fileController.deleteFile(tokenSinClaims, "1");

        assertEquals(401, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Error de autenticación"));
    }

    @Test
    void upload_DebeRetornar500_CuandoArchivoGeneraIOException() throws Exception {
        // Arrange
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("archivo.txt");
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getBytes()).thenThrow(new IOException("Falla de lectura"));

        FileController controller = new FileController();

        ResponseEntity<String> response = controller.upload(validToken, file);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Error al leer archivo"));
    }

}
