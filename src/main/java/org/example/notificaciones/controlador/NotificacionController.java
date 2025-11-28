package org.example.notificaciones.controlador;


import org.example.notificaciones.modelo.Notificacion;
import org.example.notificaciones.servicio.NotificacionService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    // --- Vista Thymeleaf ---
    @GetMapping("/notificaciones/{usuario}")
    public Mono<String> mostrarVistaNotificaciones(@PathVariable String usuario, Model model) {
        model.addAttribute("usuario", usuario);
        return Mono.just("notificaciones");
    }

    // --- Endpoint SSE ---
    @GetMapping(value = "/notificaciones/stream/{usuario}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<Notificacion> streamNotificaciones(@PathVariable String usuario) {
        return service.getNotificacionesEnTiempoReal(usuario);
    }

    // --- Endpoints CRUD y Filtrado (API) ---

    // Crear una notificación (manual, opcional ahora)
    @PostMapping("/api/notificaciones")
    @ResponseBody
    public Mono<Notificacion> crearNotificacion(@RequestBody Notificacion notificacion) {
        return service.addNotificacion(notificacion);
    }

    // Marcar como leída
    @PostMapping("/api/notificaciones/leida/{id}")
    @ResponseBody
    public Mono<Notificacion> marcarLeida(@PathVariable String id) {
        return service.marcarLeido(id);
    }

    // Eliminar
    @DeleteMapping("/api/notificaciones/{id}")
    @ResponseBody
    public Mono<Void> eliminarNotificacion(@PathVariable String id) {
        return service.eliminarNotificacion(id);
    }

    // Filtrar
    @GetMapping("/api/notificaciones/{usuario}/tipo/{tipo}")
    @ResponseBody
    public Flux<Notificacion> filtrarPorTipo(@PathVariable String usuario, @PathVariable String tipo) {
        return service.filtrarPorTipo(usuario, tipo);
    }
}
