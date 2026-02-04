package edu.vedoque.seguridadbase.service.impl;

import edu.vedoque.seguridadbase.service.FileProcessingService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileProcessingServiceImpl implements FileProcessingService {

    // Defino la ruta base usando la carpeta de usuario del sistema operativo para que funcione tanto en Windows como en Linux sin cambiar el código
    private String basePath = System.getProperty("user.home") + File.separator + "uploads" + File.separator;

    // En el constructor compruebo si la carpeta de subidas existe y si no es así la creo automáticamente para evitar errores al arrancar
    public FileProcessingServiceImpl() {
        File directory = new File(basePath);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Carpeta creada en: " + basePath);
        }
    }

    // Este método lee el contenido de la carpeta y me devuelve una lista con los nombres de los archivos que hay dentro
    @Override
    public List<String> fileList() {
        File dir = new File(basePath);
        File[] files = dir.listFiles();
        // Uso streams para convertir el array de ficheros en una lista de strings solo con los nombres
        return files != null ? Arrays.stream(files).map(File::getName).collect(Collectors.toList()) : null;
    }

    // Aquí es donde guardo físicamente el archivo que me llega desde el formulario web
    @Override
    public String uploadFile(MultipartFile multipartFile, String fileName) {
        // Construyo la ruta completa destino uniendo la carpeta base con el nombre del archivo
        Path path = Path.of(basePath + fileName);

        try {
            // Copio los bytes del archivo al disco y si ya existe uno con el mismo nombre lo sobreescribo
            Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return "CREATED";
        } catch (Exception e) {
            // Si falla imprimo el error por consola para poder depurar y devuelvo el estado de fallo
            e.printStackTrace();
            return "FAILED";
        }
    }

    // Este método sirve para recuperar un archivo del disco cuando el navegador lo pide para mostrarlo
    @Override
    public Resource downloadFile(String fileName) {
        Path path = Path.of(basePath + fileName);
        try {
            // Creo un recurso a partir de la URL del archivo y compruebo si se puede leer antes de devolverlo
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}