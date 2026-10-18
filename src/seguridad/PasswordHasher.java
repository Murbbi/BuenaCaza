package seguridad;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Guarda las contraseñas como hash (PBKDF2) para no tenerlas en texto plano en la base.
// El formato que se guarda es: v1$iteraciones$salt$hash
public class PasswordHasher {
    private static final int ITERACIONES = 210000;

    public static String hash(char[] contrasena) {
        validarContrasena(contrasena);
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derivar(contrasena, salt, ITERACIONES);
        return "v1$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean coincide(char[] contrasena, String hashGuardado) {
        if (hashGuardado == null) {
            return false;
        }
        String[] partes = hashGuardado.split("\\$");
        if (partes.length != 4) {
            return false;
        }
        int iteraciones = Integer.parseInt(partes[1]);
        byte[] salt = Base64.getDecoder().decode(partes[2]);
        byte[] hashEsperado = Base64.getDecoder().decode(partes[3]);
        return MessageDigest.isEqual(hashEsperado, derivar(contrasena, salt, iteraciones));
    }

    public static void validarContrasena(char[] contrasena) {
        if (contrasena.length < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
    }

    private static byte[] derivar(char[] contrasena, byte[] salt, int iteraciones) {
        try {
            PBEKeySpec especificacion = new PBEKeySpec(contrasena, salt, iteraciones, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(especificacion).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo calcular el hash de la contraseña", e);
        }
    }
}
