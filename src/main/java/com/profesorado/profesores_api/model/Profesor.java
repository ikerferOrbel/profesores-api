package com.profesorado.profesores_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "profesores")
public class Profesor {

    // ATRIBUTOS
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String titulacion;

    @Column(length = 1000)
    private String cursos;

    @Column(length = 100)
    private String localidad;

    @Column
    private Integer experiencia_anio;

    @Column
    private Integer experiencia_horas;

    @Column
    private String precio;

    @Column
    @Enumerated(EnumType.STRING)
    private Sexo sexo;


    public enum Sexo {
        M, F
    }

    // GETTERS Y SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulacion() {
        return titulacion;
    }

    public void setTitulacion(String titulacion) {
        this.titulacion = titulacion;
    }

    public String getCursos() {
        return cursos;
    }

    public void setCursos(String cursos) {
        this.cursos = cursos;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public Integer getExperiencia_anio() {
        return experiencia_anio;
    }

    public void setExperiencia_anio(Integer experiencia_anio) {
        this.experiencia_anio = experiencia_anio;
    }

    public Integer getExperiencia_horas() {
        return experiencia_horas;
    }

    public void setExperiencia_horas(Integer experiencia_horas) {
        this.experiencia_horas = experiencia_horas;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }
}
