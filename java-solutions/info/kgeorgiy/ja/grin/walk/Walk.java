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
    private static final String EXCEPTION_HASH = "0".repeat(40);
    private static final int BUFFER_SIZE = 1 << 16; // 65536

    private static String getSha(String pathStr, MessageDigest digest) throws NoSuchAlgorithmException {
        Path path;
        try {
            path = Path.of(pathStr);
        } catch (InvalidPathException e) {
            System.err.println("Invalid path of file: " + e.getMessage());
            return EXCEPTION_HASH;
        }

        byte[] bytes;
        try (InputStream inputStream = Files.newInputStream(path)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            digest.reset();
            int n;
            while ((n = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, n);
            }
            bytes = digest.digest();
        } catch (IOException e) {
            System.err.println("Reading exception during hashing a file: " + e.getMessage());
            return EXCEPTION_HASH;
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
            System.err.println("Invalid path of file: " + e.getMessage());
            return;
        }

        if (outputPath.getParent() != null) {
            try {
                Files.createDirectories(outputPath.getParent());
            } catch (IOException e) {
                System.err.println("Cannot create folder for output file: " + e.getMessage());
                return;
            }
        }

        try (BufferedReader br = Files.newBufferedReader(inputPath);
             BufferedWriter bw = Files.newBufferedWriter(outputPath)) {
            String curFile;
            MessageDigest sha1 = MessageDigest.getInstance(HASH_ALGORITHM);
            while ((curFile = br.readLine()) != null) {
                bw.write(String.format("%s %s", getSha(curFile, sha1), curFile));
                bw.newLine();
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No such encryption algorithm: " + e.getMessage());
        } catch (AccessDeniedException e) {
            System.err.println("Access denied: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Reading/writing exception: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Expected 2 arguments: \"input_file\" \"output_file\"");
            return;
        }

        walk(args[0], args[1]);
    }
}
