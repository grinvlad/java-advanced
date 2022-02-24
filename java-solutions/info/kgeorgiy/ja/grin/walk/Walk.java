package info.kgeorgiy.ja.grin.walk;


import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Walk {
    private static final String HASH_ALGORITHM = "SHA-1";
    private static final String NULL_HASH = "0000000000000000000000000000000000000000";
    private static final int BUFFER_SIZE = 1 << 16;

    private static String getSha(String pathStr) throws NoSuchAlgorithmException {
        Path path;
        try {
            path = Path.of(pathStr);
        } catch (InvalidPathException e) {
            System.out.println("Invalid path of file: " + e.getMessage());
            return NULL_HASH;
        }

        byte[] bytes;
        try (InputStream inputStream = Files.newInputStream(path)) {
            MessageDigest sha1 = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = inputStream.read(buffer)) != -1) {
                sha1.update(buffer, 0, n);
            }
            bytes = sha1.digest();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return NULL_HASH;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private static void walk(String input, String output) {
        Path inputPath;
        Path outputPath;
        try {
            inputPath = Path.of(input);
            outputPath = Path.of(output);
        } catch (InvalidPathException e) {
            System.out.println("Invalid path of file: " + e.getMessage());
            return;
        }

        if (outputPath.getParent() != null) {
            try {
                Files.createDirectories(outputPath.getParent());
            } catch (IOException e) {
                System.out.println("Folder creation for output denied: " + e.getMessage());
                return;
            }
        }

        try (BufferedReader br = Files.newBufferedReader(inputPath);
             BufferedWriter bw = Files.newBufferedWriter(outputPath)) {
            String curFile;
            while ((curFile = br.readLine()) != null) {
                bw.write(String.format("%s %s\n", getSha(curFile), curFile));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("Security exception: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("No such encryption algorithm: " + e.getMessage());
        } catch (AccessDeniedException e) {
            System.out.println("Access denied: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Reading/writing exception: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Problem with arguments");
            return;
        }

        walk(args[0], args[1]);
    }
}
