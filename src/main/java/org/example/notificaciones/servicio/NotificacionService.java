package org.example.notificaciones.servicio;


import org.example.notificaciones.modelo.Notificacion;
import org.example.notificaciones.repositorio.NotificacionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Date;
import java.util.Random;

@Service
public class NotificacionService {

    private final NotificacionRepository repository;
    private final Sinks.Many<Notificacion> sink;
    private final Random random = new Random();

    public NotificacionService(NotificacionRepository repository) {
        this.repository = repository;
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    // ==========================================================
    // MÉTODO AÑADIDO: Generación Automática de Notificaciones
    // ==========================================================

    @PostConstruct
    public void iniciarGeneracionAutomatica() {
        Flux.interval(Duration.ofSeconds(5)) // Genera un evento cada 5 segundos
                .map(tick -> generarNotificacionAleatoria("usuario1"))
                .flatMap(this::addNotificacionYEmitir)
                .subscribe();

        System.out.println("🤖 Generación automática de notificaciones iniciada cada 5 segundos.");
    }

    private Notificacion generarNotificacionAleatoria(String usuario) {
        String[] mensajes = {"Pedido Recibido", "Sistema Desconectado", "Oferta Flash", "Actualización de Seguridad"};
        String[] tipos = {"INFO", "ALERTA", "URGENTE"};

        int indexMensaje = random.nextInt(mensajes.length);
        int indexTipo = random.nextInt(tipos.length);

        Notificacion n = new Notificacion(
                usuario,
                mensajes[indexMensaje] + " (" + new Date().getTime() % 10000 + ")",
                tipos[indexTipo]
        );
        n.setLeido(false);
        return n;
    }

    private Mono<Notificacion> addNotificacionYEmitir(Notificacion n) {
        return repository.save(n)
                .doOnSuccess(savedNotif -> {
                    // Emitir la notificación al Sink para SSE
                    sink.tryEmitNext(savedNotif);
                });
    }

    // ==========================================================
    // MÉTODOS DE NEGOCIO Y SSE
    // ==========================================================

    /**
     * Retorna el flujo SSE: Histórico + Flujo continuo (Sink).
     */
    public Flux<Notificacion> getNotificacionesEnTiempoReal(String usuario) {
        Flux<Notificacion> historico = repository.findByUsuarioOrderByFechaDesc(usuario);
        Flux<Notificacion> nuevas = sink.asFlux()
                .filter(n -> n.getUsuario().equals(usuario));
        return historico.concatWith(nuevas);
    }

    /**
     * Se usa para peticiones manuales o automáticas.
     */
    public Mono<Notificacion> addNotificacion(Notificacion n) {
        return addNotificacionYEmitir(n);
    }

    public Mono<Notificacion> marcarLeido(String id) {
        return repository.findById(id)
                .flatMap(notif -> {
                    notif.setLeido(true);
                    return repository.save(notif);
                })
                .doOnSuccess(savedNotif -> {
                    sink.tryEmitNext(savedNotif);
                });
    }

    public Flux<Notificacion> filtrarPorTipo(String usuario, String tipo) {
        if ("TODOS".equals(tipo)) {
            return repository.findByUsuarioOrderByFechaDesc(usuario);
        }
        return repository.findByUsuarioAndTipoOrderByFechaDesc(usuario, tipo);
    }

    public Mono<Void> eliminarNotificacion(String id) {
        return repository.deleteById(id);
    }
}
