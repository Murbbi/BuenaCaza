package seguridad;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordHasher {
    private static final int ITERACIONES = 210_000;
    private static final int TAMANO_SALT = 16;
    private static final int TAMANO_HASH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(char[] contrasena) {
        validarContrasena(contrasena);
        byte[] salt = new byte[TAMANO_SALT];
        RANDOM.nextBytes(salt);
        byte[] hash = derivar(contrasena, salt, ITERACIONES);
        return "v1$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean coincide(char[] contrasena, String hashAlmacenado) {
        if (contrasena == null || contrasena.length < 12 || contrasena.length > 128
                || hashAlmacenado == null) {
            return false;
        }

        try {
            String[] partes = hashAlmacenado.split("\\$", -1);
            if (partes.length != 4 || !"v1".equals(partes[0])) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[3]);
            if (iteraciones < 1 || iteraciones > 2_000_000 || salt.length < 16
                    || hashEsperado.length != TAMANO_HASH / 8) {
                return false;
            }
            return MessageDigest.isEqual(hashEsperado, derivar(contrasena, salt, iteraciones));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static void validarContrasena(char[] contrasena) {
        if (contrasena == null || contrasena.length < 12 || contrasena.length > 128) {
            throw new IllegalArgumentException("La contraseña debe tener entre 12 y 128 caracteres.");
        }
    }

    private static byte[] derivar(char[] contrasena, byte[] salt, int iteraciones) {
        PBEKeySpec especificacion = new PBEKeySpec(contrasena, salt, iteraciones, TAMANO_HASH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña.", exception);
        } finally {
            especificacion.clearPassword();
        }
    }
}
