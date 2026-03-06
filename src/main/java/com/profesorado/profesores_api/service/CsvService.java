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

            // Empezamos en 1 para saltar la primera fila (las cabeceras del CSV)
            for (int i = 1; i < records.size(); i++) {
                String[] fila = records.get(i);
                Profesor profesor = new Profesor();

                // --- 1. DATOS BÁSICOS DIRECTOS ---
                if (fila.length > 3)
                    profesor.setTitulacion(fila[3].trim());
                if (fila.length > 7)
                    profesor.setLocalidad(fila[7].trim());
                if (fila.length > 32)
                    profesor.setPrecio(fila[32].trim()); // Columna 32 ( PRECIO)

                // --- 2. SEXO --- (Columna 2)
                if (fila.length > 2 && fila[2] != null && !fila[2].trim().isEmpty()) {
                    try {
                        profesor.setSexo(Profesor.Sexo.valueOf(fila[2].trim().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        profesor.setSexo(null); // Si en el CSV hay algo raro, se queda nulo en la BD
                    }
                }

                // --- 3. CURSOS --- (Columnas 11 a 29)
                List<String> cursosDelProfesor = new ArrayList<>();
                for (int col = 11; col <= 29; col++) {
                    if (col < fila.length && "SI".equalsIgnoreCase(fila[col].trim())) {
                        cursosDelProfesor.add(NOMBRES_CURSOS[col - 11]);
                    }
                }
                profesor.setCursos(String.join(", ", cursosDelProfesor));

                // --- 4. EXPERIENCIA --- (Columna 31)
                // Usamos el método de abajo para separar el texto en años y horas
                String experienciaCsv = (fila.length > 31) ? fila[31] : "";
                procesarExperiencia(experienciaCsv, profesor);

                // Añadimos el profesor listo a la lista
                profesoresASalvar.add(profesor);
            }

            // Guardamos todos de golpe en MySQL
            repository.saveAll(profesoresASalvar);
        }
    }

    /**
     * Método auxiliar que convierte "100H+2AÑOS" en dos enteros para la base de
     * datos
     */
    private void procesarExperiencia(String experienciaCSV, Profesor profesor) {
        // Si no hay experiencia en el CSV o tiene el símbolo raro "?¿", ponemos 0
        if (experienciaCSV == null || experienciaCSV.trim().isEmpty() || experienciaCSV.contains("?¿")) {
            profesor.setExperiencia_horas(0);
            profesor.setExperiencia_anio(0);
            return;
        }

        // Limpiamos los espacios y pasamos a minúsculas ("2 AÑO" -> "2año")
        String textoLimpio = experienciaCSV.toLowerCase().replace(" ", "");

        // Extraer HORAS
        Pattern patronHoras = Pattern.compile("(\\d+)h");
        Matcher matcherHoras = patronHoras.matcher(textoLimpio);
        if (matcherHoras.find()) {
            profesor.setExperiencia_horas(Integer.parseInt(matcherHoras.group(1)));
        } else {
            profesor.setExperiencia_horas(0);
        }

        // Extraer AÑOS
        Pattern patronAnos = Pattern.compile("(\\d+)año");
        Matcher matcherAnos = patronAnos.matcher(textoLimpio);
        if (matcherAnos.find()) {
            profesor.setExperiencia_anio(Integer.parseInt(matcherAnos.group(1)));
        } else {
            profesor.setExperiencia_anio(0);
        }
    }
}