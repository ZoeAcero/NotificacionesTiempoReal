package org.example.notificaciones.repositorio;


import org.example.notificaciones.modelo.Notificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Date;

@DataMongoTest
public class NotificacionRepositoryTest {

    @Autowired
    private NotificacionRepository repository;

    private final String TEST_USER = "usuarioTest";

    @BeforeEach
    void setup() {
        // Limpiar y poblar la base de datos antes de cada test
        repository.deleteAll().block();

        Notificacion n1 = new Notificacion(TEST_USER, "Info antigua", "INFO");
        n1.setFecha(new Date(System.currentTimeMillis() - 100000)); // Hace 100 segundos

        Notificacion n2 = new Notificacion(TEST_USER, "Alerta nueva", "ALERTA");
        n2.setFecha(new Date(System.currentTimeMillis())); // Ahora

        Notificacion n3 = new Notificacion("otroUsuario", "Mensaje ajeno", "INFO");

        repository.saveAll(Flux.just(n1, n2, n3)).blockLast();
    }

    @Test
    void testFindByUsuarioOrderByFechaDesc() {
        Flux<Notificacion> notificaciones = repository.findByUsuarioOrderByFechaDesc(TEST_USER);

        // Verifica que la notificación más nueva (Alerta) aparezca primero
        StepVerifier.create(notificaciones)
                .expectNextMatches(n -> n.getMensaje().contains("Alerta nueva"))
                .expectNextMatches(n -> n.getMensaje().contains("Info antigua"))
                .verifyComplete();
    }

    @Test
    void testFindByUsuarioAndTipo() {
        Flux<Notificacion> notificaciones = repository.findByUsuarioAndTipoOrderByFechaDesc(TEST_USER, "INFO");

        // Verifica que solo aparezcan las de tipo INFO
        StepVerifier.create(notificaciones)
                .expectNextMatches(n -> n.getMensaje().contains("Info antigua"))
                .verifyComplete();
    }
}