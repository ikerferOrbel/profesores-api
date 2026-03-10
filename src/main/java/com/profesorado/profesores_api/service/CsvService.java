package com.profesorado.profesores_api.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.profesorado.profesores_api.model.Profesor;
import com.profesorado.profesores_api.repository.ProfesorRepository;

@Service
public class CsvService {

    private final ProfesorRepository repository;

    // Los 19 cursos correspondientes a las columnas 11 hasta la 29 del CSV
    private final String[] NOMBRES_CURSOS = {
            "UNE", "PRL", "Gestión Medioambiental", "Limpieza", "Mantenimiento",
            "Electromecánica", "Electricidad", "Soldadura", "Logística", "Inglés",
            "BJ", "Carretilla", "Carretilla-Traspalet", "Maquinaria Almacén 4M",
            "Movimiento tierras 4M", "Puente Grúa y Polipasto", "PEMP(Brazo-Tijera)",
            "Altura", "Espacios Confinados"
    };

    public CsvService(ProfesorRepository repository) {
        this.repository = repository;
    }

    public void procesarCsv(MultipartFile file) throws Exception {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> records = csvReader.readAll();
            List<Profesor> profesoresASalvar = new ArrayList<>();

            // Empezamos en 1 para saltar la fila de cabeceras
            for (int i = 1; i < records.size(); i++) {
                String[] fila = records.get(i);
                
                // Validación mínima de longitud de fila para evitar errores
                if (fila.length < 2) continue;

                Profesor profesor = new Profesor();

                // --- 1. NOMBRE (Columna 1 del CSV) ---
                if (fila.length > 1 && fila[1] != null) {
                    profesor.setNombre(fila[1].trim());
                }

                // --- 2. SEXO (Columna 2 del CSV) ---
                if (fila.length > 2 && fila[2] != null && !fila[2].trim().isEmpty()) {
                    try {
                        profesor.setSexo(Profesor.Sexo.valueOf(fila[2].trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        profesor.setSexo(null);
                    }
                }

                // --- 3. TITULACIÓN (Columna 3 del CSV) ---
                if (fila.length > 3) {
                    profesor.setTitulacion(fila[3].trim());
                }

                // --- 4. OBSERVACIONES (Columna 4 del CSV: "OBERV.") ---
                if (fila.length > 4 && fila[4] != null) {
                    profesor.setObservaciones(fila[4].trim());
                } else {
                    profesor.setObservaciones("");
                }

                // --- 5. LOCALIDAD / PROVINCIA (Columna 7 del CSV) ---
                if (fila.length > 7) {
                    profesor.setLocalidad(fila[7].trim());
                }

                // --- 6. CURSOS (Columnas 11 a 29) ---
                List<String> cursosDelProfesor = new ArrayList<>();
                for (int col = 11; col <= 29; col++) {
                    if (col < fila.length && "SI".equalsIgnoreCase(fila[col].trim())) {
                        cursosDelProfesor.add(NOMBRES_CURSOS[col - 11]);
                    }
                }
                profesor.setCursos(String.join(", ", cursosDelProfesor));

                // --- 7. EXPERIENCIA (Columna 31 del CSV) ---
                String experienciaCsv = (fila.length > 31) ? fila[31] : "";
                procesarExperiencia(experienciaCsv, profesor);

                // --- 8. PRECIO (Columna 32 del CSV) ---
                if (fila.length > 32) {
                    profesor.setPrecio(fila[32].trim());
                }

                profesoresASalvar.add(profesor);
            }

            // Limpiamos la base de datos antes de importar los nuevos datos
            repository.deleteAll();

            // Guardamos todos los registros procesados
            repository.saveAll(profesoresASalvar);
        }
    }

    /**
     * Lógica para separar texto como "100H+2AÑOS" en campos numéricos
     */
    private void procesarExperiencia(String experienciaCSV, Profesor profesor) {
        if (experienciaCSV == null || experienciaCSV.trim().isEmpty() || experienciaCSV.contains("?¿")) {
            profesor.setExperiencia_horas(0);
            profesor.setExperiencia_anio(0);
            return;
        }

        String textoLimpio = experienciaCSV.toLowerCase().replace(" ", "");

        // Extraer HORAS (ej: "100h")
        Pattern patronHoras = Pattern.compile("(\\d+)h");
        Matcher matcherHoras = patronHoras.matcher(textoLimpio);
        if (matcherHoras.find()) {
            profesor.setExperiencia_horas(Integer.parseInt(matcherHoras.group(1)));
        } else {
            profesor.setExperiencia_horas(0);
        }

        // Extraer AÑOS (ej: "2año" o "2años")
        Pattern patronAnos = Pattern.compile("(\\d+)año");
        Matcher matcherAnos = patronAnos.matcher(textoLimpio);
        if (matcherAnos.find()) {
            profesor.setExperiencia_anio(Integer.parseInt(matcherAnos.group(1)));
        } else {
            profesor.setExperiencia_anio(0);
        }
    }
}