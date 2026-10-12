# Buena Caza

Aplicación de reservas para el complejo Buena Caza, implementada en JavaFX con arquitectura MVC y persistencia MySQL. Las pantallas siguen los flujos definidos en `Documentos/Urbicain Matias-AP2.pdf`: inicio de sesión, registro de invitados, reservas, administración de huéspedes, tarifas por temporada y reportes.

## Requisitos

- JDK 25 o superior (JavaFX 27 necesita Java 25 para ejecutarse).
- JavaFX SDK 27 para Windows. Descomprimilo en `lib/javafx-sdk-27/` para que exista `lib/javafx-sdk-27/lib/`.
- MySQL Server con la base `buena_caza`.
- MySQL Connector/J, ya incluido en `lib/`.

## Preparar la base de datos

Para una instalación nueva, ejecutá `Documentos/Base de datos/buena_caza.sql`. Las contraseñas se guardan como hashes PBKDF2 en `invitado.contrasena_hash`; `invitado.es_admin` determina el tipo de acceso.

Para una base que ya existe, ejecutá `Documentos/Base de datos/migracion_acceso_invitado.sql`. La migración agrega la columna de contraseña, copia los hashes existentes desde `usuario` a `invitado` y elimina la tabla `usuario`. También establece el correo como único. Si ya existen correos duplicados, MySQL rechazará el índice único: corregí esos duplicados antes de volver a ejecutar la migración.

El acceso usa el correo y la contraseña guardada como hash PBKDF2-HMAC-SHA256 con salt aleatorio; nunca se almacena la contraseña en texto plano. La contraseña debe tener entre 12 y 128 caracteres. `es_admin` es la única fuente para elegir el panel administrador o el portal de reservas.

Al registrarse con un invitado ya cargado, ingresá el mismo correo y DNI para asignarle una contraseña. Esto también permite configurar el acceso del administrador que viene en los datos iniciales. Los invitados nuevos quedan con `es_admin = 0`.

La configuración de conexión usa estas variables de entorno:

- `BUENACAZA_DB_URL` (por defecto `jdbc:mysql://localhost:3306/buena_caza`)
- `BUENACAZA_DB_USER` (por defecto `root`)
- `BUENACAZA_DB_PASSWORD` (por defecto `oso123456`)

El administrador inicial tiene el correo `matias@gmail.com` y el DNI que figura en `buena_caza.sql`; al registrarse con esos datos puede establecer su contraseña. Los invitados con al menos una reserva registrada reciben automáticamente un 10% de descuento en reservas futuras.

## Compilar y ejecutar en Windows

Desde la carpeta del proyecto, ejecutá:

```powershell
.\run.bat
```

El script compila las clases en `out/`, copia la hoja de estilos JavaFX y ejecuta la aplicación. Si preferís iniciar desde VS Code, la misma clase principal es `BuenaCazaApp`; incluí el módulo `javafx.controls`, el SDK de JavaFX en el module path y el Connector/J en el classpath.
