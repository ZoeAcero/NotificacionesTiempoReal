package org.example.notificaciones.servicio;

import org.example.notificaciones.modelo.Notificacion;
import org.example.notificaciones.repositorio.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository repository;

    // Inyecta el mock del repositorio en el servicio
    @InjectMocks
    private NotificacionService service;

    private final String USER = "usuarioTest";
    private Notificacion testNotif;

    @BeforeEach
    void setUp() {
        testNotif = new Notificacion(USER, "Mensaje de prueba", "INFO");
        testNotif.setId("123");
    }

    @Test
    void testAddNotificacionGuardaYEmite() {
        // Simular que el repositorio devuelve el objeto guardado
        when(repository.save(any(Notificacion.class))).thenReturn(Mono.just(testNotif));

        // 1. Probar el guardado
        Mono<Notificacion> result = service.addNotificacion(new Notificacion());

        StepVerifier.create(result)
                .expectNext(testNotif)
                .verifyComplete();

        // 2. Verificar que se llamó a repository.save
        verify(repository, times(1)).save(any(Notificacion.class));

        // 3. Verificar la emisión al Sink (SSE)
        // El flujo en tiempo real debe recoger la notificación recién emitida.
        StepVerifier.create(service.getNotificacionesEnTiempoReal(USER).take(1))
                .expectNext(testNotif)
                .verifyComplete();
    }

    @Test
    void testMarcarLeidoActualizaYEmite() {
        when(repository.findById("123")).thenReturn(Mono.just(testNotif));
        when(repository.save(any(Notificacion.class))).thenReturn(Mono.just(testNotif));

        Mono<Notificacion> result = service.marcarLeido("123");

        StepVerifier.create(result)
                .expectNextMatches(n -> n.isLeido()) // Verificar que el campo 'leido' es true
                .verifyComplete();
    }

    @Test
    void testEliminarNotificacionLlamaAlDelete() {
        when(repository.deleteById(anyString())).thenReturn(Mono.empty());

        Mono<Void> result = service.eliminarNotificacion("123");

        StepVerifier.create(result).verifyComplete();

        verify(repository, times(1)).deleteById("123");
    }
}
