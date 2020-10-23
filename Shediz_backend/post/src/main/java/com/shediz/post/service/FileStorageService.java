package com.shediz.post.service;

import com.shediz.post.exception.FileStorageException;
import com.shediz.post.exception.MyFileNotFoundException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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
    private static final int THUMB_IMAGE_SIZE = 300;

    @Value("${upload-dir}")
    private String uploadDir;

    public FileStorageService()
    {

    }

    private void createThumbnailJPG(MultipartFile originalFile, Path targetLocation)
            throws IOException
    {
        BufferedImage img = ImageIO.read(originalFile.getInputStream());

        int targetSize = THUMB_IMAGE_SIZE;

        if (img.getWidth() < targetSize && img.getHeight() < targetSize)
            targetSize = Math.min(img.getHeight(), img.getWidth());

        BufferedImage thumbImg = Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC,
                targetSize, Scalr.OP_ANTIALIAS);

        ImageIO.write(thumbImg, "jpg" , new File(targetLocation.toString()));
    }

    public String storeFile(MultipartFile file, String dir, String fileName, String type)
    {
        Path fileLocation = Paths.get(uploadDir + "/" + dir).toAbsolutePath().normalize();

        try
        {
            Files.createDirectories(fileLocation);
        } catch (Exception e)
        {
            throw new FileStorageException("Error in create directory", e);
        }

        try
        {
            if (fileName.contains(".."))
                throw new FileStorageException("Invalid characters in file name " + fileName);

            Files.copy(file.getInputStream(), fileLocation.resolve(fileName + type), StandardCopyOption.REPLACE_EXISTING);

            createThumbnailJPG(file, fileLocation.resolve(fileName + "_thumb" + type));

            return fileName;
        } catch (IOException e)
        {
            throw new FileStorageException("Could not store file " + fileName, e);
        }
    }

    public void deleteFile(String dir, String fileName)
    {
        Path fileLocation = Paths.get(uploadDir + "/" + dir + "/" + fileName).toAbsolutePath().normalize();

        try
        {
            Files.deleteIfExists(fileLocation);
        } catch (Exception e)
        {
            throw new FileStorageException("Error in delete file", e);
        }
    }

    public void deleteFolder(String dir)
    {
        Path directoryPath = Paths.get(uploadDir + "/" + dir).toAbsolutePath().normalize();

        try
        {
            FileUtils.deleteDirectory(directoryPath.toFile());
        } catch (IOException e)
        {
            throw new FileStorageException("Error in delete directory", e);
        }
    }

    public Resource loadFileAsResource(String dir, String fileName, String type, boolean needOriginal)
    {
        Path fileLocation = Paths.get(uploadDir + "/" + dir).toAbsolutePath().normalize();

        String completeFileName;
        if (needOriginal)
            completeFileName = fileName + type;
        else
            completeFileName = fileName + "_thumb" + type;

        try
        {
            Path filePath = fileLocation.resolve(completeFileName).normalize();

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
