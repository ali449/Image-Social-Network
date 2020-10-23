package com.shediz.gateway.service;

import com.shediz.gateway.exception.FileStorageException;
import com.shediz.gateway.exception.MyFileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Lazy
@Service
public class FileStorageService
{
    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(@Value("${upload-dir}") String uploadDir)
    {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try
        {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception e)
        {
            throw new FileStorageException("Error in create directory", e);
        }
    }



    public void storeFile(MultipartFile file, String fileName)
    {
        try
        {
            if (fileName.contains(".."))
                throw new FileStorageException("Invalid characters in file name " + fileName);

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e)
        {
            throw new FileStorageException("Could not store file " + fileName, e);
        }
    }

    public Resource loadFileAsResource(String fileName)
    {
        try
        {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists())
                return resource;
            else
                throw new MyFileNotFoundException("File Not found " + fileName);
        } catch (MalformedURLException e)
        {
            throw new MyFileNotFoundException("Error in find file " + fileName);
        }
    }
}
