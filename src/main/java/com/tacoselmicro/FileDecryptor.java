package com.tacoselmicro;



import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class FileDecryptor {

    public static void decryptFileAes128(String encryptedFilePath, String aesKeyPath, String destinationPath) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //Leer archivo encriptado
        byte[] bytesFromFile = IOUtility.getBytesFromFile(encryptedFilePath);
        //Separa el contenido del archivo con estructura contenido:iv
        String[] partsOfFile = new String(bytesFromFile).split(":");

        byte[] encryptedData = Base64.getDecoder().decode(partsOfFile[0]);
        byte[] iv = Base64.getDecoder().decode(partsOfFile[1]);

        //Leer llave AES normal
        byte[] bytesAesKey = IOUtility.loadAesKey(aesKeyPath);
        // desencriptar contenido con AES
        //Se debe de leer el iv (firma) para poder desencriptar el archivo
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(bytesAesKey, "AES"), new IvParameterSpec(iv));
        byte[] plainData = aesCipher.doFinal(encryptedData);
        //Guardar contenido desencriptado en archivo
        IOUtility.saveDecryptedFile(plainData, destinationPath);
    }

    public static void decryptFileAes128WithKeyEncrypted(String privateCertPath, String encryptedAesKeyPath, String encryptedFilePath, String destinationPath) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        //Cargar llave privada
        byte[] privKeyBytes = Base64.getDecoder().decode(Files.readAllBytes(new File(privateCertPath).toPath()));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        //Obtener clave AES cifrada
        byte[] encryptedAesKeyBytes = Base64.getDecoder().decode(IOUtility.loadAesKey(encryptedAesKeyPath));
        //Descifrar clave AES con cert priv
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedAesKey = rsaCipher.doFinal(encryptedAesKeyBytes);
        //leer archivo cifrado
        byte[] bytesFromFile = IOUtility.getBytesFromFile(encryptedFilePath);
        String[] partsOfFile = new String(bytesFromFile).split(":");

        //separar contenido de archivo cifrado
        byte[] encryptedData = Base64.getDecoder().decode(partsOfFile[0]);
        byte[] iv = Base64.getDecoder().decode(partsOfFile[1]);
        //Desencriptar contenido con clave AES desencriptada
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptedAesKey, "AES"), new IvParameterSpec(iv));
        byte[] plainData = aesCipher.doFinal(encryptedData);

        //Escribir contenido en archivo nuevo
        IOUtility.saveDecryptedFile(plainData, destinationPath);
    }


    public static void main(String[] args) {
        try {

            String encryptedFilePath = "C:\\Users\\IRVIN\\Desktop\\TacosElMicro-System\\tacos-el-micro-api\\PRUEBA ARCHIVO DUMMY_2.txt.enc";
            String aesKeyPath = "C:\\Users\\IRVIN\\Desktop\\TacosElMicro-System\\tacos-el-micro-api\\aes-infonavit-128-2.key.enc";
            String destinationPath = "C:\\Users\\IRVIN\\Desktop\\TacosElMicro-System\\tacos-el-micro-api\\banorte_descifrad.txt";
            String banortePriv = "C:\\Users\\IRVIN\\Desktop\\TacosElMicro-System\\tacos-el-micro-api\\banorte.priv";

            FileDecryptor.decryptFileAes128WithKeyEncrypted(banortePriv,aesKeyPath,encryptedFilePath ,destinationPath );

            System.out.println("Archivo desencriptado correctamente con clave AES directa.");


//            String privateKeyPath = "ruta/a/clave_privada_rsa.pem"; // Base64 PKCS#8
//            String encryptedAesKeyPath = "ruta/a/clav_aes_encriptada.txt";
//            String encryptedFilePath2 = "ruta/al/archivo_encriptado.txt";
//            String destinationPath2 = "ruta/salida/archivo_descifrado_rsa.txt";
//
//            FileDecryptor.decryptFileAes128WithKeyEncrypted(
//                    privateKeyPath, encryptedAesKeyPath, encryptedFilePath2, destinationPath2
//            );

            System.out.println("Archivo desencriptado correctamente con clave AES cifrada (RSA).");

        } catch (IOException |
                 NoSuchAlgorithmException |
                 NoSuchPaddingException |
                 InvalidKeyException |
                 InvalidAlgorithmParameterException |
                 IllegalBlockSizeException |
                 BadPaddingException
                  e) {

            System.err.println("Error durante el proceso de desencriptación: " + e.getMessage());

        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

}

