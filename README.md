# Ejemplo de proyecto EmpaLink

## Introducción

Este proyecto de muestra le brinda el código que necesita para conectarse a un dispositivo Empatica E4 y comenzar a transmitir datos.

La aplicación de muestra implementada en el proyecto tiene funcionalidades muy simples:

- Inicializa la librería EmpaLink con tu clave API.
- Si el paso anterior es exitoso, comienza a escanear los dispositivos Empatica, hasta que encuentra uno que se puede usar con la clave API que insertó en el código.
- Cuando se encuentra un dispositivo de este tipo, la aplicación se conecta a los dispositivos y transmite los datos durante 10 segundos, luego se desconecta.

## Configuración

- Clonar / descargar este repositorio.
- Abra el proyecto de muestra en Android Studio.
- Asegúrate de tener una clave API válida. Puede solicitar una para su cuenta de Empatica Connect en nuestra [Área de desarrolladores] [1].
- Edite `MainActivity.java` y asigne su clave API a la constante` EMPATICA_API_KEY`.
- Descargue el SDK de Android desde nuestra [Área de desarrolladores] [1].
- Descomprima el archivo que ha descargado y copie el archivo `.aar` que encontrará en la carpeta` libs` contenida en el proyecto de muestra.
- Construye y ejecuta el proyecto.
- Si un dispositivo está dentro del alcance y su luz parpadea en verde, pero la aplicación no se conecta, verifique que el dispositivo descubierto se pueda usar con su clave API. Si el parámetro `allowed` siempre es falso, el dispositivo no está vinculado a su clave API. Por favor revise su [Área de desarrolladores] [1].

Si necesita información adicional sobre la API de Empatica para Android, consulte la [documentación oficial] [2].

[1]: https://www.empatica.com/connect/developer.php
[2]: http://developer.empatica.com
