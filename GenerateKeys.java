import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Utility to generate RSA key pair for JWT signing.
 * Run with: java GenerateKeys.java
 */
public class GenerateKeys {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        String privatePem = "-----BEGIN PRIVATE KEY-----\n" +
                insertLineBreaks(privateKeyBase64) +
                "\n-----END PRIVATE KEY-----";

        String publicPem = "-----BEGIN PUBLIC KEY-----\n" +
                insertLineBreaks(publicKeyBase64) +
                "\n-----END PUBLIC KEY-----";

        String targetDir = "src/main/resources/META-INF/resources/";
        
        try (FileOutputStream fos = new FileOutputStream(targetDir + "privateKey.pem")) {
            fos.write(privatePem.getBytes());
        }
        try (FileOutputStream fos = new FileOutputStream(targetDir + "publicKey.pem")) {
            fos.write(publicPem.getBytes());
        }

        System.out.println("Keys generated successfully!");
        System.out.println("Private key saved to: " + targetDir + "privateKey.pem");
        System.out.println("Public key saved to: " + targetDir + "publicKey.pem");
    }

    private static String insertLineBreaks(String base64String) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < base64String.length()) {
            int end = Math.min(index + 64, base64String.length());
            sb.append(base64String, index, end);
            if (end < base64String.length()) {
                sb.append("\n");
            }
            index = end;
        }
        return sb.toString();
    }
}
