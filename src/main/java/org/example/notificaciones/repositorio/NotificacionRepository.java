package org.example.notificaciones.repositorio;


import org.example.notificaciones.modelo.Notificacion;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface NotificacionRepository extends ReactiveMongoRepository<Notificacion, String> {

    /**
     * Encuentra notificaciones por usuario, ordenadas por fecha descendente.
     * @param usuario El nombre del usuario.
     * @return Flux<Notificacion> con todas las notificaciones del usuario.
     */
    Flux<Notificacion> findByUsuarioOrderByFechaDesc(String usuario);

    /**
     * Encuentra notificaciones por usuario y tipo, ordenadas por fecha descendente.
     * @param usuario El nombre del usuario.
     * @param tipo El tipo de notificación (INFO, ALERTA, URGENTE).
     * @return Flux<Notificacion> filtrado.
     */
    Flux<Notificacion> findByUsuarioAndTipoOrderByFechaDesc(String usuario, String tipo);
}
