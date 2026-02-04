package edu.vedoque.seguridadbase.controller;


import edu.vedoque.seguridadbase.service.FileProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/file")
public class FileProcessingController {

    @Autowired
    private FileProcessingService fileProcessingService;

    // Un método simple para ver qué archivos hay subidos, me sirve para pruebas
    @GetMapping("/list")
    public ResponseEntity<?> getFileList() {
        return new ResponseEntity<>(fileProcessingService.fileList(), HttpStatus.OK);
    }

    // Este método es para mostrar las fotos
    // Busca el archivo y lo devuelve como un flujo de datos stream
    @GetMapping(value = "/download/{name}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> downloadFile(@PathVariable(value = "name") String fileName) {
        Resource file = fileProcessingService.downloadFile(fileName);
        // Si el archivo no existe devuelvo un 404 para que el navegador sepa que no hay foto
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            // Si existe lo envío con el tipo de contenido adecuado
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
        }
    }

    // Método para subir archivos desde formulario
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestBody MultipartFile file) {
        // Intento guardar el archivo y recojo el estado que me devuelve el servicio
        String status = fileProcessingService.uploadFile(file, file.getOriginalFilename());

        // Dependiendo de lo que haya pasado devuelvo un código HTTP diferente: 201 si se creó, 304 si ya existía, etc.
        return "CREATED".equals(status) ? new ResponseEntity<>(HttpStatus.CREATED) : ("EXIST".equals(status) ? new ResponseEntity<>(HttpStatus.NOT_MODIFIED) : new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED));
    }

}