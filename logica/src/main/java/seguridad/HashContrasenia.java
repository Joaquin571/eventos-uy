package seguridad;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Hash de contraseñas con PBKDF2 (incluido en el JDK, sin dependencias).
 * Formato guardado: iteraciones:sal:hash (sal y hash en Base64).
 */
public final class HashContrasenia {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private HashContrasenia() {
    }

    public static String hashear(String contrasenia) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);

        byte[] hash = derivar(contrasenia, sal, ITERACIONES);

        return ITERACIONES + ":"
                + Base64.getEncoder().encodeToString(sal) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean coincide(String contrasenia, String almacenado) {
        if (contrasenia == null || almacenado == null) {
            return false;
        }

        String[] partes = almacenado.split(":");

        if (partes.length != 3) {
            return false;
        }

        try {
            int iteraciones = Integer.parseInt(partes[0]);
            byte[] sal = Base64.getDecoder().decode(partes[1]);
            byte[] esperado = Base64.getDecoder().decode(partes[2]);

            byte[] calculado = derivar(contrasenia, sal, iteraciones);

            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String contrasenia, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec especificacion = new PBEKeySpec(
                    contrasenia.toCharArray(), sal, iteraciones, BITS_HASH);

            return SecretKeyFactory.getInstance(ALGORITMO)
                    .generateSecret(especificacion)
                    .getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo procesar la contraseña.", e);
        }
    }
}