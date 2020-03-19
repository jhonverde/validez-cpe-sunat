# Validar comprobantes (CPEs) en SUNAT

Proyecto que permite validar el estado de un CPE electrónico o fisico
en SUNAT a través de dos formas:

* Scrapping a la página de validez CPE SUNAT: http://www.sunat.gob.pe/ol-ti-itconsvalicpe/ConsValiCpe.htm
* Consumo de una API REST creada por SUNAT.

### Scrapping a la página de validez CPE SUNAT

Por este metódo puedes consultar la validez de un CPE en todos su tipos menos guías de remisión.

1. Se obtiene la imagen **captcha** y el texto es leído mediante el motor de reconocimiento de caracteres **tesseract**.
La instalación para cada sistema operativo se encuentra aquí: https://tesseract-ocr.github.io/tessdoc/Home.html
2. Luego de instalarlo deben crear una carpeta en su máquina donde está el archivo de entrenamiento (para el proyecto
se usa 'eng.traineddata') para leer el texto del **captcha** y colocarlo en el properties del proyecto.
Ejm: /opt/tesseract -> copiar el archivo **'eng.traineddata'** en esa carpeta, y luego 
reemplazar la ruta del archivo application.properties: 
**sunat.consulta.unificada.libre.tesseract-carpeta**. Los archivos de entrenamiento se encuentran aquí:
https://github.com/tesseract-ocr/tessdata
2. Se crea el request con los datos del CPE y el texto leído del **captcha**, ademas se envía una **cookie** que se
crea al solicitar el **captcha**.
3. SUNAT envía una respuesta en HTML, se filtra el resultado en base a unas tags previamente
categorizados que muestran si el CPE fue: **no aceptado, aceptado, anulado o reversado**.

### Consumo de una API REST creada por SUNAT
Por este metódo puedes consultar la validez de un CPE en todos su tipos menos guías de remisión, percepcion
y retención.

* SUNAT tiene una API REST dedicada para la validez de un CPE pero primero
tienen que registrarse en el portal SUNAT, para más detalle revisar el siguiente manual: http://orientacion.sunat.gob.pe/images/imagenes/contenido/comprobantes/Manual-de-Consulta-Integrada-de-Comprobante-de-Pago-por-ServicioWEB.pdf

### Pruebas

* En el repositorio esta el proyecto POSTMAN para que realicen sus pruebas.

### Lecciones aprendidas

* SUNAT no brinda una solución para consultar y validar masivamente CPEs asi que toca 
realizar tu propia solución. Crear un bachero donde traes de manera paginada tus CPES
de tu BD y luego por cada página realizar la validez de manera asincrona.
* La solución del scrapping con **tesseract** no es escalable para consultar grandes cantidades
de CPEs desde una BD.
* El API REST SUNAT da un buen rendimiento pero tiene como limitación que no acepta tipo
de comprobantes percepción y retención.
* La mejor solución es combinar ambas métodos con responsabilidad.

 