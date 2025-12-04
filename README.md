https://github.com/ZoeAcero/NotificacionesTiempoReal.git




Proyecto: Notificaciones Reactivas en Tiempo Real (Spring WebFlux + SSE)


Este proyecto implementa un sistema de notificaciones web en tiempo real utilizando el paradigma de programación reactiva de Spring Boot, Server-Sent Events (SSE) para el push continuo de datos, y MongoDB para la persistencia.




###1. Fundamento Teórico: Patrones de Diseño Reactivos


La programación reactiva se centra en la gestión de flujos de datos asíncronos mediante el uso de operadores y el procesamiento no-bloqueante. El diseño de este proyecto se basa en los patrones fundamentales de la especificación Reactive Streams (adoptada por Project Reactor): Publisher y Subscriber.


#1.1. Principales Patrones UtilizadosPatrónDescripciónImplementación en el CódigoPublisher (Editor)Representa la fuente de datos que puede emitir eventos (datos, errores o una señal de finalización) a uno o más Subscribers.En Spring WebFlux, el Flux<T> (cero a N elementos) y el Mono<T> (cero o un elemento) son los Publishers. Nuestro NotificacionService retorna Flux<Notificacion>.Subscriber (Suscriptor)Es la parte que consume los datos emitidos por el Publisher.El WebTestClient (en los tests) o, en el navegador, el objeto EventSource de JavaScript, actúan como Subscribers que esperan datos.Backpressure (Contrapresión)Es un mecanismo para que el Subscriber le indique al Publisher cuánto puede enviar, evitando que el Publisher desborde al Subscriber con demasiados datos.En nuestro NotificacionService, usamos Sinks.many().multicast().onBackpressureBuffer(). El onBackpressureBuffer define cómo manejar la situación si el suscriptor es lento.Server-Sent Events (SSE)Estándar web (HTML5) que permite al servidor enviar datos continuamente al cliente a través de una conexión HTTP abierta y duradera.El NotificacionController usa produces = MediaType.TEXT_EVENT_STREAM_VALUE para decirle al navegador que el Flux debe tratarse como un flujo de eventos persistente, no como una respuesta única.





#1.2. Implementación de Flujos (Mono y Flux)


En el código, la programación reactiva se implementa mediante la composición de flujos:



Mono<Notificacion> addNotificacion(...): Define una operación que resultará en un solo objeto (la notificación guardada).



Flux<Notificacion> getNotificacionesEnTiempoReal(...): Este método combina dos flujos esenciales:



Flujo Histórico: repository.findByUsuarioOrderByFechaDesc(usuario) (finito, datos antiguos).



Flujo Continuo: sink.asFlux().filter(...) (infinito, nuevos datos), gestionado por el Sinks.Many.



Composición: Usamos operadores como .concatWith() (para unir el flujo histórico con el flujo continuo) y .doOnSuccess() (para ejecutar una acción —la emisión al Sink— después de que una operación asíncrona, como repository.save(), haya finalizado con éxito).





###2. Explicación Detallada del Proyecto


#2.1. Capa de Persistencia (MongoDB Reactivo)


Notificacion.java (Modelo): Entidad POJO marcada con @Document para mapear los objetos a la colección notificaciones de MongoDB.


NotificacionRepository.java (Repositorio): Interfaz que extiende ReactiveMongoRepository, proporcionando los métodos reactivos (Mono/Flux) de acceso a datos. Spring Data genera automáticamente las consultas como findByUsuarioOrderByFechaDesc.



#2.2. Capa de Servicio (NotificacionService.java)


Esta capa es el motor de la lógica de negocio y la fuente de la programación en tiempo real:



Sinks.Many<Notificacion> sink: Este es el punto de emisión central. Todas las notificaciones nuevas o actualizadas (como marcar leída) se inyectan en este sink llamando a sink.tryEmitNext(notificacion).



Generación Automática (@PostConstruct): Para la demostración, el método iniciarGeneracionAutomatica() utiliza Flux.interval(Duration.ofSeconds(5)) para crear un timer reactivo. Cada 5 segundos, se genera una notificación aleatoria, se guarda en MongoDB, y se emite al sink.



Conexión SSE: El método getNotificacionesEnTiempoReal() fusiona el flujo de notificaciones antiguas (de la BD) con las nuevas (del sink) usando .concatWith() para proveer un flujo ininterrumpido al controlador.




#2.3. Capa de Controlador (NotificacionController.java)


Vista: El endpoint /notificaciones/{usuario} carga la plantilla Thymeleaf (notificaciones.html).


Endpoint SSE (/notificaciones/stream/{usuario}): Este método es la interfaz HTTP del patrón Publisher. Al retornar un Flux<Notificacion> y usar produces = MediaType.TEXT_EVENT_STREAM_VALUE, Spring WebFlux gestiona la conexión y la mantiene abierta, transmitiendo cada evento que recibe del servicio.





#2.4. Vista (notificaciones.html y JavaScript)


La vista es la consumidora final del flujo SSE:


new EventSource(...): El código JavaScript utiliza el objeto nativo del navegador EventSource para abrir una conexión GET al endpoint SSE.


eventSource.onmessage = function(event) {...}: Este callback se ejecuta cada vez que el servidor envía un evento.


Actualización del DOM: La función actualizarNotificacion() procesa el JSON recibido y utiliza JavaScript DOM (tablaBody.insertRow(0)) para insertar o actualizar la fila en la tabla, logrando el efecto de tiempo real.
