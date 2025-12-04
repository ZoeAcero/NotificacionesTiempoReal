package org.example.notificaciones.controlador;


import org.example.notificaciones.modelo.Notificacion;
import org.example.notificaciones.servicio.NotificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(NotificacionController.class)
class NotificacionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private NotificacionService service;

    private final String TEST_USER = "usuarioWeb";
    private final Notificacion testNotif = new Notificacion(TEST_USER, "Mensaje de API", "ALERTA");

    @Test
    void testCrearNotificacionEndpoint() {
        testNotif.setId("api-1");

        when(service.addNotificacion(any(Notificacion.class))).thenReturn(Mono.just(testNotif));

        webTestClient.post().uri("/api/notificaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testNotif)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Notificacion.class)
                .isEqualTo(testNotif);
    }

    @Test
    void testStreamNotificacionesEndpoint_SSE() {
        Notificacion n1 = new Notificacion(TEST_USER, "Stream 1", "INFO");
        Notificacion n2 = new Notificacion(TEST_USER, "Stream 2", "URGENTE");

        // Simular un flujo continuo (SSE)
        when(service.getNotificacionesEnTiempoReal(TEST_USER))
                .thenReturn(Flux.just(n1, n2));

        webTestClient.get().uri("/notificaciones/stream/" + TEST_USER)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                .expectBodyList(Notificacion.class)
                .hasSize(2);
    }

    @Test
    void testMarcarLeidaEndpoint() {
        testNotif.setLeido(true);

        when(service.marcarLeido(anyString())).thenReturn(Mono.just(testNotif));

        webTestClient.post().uri("/api/notificaciones/leida/api-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Notificacion.class);
    }
}
