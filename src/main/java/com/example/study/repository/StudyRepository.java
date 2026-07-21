package com.example.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.study.entity.Study;

public interface StudyRepository extends JpaRepository<Study, Long>{

}
