package com.duoc.sumativa2.controller;

import com.duoc.sumativa2.model.Guia;
import com.duoc.sumativa2.service.GuiaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guias")
public class GuiaController {

    private final GuiaService guiaService;

    public GuiaController(GuiaService guiaService) {
        this.guiaService = guiaService;
    }


    // POST /api/guias
    // Crear una nueva guía de despacho

    @PostMapping
    public ResponseEntity<Guia> crearGuia(@RequestBody Map<String, Object> body) {
        try {
            String transportista   = (String) body.get("transportista");
            String destinatario    = (String) body.get("destinatario");

            if (transportista == null || destinatario == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            Guia guia = guiaService.crearGuia(
                transportista, destinatario
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(guia);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // POST /api/guias/{id}/subir
    // Subir guía existente desde EFS a S3

    @PostMapping("/{id}/subir")
    public ResponseEntity<Guia> subirGuiaAS3(@PathVariable String id) {
        try {
            Guia guia = guiaService.subirGuiaAS3(id);
            return ResponseEntity.ok(guia);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    


    // GET /api/guias/{id}/descargar
    // Descargar guía con validación de permisos

    @GetMapping("/{id}/descargar")
    public ResponseEntity<byte[]> descargarGuia(@PathVariable String id) {
        try {
            byte[] contenido = guiaService.descargarGuia(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "guia_" + id + ".pdf");
            headers.setContentLength(contenido.length);

            return ResponseEntity.ok().headers(headers).body(contenido);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }   

 
    // PUT /api/guias/{id}
    // Modificar o actualizar una guía existente

    @PutMapping("/{id}")
    public ResponseEntity<Guia> modificarGuia(@PathVariable String id,
                                               @RequestBody Map<String, Object> body) {
        try {
            String nuevoDestinatario = (String) body.get("destinatario");

            Guia guia = guiaService.modificarGuia(
                id, nuevoDestinatario);
            return ResponseEntity.ok(guia);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

  
    // DELETE /api/guias/{id}
    // Eliminar una guía específica

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGuia(@PathVariable String id) {
        try {
            guiaService.eliminarGuia(id);
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/guias/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Guia> obtenerGuia(@PathVariable String id) {
        try {
            Guia guia = guiaService.obtenerGuiaPorId(id);
            return ResponseEntity.ok(guia);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    
}