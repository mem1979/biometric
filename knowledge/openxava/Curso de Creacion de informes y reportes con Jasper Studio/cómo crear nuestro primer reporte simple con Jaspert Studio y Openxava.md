**IMPORTANTE TODOS LOS INFORMES SE REALIZAN EN JASPER STUDIO Y OPENXAVA**

**1- CREAR NUESTRO PRIMER REPORTE**

En este video veremos cómo crear nuestro primer reporte, definir el controlador y la acción para generar el reporte en una aplicación OpenXava.

**Código de Ejemplo:**

En el archivo *controllers.xml*:

<**controller** name=**"YourFirstEntity"**>

`    `<**extends** controller=**"Typical"**/>

`    `<**action** name=**"printCustomReport"** icon=**"printer"** mode=**"list"** 

`        `class=**"com.yourcompany.report.actions.PrintMyCustomReportAction"**>

`    `</**action**>

</**controller**>

Copy

En el archivo *PrintMyCustomReportAction.java*:

**public** **class** PrintMyCustomReportAction **extends** JasperReportBaseAction {

`	`@Override

`	`**protected** JRDataSource getDataSource() **throws** Exception {

`		`**return** **new** JREmptyDataSource();

`	`}

`	`@Override

`	`**protected** String getJRXML() **throws** Exception {

`		`**return** **"MyCustomReport.jrxml"**;

`	`}

`	`@Override

`	`**protected** Map getParameters() **throws** Exception {

`		`**return** **null**;

`	`}

}

Copy

**Transcripción**

Hola, soy Mónica; en este curso vamos a ver cómo usar JasperSoft Studio para crear reportes pdf personalizados y generarlos en una aplicación OpenXava. En esta primera lección vamos a crear un reporte básico con JasperSoft Studio, y en OpenXava vamos a definir un controlador con una acción que al ejecutarlo, genere el reporte.\
Primero vamos a ingresar al link que tenemos en la descripción del video para descargar JasperSoft Studio; vemos que al apretar el botón, nos pide crear una cuenta o loguearnos para descargar. Podemos crear una cuenta desde este botón, Join the community. Una vez que ya iniciemos la sesión, el botón ya dirá descargar, lo apretamos y seleccionamos la opción de acuerdo a nuestro sistema operativo. Ya descargado, lo ejecutamos para instalar JasperSoft Studio.

En JasperSoft Studio creamos un nuevo proyecto haciendo clic en create project; seleccionamos JasperReport Project y apretamos next. Le asignamos un nombre, por ejemplo MyReports. Cliqueamos en Finish. Para nuestro primer reporte vamos a mostrar únicamente un texto estático. Clic derecho en el proyecto, New, Jasper Report. Seleccionamos el reporte en blanco y apretamos Next. Ponemos my custom report como nombre, y apretamos Finish. A la derecha tenemos el panel de Basic Elements. Arrastramos Static text y lo ubicamos en la zona de Title del reporte. Haciéndole doble clic podemos editar el texto. Ponemos My custom report y guardamos.

Vamos a OpenXava Studio y creamos un nuevo proyecto desde OpenXava, New OpenXava Project. Le asignamos un nombre al proyecto, por ejemplo report. Cliqueamos finish. Esperamos un rato. OpenXava ya tiene las librerías necesarias para generar un reporte, así que no hace falta agregar ninguna. Listo. Por defecto, OpenXava genera la entidad YourFirstEntity, así que lo vamos a aprovechar. Iniciamos la aplicación. Copiamos el link y lo pegamos en el navegador. Iniciamos sesión con admin, admin. Vamos al modo lista del módulo YourFirstEntity. Nuestro objetivo es tener un botón en modo lista, que cuando lo apretamos nos muestre el reporte que creamos recién. Para esto necesitamos tener una acción que haga eso. Vamos al archivo controllers.xml ubicado en la carpeta source main resources xava. Aquí se declaran los controladores y sus acciones. Copiamos este código y lo pegamos debajo. En controller name ponemos YourFirstEntity, este debe tener el mismo nombre que el módulo donde va a ser usado. Dejamos que extienda del controlador Typical para mantener las acciones que ya existían. En action es donde tenemos que definir nuestra acción que en este caso es un botón para generar un pdf. En name asignamos un nombre que se va a mostrar en el botón. En icon ponemos printer. No vamos a usar atajos de teclado. Ponemos una nueva propiedad mode con valor list, que significa que se verá en modo lista. Class es la clase donde contiene la lógica de la acción.

Vamos a crear la acción. Hacemos clic derecho en source main java, New, package para crear un nuevo paquete. Lo llamaremos com.yourcompany.report.actions. Y creamos una nueva clase haciendo clic derecho en el paquete, New, Class. Lo vamos a llamar PrintMyCustomReportAction. Una vez creado, extendemos nuestra acción de la clase JasperReportBaseAction. Y guardamos para que se importe. Vemos que nos pide añadir métodos necesarios. Lo hacemos. Aquí nos añade 3 métodos. getDataSource, no lo usaremos en esta lección, pero es un método donde nunca podemos retornar nulo, ya que un reporte puede tener un data source vacío pero no nulo. getJRXML, aquí debemos definir el nombre de nuestro archivo jrxml que va a usar para generar el reporte. getParameters, aquí podemos enviarle parámetros al reporte para que los use. Habíamos dicho que no podemos retornar nulo en getDataSource, así que enviaremos un new JREmptyDataSource(). El nombre del reporte debe ser el mismo que vamos a usar, en nuestro caso, MyCustomReport.jrxml. Guardamos. Volvemos a controllers.xml y apuntamos a la clase de la acción. com.yourcompany.report.actions.PrintMyCustomReportAction. Guardamos. La acción está lista, lo único que nos falta es copiar el reporte en el proyecto de OpenXava. En Jasper studio copiamos nuestro reporte. Y en OpenXava creamos una nueva carpeta llamada reports en source main resources. Pegamos el reporte dentro de la carpeta y listo. Iniciamos nuevamente la aplicación.

Vamos al modo lista de YourFirstEntity y vemos que la acción se muestra aquí. Le damos clic y saldrá otra ventana con el reporte que creamos.

Fue bastante simple tener el botón para generar un reporte. Espero que el video te haya servido de guía. Si tienes alguna duda sobre esta lección, puedes preguntarnos por el foro, también puedes descargar el código de esta lección por el link del repositorio, ambos enlaces se encuentran en la descripción del video. Nos vemos en la próxima lección donde vamos a aprender cómo pasar parámetros del OpenXava al reporte y utilizarlos allí. Chao.

**2- CÓMO ENVIAR PARÁMETROS A NUESTRO REPORTE DESDE NUESTRA APLICACIÓN OPENXAVA.**

**Código de Ejemplo:**

Puedes [**descargar el proyecto de esta lección**](https://github.com/openxava/report-generation-course/tree/lesson_2)**.** También puedes copiar el código que se usa en el video por aquí:

En el archivo *controllers.xml*:

<**controller** name=**"Product"**>

`    `<**extends** controller=**"Invoicing"**/>

`    `<**action** name=**"printProductDetail"**

`        `class=**"com.yourcompany.invoicing.actions.PrintProductAction"**	mode=**"detail"**	icon=**"printer"**/>

</**controller**>

Copy

En el archivo *PrintProductAction.java*:

**public** **class** PrintProductAction **extends** JasperReportBaseAction {

`	`**private** Product product;



`	`@Override

`	`**protected** JRDataSource getDataSource() **throws** Exception {

`		`**return** **new** JREmptyDataSource();

`	`}

`	`@Override

`	`**protected** String getJRXML() **throws** Exception {

`		`**return** **"ProductDetail.jrxml"**; 

`	`}

`	`@Override

`	`**protected** Map getParameters() **throws** Exception {

`		`**Messages** errors = MapFacade.validate(**"Product"**, getView().getValues());

`		`**if** (errors.contains()) **throw** **new** ValidationException(errors);



`		`**Map** parameters = **new** HashMap();	

`		`parameters.put(**"id"**, (getProduct().getNumber()));

`		`parameters.put(**"description"**, getProduct().getDescription());

`		`parameters.put(**"author"**, getProduct().getAuthor().getName());

`		`parameters.put(**"isbn"**, getProduct().getIsbn());

`		`parameters.put(**"category"**, getProduct().getCategory().getDescription());

`		`parameters.put(**"price"**, getProduct().getPrice());



`		`**return** parameters;

`	`}

`	`**private** Product getProduct() **throws** Exception {

`		`**if** (product == **null**) {

`			`**int** number = getView().getValueInt(**"number"**);

`			`product = XPersistence.getManager().find(Product.class, Integer.valueOf(number));

`		`}

`		`**return** product;

`	`}

}

Copy

**Transcripción**

Hola, soy Mónica. En esta lección veremos un ejemplo simple de cómo enviar parámetros desde OpenXava y usarlos en el reporte. Para esto, utilizaremos la última lección del curso de OpenXava.\
\
Primero vamos a importar el proyecto de la lección 26 del curso de OpenXava en el repositorio github.com/openxava/openxava-course\_en. En Code descargamos el proyecto con download ZIP.\
Vamos a OpenXava Studio, en el panel package explorer hacemos click derecho y hacemos click en Import. Seleccionamos Project from Folder or Archive de General. Apretamos Next. Aquí en Archive abrimos el explorador y seleccionamos el archivo que acabamos de descargar. Nos mostrará los proyectos o carpetas disponibles para importar. Dejamos seleccionado únicamente la opción que tenga Eclipse project en Import as. Apretamos Finish.\
Listo, ya tenemos el proyecto, pero aún hacen falta unos pasos para iniciarlo. Click derecho en el proyecto, Run As, Maven install. Una vez que diga Success, hacemos de nuevo click derecho en el proyecto, Maven, Update project.\
Ahora sí, vamos al paquete com.yourcompany.invoicing.run e iniciamos el proyecto. Click derecho en invoicing, Run As, Java Application. Copiamos el enlace y lo abrimos en el navegador. Ingresamos con Admin, admin. Podemos visualizar que ya hay registros para poder aprovechar de ello.\
\
Vamos a Jaspersoft Studio y creamos un nuevo reporte llamado Product detail. Eliminamos las secciones que no vamos a necesitar. Nuestro objetivo sería mostrar algunas de las propiedades que tiene producto a partir de los parámetros que recibimos.\
En la lección pasada vimos cómo usar static text, lo usaremos para ID. Luego arrastra un TextField y le damos doble click. Podemos ver que este elemento ofrece mucho más que un static text, se puede usar para algo tan simple como un texto, como también dejar código con una lógica simple, que en nuestro caso sería código Java. Por el momento vamos a dejar solo texto, recuerda que debe ser entre comillas dobles. Así, creamos varios TextField y le incluimos los labels de cada propiedad que queremos mostrar. Nombre, Precio, Categoría, Autor, ISBN.\
\
En OpenXava Studio, debemos crear una acción para imprimir el reporte, así que primero vamos a definirlo en su controlador. Como el módulo Producto aún no tiene controlador, creamos uno. Extendemos del controlador Invoicing. Para la acción, en nombre ponemos Print Product Detail, dejamos a class vacío y en modo ponemos detail para que la acción aparezca allí. Por último, ponemos algún ícono para la acción, por ejemplo, printer.\
Ya casi está listo, faltaría crear la clase de la acción y luego ubicar su ruta en el class que dejamos vacío. En el paquete com.yourcompany.invoicing.actions creamos una nueva clase llamada Print Product Action. Una vez creado, extendemos de JasperReportBaseAction y añadimos los métodos que nos pide. En getDataSource devolvemos new JREmptyDataSource. En getJRXML ponemos el nombre del reporte. Aquí en getParameters, es donde vamos a definir los parámetros a enviar. Añadimos un mensaje de error, que lo muestra si no se encuentran los valores de Product en la vista donde llamamos la acción.\
Importamos de openxava.validators. Creamos un map para guardar los parámetros. Hay distintas formas de obtener la información, por ejemplo, desde la base de datos o también obtenerlo directamente de la vista donde se llama la acción. Nosotros iremos por la primera opción, vamos a hacer una consulta JPA a partir del número del producto, que en este caso es su ID, obtener toda la información del producto y añadir lo necesario en el mapa. Por ejemplo, queremos añadirle al key ID, el valor de producto número. Para esto vamos a definir un Producto, y hacemos un método get para ese producto.\
En caso de que producto sea nulo cuando queremos obtenerlo, tomamos el número de la vista actual y lo usaremos para buscar el producto en nuestra base de datos por JPA. Con el getView obtenemos la vista actual y de esa vista obtenemos un valor int con el parámetro number. En este caso, lo estamos obteniendo desde aquí. Este parámetro se debe llamar igual a la propiedad como está declarado en la clase. Ahora hacemos la consulta JPA usando el número que obtuvimos y por último retornamos el producto.\
Ahora en los parámetros pondremos como clave a "id" y getProduct().getNumber() como su valor. Así, con toda la información que queremos enviar. Aquí hay dos cosas para recalcar, el número del producto es un tipo int, así que estamos enviando un tipo int en el mapa. Por otra parte, si te fijas, en el caso del autor y categoría, no estoy enviando su id, sino directamente una información más relevante. Retornamos el mapa y guardamos. Volvemos al controlador y en class ponemos la ruta completa de la acción.\
\
Ya tenemos la acción que envía parámetros al reporte. Ahora en el reporte, también debemos crear parámetros. Lo hacemos desde el panel Outline. Click derecho en Parameters. Create Parameter. Seleccionamos el elemento Parameter1 que se creó, en este caso ya está seleccionado. En el panel de properties del elemento, le damos el nombre id y en class seleccionamos Integer. Cada parámetro que enviamos desde la acción, debe haber uno igual en el reporte para poder recibirlo. Por ejemplo, si habíamos enviado un parámetro ID de tipo int, entonces en el reporte debemos tener también un parámetro con nombre ID y como class Integer. Y así creamos los otros 6 parámetros en el reporte.\
El texto id del reporte como era un static text, debemos usar un TextField para mostrar el valor del parámetro. Dentro del TextField usamos el signo de Dinero. P de parámetro. Y entre llaves el nombre del parámetro. En los otros casos, como ya teníamos un TextField, debemos agregar antes del parámetro un signo de suma, como si estuviéramos armando un String en Java. También podemos arrastrar directamente el elemento parámetro desde el panel Outline y luego le agregamos el texto. Una vez que tengamos todo listo, guardamos y copiamos el reporte a la carpeta reports que debemos crear en el proyecto. Y ya podemos iniciar la aplicación. Probamos con alguno de los registros y podemos ver los resultados.\
\
Trabajar con parámetros es interesante, puedes enviar lo que quieras como parámetro, listas, colecciones, entre otros. Luego en el reporte lo recibes con ese tipo de dato para trabajar con ello. Si tienes alguna duda sobre esta lección, puedes preguntarnos por el foro, también puedes descargar el código de esta lección por el link del repositorio, ambos enlaces se encuentran en la descripción del video. Nos vemos en la próxima lección donde vamos a ver cómo trabajar con imágenes en el reporte. Chao

**3- CÓMO CREAR UN INFORME MAESTRO DETALLE RECIBIENDO INFORMACIÓN DE UN DATA SOURCE DESDE UNA APLICACIÓN OPENXAVA.** 

**Código de Ejemplo:**

En el archivo *controllers.xml*:

<**controller** name=**"Invoice"**>

`    `<**extends** controller=**"Invoicing"**/>

`    `<**action** name=**"printInvoiceDetail"**

`        `class=**"com.yourcompany.invoicing.actions.PrintInvoiceDetailAction"**	mode=**"detail"**	icon=**"printer"**/>

</**controller**>

Copy

En el archivo *PrintInvoiceDetailAction.java*:

**public** **class** PrintInvoiceDetailAction **extends** JasperReportBaseAction {

`	`**private** Invoice invoice;



`	`@Override

`	`**protected** JRDataSource getDataSource() **throws** Exception {

`		`**return** **new** JRBeanCollectionDataSource(getInvoice().getDetails());

`	`}

`	`@Override

`	`**protected** String getJRXML() **throws** Exception {

`		`**return** **"InvoiceDetail.jrxml"**;

`	`}

`	`@Override

`	`**protected** Map getParameters() **throws** Exception {

`		`**Messages** errors = MapFacade.validate(**"Invoice"**, getView().getValues());

`		`**if** (errors.contains()) **throw** **new** ValidationException(errors);

`		`**Map** parameters = **new** HashMap();			

`		`parameters.put(**"customerNumber"**, getInvoice().getCustomer().getNumber());

`		`parameters.put(**"customerName"**, getInvoice().getCustomer().getName());

`		`parameters.put(**"invoiceNumber"**, getInvoice().getNumber());

`		`parameters.put(**"date"**, getInvoice().getDate().toString());

`		`parameters.put(**"vatPercentage"**, getInvoice().getVatPercentage());

`		`parameters.put(**"vat"**, getInvoice().getVat());

`		`parameters.put(**"totalAmount"**, getInvoice().getTotalAmount());



`		`**return** parameters;

`	`}

`	`**private** Invoice getInvoice() {

`		`**if** (invoice == **null**) {

`			`**int** year = getView().getValueInt(**"year"**);

`			`**int** number = getView().getValueInt(**"number"**);

`			`invoice = Invoice.findByYearNumber(year, number);

`		`}

`		`**return** invoice;

`	`}



}

Copy

En la clase *Invoice* añadir el método:

` `**public** **static** Invoice findByYearNumber(**int** year, **int** number) {

`    `**Query** query = XPersistence.getManager()

.createQuery(**"from Invoice as i where i.year = :year and number = :number"**);

`    `query.setParameter(**"year"**, year);

`    `query.setParameter(**"number"**, number);

`    `**return** (Invoice) query.getSingleResult();

}

Copy

**Transcripción**

Hola, soy Mónica. En esta lección veremos un ejemplo simple de cómo trabajar con data source. Para esto, utilizaremos la última lección del curso de OpenXava.\
\
El objetivo de hoy será crear un botón que imprima la factura, incluyendo las líneas de detalle. Para esto vamos a crear la acción. Como Invoice ya tiene su controlador, definimos la acción printInvoiceDetail directamente dentro. Luego vamos a crear la acción PrintInvoiceDetailAction. También extendemos de la clase JasperReportBaseAction. De la misma manera que hicimos en la lección anterior, primero enviaremos algunos datos de la factura como parámetro. Solo que vamos a cambiar un poquito la forma de buscar la factura. En la acción tendremos un método que obtiene los valores año y número de la vista. Y llama un método findByYearNumber que crearemos en Invoice, enviando como parámetro al año y número. Este método hará la búsqueda con los valores recibidos y devolverá la factura. Una vez que tengamos los parámetros definidos, ponemos el nombre del reporte que luego vamos a crear y en getDataSource, devolvemos un nuevo JRBeanCollectionDataSource. Se pueden enviar casi cualquier dato por data source, como colecciones, mapas, tablas, JSON, entre otros. Nosotros enviaremos la colección de detalles que tiene factura.\
\
En Jaspersoft Studio, creamos un nuevo reporte llamado InvoiceDetail. Y procedemos a declarar los parámetros que esperamos recibir. Recuerda que deben ser del mismo tipo de dato que enviamos desde OpenXava. Una vez hecho, arrastramos los datos de cliente y factura a la zona de título. El resumen de la línea de detalles a la zona de pie de columna. Y eliminamos las secciones que no vamos a usar. Ahora toca tratar con los datos de la colección que recibimos del data source. Para esto debemos crear fields cuyo nombre deben ser iguales a los datos que contiene. En Detalle tenemos una propiedad quantity, entonces definimos un field llamado quantity. Tenemos producto, que nos interesa su número y descripción, entonces definimos product.number y product.description. También vamos a incluir Amount y pricePerUnit. Hacemos click derecho en Fields, Create field y definimos uno por uno los campos que dijimos recién. Recuerda que deben tener los mismos tipos de dato.\
\
Listo, arrastramos los fields a la sección de detail 1. Podemos ver que en la sección del encabezado de columna se han creado automáticamente static text, uno para cada campo, donde cada campo representa una columna. Debajo encontramos una línea de TextField, que muestran los valores de los Fields que declaramos recién. Estas líneas de TextField van a repetirse uno debajo de otro hasta terminar con todos los elementos que hay en la colección. Adornamos un poco el reporte y lo copiamos al proyecto de OpenXava para probarlo.\
\
Iniciamos la aplicación. Al parecer los datos se muestran correctamente, solo quedan algunos detalles como el signo de porcentaje o dinero en donde se necesita. O si queremos cambiar el formato de fecha. También que el pie de columna está al final de reporte, esta configuración es por defecto así, pero podemos cambiarlo.\
\
Primero agregamos los signos de dinero y porcentaje. Si apretamos fuera del reporte, podemos ver en el panel de propiedades que este reporte se está trabajando en Java. Entonces, para tratar con fechas, vamos a hacerlo como si estuviésemos en Java. Cabe aclarar que en date, lo estamos recibiendo como un String. Así que primero definimos un SimpleDateFormat con un formato de fecha en que deseamos mostrar. Pero no podemos poner nuestra fecha directamente, sino que debemos convertirlo primero de String a fecha. Así que usaremos otro SimpleDateFormat con el formato de fecha que tiene para convertirlo de String a fecha.\
\
Por último, queremos que las líneas del pie de columna aparezcan justo abajo de las líneas de detalle. Así que apretamos de nuevo afuera del reporte y tildamos en la opción Float Column Footer. Esto hará que el pie de columna esté inmediatamente luego de la última línea de detalles. Con esto ya estaría, guardamos y copiamos el reporte al proyecto y probamos en generarlo nuevamente. Así serían los resultados.\
\
Como mencionamos en el video, puedes enviar muchos tipos de data source, como colecciones, mapas, listas, imágenes, JSON, entre otros. También puedes enviarlo vacío y definir el data source en el reporte, esta forma la veremos más adelante. Si tienes alguna duda sobre esta lección, puedes preguntarnos por el foro. También puedes descargar el código de esta lección por el enlace del repositorio; ambos enlaces se encuentran en la descripción del video.






**4- CÓMO ENVIAR IMÁGENES DESDE OPENXAVA COMO PARÁMETRO Y DATA SOURCE A NUESTRO REPORTE.**

**Código de Ejemplo:**

\


**public** **class** PrintProductAction **extends** JasperReportBaseAction {

`	`**private** Product product;



`	`@Override

`	`**protected** JRDataSource getDataSource() **throws** Exception {

`		`**return** **new** JRBeanCollectionDataSource(FilePersistorFactory.getInstance().findLibrary(getProduct().getPhotos()));

`	`}

`	`@Override

`	`**protected** String getJRXML() **throws** Exception {

`		`**return** **"ProductDetail.jrxml"**; 

`	`}

`	`@Override

`	`**protected** Map getParameters() **throws** Exception {

`		`**Messages** errors = MapFacade.validate(**"Product"**, getView().getValues());

`		`**if** (errors.contains()) **throw** **new** ValidationException(errors);



`		`**Map** parameters = **new** HashMap();	

`		`parameters.put(**"id"**, (getProduct().getNumber()));

`		`parameters.put(**"description"**, getProduct().getDescription());

`		`parameters.put(**"author"**, getProduct().getAuthor().getName());

`		`parameters.put(**"isbn"**, getProduct().getIsbn());

`		`parameters.put(**"category"**, getProduct().getCategory().getDescription());

`		`parameters.put(**"price"**, getProduct().getPrice());

`				`Collection<AttachedFile> attachedFiles = FilePersistorFactory.getInstance().findLibrary(getProduct().getPhotos());

`		`**byte**[] file = attachedFiles.iterator().next().getData();

`		`parameters.put(**"photoFromParameter"**, file);

`		`**return** parameters;

`	`}

`	`**private** Product getProduct() **throws** Exception {

`		`**if** (product == **null**) {

`			`**int** number = getView().getValueInt(**"number"**);

`			`product = XPersistence.getManager().find(Product.class, Integer.valueOf(number));

`		`}

`		`**return** product;

`	`}

}

Copy

**Transcripción**

Hola, soy Mónica. En esta lección aprenderás a diseñar un reporte que incluya imágenes y a enviarlas desde tu aplicación OpenXava. Veremos cómo enviar una única imagen que aparezca en los datos de cabecera, con un parámetro. Y también a enviar una colección de imágenes usando un DataSource.\
\
Vamos a incluir imágenes en el reporte que creamos en la lección 2. Para esto debemos modificar la acción.\
Primero vamos a incluir una imagen como parámetro. Declaramos una colección de AttachedFile. Esta colección la obtenemos usando findLibrary enviando el ID de la librería o galería como parámetro; este ID se encuentra en getPhotos. Si vamos a la clase Product, podemos ver que photos es un String, que es usado para guardar la llave de la librería. Y declaramos una colección de AttachedFile porque photos tiene la anotación de @Files, donde indica que puede adjuntar más de un archivo o, en este caso, imagen.\
Vamos a enviar la primera imagen de la colección. Para esto, declaramos un file de tipo byte y le asignamos el getData del primer elemento de la colección. Por último, lo enviamos como parámetro.\
En caso de que tengamos una anotación @File, en vez de declarar una colección de AttachedFile, sería solo uno. Y en vez de usar findLibrary, usamos solamente find para buscar el archivo adjunto enviando getPhoto como parámetro. También obtenemos la imagen con getData.\
Comentamos todo lo que hicimos recién porque no usaremos @File. Ahora vamos a enviar la colección de imágenes como datasource usando lo mismo que vimos en la lección anterior. Teníamos una colección de AttachedFile, enviamos directamente eso. Listo. Ahora toca modificar el reporte.\
\
En el reporte de ProductDetail, arrastramos un elemento Image al reporte. Vemos que hay varias formas de insertar una imagen: desde el workspace, desde nuestro sistema, usar un URL, entre otros. Dejamos la opción en "No image". Ahora creamos un parámetro para recibir la imagen. Llevará el mismo nombre que pusimos recién y como tipo de dato será object. Si no lo vemos en la lista desplegable, lo debemos buscar manualmente.\
Una vez creado el parámetro, vamos a la vista source del reporte y buscamos el elemento imagen para añadirle la expresión. También lo podemos hacer directamente desde el panel de propiedades. Aquí está el elemento de la imagen. Estamos enviando un array de byte y lo recibimos como un objeto. Esto está bien. Pero ahora tenemos que interpretar ese array de bytes, por eso usamos ByteArrayInputStream, ponemos el parámetro y le aclaramos que el objeto recibido es un array de bytes.\
Listo. En caso del datasource, en la clase pasada vimos que debemos acceder directamente a la propiedad de cada elemento. En este caso, data es uno de ellos. Creamos un field llamado data de tipo object. Añadimos las secciones que eliminamos anteriormente. Y arrastramos data a la sección detail. Luego en la vista source modificamos manualmente el elemento de un textField a una imagen, debemos cambiar la etiqueta de textField por image y textFieldExpression por imageExpression.\
Ahora añadimos la expresión, similar a la de recién, pero usando field data. Guardamos y ordenamos los elementos del reporte. Listo, copiamos el reporte y lo probamos. Recuerda que debes iniciar de nuevo la aplicación. Al parecer está funcionando bien.\
\
Añadí dos imágenes más para probar cómo se vería. Por defecto, las líneas de detalle se agregan una debajo de otra. Vamos a cambiar esto. Apretando fuera del reporte, podemos editar las propiedades del mismo. En Edit Page Format, cambiamos a 3 columnas y que el orden de la impresión sea horizontal. Hicimos los cambios, pero al parecer no dieron efecto. Vamos a advanced e ingresamos manualmente los valores: 3 en column count y horizontal en print order. Ahí está.\
Luego seleccionamos el static text y en el panel de propiedades destildamos Print repeated Values para que no imprima el texto cada vez que se repite el elemento. Y tildamos Print in First Whole Band para que se imprima la primera vez. Copiamos y probamos de nuevo el reporte. Así serían los resultados.\
En esta lección has visto dos técnicas para enviar imágenes: los parámetros y el datasource. También has aprendido a obtener las imágenes de propiedades anotadas con Files o File, aunque puedes enviar cualquier imagen obtenida de cualquier fuente como un array de bytes. Además, vimos cómo diseñar el informe para que distribuya las imágenes a nuestro gusto.


**5- CÓMO TRABAJAR CON CÓDIGOS DE BARRA EN NUESTRO REPORTE UTILIZANDO DATOS ENVIADOS DESDE LA APLICACIÓN OPENXAVA**.

**Código de Ejemplo:**

En el archivo *pom.xml*:

<**dependency**>

`    `<**groupId**>net.sf.barcode4j</**groupId**>

`    `<**artifactId**>barcode4j</**artifactId**>

`    `<**version**>2.1</**version**>

</**dependency**>

<**dependency**>

`    `<**groupId**>org.apache.xmlgraphics</**groupId**>

`    `<**artifactId**>batik-bridge</**artifactId**>

`    `<**version**>1.11</**version**>

</**dependency**>

<**dependency**>

`    `<**groupId**>com.google.zxing</**groupId**>

`    `<**artifactId**>core</**artifactId**>

`    `<**version**>3.3.0</**version**>

</**dependency**>

<**dependency**>

`    `<**groupId**>com.google.zxing</**groupId**>

`    `<**artifactId**>javase</**artifactId**>

`    `<**version**>3.3.0</**version**>

</**dependency**>

Copy

**Transcripción**

Hola, soy Mónica. En esta lección aprenderás a generar códigos de barra y códigos QR en el reporte, a partir de información enviada desde OpenXava.\
\
Siguiendo con el reporte de la lección pasada, arrastramos un elemento barcode al reporte. De todas las opciones disponibles elegimos EAN13 de Barcode4J. En el barcode del panel de properties podemos asignarle una expresión. Vamos a usar el parámetro ISBN. Guardamos, copiamos e iniciamos la aplicación. Luego de esperar un rato no genera el reporte. Vamos a la consola de OpenXava Studio y aparece un error. Dice que no puede encontrar una clase de la librería barcode4j. Copiamos la traza y lo pegamos en el buscador. Aquí nos propone una solución, que es agregar una dependencia en el archivo pom.xml ubicado en la raíz del proyecto. Copiamos la dependencia y lo pegamos en el pom. Guardamos y hacemos un maven install antes de iniciar la aplicación. Probamos en generar el reporte de nuevo. El reporte se generó pero está incompleto, vamos a la consola a ver qué error dio esta vez. También dice que no encuentra una clase. Hacemos lo mismo, buscamos en el navegador. Aquí dice que la solución es agregando una dependencia más. Así que lo pegamos en el pom y hacemos maven install antes de iniciar la aplicación. Esta vez funcionó. Así sería el resultado, el elemento genera un código de barra a partir de los 13 dígitos del ISBN que recibe.\
\
Vamos a probar otro más. Esta vez vamos a seleccionar QRCode y también le asignamos el parámetro de ISBN. Guardamos, copiamos y probamos el reporte. Nos dice que no puede encontrar una clase. Copiamos la traza en el buscador a ver si encontramos alguna solución. Al parecer nos faltan estas dos dependencias. Lo agregamos en nuestro pom. Guardamos y hacemos maven install antes de reiniciar la aplicación. Ahí está el QRCode, si lo escaneamos nos mostrará su información, que sería el ISBN.\
\
En esta lección has visto dos tipos de código de barra: EAN13 y QRCode. Existen otros tipos de barcode, cada uno de ellos tiene un diseño específico para satisfacer las necesidades de su ámbito particular. Te invitamos a que pruebes los otros tipos.

**6- CÓMO GENERAR INFORMES PERSONALIZADOS DESDE MODO LISTA DE CADA MÓDULO DE NUESTRA APLICACIÓN OPENXAVA.**

**Código de Ejemplo:**

En el archivo *PrintInvoiceListAction.java*:

**public** **class** PrintInvoiceListAction **extends** JasperReportBaseAction {

`	`@Inject

`	`**private** Tab tab;

`	`@Override

`	`**protected** JRDataSource getDataSource() **throws** Exception {

`		`**List** invoiceList = **new** ArrayList();

`		`**if** (tab.getSelectedKeys().length > 0) {

`			`**for** (Map key : tab.getSelectedKeys()) {

`				`**Invoice** invoice = (Invoice) MapFacade.findEntity(**"Invoice"**, key);

`				`invoiceList.add(invoice);

`			`}

`		`} **else** {

`			`**for** (**int** i = 0; i<tab.getTableModel().getRowCount(); i++) {

`				`**Invoice** invoice = (Invoice) MapFacade.findEntity(**"Invoice"**, (Map) tab.getTableModel().getObjectAt(i));

`				`invoiceList.add(invoice);

`			`}

`		`}

`		`**return** **new** JRBeanCollectionDataSource(invoiceList);

`	`}

`	`@Override

`	`**protected** String getJRXML() **throws** Exception {

`		`**return** **"InvoiceList.jrxml"**;

`	`}

`	`@Override

`	`**protected** Map getParameters() **throws** Exception {

`		`**return** **null**;

`	`}

}

Copy

En el archivo *controllers.xml*:

<**action** name=**"printInvoiceList"**

`      `class=**"com.yourcompany.invoicing.actions.PrintInvoiceListAction"**

`      `mode=**"list"**

`      `icon=**"printer"**/>

Copy

**Transcripción**

Hola, soy Mónica. En esta lección aprenderás a diseñar reportes personalizados para el modo lista.\
\
Actualmente OpenXava ofrece generar reportes en el modo lista de cada módulo. Aunque puedas agregar columnas, quitarlas y ordenar los registros antes de generar el reporte, estos siempre tienen el mismo diseño. Así que en esta lección veremos cómo generar nuestro propio reporte desde modo lista de Invoice. Para esto necesitamos crear una acción primero. Cambiamos el nombre de la acción, también cambiamos la clase por otra que vamos a crear luego y en mode debemos especificar list para que la acción aparezca en la vista de lista. Luego creamos la acción PrintInvoiceListAction. Extendemos de JasperReportBaseAction. No trabajaremos con parámetros esta vez. Si no que enviaremos directamente una colección, en este caso, la lista de registros. Para trabajar con la tabla que vemos en la lista, debemos declarar un Tab con la anotación Inject. Importamos de openxava tab. En caso de que el usuario haya seleccionado alguna fila, tomamos las claves de las filas seleccionadas y buscamos uno por uno a partir de la clave, para luego añadirlo a la lista. En caso de que el usuario no haya seleccionado nada, recorremos toda la tabla y usamos la clave de cada fila para buscar el registro y añadirlo a la lista. Por último enviamos esa lista como data source.\
\
En Jaspersoft, creamos un nuevo reporte llamado InvoiceList. Creamos título y fecha en la sección Title. Y eliminamos las secciones que no vamos a usar. Luego creamos los fields, recuerda que deben tener el mismo tipo de dato con el cual se enviaron. Una vez que lo tengamos listo, los arrastramos a la sección detail 1 y le agregamos unos detalles para que esté ordenado. Listo. Guardamos y lo copiamos al proyecto antes de reiniciarlo. Vamos a generar un reporte sin seleccionar registros, de esta forma se ven todos los registros de la lista. En cambio, si seleccionamos algunos, solo se mostrarán esos.\
\
Esto es un ejemplo simple. Pero en tu reporte puedes agregar estilos, imágenes y otros elementos más. Por ejemplo, yo quiero que el reporte me marque las filas de las facturas cuando el monto total supere los 100. Entonces puedo hacer algo así. Arrastro un elemento rectángulo a la sección detail, lo ajusto a que tenga un tamaño similar a una fila, le cambio el color de fondo y en la lógica le agrego que si totalAmount es mayor a 100, muestra el elemento. De lo contrario, no lo hace. Como totalAmount es un BigDecimal, tengo que agregarle un intValue para que tome el valor entero. Por último hago que el elemento se muestre en el fondo de la fila. Guardamos, copiamos el reporte al proyecto y reiniciamos la aplicación. Así quedaría.\
\
Con lo visto en esta lección ya puedes acceder a cada registro que se encuentra en modo lista, trabajar con ello y luego enviarlo al reporte. Hemos usado data source para enviar una lista al reporte, pero también puedes hacerlo con parámetros.

**7- CÓMO CREAR UN INFORME MAESTRO DETALLE TRABAJANDO CON VARIAS COLECCIONES EN EL MISMO INFORME.**

**Código de Ejemplo:**

En el archivo *controllers.xml*:

<**controller** name=**"Invoice"**>

`    `<**extends** controller=**"Invoicing"**/>

`    `<**action** name=**"printInvoiceWithOrders"**

`	    `class=**"com.yourcompany.invoicing.actions.PrintInvoiceWithOrdersAction"**

`	    `mode=**"detail"**

`	    `icon=**"printer"**/>

</**controller**>

Copy

En el archivo *PrintInvoiceWithOrdersAction.java*:

**public** **class** PrintInvoiceWithOrdersAction **extends** JasperReportBaseAction {

`	`**private** Invoice invoice;



`	`@Override

`	`**protected** JRDataSource getDataSource() **throws** Exception {

`		`**return** **new** JREmptyDataSource();

`	`}

`	`@Override

`	`**protected** String getJRXML() **throws** Exception {

`		`**return** **"InvoiceWithOrders.jrxml"**;

`	`}

`	`@Override

`	`**protected** Map getParameters() **throws** Exception {

`		`**Messages** errors = MapFacade.validate(**"Invoice"**, getView().getValues());

`		`**if** (errors.contains()) **throw** **new** ValidationException(errors);

`		`**Map** parameters = **new** HashMap();		

`		`parameters.put(**"customerNumber"**, getInvoice().getCustomer().getNumber());

`		`parameters.put(**"customerName"**, getInvoice().getCustomer().getName());

`		`parameters.put(**"invoiceNumber"**, getInvoice().getNumber());

`		`parameters.put(**"date"**, getInvoice().getDate().toString());

`		`parameters.put(**"vatPercentage"**, getInvoice().getVatPercentage());

`		`parameters.put(**"vat"**, getInvoice().getVat());

`		`parameters.put(**"totalAmount"**, getInvoice().getTotalAmount());



`		`parameters.put(**"details"**, **new** JRBeanCollectionDataSource(getInvoice().getDetails()));

`		`parameters.put(**"orders"**, **new** JRBeanCollectionDataSource(getInvoice().getOrders()));



`		`**return** parameters;

`	`}

`	`**private** Invoice getInvoice() {

`		`**if** (invoice == **null**) {

`			`**int** year = getView().getValueInt(**"year"**);

`			`**int** number = getView().getValueInt(**"number"**);

`			`invoice = Invoice.findByYearNumber(year, number);

`		`}

`		`**return** invoice;

`	`}



}

Copy

**Transcripción**

Hola, soy Mónica. En esta lección aprenderás a diseñar reportes para trabajar con dos o más colecciones enviando una JRDataSource como parámetro para cada colección.\
\
En controllers creamos una acción para imprimir una factura en modo detalle. Lo llamaremos printInvoiceWithOrders y apuntará a la acción PrintInvoiceWithOrdersAction. Creamos esta nueva acción en el paquete actions y copiamos de PrintInvoiceDetailAction todo el código que tiene. Agregamos dos parámetros a enviar: las líneas de detalles de factura y los órdenes asociados. Hemos enviado colecciones de esta manera por data source; lo haremos de la misma manera en parámetros. Cambiamos el nombre del reporte por InvoiceWithOrders, que crearemos luego, y retornamos una data source vacía.\
\
Creamos un nuevo reporte llamado InvoiceWithOrders y vamos a copiar algunos elementos del reporte InvoiceDetail.Seleccionamos la sección de título y lo pegamos. Aquí también, copiamos estos elementos y seleccionamos la sección de column footer para pegarlo allí. Luego eliminamos las secciones que no vamos a usar. Añadimos otra sección de detalle. Ahora copiamos los parámetros que hay en InvoiceDetail y los pegamos en los parámetros de nuestro reporte. Listo, ahora agregamos un nuevo elemento table a details 1. Apretamos next. En Dataset name ponemos un nombre para identificarlo y seleccionamos create an empty dataset. Hacemos doble clic en la tabla. Podemos ver que, al igual que el reporte, tenemos parámetros y campos. Eliminamos las líneas que no vamos a usar, dejando solo el header y el detail. Luego ajustamos el tamaño de la tabla para que ocupe todo el ancho del reporte. Y agregamos la cantidad de columnas que necesitemos. Por último, agregamos los fields copiándolos directamente de InvoiceDetail. Agregamos los fields en el orden que queremos mostrar. Y ajustamos el ancho de cada columna. Luego arrastramos static text a la línea header para nombrar cada columna. Listo.\
Creamos otra tabla para mostrar la colección de órdenes asociadas a la factura.\
Seleccionamos create a table using a new dataset. Lo nombramos orders y también seleccionamos create an empty dataset. Eliminamos las líneas que no nos interesan, agregamos fields y ajustamos la tabla. Volvemos al reporte y creamos los parámetros para recibir ambas colecciones. Lo hemos enviado como JRBeanCollectionDataSource, entonces lo recibiremos de esa manera también. Por último, le asignaremos a cada tabla usar el parámetro como data source. Guardamos. Copiamos el reporte.\
\
Pegamos el reporte e iniciamos la aplicación. Hemos logrado mostrar ambas colecciones, quizá podamos mejorar un poco estéticamente, por ejemplo, separar un poco ambas colecciones y mostrar el column footer apenas terminen las líneas de detalle. Expandimos un poco las secciones de detalle para separar ambas colecciones. Hacemos clic fuera del reporte, luego seleccionamos float column footer. Guardamos, copiamos el reporte y reiniciamos la aplicación. Listo.\
\
Hay muchas formas de mostrar varias colecciones diferentes en un informe; hemos visto una de ellas, que es enviando una JRDataSource por cada colección como un parámetro.

**8- CÓMO GENERAR INFORMES INCLUYENDO CONSULTAS SQL DENTRO DE ÉL, APROVECHANDO PARÁMETROS ENVIADOS DESDE NUESTRA APLICACIÓN OPENXAVA.**

**Código de Ejemplo:**

En el archivo *PrintCustomerInvoicesAction.java*:

**public** **class** PrintCustomerInvoicesAction **extends** JasperReportBaseAction {



`	`@Override

`	`**protected** JRDataSource getDataSource() **throws** Exception {

`		`**return** **null**;

`	`}

`	`@Override

`	`**protected** String getJRXML() **throws** Exception {

`		`**return** **"CustomerInvoices.jrxml"**;

`	`}

`	`@Override

`	`**protected** Map getParameters() **throws** Exception {

`		`**Messages** errors = MapFacade.validate(**"Customer"**, getView().getValues());

`		`**if** (errors.contains()) **throw** **new** ValidationException(errors);

`		`**Map** parameters = **new** HashMap();			

`		`parameters.put(**"number"**, getView().getValueInt(**"number"**));

`		`parameters.put(**"name"**, getView().getValue(**"name"**));

`		`**return** parameters;

`	`}

}

Copy

En el archivo *controllers.xml*:

<**controller** name=**"Customer"**>

`	`<**extends** controller=**"Invoicing"**/>

`	`<**action** name=**"printCustomerInvoices"**

`		`class=**"com.yourcompany.invoicing.actions.PrintCustomerInvoicesAction"**

`		`mode=**"detail"**

`		`icon=**"printer"**/>

</**controller**>

Copy

**Transcripción**

Hola, soy Mónica. En esta lección aprenderás a diseñar reportes para trabajar directamente con la base de datos, creando tu propia consulta SQL en el reporte mismo.\
\
Primero creamos un reporte nuevo llamado customer invoices. Luego vamos al panel repository explorer. Clic derecho en data adapter y creamos uno nuevo. Elegimos la conexión por JDBC y le damos un nombre para diferenciarlo de otros: Invoicing. En JDBC driver, para nuestro caso seleccionamos la opción de HSQLDB server. Pero puedes trabajar con cualquier otra base de datos, como MySQL, Oracle, PostgreSQL, entre otros. En cuanto al URL, usuario y contraseña podemos encontrarlo en el archivo context.xml ubicado en la carpeta src/main/webapp/META-INF. Por defecto, en HSQLDB no se diferencia entre mayúsculas y minúsculas para el usuario. Recuerda iniciar el proyecto para que inicie la base de datos HSQLDB. Ingresamos el URL y usuario que vimos en database manager y hacemos clic en "Test" para probar la conexión. Perfecto. Ahora creamos unos parámetros que se usarán en el reporte: number y name. Luego hacemos clic en el icono para abrir la ventana de dataset and query dialog. Aquí seleccionamos el data adapter que creamos recién. Podemos ver todas las tablas de la aplicación. En la derecha podemos ingresar nuestra consulta. Usaremos el parámetro number en la consulta, que básicamente busca todas las facturas de ese cliente y muestra información de esas facturas. Luego hacemos clic en "read fields" para que se lean los campos; esto a la vez verifica que la consulta esté bien escrita. Una vez leídos los campos, aquí abajo se cargan los nombres y tipos. En caso de que sea necesario, podemos seleccionar alguno y editarlo. Estos campos se cargan automáticamente en fields del reporte. Vamos a probar la consulta. Cambiamos el parámetro por el número 1 y vamos a data preview. Hacemos clic en "refresh preview data" y nos mostrará los resultados de la consulta. Perfecto. Volvemos a poner el parámetro en su lugar y hacemos clic en "OK". Aquí vemos que efectivamente están todos los fields de la consulta. Vamos a agregarlos al reporte. Otra manera de probar el reporte es en la vista de preview. Ingresamos los parámetros. En nuestro caso, el número debe ser real, ya que se usa para buscar en la base de datos. Al parecer, se cargan bien los datos. Vamos a ordenar un poco la vista y cambiar el formato de fecha. En el pattern expression de la fecha ingresamos el formato que queremos. Probamos de nuevo. Perfecto, así lo deberíamos visualizar en la aplicación.\
\
Vamos a crear la acción para generar el reporte. Como siempre, primero creamos el controlador si es necesario.\
Luego creamos la acción que extienda de JasperReportBaseAction. En dataSource podemos enviar null, porque el reporte ya tiene su dataset. En parámetros vamos a enviar number y name, tomando los datos desde la vista. En getView().getValue() debemos usar como parámetro el nombre de las propiedades para obtener su valor desde la vista donde se está ejecutando la acción. Copiamos el reporte e iniciamos la aplicación. Perfecto. Vamos al módulo de invoice para ver si se han traído los registros correctamente. Sí. Los datos del monto total están bien.\
\
Hay muchas formas de trabajar con la base de datos. No necesariamente se debe hacer desde el reporte; también puedes hacer una consulta JPA en la acción de OpenXava y luego enviar los resultados al reporte para trabajarlos allí.





**9- CÓMO CREAR UN DIÁLOGO DONDE EL USUARIO PODRÁ INTRODUCIR UN RANGO DE FECHA, LAS MISMAS SE USARÁN EN LA CONSULTA SQL DEL REPORTE.**

**Código de Ejemplo:**

En el archivo *CustomDateRange.java*:

@Getter @Setter

**public** **class** CustomDateRange {

`	`**int** customerNumber;



`	`LocalDate startDate;



`	`LocalDate endDate;



}

Copy

En el archivo *ShowCustomDateRangeDialogAction.java*:

**public** **class** ShowCustomDateRangeDialogAction **extends** ViewBaseAction {

`	`@Override

`	`**public** **void** execute() **throws** Exception {

`		`**int** number = getView().getValueInt(**"number"**);

`		`showDialog();

`		`getView().setModelName(**"CustomDateRange"**);

`		`getView().setValue(**"customerNumber"**, number);

`		`addActions(**"CustomDateRange.print"**);

`	`}

}

Copy

En el archivo *PrintCustomerInvoicesByDateRangeAction.java*:

**public** **class** PrintCustomerInvoicesByDateRangeAction **extends** JasperReportBaseAction {

`	`@Override

`	`**protected** JRDataSource getDataSource() **throws** Exception {

`		`**return** **null**;

`	`}

`	`@Override

`	`**protected** String getJRXML() **throws** Exception {

`		`**return** **"CustomerInvoicesByDateRange.jrxml"**;

`	`}

`	`@Override

`	`**protected** Map getParameters() **throws** Exception {

`		`**Messages** errors = MapFacade.validate(**"CustomDateRange"**, getView().getValues());

`		`**if** (errors.contains()) **throw** **new** ValidationException(errors);

`		`**Map** parameters = **new** HashMap();

`		`parameters.put(**"name"**, getPreviousView().getValue(**"name"**));

`		`parameters.put(**"number"**, getView().getValueInt(**"customerNumber"**));

`		`parameters.put(**"startDate"**, getView().getValue(**"startDate"**).toString());

`		`parameters.put(**"endDate"**, getView().getValue(**"endDate"**).toString());

`		`**return** parameters;

`	`}

}

Copy

En el archivo *controllers.xml*:

<**controller** name=**"Customer"**>

...

`	`<**action** name=**"printInvoicesByDateRange"**

`		`class=**"com.yourcompany.invoicing.actions.ShowCustomDateRangeDialogAction"**

`		`mode=**"detail"**

`		`icon=**"printer"**/>

</**controller**>



<**controller** name=**"CustomDateRange"**>

`	`<**extends** controller=**"Invoicing"**/>

`	`<**action** name=**"print"**

`		`class=**"com.yourcompany.invoicing.actions.PrintCustomerInvoicesByDateRangeAction"**

`		`mode=**"detail"**

`		`icon=**"printer"**/>

</**controller**>

Copy

**Transcripción**

Hola, soy Mónica. En esta lección aprenderás a visualizar un diálogo donde el usuario podrá introducir un rango de fechas, que se usarán para filtrar los datos que aparecerán en el reporte.\
\
Primero creamos la clase transitoria Custom Date Range, con la propiedad customerNumber para dejar el valor del número de cliente. Como usamos LocalDate en fecha, usaremos el mismo tipo de dato para el inicio y fin de fecha. Listo. Vamos a crear la acción para sacar el diálogo. Esta acción va a extender de ViewBaseAction, así podemos trabajar con la vista. Con showDialog mostraremos un diálogo vacío. Al mostrar el diálogo, el método getView pasará a apuntar al diálogo. Le diremos que el nombre del modelo del diálogo es CustomDateRange, este es igual al nombre de la clase transitoria. Vamos a probarlo. Perfecto. Lo ideal es que cargue automáticamente el número del cliente, también se debe agregar una acción para imprimir el reporte. Primero vamos a obtener el valor del número de cliente. Y debemos hacerlo antes de showDialog, así la vista que devuelve getView, es la vista de detalles del cliente. Luego setteamos el valor. Por último, con addActions vamos a añadir la acción de imprimir. Esta acción lo vamos a crear en un controlador aparte. El nombre del controlador puede ser cualquiera, en mi caso voy a usar el nombre de la clase transitoria. Antes de seguir con la acción del reporte, agregamos la acción al diálogo. Usamos el nombre del controlador y el nombre de la acción para agregarlo, esto significa que podemos agregar cualquier acción de cualquier controlador. Similar al reporte de la lección pasada, enviamos un dataSource nulo y en parámetros enviamos los valores obteniéndolos de la vista. Iniciamos el proyecto para tener disponible la base de datos.\
\
Creamos un nuevo reporte llamado Customer Invoice By Date Range. Definimos los parámetros a recibir. Seleccionamos la base de datos Invoicing que creamos en la lección pasada. También tipeamos un query similar, pero agregando una línea donde indica que la fecha de la factura debe estar entre startDate y endDate. Perfecto. Agregamos los elementos al reporte. Y lo probamos. El formato de fecha que obtenemos de la vista es como lo estoy ingresando. Funciona bien. Agregamos otro parámetro para recibir el nombre del cliente. En la acción del reporte, ya estamos en la vista del diálogo, por lo que si queremos obtener el nombre del cliente debemos hacer getPreviousView. Listo, luego de hacer unos cambios en el reporte, lo copiamos al proyecto e iniciamos la aplicación. Voy a cambiar la fecha de una factura para que sea distinta a las otras. Nos dice un error de expresión, se debe referir a los parámetros. Debe ser que estamos recibiéndolo como String, pero lo estamos enviando como Object y si te acuerdas, ellos son de tipo LocalDate. Listo, probamos de nuevo. Perfecto, ahí nos está mostrando las facturas entre las fechas que seleccionamos.\
\
Con esto que el usuario pueda personalizar parámetros para el reporte, puedes aprovechar y crear reportes más complejos, sin necesidad de agregar mucho código.

**10- CÓMO CREAR UN REPORTE CON DATOS AGRUPADOS, SE MOSTRARÁ UN RESUMEN ANUAL DE CLIENTES, AGRUPADOS POR ESTADO Y CIUDAD.**

**Código de Ejemplo**

En el archivo *controllers.xml*:

<**controller** name=**"Customer"**>

`	`<**action** name=**"printAnnualSummary"**

`		`class=**"com.yourcompany.invoicing.actions.PrintAnnualSummaryAction"**

`		`mode=**"list"**

`		`icon=**"printer"**/>

</**controller**>

Copy

En el archivo *PrintAnnualSummaryAction.java*:

**public** **class** PrintAnnualSummaryAction **extends** JasperReportBaseAction {

`	`@Override

`	`**protected** JRDataSource getDataSource() **throws** Exception {

`		`**return** **null**;

`	`}

`	`@Override

`	`**protected** String getJRXML() **throws** Exception {

`		`**return** **"AnnualBillingSummary.jrxml"**;

`	`}

`	`@Override

`	`**protected** Map getParameters() **throws** Exception {

`		`**return** **null**;

`	`}

}

Copy

**Transcripción**

Hola, soy Mónica. En esta lección aprenderás a hacer un resumen anual de clientes, los cuales estarán agrupados alfabéticamente por estado y ciudad con una sumatoria por cada grupo.\
\
Primero creamos un nuevo reporte llamado "annual billing summary". En Dataset tipeamos el query para obtener una lista de clientes con la cantidad de facturas y su monto facturado en este año. Nuestro objetivo es mostrar un resumen del año actual, pero como las facturas de esta aplicación ejemplo llegan hasta 2024, haremos que el reporte muestre únicamente las de 2024. Probamos a ver si el query está bien hecho. Perfecto. En el panel outline, hacemos clic derecho en el reporte y creamos un grupo. Le damos el nombre STATE y seleccionamos STATE también. No hace falta que el nombre sea igual a lo seleccionado. Hacemos lo mismo con CITY. Vemos que al crear los grupos, se han agregado headers y footers para cada uno. Arrastramos los fields al reporte. En los headers no nos hace falta hacer cálculos, así que lo dejamos así como está. Arrastramos TOTAL AMOUNT al footer de city y seleccionamos SUM. Hay otros cálculos disponibles, pero no nos interesa. Esto nos hará la sumatoria de TOTAL AMOUNT en cada grupo de CITY. Hacemos lo mismo en STATE. Ordenamos un poco el reporte y probamos. Los grupos se muestran correctamente, las sumatorias también. Aquí al parecer la página se termina y corta los datos que se están mostrando. Podemos hacer que la información del grupo se muestre junta. En Group header de STATE tildamos en "keep together". Esto hará que si el grupo de STATE a mostrar no entra en el espacio que sobra de la página, pasará a la siguiente página. Obviamente, si el grupo es muy grande, tendrá que separarlo igual. Hemos hecho que el grupo de STATE no se separe, pero puede ocurrir que justo la información del grupo CITY se separe también. Lo podemos arreglar de la misma manera. Si vemos la sumatoria de total amount, es un elemento variable. Podemos crear una variable de la siguiente forma. Clic derecho en variable y crear una. Le damos un nombre, "number of customers". Este va a ser un Integer. En calculation seleccionamos count y en expression ponemos el field de name. Por último, en reset type seleccionamos STATE. Esta variable contará la cantidad de NAME en cada grupo de STATE. Terminamos de ordenar el reporte y lo probamos. Para esta ocasión, he hecho unos cambios en la base de datos, agregando y modificando clientes y facturas. Copiamos el reporte al proyecto.

En el archivo controllers.xml vamos al controlador Customer y definimos una acción "print annual summary" para el modo lista. Luego en el paquete actions creamos la acción "Print annual summary action". Extendemos de Jasper Report Base Action. Dejamos data source y parámetros como null, solo ponemos el nombre del reporte. Listo. Iniciamos la aplicación. Perfecto.\
\
Hemos podido visualizar todos los clientes que tuvimos en 2024, sus facturaciones y lo más importante, agrupados en estado y ciudad.

