package com.tacoselmicro;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class IOUtility {
    public static byte[]  loadAesKey(String aesKeyPath) throws IOException {
        return Files.readAllBytes(Paths.get(aesKeyPath));
    }

    public static void saveAesKey(byte[] aesKeyContent, String destinationPath){
        //logica para guardar la clave AES generada
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destinationPath))) {
            writer.write(Base64.getEncoder().encodeToString(aesKeyContent));
            System.out.println("Se guardó una llave AES en " + destinationPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey loadPublicCert(String publicCertPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] pubKeyBytes = Base64.getDecoder().decode(Files.readAllBytes(new File(publicCertPath).toPath()));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return  keyFactory.generatePublic(keySpec);
    }

    public static void saveEncryptedFile(byte[] data, byte[] digitalSignIV,  String destinationPath)  {
        System.out.println("Escribiendo contenido en archivo...");
        try (BufferedWriter fos = new BufferedWriter(new FileWriter(destinationPath))) {
            fos.write(Base64.getEncoder().encodeToString(data) + ":" + Base64.getEncoder().encodeToString(digitalSignIV));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getBytesFromFile(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }

    public static void saveDecryptedFile(byte [] plainData, String destinationPath) throws IOException {
        Files.write(new File(destinationPath).toPath(), plainData);
        System.out.println("Se guardó archivo desencriptado correctamente");
    }

}
