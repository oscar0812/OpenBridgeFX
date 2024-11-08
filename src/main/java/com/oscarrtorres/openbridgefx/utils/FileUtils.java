package com.oscarrtorres.openbridgefx.utils;

import com.oscarrtorres.openbridgefx.models.Constants;
import com.oscarrtorres.openbridgefx.models.YamlData;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    public static YamlData getYamlData() {
        // Configure LoaderOptions to allow the specific package or class
        var loaderoptions = new LoaderOptions();
        TagInspector taginspector =
                tag -> tag.getClassName().equals(YamlData.class.getName());
        loaderoptions.setTagInspector(taginspector);
        Yaml yaml = new Yaml(new Constructor(YamlData.class, loaderoptions));

        YamlData yamlData = new YamlData();

        try (InputStream inputStream = new FileInputStream(Constants.PROJECT_YAML_FILE_PATH)) {
            yamlData = yaml.load(inputStream);
            System.out.println(yamlData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return yamlData;
    }

    public static void saveYamlData(YamlData yamlData) {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(Constants.PROJECT_YAML_FILE_PATH)) {
            yaml.dump(yamlData, writer);
        } catch (IOException e) {
            e.printStackTrace();
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
