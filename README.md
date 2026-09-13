# Telemétrico — Android telemetry dashboard for F1 25

App Android nativa (Kotlin + Jetpack Compose) para recibir telemetría UDP de **F1 25 / formato 2025** desde PS5 en la misma red Wi‑Fi.

## v0.4.0-test

Esta versión amplía el milestone validado de v0.3 con el dashboard completo y los ajustes surgidos de la primera prueba real en tablet.

Incluye:

- Tablet horizontal, responsive, modo inmersivo y pantalla siempre encendida.
- Pantalla de espera, configuración y dashboard con el sistema visual definido para Telemétrico.
- Nuevo logo de espera integrado sobre fondo oscuro.
- Soundtrack de espera en loop, con el silencio final artificial removido antes de empaquetarlo como OGG.
- Detección automática de la IPv4 local de la tablet.
- Listener UDP en puerto 20777.
- Parser versionado para F1 25 / 2025 y combinación de múltiples tipos de paquetes.
- Velocidad, throttle, brake, marcha, RPM y DRS.
- Rev Lights con fallback: usa el bitfield de 15 luces cuando está disponible y, si llega en cero, deriva las luces desde `revLightsPercent`.
- Posición, vuelta actual/total, tiempos de vuelta y sectores.
- Gaps, combustible, ERS, compuesto y datos de neumáticos.
- Speed Trap, warnings y penalizaciones disponibles en el modelo de dashboard.
- Eliminación del índice interno del vehículo que se mostraba únicamente para diagnóstico en v0.3.

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

## Publicación del APK en GitHub Releases

El circuito de distribución está automatizado en `.github/workflows/publish-apk.yml`.

1. La versión se define en `app/build.gradle.kts` mediante `versionCode` y `versionName`.
2. Cada push a `main` verifica si ya existe una Release para ese `versionName`.
3. Si todavía no fue publicada, GitHub Actions compila el APK.
4. Se crea una GitHub Release con tag `v<versionName>`.
5. La Release contiene `Telemetrico-<versionName>.apk` y `Telemetrico.apk`.

Cuando el repositorio sea público, la URL estable para descargar siempre la última versión será:

`https://github.com/acarrera10/telemetrico/releases/latest/download/Telemetrico.apk`
