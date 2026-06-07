package com.duoc.sumativa1.service;

import com.duoc.sumativa1.model.Guia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GuiaService {

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${efs.mount.path}")
    private String efsMountPath;

    private final S3Client s3Client;

    // Simula base de datos en memoria
    private final Map<String, Guia> db = new ConcurrentHashMap<>();

    // Constructor
    public GuiaService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

   
    // CREAR guía: guarda en EFS y luego sube a S3
    // Para POST endpoint 
   
    public Guia crearGuia(String transportista, String destinatario) {

        String id = UUID.randomUUID().toString();
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String nombreArchivo = "guia_" + id + ".pdf";

        // Guardar temporalmente en EFS
        String rutaEFS = efsMountPath + "/" + nombreArchivo;
        String contenidoGuia = buildContenidoGuia(id, transportista, destinatario, fecha);
        try {
            Path pathEFS = Paths.get(rutaEFS);
            Files.createDirectories(pathEFS.getParent());
            Files.write(pathEFS, contenidoGuia.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar guía en EFS: " + e.getMessage(), e);
        }

        // Subir a S3 organizado por fecha y transportista
        String claveS3 = fecha + "/" + transportista + "/" + nombreArchivo;
        try {
            Path pathEFS = Paths.get(rutaEFS);
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(claveS3)
                    .contentType("application/pdf")
                    .build(),
                RequestBody.fromFile(pathEFS)
            );
        } catch (S3Exception e) {
            throw new RuntimeException("Error al subir guía a S3: " + e.getMessage(), e);
        }

        // Construir y guardar objeto Guia
        Guia guia = new Guia(
            id,
            transportista,
            fecha,
            destinatario,
            rutaEFS,
            claveS3,
            Guia.EstadoGuia.SUBIDA_S3,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        db.put(id, guia);
        return guia;
    }


    // SUBIR guía existente a S3
     // Para POST endpoint 

    public Guia subirGuiaAS3(String id) {

        Guia guia = obtenerGuiaPorId(id);

        Path pathEFS = Paths.get(guia.getRutaEFS());
        if (!Files.exists(pathEFS)) {
            throw new RuntimeException("El archivo no existe en EFS: " + guia.getRutaEFS());
        }

        try {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(guia.getRutaS3())
                    .contentType("application/pdf")
                    .build(),
                RequestBody.fromFile(pathEFS)
            );
        } catch (S3Exception e) {
            throw new RuntimeException("Error al subir a S3: " + e.getMessage(), e);
        }

        guia.setEstado(Guia.EstadoGuia.SUBIDA_S3);
        guia.setFechaActualizacion(LocalDateTime.now());
        db.put(id, guia);
        return guia;
    }


    // DESCARGAR guía
    // Para GET endpoint
    
    public byte[] descargarGuia(String id, String rol) {

        if (rol == null || (!rol.equals("ADMIN") && !rol.equals("TRANSPORTISTA"))) {
            throw new RuntimeException("Acceso denegado: rol sin permisos para descargar guías.");
        }

        Guia guia = obtenerGuiaPorId(id);

        try {
            ResponseBytes<GetObjectResponse> objeto = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(guia.getRutaS3())
                    .build()
            );

            guia.setEstado(Guia.EstadoGuia.DESCARGADA);
            guia.setFechaActualizacion(LocalDateTime.now());
            db.put(id, guia);

            return objeto.asByteArray();

        } catch (S3Exception e) {
            throw new RuntimeException("Error al descargar guía desde S3: " + e.getMessage(), e);
        }
    }

    
    // MODIFICAR guía
    // Para PUT endpoint
   
    public Guia modificarGuia(String id, String nuevoDestinatario) {

        Guia guia = obtenerGuiaPorId(id);

        if (nuevoDestinatario != null) guia.setDestinatario(nuevoDestinatario);


        // Regenerar contenido del archivo
        String contenidoActualizado = buildContenidoGuia(
            guia.getId(), guia.getTransportista(), guia.getDestinatario(),
            guia.getFecha()
        );

        // Reescribir en EFS
        try {
            Files.write(Paths.get(guia.getRutaEFS()), contenidoActualizado.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error al actualizar guía en EFS: " + e.getMessage(), e);
        }

        // Reemplazar en S3
        try {
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(guia.getRutaS3())
                    .contentType("application/pdf")
                    .build(),
                RequestBody.fromBytes(contenidoActualizado.getBytes())
            );
        } catch (S3Exception e) {
            throw new RuntimeException("Error al actualizar guía en S3: " + e.getMessage(), e);
        }

        guia.setEstado(Guia.EstadoGuia.MODIFICADA);
        guia.setFechaActualizacion(LocalDateTime.now());
        db.put(id, guia);
        return guia;
    }

    
    // ELIMINAR guía
    // Para DELETE endpoint
   
    public void eliminarGuia(String id) {

        Guia guia = obtenerGuiaPorId(id);

        // Eliminar de S3
        try {
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(guia.getRutaS3())
                    .build()
            );
        } catch (S3Exception e) {
            throw new RuntimeException("Error al eliminar guía de S3: " + e.getMessage(), e);
        }

        // Eliminar archivo local en EFS
        try {
            Files.deleteIfExists(Paths.get(guia.getRutaEFS()));
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar guía de EFS: " + e.getMessage(), e);
        }

        db.remove(id);
    }

    /* 
    // CONSULTAR guías por transportista y fecha
    // Para GET endpoint
 
    public List<Guia> consultarPorTransportistaYFecha(String transportista, String fecha) {

        return db.values().stream()
                .filter(g -> g.getTransportista().equals(transportista)
                          && g.getFecha().equals(fecha))
                .collect(Collectors.toList());
    }
    */

    
    // OBTENER guía por ID
    //Para GET endpoint
   
    public Guia obtenerGuiaPorId(String id) {
        Guia guia = db.get(id);
        if (guia == null) {
            throw new RuntimeException("Guía no encontrada con id: " + id);
        }
        return guia;
    }

    // Helper: construye el contenido textual de la guía
   
    private String buildContenidoGuia(String id, String transportista, String destinatario, String fecha) {
        return "=== GUÍA DE DESPACHO ===" + "\n" +
               "ID:               " + id + "\n" +
               "Fecha:            " + fecha + "\n" +
               "Transportista:    " + transportista + "\n" +
               "Destinatario:     " + destinatario + "\n" +
               "========================";
    }
}