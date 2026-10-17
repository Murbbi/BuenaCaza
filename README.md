# Buena Caza

Aplicación de reservas para el complejo Buena Caza, implementada en JavaFX con arquitectura MVC y persistencia MySQL. Las pantallas siguen los flujos definidos en `Documentos/Urbicain Matias-AP2.pdf`: inicio de sesión, registro de invitados, reservas, administración de huéspedes, tarifas por temporada y reportes.

## Requisitos

- JDK 25 o superior (JavaFX 27 necesita Java 25 para ejecutarse). Instalar con `winget install Oracle.JDK.25`
- JavaFX SDK 27 para Windows. Descomprimilo en `lib/javafx-sdk-27/` para que exista `lib/javafx-sdk-27/lib/`.
- MySQL Server con la base `buena_caza`.
- MySQL Connector/J, ya incluido en `lib/`.

## Preparar la base de datos

La configuración de conexión usa estas variables de entorno:

- `BUENACAZA_DB_URL` (por defecto `jdbc:mysql://localhost:3306/buena_caza`)
- `BUENACAZA_DB_USER` (por defecto `buena_caza_app`)
- `BUENACAZA_DB_PASSWORD`

## Compilar y ejecutar en Windows

Desde la carpeta del proyecto, ejecutá:

```powershell
.\run.bat
```


