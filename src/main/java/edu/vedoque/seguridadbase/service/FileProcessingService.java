package edu.vedoque.seguridadbase.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;



public interface FileProcessingService {
    List<String> fileList();

    String uploadFile(MultipartFile file, String fileName);

    Resource downloadFile(String fileName);

}
