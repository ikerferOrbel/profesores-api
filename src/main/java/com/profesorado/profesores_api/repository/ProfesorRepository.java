package com.profesorado.profesores_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.profesorado.profesores_api.model.Profesor;

public interface ProfesorRepository extends JpaRepository<Profesor, Long> {

    @Override
    public List<Profesor> findAll();

    
}
