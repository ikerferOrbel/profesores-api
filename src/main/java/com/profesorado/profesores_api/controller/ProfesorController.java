package com.profesorado.profesores_api.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.profesorado.profesores_api.model.Profesor;
import com.profesorado.profesores_api.model.Profesor.Sexo;
import com.profesorado.profesores_api.repository.ProfesorRepository;
import com.profesorado.profesores_api.service.CsvService;

@RestController
@RequestMapping("api/profesores")
@CrossOrigin
public class ProfesorController {

    private final ProfesorRepository repository;
    private final CsvService csvService;

    public ProfesorController(ProfesorRepository repository, CsvService csvService) {
        this.repository = repository;
        this.csvService = csvService;
    }

    @GetMapping("/todos")
    public List<Profesor> obtenerProfesores() {
        return repository.findAll();
    }

    @GetMapping("/buscar")
    public List<Profesor> buscarProfesores(
            @RequestParam(required = false) String titulacion,
            @RequestParam(required = false) String cursos,
            @RequestParam(required = false) String localidad,
            @RequestParam(required = false) Integer experiencia_anio,
            @RequestParam(required = false) Integer experiencia_horas,
            @RequestParam(required = false) String precio,
            @RequestParam(required = false) Sexo sexo) {

        return repository.findAll().stream()
                .filter(p -> titulacion == null || (p.getTitulacion() != null
                        && p.getTitulacion().toLowerCase().contains(titulacion.toLowerCase())))
                .filter(p -> cursos == null
                        || (p.getCursos() != null && p.getCursos().toLowerCase().contains(cursos.toLowerCase())))
                .filter(p -> localidad == null
                        || (p.getLocalidad() != null && p.getLocalidad().equalsIgnoreCase(localidad)))

                .filter(p -> experiencia_anio == null
                        || (p.getExperiencia_anio() != null && p.getExperiencia_anio() >= experiencia_anio))
                .filter(p -> experiencia_horas == null
                        || (p.getExperiencia_horas() != null && p.getExperiencia_horas() >= experiencia_horas))

                .filter(p -> precio == null || (p.getPrecio() != null && p.getPrecio().contains(precio)))

                .filter(p -> sexo == null || p.getSexo() == sexo)
                .collect(Collectors.toList());
    }

    @PostMapping("/importar")
    public ResponseEntity<String> importarCsv(@RequestParam("file") MultipartFile file) {
        try {
            csvService.procesarCsv(file);
            return ResponseEntity.ok("CSV importado correctamente. Base de datos actualizada.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al importar el CSV: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/observaciones")
    public ResponseEntity<Profesor> actualizarObservaciones(
            @PathVariable Long id,
            @RequestBody(required = false) String nuevasObservaciones) { // required = false es la clave

        return repository.findById(id).map(profesor -> {
            // Si nuevasObservaciones es null (body vacío), guardamos un texto vacío ""
            profesor.setObservaciones(nuevasObservaciones != null ? nuevasObservaciones : "");

            Profesor actualizado = repository.save(profesor);
            return ResponseEntity.ok(actualizado);
        }).orElse(ResponseEntity.notFound().build());
    }

}
