https://github.com/ZoeAcero/NotificacionesTiempoReal.git



🔔 Proyecto: Notificaciones Reactivas en Tiempo Real (Spring WebFlux + SSE)
Este proyecto implementa un sistema de notificaciones web en tiempo real utilizando el paradigma de programación reactiva de Spring Boot, Server-Sent Events (SSE) para el push continuo de datos, y MongoDB para la persistencia.

1. 💡 Fundamento Teórico: Patrones de Diseño Reactivos
La programación reactiva se centra en la gestión de flujos de datos asíncronos mediante el uso de operadores y la no-bloqueante del procesamiento.

1.1. Principales Patrones
El diseño de este proyecto se basa en dos patrones fundamentales de la especificación Reactive Streams (adoptada por Project Reactor): Publisher y Subscriber .

Patrón	Descripción	Implementación en el Código
Publisher (Editor)	Representa la fuente de datos que puede emitir eventos (datos, errores o una señal de finalización) a uno o más Subscribers.	En Spring WebFlux, el Flux<T> (cero a N elementos) y el Mono<T> (cero o un elemento) son los Publishers. Nuestro NotificacionService retorna Flux<Notificacion>.
Subscriber (Suscriptor)	Es la parte que consume los datos emitidos por el Publisher.	El WebTestClient (en los tests) o, en el navegador, el objeto EventSource de JavaScript, actúan como Subscribers que esperan datos.
Backpressure (Contrapresión)	Es un mecanismo para que el Subscriber le indique al Publisher cuánto puede enviar, evitando que el Publisher desborde al Subscriber con demasiados datos.	En nuestro NotificacionService, usamos Sinks.many().multicast().onBackpressureBuffer(). El onBackpressureBuffer define cómo manejar la situación si el suscriptor es lento.
Server-Sent Events (SSE)	No es un patrón de Reactor, sino un estándar web (HTML5) que permite al servidor enviar datos continuamente al cliente a través de una conexión HTTP abierta y duradera.	El NotificacionController usa produces = MediaType.TEXT_EVENT_STREAM_VALUE para decirle al navegador que el Flux debe tratarse como un flujo de eventos persistente, no como una respuesta única.

1.2. Implementación de Flujos (Mono y Flux)
En el código, la programación reactiva se implementa mediante la composición de estos flujos:

Mono<Notificacion> addNotificacion(...): Define una operación que resultará en un solo objeto (la notificación guardada).

Flux<Notificacion> getNotificacionesEnTiempoReal(...): Combina dos flujos:

Flujo Histórico: repository.findByUsuarioOrderByFechaDesc(usuario) (finito).

Flujo Continuo: sink.asFlux().filter(...) (infinito, gestionado por el Sinks.Many).

Composición: Usamos operadores como .concatWith() (para unir el flujo histórico con el flujo continuo) y .doOnSuccess() (para ejecutar una acción —la emisión al Sink— después de que una operación asíncrona, como repository.save(), haya finalizado con éxito).

2. 🏗️ Explicación del Proyecto
2.1. Capa de Persistencia (MongoDB Reactivo)
Notificacion.java (Modelo): Entidad POJO marcada con @Document para mapear los objetos a la colección notificaciones de MongoDB.

NotificacionRepository.java (Repositorio): Interfaz que extiende ReactiveMongoRepository, lo que le permite retornar Mono y Flux en lugar de colecciones estándar (List, Optional). Spring Data genera automáticamente las consultas como findByUsuarioOrderByFechaDesc.

2.2. Capa de Servicio (NotificacionService.java)
Esta es la capa central que implementa la lógica de tiempo real:

Sinks.Many<Notificacion> sink: Este es el punto de emisión interno. Cada vez que se crea o actualiza una notificación, el servicio llama a sink.tryEmitNext(notificacion), inyectando el objeto en el flujo continuo.

@PostConstruct iniciarGeneracionAutomatica(): Utiliza Flux.interval(Duration.ofSeconds(5)) para crear un flujo infinito que actúa como simulador de eventos externos. Cada 5 segundos, llama al repositorio para guardar una nueva notificación y la emite al sink.

getNotificacionesEnTiempoReal(): Combina las notificaciones antiguas (de la BD) con las nuevas (del sink) usando .concatWith() para formar el flujo continuo de SSE.

2.3. Capa de Controlador (NotificacionController.java)
Vista: El endpoint /notificaciones/{usuario} carga la plantilla Thymeleaf (notificaciones.html).

Endpoint SSE: El endpoint /notificaciones/stream/{usuario} es el punto de conexión en tiempo real. Está anotado con:

Java

@GetMapping(..., produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<Notificacion> streamNotificaciones(...) 
Al retornar un Flux, Spring WebFlux gestiona la conexión HTTP y la mantiene abierta indefinidamente, enviando cada evento (Notificacion) que el servicio emite al sink.

2.4. Vista (notificaciones.html y JavaScript)
La vista es la consumidora final del flujo SSE:

new EventSource(...): El JavaScript utiliza el objeto nativo del navegador EventSource para abrir una conexión GET al endpoint SSE (http://localhost:8085/notificaciones/stream/usuario1).

eventSource.onmessage = function(event) {...}: Este callback se ejecuta cada vez que el servidor envía un evento (es decir, cada vez que se emite una nueva o se marca una existente como leída).

Actualización del DOM: La función actualizarNotificacion() toma el objeto JSON recibido, lo parsea, y lo inserta o actualiza la fila correspondiente en la tabla HTML, logrando el efecto de tiempo real sin recarga.
