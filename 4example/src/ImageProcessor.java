import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class ImageProcessor {
    private static final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".bmp", ".gif"};

    public static void main(String[] args) {
        // Start a thread to monitor for Esc key (simulated via console input)
        Thread inputThread = new Thread(() -> {
            try {
                while (System.in.read() != 27) { // ASCII for Esc
                    // Wait for Esc key
                }
                isCancelled.set(true);
                System.out.println("Operation cancelled by user.");
            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        // Validate command-line arguments
        if (args.length < 2) {
            System.out.println("Usage: java ImageProcessor <sourceDir> [/sub] </s scale | /n | /r | /c targetDir>");
            return;
        }

        String sourceDir = args[0];
        boolean traverseSubdirs = false;
        String operation = null;
        double scaleFactor = 0.0;
        String targetDir = null;

        // Count operation flags to ensure exactly one is provided
        int operationCount = 0;

        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("/sub")) {
                if (traverseSubdirs) {
                    System.out.println("Error: /sub flag specified multiple times");
                    return;
                }
                traverseSubdirs = true;
            } else if (args[i].equals("/s")) {
                if (operation != null) {
                    System.out.println("Error: Only one operation flag (/s, /n, /r, /c) can be specified");
                    return;
                }
                if (i + 1 >= args.length) {
                    System.out.println("Error: /s requires a scale factor");
                    return;
                }
                try {
                    scaleFactor = Double.parseDouble(args[i + 1]);
                    if (scaleFactor <= 0) {
                        System.out.println("Error: Scale factor must be positive");
                        return;
                    }
                    operation = "/s";
                    operationCount++;
                    i++;
                } catch (NumberFormatException e) {
                    System.out.println("Error: Invalid scale factor");
                    return;
                }
            } else if (args[i].equals("/n")) {
                if (operation != null) {
                    System.out.println("Error: Only one operation flag (/s, /n, /r, /c) can be specified");
                    return;
                }
                operation = "/n";
                operationCount++;
            } else if (args[i].equals("/r")) {
                if (operation != null) {
                    System.out.println("Error: Only one operation flag (/s, /n, /r, /c) can be specified");
                    return;
                }
                operation = "/r";
                operationCount++;
            } else if (args[i].equals("/c")) {
                if (operation != null) {
                    System.out.println("Error: Only one operation flag (/s, /n, /r, /c) can be specified");
                    return;
                }
                if (i + 1 >= args.length) {
                    System.out.println("Error: /c requires a target directory");
                    return;
                }
                targetDir = args[i + 1];
                operation = "/c";
                operationCount++;
                i++;
            } else {
                System.out.println("Error: Invalid argument: " + args[i]);
                return;
            }
        }

        if (operationCount == 0) {
            System.out.println("Error: Exactly one operation flag (/s, /n, /r, /c) must be specified");
            return;
        }

        // Validate directories
        File source = new File(sourceDir);
        if (!source.exists() || !source.isDirectory()) {
            System.out.println("Error: Source directory does not exist or is not a directory");
            return;
        }
        if (operation.equals("/c")) {
            File target = new File(targetDir);
            if (!target.isDirectory() && !target.mkdirs()) {
                System.out.println("Error: Cannot create target directory");
                return;
            }
        }

        // Process files
        processDirectory(source, traverseSubdirs, operation, scaleFactor, targetDir);
    }

    private static void processDirectory(File sourceDir, boolean traverseSubdirs, String operation,
                                         double scaleFactor, String targetDir) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try (Stream<Path> paths = traverseSubdirs ?
                Files.walk(Paths.get(sourceDir.getAbsolutePath())) :
                Files.list(Paths.get(sourceDir.getAbsolutePath()))) {

            paths.filter(Files::isRegularFile)
                    .filter(path -> isImageFile(path.toString()))
                    .forEach(path -> {
                        if (isCancelled.get()) return;
                        executor.submit(() -> processFile(path.toFile(), operation, scaleFactor, targetDir));
                    });
        } catch (IOException e) {
            System.err.println("Error accessing directory: " + e.getMessage());
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
    }

    private static boolean isImageFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (lowerCaseName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static void processFile(File file, String operation, double scaleFactor, String targetDir) {
        if (isCancelled.get()) return;

        try {
            switch (operation) {
                case "/s":
                    scaleImage(file, scaleFactor);
                    break;
                case "/n":
                    negateImage(file);
                    break;
                case "/r":
                    if (!file.delete()) {
                        System.err.println("Failed to delete: " + file.getAbsolutePath());
                    }
                    break;
                case "/c":
                    copyImage(file, targetDir);
                    break;
            }
        } catch (IOException e) {
            System.err.println("Error processing " + file.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    private static void scaleImage(File file, double scaleFactor) throws IOException {
        BufferedImage img = ImageIO.read(file);
        int newWidth = (int) (img.getWidth() * scaleFactor);
        int newHeight = (int) (img.getHeight() * scaleFactor);
        BufferedImage scaledImg = new BufferedImage(newWidth, newHeight, img.getType());
        scaledImg.getGraphics().drawImage(img.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
        ImageIO.write(scaledImg, getFileExtension(file), file);
    }

    private static void negateImage(File file) throws IOException {
        BufferedImage img = ImageIO.read(file);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int r = 255 - ((rgb >> 16) & 0xFF);
                int g = 255 - ((rgb >> 8) & 0xFF);
                int b = 255 - (rgb & 0xFF);
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        ImageIO.write(img, getFileExtension(file), file);
    }

    private static void copyImage(File file, String targetDir) throws IOException {
        Path targetPath = Paths.get(targetDir, file.getName());
        Files.copy(file.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        return lastIndex > 0 ? name.substring(lastIndex + 1) : "jpg";
    }
}