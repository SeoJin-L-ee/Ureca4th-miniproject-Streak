package com.example.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.application.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long>{

}
