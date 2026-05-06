package services;

import contracts.IFileService;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileService implements IFileService {
    private static final String IMAGES_DIR = "images";
    private static final String WARDROBE_DIR = "wardrobe";
    private static final String IMPORTED_FILE = "imported.txt";

    public FileService() {
        new File(WARDROBE_DIR).mkdirs();
    }

    @Override
    public List<File> scanUnimported() {
        Set<String> imported = loadImported();
        List<File> result = new ArrayList<>();
        File dir = new File(IMAGES_DIR);
        if (!dir.exists()) return result;
        File[] files = dir.listFiles();
        if (files == null) return result;
        for (File f : files) {
            String name = f.getName().toLowerCase();
            if ((name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
                String baseName = f.getName().replaceAll("\\.[^.]+$", "");
                File processed = new File(WARDROBE_DIR, baseName + "_nobg.png");
                if (!imported.contains(f.getName()) || !processed.exists()) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    @Override
    public File removeBackground(File source) {
        String baseName = source.getName().replaceAll("\\.[^.]+$", "");
        File output = new File(WARDROBE_DIR, baseName + "_nobg.png");
        try {
            System.out.println("Removing background...");
            ProcessBuilder pb = new ProcessBuilder(
                    "/opt/homebrew/bin/rembg",
                    "i", source.getAbsolutePath(), output.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String out = new String(p.getInputStream().readAllBytes());
            int exitCode = p.waitFor();
            if (!out.isBlank()) System.out.println("rembg: " + out.trim());
            if (exitCode == 0 && output.exists()) return output;
            System.out.println("rembg failed (exit " + exitCode + "), using original image.");
        } catch (Exception e) {
            System.out.println("rembg error: " + e.getMessage());
        }
        try {
            Files.copy(source.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public void markImported(String filename) {
        try (FileWriter fw = new FileWriter(IMPORTED_FILE, true)) {
            fw.write(filename + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<String> loadImported() {
        Set<String> imported = new HashSet<>();
        File f = new File(IMPORTED_FILE);
        if (!f.exists()) return imported;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) imported.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imported;
    }
}
