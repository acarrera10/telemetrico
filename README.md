# Telemétrico v0.3 — Primer milestone de prueba

App Android nativa (Kotlin + Jetpack Compose) para recibir telemetría UDP de **F1 25 / formato 2025** desde PS5 en la misma red Wi‑Fi.

## Qué incluye v0.3

- Tablet horizontal, responsive, modo inmersivo y pantalla siempre encendida.
- Pantalla de espera / estado de conexión.
- Guía de configuración PS5 + F1 25.
- Detección automática de la IPv4 local de la tablet (no hay ninguna IP personal hardcodeada).
- Listener UDP en puerto 20777.
- Parser versionado para F1 25 / 2025.
- Identificación del auto del jugador mediante `m_playerCarIndex`.
- Telemetría visible: velocidad, throttle, brake, marcha, RPM, 15 rev lights, DRS activo y temperatura interna de los 4 neumáticos.
- Recibe y procesa todos los datagramas; la publicación visual está limitada a ~30 Hz.
- Si deja de recibir paquetes F1 25 válidos por 2.5 s, vuelve automáticamente a la pantalla de espera.
- Si detecta un formato UDP distinto de 2025, muestra un aviso para seleccionar F1 25 / 2025 en el juego.

## Configuración en F1 25

1. Tablet y PS5 en la misma red Wi‑Fi.
2. Abrir F1 25.
3. Ajustes > Telemetría: activar UDP.
4. Formato UDP: F1 25.
5. IP destino: copiar la IP que muestra Telemétrico en la pantalla Configuración.
6. Puerto UDP: 20777.
7. Your Telemetry: Restricted.
8. Mostrar ID online: Off.

La app no necesita la IP de la PS5. La PS5 envía los paquetes a la IP local de la tablet.

## Build

Requisitos locales: Android Studio reciente / JDK 17 / Android SDK 35.

```bash
gradle :app:assembleDebug
```

APK resultante:

`app/build/outputs/apk/debug/app-debug.apk`

El repo incluye `.github/workflows/android-debug.yml` para validar que el APK compile correctamente mediante GitHub Actions.

## Publicación del APK en GitHub Releases

El circuito de distribución está automatizado en `.github/workflows/publish-apk.yml`.

1. La versión de la app se define en `app/build.gradle.kts` mediante `versionCode` y `versionName`.
2. Cada push a `main` verifica si ya existe una Release para ese `versionName`.
3. Si la versión todavía no fue publicada, GitHub Actions compila el APK.
4. Se crea automáticamente una GitHub Release con tag `v<versionName>`.
5. La Release contiene dos archivos equivalentes:
   - `Telemetrico-<versionName>.apk`, para conservar cada versión identificada.
   - `Telemetrico.apk`, con nombre estable para facilitar la descarga desde la tablet.
6. Si esa versión ya existe, el workflow no vuelve a publicarla.

Cuando el repositorio sea público, la URL estable para descargar siempre la última versión será:

`https://github.com/acarrera10/telemetrico/releases/latest/download/Telemetrico.apk`

Para publicar una nueva versión basta con incrementar `versionCode`, cambiar `versionName` y hacer push a `main`; el resto del proceso queda automatizado.
