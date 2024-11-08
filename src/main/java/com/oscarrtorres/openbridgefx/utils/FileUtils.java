package com.oscarrtorres.openbridgefx.utils;

import com.oscarrtorres.openbridgefx.models.Constants;
import com.oscarrtorres.openbridgefx.models.EnvData;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    public static EnvData getEnvData() {
        Dotenv dotenv = Dotenv.configure().directory(Constants.ENV_FILE_PATH).load();
        String apiKey = dotenv.get("API_KEY");
        String apiUrl = dotenv.get("API_URL");
        String model = dotenv.get("MODEL");
        String voskModel = dotenv.get("VOSK_MODEL");

        EnvData envData = new EnvData();
        envData.setApiKey(apiKey);
        envData.setApiUrl(apiUrl);
        envData.setModel(model);
        envData.setVoskModel(voskModel);

        return envData;
    }

    public static void saveEnvFile(EnvData envData) {
        try (FileWriter writer = new FileWriter(Constants.ENV_FILE_PATH)) {
            writer.write("API_KEY=" + envData.getApiKey() + "\n");
            writer.write("API_URL=" + envData.getApiUrl() + "\n");
            writer.write("MODEL=" + envData.getModel() + "\n");
            writer.write("VOSK_MODEL=" + envData.getVoskModel() + "\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving .env file: " + e.getMessage());
        }
    }

    public static void extractZipFile(Path zipFilePath, boolean deleteOldExtract) {
        if(deleteOldExtract) {
            String fileNameWithoutExtension = zipFilePath.getFileName().toString().replaceFirst("\\.zip$", "");
            Path extractDir = zipFilePath.getParent().resolve(fileNameWithoutExtension);
            FileUtils.deleteFilePath(extractDir);
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path extractedPath = Paths.get(zipFilePath.getParent().toString(), entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(extractedPath);
                } else {
                    try (OutputStream out = Files.newOutputStream(extractedPath, StandardOpenOption.CREATE)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFilePath(Path filePath) {
        if(!Files.exists(filePath)) {
            System.out.println("Nothing to delete");
            return;
        }
        try {
            if (Files.isDirectory(filePath)) {
                Files.walkFileTree(filePath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                // Delete single file
                Files.delete(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
