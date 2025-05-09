package edu.eci.cvds.EciBienestarTotal.ModuloTurnos;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Controller.FileController;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.FileModel;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class FileControllerTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpload_Success() throws IOException {
        String fileName = "test.pdf";
        String contentType = "application/pdf";
        byte[] content = "Mock PDF content".getBytes();

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                fileName,
                contentType,
                content);

        when(fileRepository.save(any(FileModel.class))).thenAnswer(invocation -> {
            FileModel fileToSave = invocation.getArgument(0);
            assertEquals(fileName, fileToSave.getName());
            assertEquals(contentType, fileToSave.getContentType());
            assertArrayEquals(content, fileToSave.getData());

            fileToSave.setId("123");
            return fileToSave;
        });

        ResponseEntity<String> response = fileController.upload(mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded successfully: 123", response.getBody());

        verify(fileRepository, times(1)).save(any(FileModel.class));
    }

    @Test
    void testUpload_WithEmptyFile() throws IOException {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]);


        when(fileRepository.save(any(FileModel.class))).thenAnswer(invocation -> {
            FileModel fileToSave = invocation.getArgument(0);
            fileToSave.setId("456");
            return fileToSave;
        });

        ResponseEntity<String> response = fileController.upload(emptyFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded successfully: 456", response.getBody());

        verify(fileRepository, times(1)).save(any(FileModel.class));
    }

    @Test
    void testGetFile_WhenFileExists() {
        // Arrange
        String fileId = "123";
        String contentType = "application/pdf";
        byte[] fileData = "PDF content".getBytes();

        FileModel mockFile = new FileModel();
        mockFile.setId(fileId);
        mockFile.setName("document.pdf");
        mockFile.setContentType(contentType);
        mockFile.setData(fileData);

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(mockFile));

        ResponseEntity<byte[]> response = fileController.getFile(fileId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(contentType, response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertArrayEquals(fileData, response.getBody());

        verify(fileRepository).findById(fileId);
    }

    @Test
    void testGetFile_WhenFileDoesNotExist() {
        String fileId = "nonexistent";
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        ResponseEntity<byte[]> response = fileController.getFile(fileId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(fileRepository).findById(fileId);
    }

    @Test
    void testGetAllFiles() {
        FileModel file1 = new FileModel();
        file1.setId("1");
        file1.setName("file1.pdf");
        file1.setContentType("application/pdf");
        file1.setData("content1".getBytes());

        FileModel file2 = new FileModel();
        file2.setId("2");
        file2.setName("file2.jpg");
        file2.setContentType("image/jpeg");
        file2.setData("content2".getBytes());

        when(fileRepository.findAll()).thenReturn(Arrays.asList(file1, file2));

        List<FileModel> result = fileController.getAllFiles();

        assertEquals(2, result.size());

        FileModel resultFile1 = result.get(0);
        assertEquals("1", resultFile1.getId());
        assertEquals("file1.pdf", resultFile1.getName());
        assertEquals("application/pdf", resultFile1.getContentType());
        assertNull(resultFile1.getData());

        FileModel resultFile2 = result.get(1);
        assertEquals("2", resultFile2.getId());
        assertEquals("file2.jpg", resultFile2.getName());
        assertEquals("image/jpeg", resultFile2.getContentType());
        assertNull(resultFile2.getData());

        verify(fileRepository).findAll();
    }

    @Test
    void testGetAllFiles_EmptyList() {
        // Arrange
        when(fileRepository.findAll()).thenReturn(List.of());

        // Act
        List<FileModel> result = fileController.getAllFiles();

        // Assert
        assertTrue(result.isEmpty());
        verify(fileRepository).findAll();
    }

    @Test
    void testUpload_WithLargeFile() throws IOException {
        String fileName = "large.zip";
        String contentType = "application/zip";
        byte[] content = new byte[1024];
        Arrays.fill(content, (byte) 1);

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                fileName,
                contentType,
                content);

        when(fileRepository.save(any(FileModel.class))).thenAnswer(invocation -> {
            FileModel fileToSave = invocation.getArgument(0);
            assertEquals(fileName, fileToSave.getName());
            assertEquals(contentType, fileToSave.getContentType());
            assertEquals(content.length, fileToSave.getData().length);

            fileToSave.setId("789");
            return fileToSave;
        });

        ResponseEntity<String> response = fileController.upload(mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("File uploaded successfully: 789", response.getBody());

        verify(fileRepository, times(1)).save(any(FileModel.class));
    }

    @Test
    void testDeleteFile_WhenFileExists() {
        String fileId = "123";

        when(fileRepository.existsById(fileId)).thenReturn(true);
        doNothing().when(fileRepository).deleteById(fileId);

        ResponseEntity<String> response = fileController.deleteFile(fileId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Archivo eliminado correctamente: 123", response.getBody());
        verify(fileRepository).deleteById(fileId);
    }

    @Test
    void testDeleteFile_WhenFileDoesNotExist() {
        String fileId = "not_found";

        when(fileRepository.existsById(fileId)).thenReturn(false);

        ResponseEntity<String> response = fileController.deleteFile(fileId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(fileRepository, never()).deleteById(any());
    }


}